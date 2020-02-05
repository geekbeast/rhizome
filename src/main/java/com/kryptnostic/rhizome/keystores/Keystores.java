package com.kryptnostic.rhizome.keystores;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import com.google.common.io.Resources;

public final class Keystores {
    private Keystores() {}

    public static KeyStore loadKeystoreFromStream( InputStream in, char[] password )
            throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException {
        KeyStore ks = KeyStore.getInstance( "JKS" );
        ks.load( in, password );
        return ks;
    }
}
