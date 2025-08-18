/*
 * Copyright (c) 2001-2004 Caucho Technology, Inc.  All rights reserved.
 *
 * The Apache Software License, Version 1.1
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Caucho Technology (http://www.caucho.com/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Hessian", "Resin", and "Caucho" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    info@caucho.com.
 *
 * 5. Products derived from this software may not be called "Resin"
 *    nor may "Resin" appear in their names without prior written
 *    permission of Caucho Technology.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL CAUCHO TECHNOLOGY OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author Scott Ferguson
 */

package com.caucho.hessian.security;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.HessianEnvelope;
import com.caucho.hessian.io.HessianRpcInput;
import com.caucho.hessian.io.HessianRpcOutput;
import io.github.wuwen5.hessian.io.HessianDecoder;
import java.io.*;
import java.security.*;
import java.security.cert.*;
import javax.crypto.*;
import lombok.Getter;
import lombok.Setter;

public class X509Encryption extends HessianEnvelope {
    /**
     * -- GETTER --
     *  Gets the encryption algorithm for the content.
     */
    @Getter
    private String algorithm = "AES";

    /**
     * certificate for encryption/decryption
     */
    private X509Certificate cert;

    /**
     * private key for decryption
     * -- GETTER --
     *  The private key for decryption.
     * -- SETTER --
     *  The X509 certificate to obtain the public key of the recipient.
     *
     *
     */
    @Setter
    @Getter
    private PrivateKey privateKey;

    /**
     * -- GETTER --
     *  The random number generator for the shared secrets.
     * -- SETTER --
     *  The random number generator for the shared secrets.
     *
     */
    @Setter
    @Getter
    private SecureRandom secureRandom;

    public X509Encryption() {}

    /**
     * Sets the encryption algorithm for the content.
     */
    public void setAlgorithm(String algorithm) {
        if (algorithm == null) {
            throw new NullPointerException();
        }

        this.algorithm = algorithm;
    }

    /**
     * The X509 certificate to obtain the public key of the recipient.
     */
    public X509Certificate getCertificate() {
        return cert;
    }

    /**
     * The X509 certificate to obtain the public key of the recipient.
     */
    public void setCertificate(X509Certificate cert) {
        this.cert = cert;
    }

    @Override
    public HessianRpcOutput wrap(HessianRpcOutput out) throws IOException {
        if (cert == null) {
            throw new IOException("X509Encryption.wrap requires a certificate");
        }

        OutputStream os = new EncryptOutputStream(out);

        HessianRpcOutput filterOut = new HessianRpcOutput(os);

        filterOut.setCloseStreamOnClose(true);

        return filterOut;
    }

    @Override
    public HessianRpcInput unwrap(HessianRpcInput in) throws IOException {
        if (privateKey == null) {
            throw new IOException("X509Encryption.unwrap requires a private key");
        }

        if (cert == null) {
            throw new IOException("X509Encryption.unwrap requires a certificate");
        }

        int version = in.readEnvelope();

        String method = in.readMethod();

        if (!method.equals(getClass().getName())) {
            throw new IOException(
                    "expected hessian Envelope method '" + getClass().getName() + "' at '" + method + "'");
        }

        return unwrapHeaders(in);
    }

    @Override
    public HessianRpcInput unwrapHeaders(HessianRpcInput in) throws IOException {
        if (privateKey == null) {
            throw new IOException("X509Encryption.unwrap requires a private key");
        }

        if (cert == null) {
            throw new IOException("X509Encryption.unwrap requires a certificate");
        }

        InputStream is = new EncryptInputStream(in);

        HessianRpcInput filter = new HessianRpcInput(is);

        HessianDecoder.setCloseStreamOnClose(true);

        return filter;
    }

    class EncryptOutputStream extends OutputStream {
        private HessianRpcOutput out;

        private final OutputStream bodyOut;
        private final CipherOutputStream cipherOut;

