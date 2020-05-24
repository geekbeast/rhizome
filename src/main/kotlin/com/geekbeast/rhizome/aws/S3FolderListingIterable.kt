package com.geekbeast.rhizome.aws

import com.amazonaws.services.s3.AmazonS3

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
        s3: AmazonS3,
        bucket: String,
        folderPrefix: String,
        maxKeys: Int = 1000,
        delimiter: String = "/",
        mapper: (String) -> T
) : S3ListingIterator<T>(s3, bucket, folderPrefix, maxKeys, delimiter, mapper) {

    override fun getBufferLength(): Int = result.commonPrefixes.size

    override fun trimElement(nextElem: String) = nextElem.removePrefix(folderPrefix).removeSuffix(delimiter)

    override fun getElement(index: Int): String = result.commonPrefixes[index]
}