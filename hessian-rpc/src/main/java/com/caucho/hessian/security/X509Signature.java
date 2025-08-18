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

import com.caucho.hessian.io.HessianEnvelope;
import com.caucho.hessian.io.HessianRpcInput;
import com.caucho.hessian.io.HessianRpcOutput;
import io.github.wuwen5.hessian.io.HessianDecoder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.Setter;

public class X509Signature extends HessianEnvelope {
    /**
     * -- GETTER --
     *  Gets the encryption algorithm for the content.
     */
    @Getter
    private String algorithm = "HmacSHA256";

    private X509Certificate cert;
    /**
     * -- GETTER --
     *  The key to obtain the private key of the recipient.
     * -- SETTER --
     *  The private key.
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

    public X509Signature() {}

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
        if (privateKey == null) {
            throw new IOException("X509Signature.wrap requires a private key");
        }

        if (cert == null) {
            throw new IOException("X509Signature.wrap requires a certificate");
        }

        OutputStream os = new SignatureOutputStream(out);

        HessianRpcOutput filterOut = new HessianRpcOutput(os);

        filterOut.setCloseStreamOnClose(true);

        return filterOut;
    }

    @Override
    public HessianRpcInput unwrap(HessianRpcInput in) throws IOException {
        if (cert == null) {
            throw new IOException("X509Signature.unwrap requires a certificate");
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
        if (cert == null) {
            throw new IOException("X509Signature.unwrap requires a certificate");
        }

        InputStream is = new SignatureInputStream(in);

        HessianRpcInput filter = new HessianRpcInput(is);

        HessianDecoder.setCloseStreamOnClose(true);

        return filter;
    }

    class SignatureOutputStream extends OutputStream {
        private HessianRpcOutput out;
        private final OutputStream bodyOut;
        private final Mac mac;

        SignatureOutputStream(HessianRpcOutput out) throws IOException {
            try {
                KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);

                if (secureRandom != null) {
                    keyGen.init(secureRandom);
                }

                SecretKey sharedKey = keyGen.generateKey();

                this.out = out;

                this.out.startEnvelope(X509Signature.class.getName());

                PublicKey publicKey = cert.getPublicKey();

                byte[] encoded = publicKey.getEncoded();
                MessageDigest md = MessageDigest.getInstance("SHA1");
                md.update(encoded);
                byte[] fingerprint = md.digest();

                String keyAlgorithm = privateKey.getAlgorithm();
                Cipher keyCipher = Cipher.getInstance(keyAlgorithm);
                keyCipher.init(Cipher.WRAP_MODE, privateKey);

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

                mac = Mac.getInstance(algorithm);
                mac.init(sharedKey);

                bodyOut = this.out.getBytesOutputStream();
            } catch (RuntimeException | IOException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void write(int ch) throws IOException {
            bodyOut.write(ch);
            mac.update((byte) ch);
        }

        @Override
        public void write(byte[] buffer, int offset, int length) throws IOException {
            bodyOut.write(buffer, offset, length);
            mac.update(buffer, offset, length);
        }

        @Override
        public void close() throws IOException {
            HessianRpcOutput out = this.out;
            this.out = null;

            if (out == null) {
                return;
            }

            bodyOut.close();

            byte[] sig = mac.doFinal();

            out.writeInt(1);
            out.writeString("signature");
            out.writeBytes(sig);

            out.completeEnvelope();
            out.close();
        }
    }

    class SignatureInputStream extends InputStream {
        private HessianRpcInput in;

        private final Mac mac;
        private final InputStream bodyIn;

        SignatureInputStream(HessianRpcInput in) throws IOException {
            try {
                this.in = in;

                byte[] fingerprint = null;
                String keyAlgorithm = null;
                String algorithm = null;
                byte[] encKey = null;

                int len = in.readInt();

                for (int i = 0; i < len; i++) {
                    String header = in.readString();

                    if ("fingerprint".equals(header)) {
                        fingerprint = in.readBytes();
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

                if (keyAlgorithm == null || algorithm == null || encKey == null) {
                    throw new IOException("missing required headers");
                }

                Cipher keyCipher = Cipher.getInstance(keyAlgorithm);
                keyCipher.init(Cipher.UNWRAP_MODE, cert);

                Key key = keyCipher.unwrap(encKey, algorithm, Cipher.SECRET_KEY);
                bodyIn = this.in.readInputStream();

                mac = Mac.getInstance(algorithm);
                mac.init(key);
            } catch (RuntimeException | IOException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public int read() throws IOException {
            int ch = bodyIn.read();

            if (ch < 0) {
                return ch;
            }

            mac.update((byte) ch);

            return ch;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            int len = bodyIn.read(buffer, offset, length);

            if (len < 0) {
                return len;
            }

            mac.update(buffer, offset, len);

            return len;
        }

        @Override
        public void close() throws IOException {
            HessianRpcInput in = this.in;
            this.in = null;

            if (in != null) {
                bodyIn.close();

                int len = in.readInt();
                byte[] signature = null;

                for (int i = 0; i < len; i++) {
                    String header = in.readString();

                    if ("signature".equals(header)) signature = in.readBytes();
                }

                in.completeEnvelope();
                in.close();

                if (signature == null) {
                    throw new IOException("Expected signature");
                }

                byte[] sig = mac.doFinal();

                if (sig.length != signature.length) {
                    throw new IOException("mismatched signature");
                }

                for (int i = 0; i < sig.length; i++) {
                    if (signature[i] != sig[i]) {
                        throw new IOException("mismatched signature");
                    }
                }

                // XXX: save principal
            }
        }
    }
}
