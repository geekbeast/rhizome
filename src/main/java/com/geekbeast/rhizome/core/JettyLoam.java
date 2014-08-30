package com.geekbeast.rhizome.core;

import java.io.IOException;

import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.geekbeast.rhizome.configuration.jetty.ConnectorConfiguration;
import com.geekbeast.rhizome.configuration.jetty.ContextConfiguration;
import com.geekbeast.rhizome.configuration.jetty.JettyConfiguration;
import com.geekbeast.rhizome.configuration.service.ConfigurationService;

public class JettyLoam implements Loam {
    private static final Logger logger = LoggerFactory.getLogger( JettyLoam.class );
    private final JettyConfiguration config;
    private final Server server;
    
    protected JettyLoam() throws InterruptedException, JsonParseException, JsonMappingException, IOException {
        this( ConfigurationService.StaticLoader.loadConfiguration( JettyConfiguration.class ) );
    }
    
    public <T extends JettyConfiguration> JettyLoam( T config ) throws IOException {
        this.config = config;

        WebAppContext context = new WebAppContext();
        if( config.getContextConfiguration().isPresent() ) {
            ContextConfiguration contextConfig = config.getContextConfiguration().get();
            context.setContextPath( contextConfig.getPath() );
            context.setResourceBase( contextConfig.getResourceBase() );
            context.setParentLoaderPriority( contextConfig.isParentLoaderPriority() );
        }
        
        /* 
         * Work around for https://bugs.eclipse.org/bugs/show_bug.cgi?id=404176
         * 
         * Probably need to report new bug as Jetty picks up the SpringContextInitializer, but cannot find
         * Spring WebApplicationInitializer types, without the configuration hack.  
         */
        JettyAnnotationConfigurationHack configurationHack = new JettyAnnotationConfigurationHack();
        if( config.isSecurityEnabled() ) {
            configurationHack.registerInitializer( RhizomeSecurity.class.getName() );
        }
        context.setConfigurations( new org.eclipse.jetty.webapp.Configuration[] { configurationHack } );
              
        //TODO: Make loaded servlet classes configurable
        context.addServlet(JspServlet.class, "*.jsp");
        
        //TODO: Make max threads configurable ( queued vs concurrent thread pool needs to be configured )
        server = new Server( );
        
        if( config.getWebConnectorConfiguration().isPresent() ) {
            configureEndpoint( config.getWebConnectorConfiguration().get() );
        }
        if( config.getServiceConnectorConfiguration().isPresent() ) {
            configureEndpoint( config.getServiceConnectorConfiguration().get() );
        }
        
        server.setHandler(context);
    }

    public JettyLoam( Class<? extends JettyConfiguration> clazz ) throws IOException {
         this( ConfigurationService.StaticLoader.loadConfiguration( clazz ) );
    }

    public static void main(String[] args) throws Exception {
        JettyLoam server = new JettyLoam();
        server.start();
        server.join();
    }
    
    protected void configureEndpoint( ConnectorConfiguration configuration ) throws IOException {
        HttpConfiguration http_config = new HttpConfiguration();
        
        if( !configuration.requireSSL() ) {
            ServerConnector http = new ServerConnector( server , new HttpConnectionFactory( http_config ) );
            http.setPort( configuration.getHttpPort() );
            server.addConnector( http );
        }
        
        if( ( configuration.requireSSL() || configuration.useSSL() ) && 
                config.getTruststoreConfiguration().isPresent() && 
                config.getKeystoreConfiguration().isPresent() ) {
            http_config.setSecureScheme( "https" );
            http_config.setSecurePort( configuration.getHttpsPort() );
            
            SslContextFactory contextFactory = new SslContextFactory();
            
            contextFactory.setTrustStorePath( getFromClasspath( config.getTruststoreConfiguration().get().getStorePath() ) );
            contextFactory.setTrustStorePassword( config.getTruststoreConfiguration().get().getStorePassword() );

            contextFactory.setKeyStorePath( getFromClasspath( config.getKeystoreConfiguration().get().getStorePath() ) );
            contextFactory.setKeyStorePassword( config.getKeystoreConfiguration().get().getStorePassword() );
            contextFactory.setKeyManagerPassword( config.getKeyManagerPassword().get() );
            contextFactory.setWantClientAuth( configuration.wantClientAuth() );
//            contextFactory.setNeedClientAuth( configuration.needClientAuth() );
            
            //TODO: Get rid of magic values.
            HttpConfiguration https_config = new HttpConfiguration( http_config );
            https_config.addCustomizer( new SecureRequestCustomizer() );
            
            SslConnectionFactory connectionFactory = new SslConnectionFactory( contextFactory , "http/1.1" );
            ServerConnector ssl = new ServerConnector( server, connectionFactory , new HttpConnectionFactory( https_config ) );
            //Jetty needs this twice, straight for the Jetty samples
            ssl.setPort( configuration.getHttpsPort() );
            server.addConnector( ssl );
        } else if( ( configuration.requireSSL() || configuration.useSSL() )  && (
                !config.getTruststoreConfiguration().isPresent() ||
                !config.getKeystoreConfiguration().isPresent() ) ) { 
            logger.warn("SSL Configuration is incomplete.");
        }
    }
    
    private String getFromClasspath( String path ) throws IOException {
        return new ClassPathResource( path ).getURL().toString();
    }
    void initializeSslContextFactory() {
        
    }
    
    public Server getServer() {
        return server;
    }

    @Override
    public synchronized void start() throws Exception {
        if( !server.isRunning() ) { 
            server.start();
        }
    }

    @Override
    public synchronized void stop() throws Exception {
        if( server.isRunning() ) {
            server.stop();
        }
    }

    @Override
    public synchronized void join() throws BeansException, InterruptedException {
        server.join();
    }
}
