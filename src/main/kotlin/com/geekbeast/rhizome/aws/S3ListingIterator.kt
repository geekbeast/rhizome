package com.geekbeast.rhizome.aws

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ListObjectsV2Request
import com.amazonaws.services.s3.model.ListObjectsV2Result
import java.util.concurrent.locks.ReentrantLock

/**
 * This is the base class for s3 listing iterator.
 *
 * This class will make a call to s3 using the provided client at creation in order to initialize
 * the listing iterator and paging mechanism.
 */
abstract class S3ListingIterator<T> @JvmOverloads constructor(
        private val s3: AmazonS3,
        private val bucket: String,
        protected val folderPrefix: String,
        private val maxKeys: Int = 1000,
        protected val delimiter: String = "/",
        private val mapper: (String) -> T
) : Iterator<T> {
    private val lock = ReentrantLock()
    private var continuationToken: String? = null
    protected var result = getNextListing()
    protected var index = 0

    init {
        continuationToken = result.nextContinuationToken
    }

    override fun next(): T {
        val nextElem = try {
            lock.lock()
            require(hasNext()) { "No element available." }
            if (index == result.commonPrefixes.size) {
                result = getNextListing()
                continuationToken = result.nextContinuationToken
                index = 0
            }
            getElement(index++)
        } finally {
            lock.unlock()
        }

        return mapper(trimElement(nextElem))
    }

    abstract fun trimElement(nextElem: String): String
    abstract fun getElement( index: Int ) : String
    abstract fun getBufferLength() : Int

    override fun hasNext(): Boolean {
        return try {
            lock.lock()
            (index < getBufferLength()) || (continuationToken != null)
        } finally {
            lock.unlock()
        }
    }

    private fun getNextListing(): ListObjectsV2Result {
        val request = ListObjectsV2Request()
                .withBucketName(bucket)
                .withPrefix(folderPrefix)
                .withDelimiter(delimiter)
                .withMaxKeys(maxKeys)

        if (continuationToken != null) {
            request.continuationToken = continuationToken
        }

        return s3.listObjectsV2(request)
    }
}