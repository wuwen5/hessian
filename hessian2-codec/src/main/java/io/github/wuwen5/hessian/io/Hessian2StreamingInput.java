/*
 * Copyright (c) 2001-2008 Caucho Technology, Inc.  All rights reserved.
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
 * 4. The names "Burlap", "Resin", and "Caucho" must not be used to
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

package io.github.wuwen5.hessian.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.*;

/**
 * Input stream for Hessian 2 streaming requests using WebSocket.
 * For best performance, use HessianFactory:
 *<pre>
 * <code>
 * HessianFactory factory = new HessianFactory();
 * Hessian2StreamingInput hIn = factory.createHessian2StreamingInput(is);
 * </code>
 * </pre>
 */
public class Hessian2StreamingInput {
    private static final Logger log = Logger.getLogger(Hessian2StreamingInput.class.getName());

    private final StreamingInputStream is;
    private final HessianDecoder in;

    /**
     * Creates a new Hessian input stream, initialized with an
     * underlying input stream.
     *
     * @param is the underlying output stream.
     */
    public Hessian2StreamingInput(InputStream is) {
        this.is = new StreamingInputStream(is);
        in = new HessianDecoder(this.is);
    }

    public void setSerializerFactory(SerializerFactory factory) {
        in.setSerializerFactory(factory);
    }

    public boolean isDataAvailable() {
        StreamingInputStream is = this.is;

        return is != null && is.isDataAvailable();
    }

    public HessianDecoder startPacket() throws IOException {
        if (is.startPacket()) {
            in.resetReferences();
            in.resetBuffer(); // XXX:
            return in;
        } else return null;
    }

    public void endPacket() throws IOException {
        is.endPacket();
        in.resetBuffer(); // XXX:
    }

    public HessianDecoder getHessianInput() {
        return in;
    }

    /**
     * Read the next object
     */
    public Object readObject() throws IOException {
        is.startPacket();

        Object obj = in.readStreamingObject();

        is.endPacket();

        return obj;
    }

    /**
     * Close the output.
     */
    public void close() throws IOException {
        in.close();
    }

    static class StreamingInputStream extends InputStream {
        private final InputStream is;

        private int length;
        private boolean isPacketEnd;

        StreamingInputStream(InputStream is) {
            this.is = is;
        }

        public boolean isDataAvailable() {
            try {
                return is != null && is.available() > 0;
            } catch (IOException e) {
                log.log(Level.FINER, e.toString(), e);

                return true;
            }
        }

        public boolean startPacket() throws IOException {
            // skip zero-length packets
            do {
                isPacketEnd = false;
            } while ((length = readChunkLength(is)) == 0);

            return length > 0;
        }

        public void endPacket() throws IOException {
            while (!isPacketEnd) {
                if (length <= 0) {
                    length = readChunkLength(is);
                }

                if (length > 0) {
                    is.skip(length);
                    length = 0;
                }
            }

            if (length > 0) {
                is.skip(length);
                length = 0;
            }
        }

        @Override
        public int read() throws IOException {
            InputStream is = this.is;

            if (length == 0) {
                if (isPacketEnd) {
                    return -1;
                }

                length = readChunkLength(is);

                if (length <= 0) {
                    return -1;
                }
            }

            length--;

            return is.read();
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            InputStream is = this.is;

            if (this.length <= 0) {
                if (isPacketEnd) {
                    return -1;
                }

                this.length = readChunkLength(is);

                if (this.length <= 0) {
                    return -1;
                }
            }

            int sublen = this.length;
            if (length < sublen) {
                sublen = length;
            }

            sublen = is.read(buffer, offset, sublen);

            if (sublen < 0) {
                return -1;
            }

            this.length -= sublen;

            return sublen;
        }

        private int readChunkLength(InputStream is) throws IOException {
            if (isPacketEnd) {
                return -1;
            }

            int length = 0;

            int code = is.read();

            if (code < 0) {
                isPacketEnd = true;
                return -1;
            }

            isPacketEnd = (code & 0x80) == 0;

            int len = is.read() & 0x7f;

            if (len < 0x7e) {
                length = len;
            } else if (len == 0x7e) {
                length = (((is.read() & 0xff) << 8) + (is.read() & 0xff));
            } else {
                length = (((is.read() & 0xff) << 56)
                        + ((is.read() & 0xff) << 48)
                        + ((is.read() & 0xff) << 40)
                        + ((is.read() & 0xff) << 32)
                        + ((is.read() & 0xff) << 24)
                        + ((is.read() & 0xff) << 16)
                        + ((is.read() & 0xff) << 8)
                        + ((is.read() & 0xff)));
            }

            return length;
        }
    }
}
