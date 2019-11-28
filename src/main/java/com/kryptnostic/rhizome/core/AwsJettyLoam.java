package com.kryptnostic.rhizome.core;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.google.common.base.Preconditions;
import com.kryptnostic.rhizome.configuration.amazon.AmazonLaunchConfiguration;
import com.kryptnostic.rhizome.configuration.jetty.JettyConfiguration;
import com.kryptnostic.rhizome.keystores.Keystores;
import com.openlattice.aws.AwsS3Pod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        AmazonS3 s3 = AwsS3Pod.newS3Client( awsConfig );

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
        S3Object truststoreObj = s3.getObject( awsConfig.getBucket(), truststoreKey );
        S3Object keystoreObj = s3.getObject( awsConfig.getBucket(), keystoreKey );
        InputStream ksStream = keystoreObj.getObjectContent();
        InputStream tsStream = truststoreObj.getObjectContent();

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
