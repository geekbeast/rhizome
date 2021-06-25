package com.kryptnostic.rhizome.emails;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.kryptnostic.rhizome.pods.hazelcast.SelfRegisteringStreamSerializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jodd.mail.Email;
import jodd.mail.EmailAddress;
import jodd.mail.EmailMessage;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractEmailStreamSerializer implements SelfRegisteringStreamSerializer<Email> {

    @Override
    public void write( ObjectDataOutput out, Email object ) throws IOException {
        serialize( out, object );
    }

    @Override
    public Email read( ObjectDataInput in ) throws IOException {
        return deserialize( in );
    }

    @Override
    public abstract int getTypeId();

    @Override
    public void destroy() {

    }

    @Override
    public Class<Email> getClazz() {
        return Email.class;
    }

    public static void serialize( ObjectDataOutput out, Email object ) throws IOException {
        serializeMailAddresses( out, object.from() );
        serializeMailAddresses( out, object.to() );
        serializeMailAddresses( out, object.cc() );
        serializeMailAddresses( out, object.bcc() );
        serializeMailAddresses( out, object.replyTo() );
        out.writeUTF( object.subject() );
        out.writeUTF( object.subjectEncoding() );
        out.writeInt( object.priority() );
        Date sentDate = object.sentDate();
        boolean hasSentDate = ( sentDate != null );
        out.writeBoolean( hasSentDate );
        if ( hasSentDate ) {
            out.writeLong( object.sentDate().getTime() );
        }
        serializeEmailMessages( out, object.messages() );
        // TODO: Handle attachments  
        // TODO: What do we do with headers in email?
    }

    public static Email deserialize( ObjectDataInput in ) throws IOException {
        Email email = new Email();
        EmailAddress from = deserializeMailAddresses( in )[ 0 ];
        EmailAddress[] tos = deserializeMailAddresses( in );
        EmailAddress[] ccs = deserializeMailAddresses( in );
        EmailAddress[] bccs = deserializeMailAddresses( in );
        EmailAddress[] replyTo = deserializeMailAddresses( in );

        String subject = in.readString();
        String subjectEncoding = in.readString();
        int priority = in.readInt();
        boolean hasSentDate = in.readBoolean();

        if ( hasSentDate ) {
            long sentTime = in.readLong();
            email.sentDate( new Date( sentTime ) ); // This is here to avoid double if check & auto-boxing
        }

        List<EmailMessage> messages = deserializeEmailMessages( in );
        for ( EmailMessage message : messages ) {
            email.message( message );
        }

        email.from( from );
        email.to( tos );
        email.cc( ccs );
        email.bcc( bccs );
        email.replyTo( replyTo );
        email.subject( subject, subjectEncoding );
        email.priority( priority );

        return email;
    }

    public static void serializeMailAddresses( ObjectDataOutput out, EmailAddress... addresses ) throws IOException {
        out.writeInt( addresses.length );
        for ( EmailAddress address : addresses ) {
            out.writeUTF( address.getEmail() );

            String personalName = address.getPersonalName();
            boolean hasPersonalName = StringUtils.isNotBlank( personalName );
            out.writeBoolean( hasPersonalName );
            if ( hasPersonalName ) {
                out.writeUTF( address.getPersonalName() );
            }
        }
    }

    public static EmailAddress[] deserializeMailAddresses( ObjectDataInput in ) throws IOException {
        int length = in.readInt();
        EmailAddress[] addresses = new EmailAddress[ length ];
        for ( int i = 0; i < length; ++i ) {
            String email = in.readString();
            boolean hasPersonalName = in.readBoolean();
            if ( hasPersonalName ) {
                String personalName = in.readString();
                addresses[ i ] = new EmailAddress( personalName, email );
            } else {
                addresses[ i ] = EmailAddress.of( email );
            }
        }
        return addresses;
    }

    public static void serializeEmailMessages( ObjectDataOutput out, List<EmailMessage> messages ) throws IOException {
        out.writeInt( messages.size() );
        for ( EmailMessage message : messages ) {
            out.writeUTF( message.getContent() );
            out.writeUTF( message.getEncoding() );
            out.writeUTF( message.getMimeType() );
        }
    }

    public static List<EmailMessage> deserializeEmailMessages( ObjectDataInput in ) throws IOException {
        int size = in.readInt();
        List<EmailMessage> messages = new ArrayList<>( size );
        for ( int i = 0; i < size; ++i ) {
            String content = in.readString();
            String encoding = in.readString();
            String mimeType = in.readString();
            messages.add( new EmailMessage( content, mimeType, encoding ) );
        }
        return messages;
    }

}
