package com.geekbeast.rhizome.controllers;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;

@Controller("/metrics")
public class MetricsController {
    private final MetricRegistry globalMetricRegistry;
    private final MetricRegistry serverMetricRegistry;
    
    @Inject
    public MetricsController( 
            MetricRegistry globalMetricRegistry , MetricRegistry serverMetricRegistry ) {
        this.globalMetricRegistry = globalMetricRegistry;
        this.serverMetricRegistry = serverMetricRegistry;
        
    }
    
    @RequestMapping( method = RequestMethod.GET , produces = MediaType.APPLICATION_JSON )
    public @ResponseBody Map<String, Metric> getMetrics() {
        return ImmutableMap.<String, Metric>builder()
                .putAll( globalMetricRegistry.getMetrics() )
                .putAll( serverMetricRegistry.getMetrics() ) 
                .build();
    }
    
}
