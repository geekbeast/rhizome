package com.kryptnostic.rhizome.pods;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.Arrays;

@Configuration
@EnableScheduling
@EnableAsync
public class AsyncPod implements AsyncConfigurer, SchedulingConfigurer {
    private static final Logger logger         = LoggerFactory.getLogger( AsyncPod.class );
    private static final int    CORE_POOL_SIZE = 8;

    // TODO: Make thread names prefixes configurable.
    @Override
    public void configureTasks( ScheduledTaskRegistrar registrar ) {
        registrar.setScheduler( rhizomeScheduler() );
    }

    @Bean(
            destroyMethod = "shutdown" )
    public ThreadPoolTaskScheduler rhizomeScheduler() {
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.setPoolSize( 8 );
        executor.setThreadNamePrefix( "rhizome-pulse-" );
        executor.initialize();
        return executor;
    }

    @Override
    @Bean(
            destroyMethod = "shutdown" )
    public ThreadPoolTaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize( Math.max( CORE_POOL_SIZE, Runtime.getRuntime().availableProcessors() ) );
        logger.info( "Setting MaxPoolSize to " + executor.getMaxPoolSize() );
        executor.setThreadNamePrefix( "rhizome-offshoot-" );
        executor.initialize();
        return executor;
    }

    @Bean
    public ListeningExecutorService listeningExecutorService() {
        return MoreExecutors.listeningDecorator( getAsyncExecutor().getThreadPoolExecutor() );
    }

    @Bean
    public AsyncEventBus eventBus() {
        return new AsyncEventBus( getAsyncExecutor() );
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return ( ex, method, params ) -> logger.error(
                "Error executing async method {} with params {}.",
                method.getName(),
                Arrays.asList( params ),
                ex );
    }

}