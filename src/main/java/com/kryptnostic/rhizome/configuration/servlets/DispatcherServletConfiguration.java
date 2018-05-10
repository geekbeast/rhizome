package com.kryptnostic.rhizome.configuration.servlets;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

public class DispatcherServletConfiguration {
    private final String            servletName;
    private final String[]          mappings;
    private final Optional<Integer> loadOnStartup;
    private final List<Class<?>> pods = new ArrayList<>();

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

    @Override
    public String toString() {
        return "DispatcherServletConfiguration [servletName=" + servletName + ", mappings="
                + Arrays.toString( mappings ) + ", loadOnStartup=" + loadOnStartup + ", pods=" + pods + "]";
    }

}
