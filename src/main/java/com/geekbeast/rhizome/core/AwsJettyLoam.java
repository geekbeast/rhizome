package com.geekbeast.rhizome.core;

import com.geekbeast.rhizome.configuration.jetty.JettyConfiguration;
import com.geekbeast.rhizome.keystores.Keystores;
import com.google.common.base.Preconditions;
import com.geekbeast.rhizome.configuration.configuration.amazon.AmazonLaunchConfiguration;
import com.geekbeast.aws.AwsS3Pod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Optional;

public class AwsJettyLoam extends JettyLoam {
    private static final Logger logger = LoggerFactory.getLogger( AwsJettyLoam.class );

    public AwsJettyLoam( JettyConfiguration config, AmazonLaunchConfiguration awsConfig ) throws IOException {
        super( Preconditions.checkNotNull( config, "Jetty configuration cannot be null" ), Optional.of( awsConfig ) );
    }

    @Override
    protected void configureSslStores( SslContextFactory contextFactory ) throws IOException {
        AmazonLaunchConfiguration awsConfig = maybeAmazonLaunchConfiguration.get();
        S3Client s3 = AwsS3Pod.newS3Client( awsConfig );

        String truststoreKey = Preconditions.checkNotNull( awsConfig.getFolder(), "awsConfig folder cannot be null" )
                + Preconditions
                .checkNotNull( config.getTruststoreConfiguration(), "keystore configuration cannot be null" )
                .get().getStorePath();
        String keystoreKey = Preconditions.checkNotNull( awsConfig.getFolder(), "awsConfig folder cannot be null" )
                + Preconditions
                .checkNotNull( config.getKeystoreConfiguration(), "keystore configuration cannot be null" )
                .get().getStorePath();
        logger.info( "AwsConfig: {}", awsConfig );
        logger.info( "Trust store key: {}", truststoreKey );
        logger.info( "Keystore key: {}", keystoreKey );
        String truststorePassword = config.getTruststoreConfiguration().get().getStorePassword();
        String keystorePassword = config.getKeystoreConfiguration().get().getStorePassword();
        InputStream ksStream = s3.getObject( GetObjectRequest.builder().bucket( awsConfig.getBucket() ).key( keystoreKey ).build() );
        InputStream tsStream = s3.getObject( GetObjectRequest.builder().bucket( awsConfig.getBucket() ).key( truststoreKey ).build() );

        try {
            contextFactory.setKeyStore( Keystores.loadKeystoreFromStream( ksStream, keystorePassword.toCharArray() ) );
            contextFactory
                    .setTrustStore( Keystores.loadKeystoreFromStream( tsStream, truststorePassword.toCharArray() ) );
        } catch ( NoSuchAlgorithmException | CertificateException | KeyStoreException e ) {
            throw new IOException( "Unable to load keystores from S3.", e );
        }

        contextFactory.setTrustStorePassword( truststorePassword );
        contextFactory.setKeyStorePassword( keystorePassword );
    }
}
