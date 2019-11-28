package com.kryptnostic.rhizome.configuration.jetty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GzipConfiguration {
    protected static final String       GZIP_ENABLED_PROPERTY       = "enabled";
    protected static final String       GZIP_CONTENT_TYPES_PROPERTY = "content-types";
    protected static final String       GZIP_METHODS_PROPERTY       = "methods";
    protected static final boolean      GZIP_ENABLED_DEFAULT        = true;
    protected static final List<String> GZIP_CONTENT_TYPES          = Arrays
            .asList( new String[] { "application/json", "text/html",
                    "text/plain", "text/xml", "application/xhtml+xml", "text/css", "application/javascript",
                    "image/svg+xml",
                    MediaType.APPLICATION_OCTET_STREAM_VALUE } );
    protected static final List<String> GZIP_METHODS                = Arrays
            .asList( new String[] { "GET", "DELETE", "POST", "PUT" } );

    protected final boolean             gzipEnabled;
    protected final List<String>        gzipContentTypes;
    protected List<String>              gzipMethods;

    @JsonCreator
    public GzipConfiguration(
            @JsonProperty( GZIP_ENABLED_PROPERTY ) Optional<Boolean> gzipEnabled,
            @JsonProperty( GZIP_CONTENT_TYPES_PROPERTY ) Optional<List<String>> contentTypes,
            @JsonProperty( GZIP_METHODS_PROPERTY ) Optional<List<String>> methods ) {
        this.gzipEnabled = gzipEnabled.orElse( GZIP_ENABLED_DEFAULT );
        if ( this.gzipEnabled ) {
            this.gzipContentTypes = contentTypes.orElse( GZIP_CONTENT_TYPES );
            this.gzipMethods = methods.orElse( GZIP_METHODS );
        } else {
            this.gzipContentTypes = ImmutableList.of();
        }
    }

    @JsonProperty( GZIP_ENABLED_PROPERTY )
    public boolean isGzipEnabled() {
        return gzipEnabled;
    }

    @JsonProperty( GZIP_CONTENT_TYPES_PROPERTY )
    public List<String> getGzipContentTypes() {
        return gzipContentTypes;
    }

    @JsonProperty( GZIP_METHODS_PROPERTY )
    public List<String> getGzipMethods() {
        return gzipMethods;
    }
}
