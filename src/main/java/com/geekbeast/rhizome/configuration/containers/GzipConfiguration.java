package com.geekbeast.rhizome.configuration.containers;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class GzipConfiguration {
    protected static final String GZIP_ENABLED_PROPERTY = "enabled";
    protected static final String GZIP_CONTENT_TYPES_PROPERTY = "content-types";
    //TODO: Consider switching from Arrays.asList to ImmutableList.copyOf(...)
    protected static final boolean GZIP_ENABLED_DEFAULT = true;
    protected static final List<String> GZIP_CONTENT_TYPES = 
        Arrays.asList(new String[]{"text/html","text/plain","text/xml","application/xhtml+xml","text/css","application/javascript","image/svg+xml"});
    
    protected final boolean gzipEnabled; 
    protected final List<String> gzipContentTypes;
    
    @JsonCreator
    public GzipConfiguration( 
            @JsonProperty( GZIP_ENABLED_PROPERTY ) Optional<Boolean> gzipEnabled , 
            @JsonProperty( GZIP_CONTENT_TYPES_PROPERTY) Optional<List<String>> contentTypes ) {
        this.gzipEnabled = gzipEnabled.or( GZIP_ENABLED_DEFAULT );
        if( this.gzipEnabled ) {
            this.gzipContentTypes = contentTypes.or( GZIP_CONTENT_TYPES );
        } else {
            this.gzipContentTypes = ImmutableList.of();
        }
    }
    
    @JsonProperty( GZIP_ENABLED_PROPERTY ) 
    public boolean isGzipEnabled() {
        return gzipEnabled;
    }

    @JsonProperty( GZIP_CONTENT_TYPES_PROPERTY)
    public List<String> getGzipContentTypes() {
        return gzipContentTypes;
    }
}
