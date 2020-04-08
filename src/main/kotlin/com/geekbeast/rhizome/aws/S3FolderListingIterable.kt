package com.geekbeast.rhizome.aws

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ListObjectsV2Request
import com.amazonaws.services.s3.model.ListObjectsV2Result
import java.util.concurrent.locks.ReentrantLock

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
class S3FolderListingIterable<T>(
        private val s3: AmazonS3,
        private val bucket: String,
        private val folderPrefix: String,
        private val maxKeys: Int = 1000,
        private val delimiter: String = "/",
        private val mapper: (String) -> T
) : Iterable<T> {
    override fun iterator(): Iterator<T> {
        return S3FolderIterator(
                s3, bucket, folderPrefix, maxKeys, delimiter, mapper
        )
    }

}

/**
 * This class will list
 */
class S3FolderIterator<T> @JvmOverloads constructor(
        private val s3: AmazonS3,
        private val bucket: String,
        private val folderPrefix: String,
        private val maxKeys: Int = 1000,
        private val delimiter: String = "/",
        private val mapper: (String) -> T
) : Iterator<T> {
    private val lock = ReentrantLock()
    private var continuationToken: String? = null
    private var result = getNextListing()
    private var index = 0

    init {
        continuationToken = result.nextContinuationToken
    }

    override fun next(): T {
        val nextElem = try {
            lock.lock()
            require(hasNext()) { "No element available." }
            if (index == result.commonPrefixes.size) {
                result = getNextListing()
                continuationToken =  result.nextContinuationToken
                index = 0
            }
            result.commonPrefixes[index++]
        } finally {
            lock.unlock()
        }

        return mapper(nextElem.removePrefix(folderPrefix).removeSuffix(delimiter))
    }

    override fun hasNext(): Boolean {
        return try {
            lock.lock()
            (index < result.commonPrefixes.size) || (continuationToken != null)
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