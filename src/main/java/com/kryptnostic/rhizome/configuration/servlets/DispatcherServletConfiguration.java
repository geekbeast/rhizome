package com.kryptnostic.rhizome.configuration.servlets;

import java.util.List;

import javax.annotation.Nullable;

import jersey.repackaged.com.google.common.base.Preconditions;
import jersey.repackaged.com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Optional;

public class DispatcherServletConfiguration {
    private final String            servletName;
    private final String[]          mappings;
    private final Optional<Integer> loadOnStartup;
    private final List<Class<?>>    pods = Lists.newArrayList();

    // Rhizome calls registerDispatcherServlets to all DispatcherServletConfigurations that are @Beans inside a Pod
    // registered to Rhizome
    public DispatcherServletConfiguration(
            String servletName,
            String[] mappings,
            @Nullable Integer loadOnStartup,
            List<Class<?>> pods ) {
        Preconditions.checkArgument( StringUtils.isNotBlank( servletName ), "Servlet name cannot be blank." );
        Preconditions.checkNotNull( mappings, "Mappings cannot be null" );
        Preconditions.checkArgument( mappings.length > 0, "At least on url patterns must be provided for mapping" );
        for ( String mapping : mappings ) {
            Preconditions.checkArgument( StringUtils.isNotBlank( mapping ), "Mappings cannot be blank." );
        }
        this.servletName = servletName;
        this.mappings = mappings;
        this.loadOnStartup = Optional.fromNullable( loadOnStartup );
        this.pods.addAll( Preconditions.checkNotNull( pods, "Pods cannot be null." ) );
    }

    public String getServletName() {
        return servletName;
    }

    public String[] getMappings() {
        return mappings;
    }

    public Optional<Integer> getLoadOnStartup() {
        return loadOnStartup;
    }

    public List<Class<?>> getPods() {
        return pods;
    }

    public void intercrop( List<Class<?>> servletPods ) {
        this.pods.addAll( servletPods );
    }
}
