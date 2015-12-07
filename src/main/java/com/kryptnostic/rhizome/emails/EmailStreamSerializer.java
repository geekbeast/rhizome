package com.kryptnostic.rhizome.emails;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import jersey.repackaged.com.google.common.collect.Lists;
import jodd.mail.Email;
import jodd.mail.EmailMessage;
import jodd.mail.MailAddress;

import org.apache.commons.lang3.StringUtils;

import com.geekbeast.rhizome.pods.hazelcast.SelfRegisteringStreamSerializer;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public abstract class EmailStreamSerializer implements SelfRegisteringStreamSerializer<Email> {

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
        serializeMailAddresses( out, object.getFrom() );
        serializeMailAddresses( out, object.getTo() );
        serializeMailAddresses( out, object.getCc() );
        serializeMailAddresses( out, object.getBcc() );
        serializeMailAddresses( out, object.getReplyTo() );
        out.writeUTF( object.getSubject() );
        out.writeUTF( object.getSubjectEncoding() );
        out.writeInt( object.getPriority() );
        Date sentDate = object.getSentDate();
        boolean hasSentDate = ( sentDate != null );
        out.writeBoolean( hasSentDate );
        if ( hasSentDate ) {
            out.writeLong( object.getSentDate().getTime() );
        }
        serializeEmailMessages( out, object.getAllMessages() );
        // TODO: Handle attachments  
        // TODO: What do we do with headers in email?
    }

    public static Email deserialize( ObjectDataInput in ) throws IOException {
        Email email = new Email();
        MailAddress from = deserializeMailAddresses( in )[ 0 ];
        MailAddress[] tos = deserializeMailAddresses( in );
        MailAddress[] ccs = deserializeMailAddresses( in );
        MailAddress[] bccs = deserializeMailAddresses( in );
        MailAddress[] replyTo = deserializeMailAddresses( in );

        String subject = in.readUTF();
        String subjectEncoding = in.readUTF();
        int priority = in.readInt();
        boolean hasSentDate = in.readBoolean();

        if ( hasSentDate ) {
            long sentTime = in.readLong();
            email.setSentDate( new Date( sentTime ) ); // This is here to avoid double if check & auto-boxing
        }

        List<EmailMessage> messages = deserializeEmailMessages( in );
        for ( EmailMessage message : messages ) {
            email.addMessage( message );
        }

        email.setFrom( from );
        email.setTo( tos );
        email.setCc( ccs );
        email.setBcc( bccs );
        email.setReplyTo( replyTo );
        email.setSubject( subject, subjectEncoding );
        email.setPriority( priority );

        return email;
    }

    public static void serializeMailAddresses( ObjectDataOutput out, MailAddress... addresses ) throws IOException {
        out.writeInt( addresses.length );
        for ( MailAddress address : addresses ) {
            out.writeUTF( address.getEmail() );

            String personalName = address.getPersonalName();
            boolean hasPersonalName = StringUtils.isNotBlank( personalName );
            out.writeBoolean( hasPersonalName );
            if ( hasPersonalName ) {
                out.writeUTF( address.getPersonalName() );
            }
        }
    }

    public static MailAddress[] deserializeMailAddresses( ObjectDataInput in ) throws IOException {
        int length = in.readInt();
        MailAddress[] addresses = new MailAddress[ length ];
        for ( int i = 0; i < length; ++i ) {
            String email = in.readUTF();
            boolean hasPersonalName = in.readBoolean();
            if ( hasPersonalName ) {
                String personalName = in.readUTF();
                addresses[ i ] = new MailAddress( personalName, email );
            } else {
                addresses[ i ] = new MailAddress( email );
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
        List<EmailMessage> messages = Lists.newArrayListWithCapacity( size );
        for ( int i = 0; i < size; ++i ) {
            String content = in.readUTF();
            String encoding = in.readUTF();
            String mimeType = in.readUTF();
            messages.add( new EmailMessage( content, mimeType, encoding ) );
        }
        return messages;
    }

    
}