        EncryptOutputStream(HessianRpcOutput out) throws IOException {
            try {
                this.out = out;

                KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);

                if (secureRandom != null) {
                    keyGen.init(secureRandom);
                }

                SecretKey sharedKey = keyGen.generateKey();

                this.out = out;

                this.out.startEnvelope(X509Encryption.class.getName());

                PublicKey publicKey = cert.getPublicKey();

                byte[] encoded = publicKey.getEncoded();
                MessageDigest md = MessageDigest.getInstance("SHA1");
                md.update(encoded);
                byte[] fingerprint = md.digest();

                String keyAlgorithm = publicKey.getAlgorithm();
                Cipher keyCipher = Cipher.getInstance(keyAlgorithm);
                if (secureRandom != null) {
                    keyCipher.init(Cipher.WRAP_MODE, cert, secureRandom);
                } else {
                    keyCipher.init(Cipher.WRAP_MODE, cert);
                }

                byte[] encKey = keyCipher.wrap(sharedKey);

                this.out.writeInt(4);

                this.out.writeString("algorithm");
                this.out.writeString(algorithm);
                this.out.writeString("fingerprint");
                this.out.writeBytes(fingerprint);
                this.out.writeString("key-algorithm");
                this.out.writeString(keyAlgorithm);
                this.out.writeString("key");
                this.out.writeBytes(encKey);

                bodyOut = this.out.getBytesOutputStream();

                Cipher cipher = Cipher.getInstance(algorithm);
                if (secureRandom != null) {
                    cipher.init(Cipher.ENCRYPT_MODE, sharedKey, secureRandom);
                } else {
                    cipher.init(Cipher.ENCRYPT_MODE, sharedKey);
                }

                cipherOut = new CipherOutputStream(bodyOut, cipher);
            } catch (RuntimeException | IOException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void write(int ch) throws IOException {
            cipherOut.write(ch);
        }

        @Override
        public void write(byte[] buffer, int offset, int length) throws IOException {
            cipherOut.write(buffer, offset, length);
        }

        @Override
        public void close() throws IOException {
            HessianRpcOutput out = this.out;
            this.out = null;

            if (out != null) {
                cipherOut.close();
                bodyOut.close();

                out.writeInt(0);
                out.completeEnvelope();
                out.close();
            }
        }
    }

    class EncryptInputStream extends InputStream {
        private Hessian2Input in;

        private final InputStream bodyIn;
        private final CipherInputStream cipherIn;

        EncryptInputStream(Hessian2Input in) throws IOException {
            try {
                this.in = in;

                String keyAlgorithm = null;
                String algorithm = null;
                byte[] encKey = null;

                int len = in.readInt();

                for (int i = 0; i < len; i++) {
                    String header = in.readString();

                    if ("fingerprint".equals(header)) {
                        in.readBytes();
                    } else if ("key-algorithm".equals(header)) {
                        keyAlgorithm = in.readString();
                    } else if ("algorithm".equals(header)) {
                        algorithm = in.readString();
                    } else if ("key".equals(header)) {
                        encKey = in.readBytes();
                    } else {
                        throw new IOException("'" + header + "' is an unexpected header");
                    }
                }

                if (keyAlgorithm == null || algorithm == null) {
                    throw new IOException("X509Encryption: missing key-algorithm or algorithm header");
                }

                Cipher keyCipher = Cipher.getInstance(keyAlgorithm);
                keyCipher.init(Cipher.UNWRAP_MODE, privateKey);

                Key key = keyCipher.unwrap(encKey, algorithm, Cipher.SECRET_KEY);
                bodyIn = this.in.readInputStream();

                Cipher cipher = Cipher.getInstance(algorithm);
                cipher.init(Cipher.DECRYPT_MODE, key);

                cipherIn = new CipherInputStream(bodyIn, cipher);
            } catch (RuntimeException | IOException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public int read() throws IOException {
            return cipherIn.read();
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            return cipherIn.read(buffer, offset, length);
        }

        @Override
        public void close() throws IOException {
            Hessian2Input in = this.in;
            this.in = null;

            if (in != null) {
                cipherIn.close();
                bodyIn.close();

                int len = in.readInt();

                if (len != 0) {
                    throw new IOException("Unexpected footer");
                }

                ((HessianRpcInput) in).completeEnvelope();

                in.close();
            }
        }
    }
}
