package com.geekbeast.postgres.streams

import com.geekbeast.streams.StreamUtil
import com.google.common.base.Preconditions.checkState
import com.zaxxer.hikari.HikariDataSource
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.IOException
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Supplier
import java.util.stream.Stream

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
class BasePostgresIterable<T>(
    private val rsh: Supplier<StatementHolder>,
    private val mapper: (ResultSet) -> T
) : Iterable<T> {

    private val logger = LoggerFactory.getLogger(BasePostgresIterable::class.java)

    override fun iterator(): PostgresIterator<T> {
        try {
            return PostgresIterator(rsh.get(), mapper)
        } catch (e: SQLException) {
            logger.error("Error creating postgres stream iterator.")
            throw IllegalStateException("Unable to instantiate postgres iterator.", e)
        } catch (e: IOException) {
            logger.error("Error creating postgres stream iterator.")
            throw IllegalStateException("Unable to instantiate postgres iterator.", e)
        }
    }

    fun stream(): Stream<T> {
        return StreamUtil.stream(this)
    }
}

open class StatementHolderSupplier @JvmOverloads constructor(
    val hds: HikariDataSource,
    val sql: String,
    val fetchSize: Int = 0,
    val autoCommit: Boolean = (fetchSize == 0),
    private val longRunningQueryLimit: Long = 0,
    private val statementTimeoutMillis: Long = 0
) : Supplier<StatementHolder> {
    init {
        check(fetchSize >= 0) { "Fetch-size must be nonnegative." }
        check(((!autoCommit) && (fetchSize > 0)) || (fetchSize == 0)) {
            "Auto-commit must be false if fetch size > 0."
        }
    }

    protected val logger = LoggerFactory.getLogger(javaClass)!!

    @SuppressFBWarnings(value = ["SECSQLIJDBC"], justification = "Provided by caller.")
    open fun execute(statement: Statement): ResultSet {
        return statement.executeQuery(sql)
    }

    open fun buildStatement(connection: Connection): Statement {
        return connection.createStatement()
    }

    open fun setStatementTimeout(statement: Statement): Statement {
        if(statementTimeoutMillis > 0 ) {
            statement.execute("SET statement_timeout = '${statementTimeoutMillis}ms';")
        }
        return statement
    }

    override fun get(): StatementHolder {
        val connection = hds.connection //okay to fail + propagate
        connection.autoCommit = autoCommit

        val statement = setStatementTimeout(buildStatement(connection)) //okay to fail + propagate

        statement.fetchSize = fetchSize

        val rs = try {
            execute(statement)
        } catch (ex: Exception) {
            logger.error("Error while executing sql: {}. The following exception was thrown: ", sql, ex)
            if (!connection.autoCommit) {
                connection.rollback()
                connection.close() // Don't remove me, I keep us from leaking connections
                logger.error("Rolled back the offending commit and closed the connection")
            }
            throw ex
        }

        return if (longRunningQueryLimit == 0L) {
            StatementHolder(connection, statement, rs)
        } else {
            StatementHolder(connection, statement, rs, longRunningQueryLimit)
        }
    }
}

class PreparedStatementHolderSupplier(
    hds: HikariDataSource,
    sql: String,
    fetchSize: Int = 0,
    autoCommit: Boolean = (fetchSize == 0),
    statementTimeoutMillis: Long = 0,
    val bind: (PreparedStatement) -> Unit
) : StatementHolderSupplier(
    hds = hds,
    sql = sql,
    fetchSize = fetchSize,
    autoCommit = autoCommit,
    statementTimeoutMillis = statementTimeoutMillis
) {

    override fun execute(statement: Statement): ResultSet {
        return (statement as PreparedStatement).executeQuery()
    }

    @SuppressFBWarnings(value = ["SECSQLIJDBC"], justification = "Provided by caller.")
    override fun buildStatement(connection: Connection): Statement {
        val ps = connection.prepareStatement(sql)
        bind(ps)
        return ps
    }
}

class PostgresIterator<T> @Throws(SQLException::class)
@JvmOverloads constructor(
    private val rsh: StatementHolder,
    private val mapper: (ResultSet) -> T,
    private val timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS
) : Iterator<T>, AutoCloseable, Closeable {
    companion object {
        private const val DEFAULT_TIMEOUT_MILLIS: Long = 600000
        private val executor = Executors.newSingleThreadExecutor()
        private val logger = LoggerFactory.getLogger(PostgresIterator::class.java)
    }

    private val lock = ReentrantLock()
    private val rs: ResultSet = rsh.resultSet
    private var expiration: Long = 0

    private var notExhausted: Boolean = false

    init {
        notExhausted = this.rs.next()
        updateExpiration()

        if (!notExhausted) {
            rsh.close()
        }

        executor.execute {
            while (rsh.isOpen) {
                if (System.currentTimeMillis() > expiration || !notExhausted) {
                    logger.info("PostgresIterator is closing because it has expired, even though it is not exhausted.")
                    rsh.close()
                } else {
                    try {
                        Thread.sleep(timeoutMillis)
                    } catch (e: InterruptedException) {
                        logger.error("Unable to sleep thread for {} millis", timeoutMillis, e)
                    }

                }
            }
        }
    }

    override fun hasNext(): Boolean {
        //We don't lock here, because multiple calls to has next can still cause an exception to be thrown while
        //calling next
        updateExpiration()
        return notExhausted
    }

    private fun updateExpiration() {
        this.expiration = System.currentTimeMillis() + timeoutMillis
    }

    override fun next(): T {
        updateExpiration()
        val nextElem: T
        try {
            lock.lock()
            checkState(hasNext(), "There are no more items remaining in the stream.")
            nextElem = mapper(rs)
            notExhausted = rs.next()
        } catch (e: SQLException) {
            logger.error("Unable to retrieve next element from result set.", e)
            notExhausted = false
            throw NoSuchElementException("Unable to retrieve next element from result set.")
        } catch (e: Exception) {
            logger.error("An error occurred while trying to retrieve next element from result set.", e)
            notExhausted = false
            throw e
        } finally {
            try {
                if (!notExhausted) {
                    rsh.close()
                }
            } finally {
                lock.unlock()
            }
        }

        return nextElem
    }

    override fun close() {
        rsh.close()
    }
}
