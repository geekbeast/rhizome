package com.geekbeast.rhizome.aws

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.retry.PredefinedBackoffStrategies
import com.amazonaws.retry.PredefinedRetryPolicies
import com.amazonaws.retry.RetryPolicy
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */

private const val MAX_ERROR_RETRIES = 5
private val DEFAULT_RETRY_POLICY = RetryPolicy(
        PredefinedRetryPolicies.DEFAULT_RETRY_CONDITION,
        PredefinedBackoffStrategies.SDKDefaultBackoffStrategy(), //TODO try jitter
        MAX_ERROR_RETRIES,
        false
)

fun newS3Client(
        accessKeyId: String,
        secretAccessKey: String,
        regionName: String,
        retryPolicy: RetryPolicy = DEFAULT_RETRY_POLICY
): AmazonS3 {
    val s3Credentials = BasicAWSCredentials(accessKeyId, secretAccessKey)
    val builder = AmazonS3ClientBuilder.standard()
    builder.region = regionName
    builder.credentials = AWSStaticCredentialsProvider(s3Credentials)

    builder.clientConfiguration = ClientConfiguration().withRetryPolicy(retryPolicy)
    return builder.build()
}

fun newS3Client(
        profileName: String,
        regionName: String,
        retryPolicy: RetryPolicy = DEFAULT_RETRY_POLICY
): AmazonS3 {
    val builder = AmazonS3ClientBuilder.standard()
    builder.region = regionName
    builder.credentials = ProfileCredentialsProvider(profileName)
    builder.clientConfiguration = ClientConfiguration().withRetryPolicy(retryPolicy)
    return builder.build()
}