package com.geekbeast.rhizome.aws

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ListObjectsV2Request
import com.amazonaws.services.s3.model.ListObjectsV2Result
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.max

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
class S3ObjectListingIterable<T>(
        private val s3: AmazonS3,
        private val bucket: String,
        private val folderPrefix: String,
        private val maxKeys: Int = 1000,
        private val delimiter: String = "/",
        private val mapper: (String) -> T
) : Iterable<T> {
    override fun iterator(): Iterator<T> {
        return S3ObjectIterator(
                s3, bucket, folderPrefix, maxKeys, delimiter, mapper
        )
    }
}

/**
 * This class will list
 */
class S3ObjectIterator<T> @JvmOverloads constructor(
        s3: AmazonS3,
        bucket: String,
        folderPrefix: String,
        maxKeys: Int = 1000,
        delimiter: String = "/",
        mapper: (String) -> T
) : S3ListingIterator<T>(s3, bucket, folderPrefix, maxKeys, delimiter, mapper) {

    override fun trimElement(nextElem: String): String = nextElem.removePrefix(folderPrefix)
    override fun getElement(index: Int): String = result.objectSummaries[index].key
    override fun getBufferLength(): Int = result.objectSummaries.size
}