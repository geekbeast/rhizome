package com.geekbeast.rhizome.aws

import software.amazon.awssdk.services.s3.S3Client

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
class S3ObjectListingIterable<T>(
        private val s3: S3Client,
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
        s3: S3Client,
        bucket: String,
        folderPrefix: String,
        maxKeys: Int = 1000,
        delimiter: String = "/",
        mapper: (String) -> T
) : S3ListingIterator<T>(s3, bucket, folderPrefix, maxKeys, delimiter, mapper) {

    override fun trimElement(nextElem: String): String = nextElem.removePrefix(folderPrefix)
    override fun getElement(index: Int): String = result.contents()[index].key()
    override fun getBufferLength(): Int = result.contents().size
}
