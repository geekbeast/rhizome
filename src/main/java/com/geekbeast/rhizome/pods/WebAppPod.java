package com.geekbeast.rhizome.pods;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import com.geekbeast.rhizome.configuration.RhizomeConfiguration;
import com.geekbeast.rhizome.configuration.service.RhizomeConfigurationService;
import com.google.common.eventbus.AsyncEventBus;

@Configuration
@ImportResource({"classpath:/rhizome-hazel.xml"}) 
@ComponentScan(
        basePackages={"com.geekbeast.rhizome" , "com.geekbeast.jersey"}, 
        excludeFilters = @ComponentScan.Filter( 
                value = {
                    org.springframework.stereotype.Controller.class , 
                } ,
       
                type = FilterType.ANNOTATION 
                )
        )
@EnableScheduling
@EnableAsync
public class WebAppPod implements AsyncConfigurer, SchedulingConfigurer {
    //TODO: Make thread names prefixes configurable.
    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        registrar.setScheduler( rhizomeScheduler() );
    }

    @Bean(destroyMethod="shutdown")
    public ThreadPoolTaskScheduler rhizomeScheduler() {
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.setPoolSize( 8 );
        executor.setThreadNamePrefix("rhizome-pulse-");
        executor.initialize();
        return executor;
    }
    
    @Override
    @Bean(destroyMethod="shutdown" )
    public ThreadPoolTaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(1024);
        executor.setThreadNamePrefix("rhizome-offshoot-");
        executor.initialize();
        return executor;
    }
    
    @Bean
    public AsyncEventBus localSettingsUpdates() {
        return new AsyncEventBus( getAsyncExecutor() );
    }
    
    @Bean
    public RhizomeConfiguration rhizomeConfiguration() {
        return RhizomeConfigurationService.loadConfiguration( RhizomeConfiguration.class );
    }
}