package com.kryptnostic.rhizome.configuration.amazon;

import com.auth0.jwt.internal.org.apache.commons.lang3.StringUtils;
import com.clearspring.analytics.util.Preconditions;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.kryptnostic.rhizome.configuration.annotation.ReloadableConfiguration;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 *
 */
@ReloadableConfiguration(
    uri = "aws.yaml" )
public class AmazonLaunchConfiguration {
    public static final String  BUCKET_FIELD   = "bucket";
    public static final String  FOLDER_FIELD   = "folder";
    private static final String DEFAULT_FOLDER = "";
    private final String        bucket;
    private final String        folder;

    public AmazonLaunchConfiguration(
            @JsonProperty( BUCKET_FIELD ) String bucket,
            @JsonProperty( FOLDER_FIELD ) Optional<String> folder ) {
        Preconditions.checkArgument( StringUtils.isNotBlank( bucket ),
                "S3 bucket for configuration must be specified." );
        this.bucket = bucket;
        String rawFolder = folder.or( DEFAULT_FOLDER );

        while ( StringUtils.endsWith( rawFolder, "/" ) ) {
            StringUtils.removeEnd( rawFolder, "/" );
        }
        // We shouldn't prefix
        if ( StringUtils.isNotBlank( rawFolder ) ) {
            this.folder = rawFolder + "/";
        } else {
            this.folder = rawFolder;
        }

    }

    @JsonProperty( BUCKET_FIELD )
    public String getBucket() {
        return bucket;
    }

    @JsonProperty( FOLDER_FIELD )
    public String getFolder() {
        return folder;
    }

    @Override
    public String toString() {
        return "AmazonLaunchConfiguration [bucket=" + bucket + ", folder=" + folder + "]";
    }

}
