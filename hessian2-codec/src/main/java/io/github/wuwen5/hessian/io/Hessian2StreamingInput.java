/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.wuwen5.hessian.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class Hessian2StreamingInput {

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

    public void setSerializerFactory(Hessian2SerializerFactory factory) {
        in.setSerializerFactory(factory);
    }

    public boolean isDataAvailable() {
        return this.is.isDataAvailable();
    }

    public HessianDecoder startPacket() throws IOException {
        if (is.startPacket()) {
            in.resetReferences();
            in.resetBuffer(); // XXX:
            return in;
        } else {
            return null;
        }
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

        private long length;
        private boolean isPacketEnd;

        StreamingInputStream(InputStream is) {
            this.is = is;
        }

        public boolean isDataAvailable() {
            try {
                return is != null && is.available() > 0;
            } catch (IOException e) {
                log.trace(e.toString(), e);

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

                while (length > 0) {
                    long skipped = is.skip(length);
                    if (skipped <= 0) {
                        if (is.read() == -1) {
                            throw new EOFException("Unexpected end of stream while skipping packet");
                        } else {
                            skipped = 1;
                        }
                    }
                    length -= skipped;
                }
            }
        }

        @Override
        public int read() throws IOException {

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

            if (this.length <= 0) {
                if (isPacketEnd) {
                    return -1;
                }

                this.length = readChunkLength(is);

                if (this.length <= 0) {
                    return -1;
                }
            }

            long sublen = this.length;
            if (length < sublen) {
                sublen = length;
            }

            sublen = is.read(buffer, offset, (int) sublen);

            if (sublen < 0) {
                return -1;
            }

            this.length -= sublen;

            return (int) sublen;
        }

        private long readChunkLength(InputStream is) throws IOException {
            if (isPacketEnd) {
                return -1;
            }

            int code = is.read();

            if (code < 0) {
                isPacketEnd = true;
                return -1;
            }

            isPacketEnd = (code & 0x80) == 0;

            int len = is.read() & 0x7f;

            if (len < 0x7e) {
                return len;
            } else if (len == 0x7e) {
                int hi = is.read();
                int lo = is.read();
                if (hi < 0 || lo < 0) {
                    throw new EOFException("Unexpected end of stream in medium chunk length");
                }
                return ((hi & 0xff) << 8) | (lo & 0xff);
            } else {
                long l = 0;
                for (int i = 0; i < 8; i++) {
                    int b = is.read();
                    if (b < 0) {
                        throw new EOFException("Unexpected end of stream in large chunk length");
                    }
                    l = (l << 8) | (b & 0xff);
                }
                return l;
            }
        }
    }
}
