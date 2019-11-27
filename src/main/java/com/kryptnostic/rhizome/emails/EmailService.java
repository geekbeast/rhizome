package com.kryptnostic.rhizome.emails;

import com.google.common.eventbus.Subscribe;
import com.kryptnostic.rhizome.configuration.service.ConfigurationService;
import com.kryptnostic.rhizome.emails.configuration.MailServiceConfiguration;
import jodd.mail.Email;
import jodd.mail.SendMailSession;
import jodd.mail.SmtpServer;
import jodd.mail.SmtpSslServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 */
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger( EmailService.class );

    private Session    session;
    @SuppressWarnings( "rawtypes" )
    private SmtpServer smtpServer;

    @Inject
    public EmailService( ConfigurationService configService ) throws IOException {
        configService.subscribe( this );
        updateMailConfiguration( configService.getConfiguration( MailServiceConfiguration.class ) );
    }

    /**
     * Updates email server preferences, including username and password, as well as the message templates.
     */
    @Subscribe
    public void updateMailConfiguration( MailServiceConfiguration config ) {
        Properties props = new Properties();
        props.setProperty( "mail.smtp.auth", config.getSmtpAuth() );
        props.setProperty( "mail.smtp.starttls.enable", config.getStartTtlsEnable() );
        props.setProperty( "mail.smtp.host", config.getSmtpHost() );
        props.setProperty( "mail.smtp.port", config.getSmtpPort() );

        String username = config.getUsername();
        String password = config.getPassword();
        session = Session.getInstance( props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication( username, password );
            }
        } );

        if ( Boolean.parseBoolean( config.getStartTtlsEnable() ) ) {
            smtpServer = SmtpSslServer.create()
                    .host( config.getSmtpHost() )
                    .port( Integer.parseInt( config.getSmtpPort() ) )
                    .auth( config.getSmtpAuth(), config.getPassword() )
                    .buildSmtpMailServer();
        } else {
            smtpServer = SmtpSslServer.create()
                    .host( config.getSmtpHost() )
                    .port(Integer.parseInt( config.getSmtpPort() ) )
                    .auth( config.getSmtpAuth(), config.getPassword() )
                    .buildSmtpMailServer();
        }

    }

    @Async
    public void sendMessage( String subject, String from, String to, String text ) {
        MimeMessage message = new MimeMessage( session );
        try {
            message.setSubject( subject );
            message.setFrom( new InternetAddress( from ) );
            message.setRecipients( Message.RecipientType.TO, InternetAddress.parse( to ) );
            message.setText( text, "utf-8", "html" );
            Transport.send( message );
        } catch ( MessagingException e ) {
            logger.error( e.getMessage() );
        }
    }

    public void sendMessage( Email email ) {
        sendManyMessages( Arrays.asList( email ) );
    }

    @Async
    public void sendManyMessages( Collection<Email> emails ) {
        SendMailSession session = smtpServer.createSession();
        emails.forEach( email -> {
            session.sendMail( email );
        } );
        session.close();
    }
}
