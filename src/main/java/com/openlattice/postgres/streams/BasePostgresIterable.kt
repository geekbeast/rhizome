package com.openlattice.postgres.streams

import com.dataloom.streams.StreamUtil
import com.google.common.base.Preconditions.checkState
import com.zaxxer.hikari.HikariDataSource
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.IOException
import java.sql.*
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

    private val logger = LoggerFactory.getLogger(PostgresIterable::class.java)

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

open class StatementHolderSupplier(
        val hds: HikariDataSource,
        val sql: String,
        val fetchSize: Int = 0,
        val autoCommit: Boolean = (fetchSize == 0),
        private val longRunningQueryLimit: Long = 0
) : Supplier<StatementHolder> {
    init {
        check(fetchSize >= 0) { "Fetch-size must be nonnegative." }
        check(((!autoCommit) && (fetchSize > 0)) || (fetchSize == 0)) {
            "Auto-commit must be false if fetch size > 0."
        }
    }

    protected val logger = LoggerFactory.getLogger(javaClass)!!

    open fun execute(statement: Statement): ResultSet {
        return statement.executeQuery(sql)
    }

    open fun buildStatement(connection: Connection): Statement {
        return connection.createStatement()
    }

    override fun get(): StatementHolder {
        val connection = hds.connection //okay to fail + propagate
        connection.autoCommit = autoCommit

        val statement = buildStatement(connection) //okay to fail + propagate

        statement.fetchSize = fetchSize

        val rs = try {
            execute(statement)
        } catch (ex: Exception) {
            logger.error("Error while executing sql: {}. The following exception was thrown: ", sql, ex)
            if ( !connection.autoCommit ){
                connection.rollback()
                logger.error("Rolled back the offending commit ")
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
        val bind: (PreparedStatement) -> Unit
) : StatementHolderSupplier(hds, sql, fetchSize, autoCommit) {

    override fun execute(ps: Statement): ResultSet {
        return (ps as PreparedStatement).executeQuery()
    }

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
        } finally {
            if (!notExhausted) {
                rsh.close()
            }

            lock.unlock()
        }

        return nextElem
    }

    override fun close() {
        rsh.close()
    }
}
