package com.kryptnostic.rhizome.configuration.jetty;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

@JsonIgnoreProperties(
    ignoreUnknown = false )
public class ContextConfiguration {
    private static final String  DESCRIPTOR_PROPERTY             = "descriptor";
    private static final String  RESOURCE_BASE_PROPERTY          = "resource-base";
    private static final String  PATH_PROPERTY                   = "path";
    private static final String  PARENT_LOADER_PRIORITY_PROPERTY = "parent-loader-priority";

    private static final boolean PARENT_LOADER_PRIORITY_DEFAULT  = true;

    private final String         descriptor;
    private final String         resourceBase;
    private final String         path;
    private final boolean        parentLoaderPriority;

    @JsonCreator
    public ContextConfiguration(
            @JsonProperty( DESCRIPTOR_PROPERTY ) String descriptor,
            @JsonProperty( RESOURCE_BASE_PROPERTY ) String resourceBase,
            @JsonProperty( PATH_PROPERTY ) String path,
            @JsonProperty( PARENT_LOADER_PRIORITY_PROPERTY ) Optional<Boolean> parentLoaderPriority ) {
        Preconditions.checkArgument( StringUtils.isNotBlank( descriptor ), "Descriptor cannot be blank" );
        Preconditions.checkArgument( StringUtils.isNotBlank( resourceBase ), "Resource base cannot be blank" );
        Preconditions.checkArgument( StringUtils.isNotBlank( path ), "Context path cannot be blank" );

        this.descriptor = descriptor;
        this.resourceBase = resourceBase;
        this.path = path;
        this.parentLoaderPriority = parentLoaderPriority.or( PARENT_LOADER_PRIORITY_DEFAULT );
    }

    @JsonProperty( DESCRIPTOR_PROPERTY )
    public String getDescriptor() {
        return descriptor;
    }

    @JsonProperty( RESOURCE_BASE_PROPERTY )
    public String getResourceBase() {
        return resourceBase;
    }

    @JsonProperty( PATH_PROPERTY )
    public String getPath() {
        return path;
    }

    @JsonProperty( PARENT_LOADER_PRIORITY_PROPERTY )
    public boolean isParentLoaderPriority() {
        return parentLoaderPriority;
    }
}
