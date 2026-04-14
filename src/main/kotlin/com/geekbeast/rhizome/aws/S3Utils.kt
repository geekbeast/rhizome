package com.geekbeast.rhizome.aws

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.retry.RetryPolicy
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client


/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */

private const val MAX_ERROR_RETRIES = 5

fun newS3Client(
        accessKeyId: String,
        secretAccessKey: String,
        regionName: String,
        retryPolicy: RetryPolicy = RetryPolicy.builder().numRetries(MAX_ERROR_RETRIES).build()
): S3Client {
    val credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey)
    return S3Client.builder()
            .region(Region.of(regionName))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .overrideConfiguration { it.retryPolicy(retryPolicy) }
            .build()
}

fun newS3Client(
        profileName: String,
        regionName: String,
        retryPolicy: RetryPolicy = RetryPolicy.builder().numRetries(MAX_ERROR_RETRIES).build()
): S3Client {
    return S3Client.builder()
            .region(Region.of(regionName))
            .credentialsProvider(ProfileCredentialsProvider.create(profileName))
            .overrideConfiguration { it.retryPolicy(retryPolicy) }
            .build()
}
