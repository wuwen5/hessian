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

package io.github.wuwen5.hessian.io;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Input stream for Hessian requests.
 *
 * <p>HessianInput is unbuffered, so any client needs to provide
 * its own buffering.
 *
 * <pre>
 * InputStream is = ...;
 * HessianInput in = new HessianInput(is);
 * in.readObject();
 *
 * </pre>
 */
@Slf4j
public class HessianDecoder extends AbstractHessianDecoder implements Hessian2Constants {

    private static final int END_OF_DATA = -2;

    private static final int SIZE = 1024;
    private static final int GAP = 16;

    /**
     * standard, unmodified factory for deserializing objects
     */
    protected SerializerFactory defaultSerializerFactory;
    /**
     * factory for deserializing objects in the input stream
     * -- SETTER --
     * Sets the serializer factory.
     */
    @Setter
    protected SerializerFactory serializerFactory;

    private static boolean isCloseStreamOnClose;

    protected ArrayList<Object> refs = new ArrayList<>();
    protected ArrayList<ObjectDefinition> classDefs = new ArrayList<>();
    protected ArrayList<String> types = new ArrayList<>();

    /**
     * the underlying input stream
     */
    private InputStream is;

    private final byte[] buffer = new byte[SIZE];

    /**
     * a peek character
     */
    private int offset;

    private int length;

    private final StringBuilder sbuf = new StringBuilder();

    /**
     * true if this is the last chunk
     */
    private boolean isLastChunk;

    /**
     * the chunk length
     */
    private int chunkLength;

    private HessianDebugInputStream dIs;

    public HessianDecoder() {
        if (log.isTraceEnabled()) {
            dIs = new HessianDebugInputStream(log::trace);
        }
    }

    /**
     * Creates a new Hessian input stream, initialized with an
     * underlying input stream.
     *
     * @param is the underlying input stream.
     */
    public HessianDecoder(InputStream is) {
        this();

        init(is);
    }

    /**
     * Gets the serializer factory.
     */
    public SerializerFactory getSerializerFactory() {
        // the default serializer factory cannot be modified by external
        // callers
        if (serializerFactory == defaultSerializerFactory) {
            serializerFactory = new SerializerFactory();
        }

        return serializerFactory;
    }

    /**
     * Gets the serializer factory.
     */
    protected final SerializerFactory findSerializerFactory() {
        SerializerFactory factory = serializerFactory;

        if (factory == null) {
            factory = SerializerFactory.createDefault();
            defaultSerializerFactory = factory;
            serializerFactory = factory;
        }

        return factory;
    }

    public void allow(String pattern) {
        ClassFactory factory = getSerializerFactory().getClassFactory();

        factory.allow(pattern);
    }

    public void setCloseStreamOnClose(boolean isClose) {
        isCloseStreamOnClose = isClose;
    }

    public boolean isCloseStreamOnClose() {
        return isCloseStreamOnClose;
    }

    @Override
    public void init(InputStream is) {
        if (dIs != null) {
            dIs.initPacket(is);
            is = dIs;
        }

        this.is = is;

        reset();
    }

    public void initPacket(InputStream is) {
        if (dIs != null) {
            dIs.initPacket(is);
            is = dIs;
        }

        this.is = is;

        resetReferences();
    }

    /**
     * Starts reading the envelope
     *
     * <pre>
     * E major minor
     * </pre>
     */
    public int readEnvelope() throws IOException {
        int tag = read();
        int version = 0;

        if (tag == 'H') {
            int major = read();
            int minor = read();

            version = (major << 16) + minor;

            tag = read();
        }

        if (tag != 'E') {
            throw error("expected hessian Envelope ('E') at " + codeName(tag));
        }

        return version;
    }

    /**
     * Completes reading the envelope
     *
     * <p>A successful completion will have a single value:
     *
     * <pre>
     * Z
     * </pre>
     */
    public void completeEnvelope() throws IOException {
        int tag = read();

        if (tag != 'Z') {
            error("expected end of envelope at " + codeName(tag));
        }
    }

    public Object[] readArguments() throws IOException {
        int len = readInt();

        Object[] args = new Object[len];

        for (int i = 0; i < len; i++) {
            args[i] = readObject();
        }

        return args;
    }

    /**
     * Completes reading the call
     *
     * <p>A successful completion will have a single value:
     *
     * <pre>
     * z
     * </pre>
     */
    public void completeValueReply() throws IOException {
        int tag = read();

        if (tag != 'Z') {
            error("expected end of reply at " + codeName(tag));
        }
    }

    /**
     * Starts reading a packet
     *
     * <pre>
     * p major minor
     * </pre>
     */
    public int startMessage() throws IOException {
        int tag = read();

        if (tag != 'p' && tag != 'P') {
            throw error("expected Hessian message ('p') at " + codeName(tag));
        }

        int major = read();
        int minor = read();

        return (major << 16) + minor;
    }

    /**
     * Completes reading the message
     *
     * <p>A successful completion will have a single value:
     *
     * <pre>
     * z
     * </pre>
     */
    public void completeMessage() throws IOException {
        int tag = read();

        if (tag != 'Z') {
            error("expected end of message at " + codeName(tag));
        }
    }

    /**
     * Reads a null
     *
     * <pre>
     * N
     * </pre>
     */
    @Override
    public void readNull() throws IOException {
        int tag = read();

        if (tag == BC_NULL) {
            return;
        }
        throw expect("null", tag);
    }

    /**
     * Reads a boolean
     *
     * <pre>
     * T
     * F
     * </pre>
     */
    @Override
    public boolean readBoolean() throws IOException {
        int tag = offset < length ? (buffer[offset++] & 0xff) : read();

        switch (tag) {
            case BC_TRUE:
                return true;
            case BC_FALSE:
                return false;

                // direct integer
            case 0x80:
            case 0x81:
            case 0x82:
            case 0x83:
            case 0x84:
            case 0x85:
            case 0x86:
            case 0x87:
            case 0x88:
            case 0x89:
            case 0x8a:
            case 0x8b:
            case 0x8c:
            case 0x8d:
            case 0x8e:
            case 0x8f:

            case 0x90:
            case 0x91:
            case 0x92:
            case 0x93:
            case 0x94:
            case 0x95:
            case 0x96:
            case 0x97:
            case 0x98:
            case 0x99:
            case 0x9a:
            case 0x9b:
            case 0x9c:
            case 0x9d:
            case 0x9e:
            case 0x9f:

            case 0xa0:
            case 0xa1:
            case 0xa2:
            case 0xa3:
            case 0xa4:
            case 0xa5:
            case 0xa6:
            case 0xa7:
            case 0xa8:
            case 0xa9:
            case 0xaa:
            case 0xab:
            case 0xac:
            case 0xad:
            case 0xae:
            case 0xaf:

            case 0xb0:
            case 0xb1:
            case 0xb2:
            case 0xb3:
            case 0xb4:
            case 0xb5:
            case 0xb6:
            case 0xb7:
            case 0xb8:
            case 0xb9:
            case 0xba:
            case 0xbb:
            case 0xbc:
            case 0xbd:
            case 0xbe:
            case 0xbf:
                return tag != BC_INT_ZERO;

                // INT_BYTE = 0
            case 0xc8:
                return read() != 0;

                // INT_BYTE != 0
            case 0xc0:
            case 0xc1:
            case 0xc2:
            case 0xc3:
            case 0xc4:
            case 0xc5:
            case 0xc6:
            case 0xc7:
            case 0xc9:
            case 0xca:
            case 0xcb:
            case 0xcc:
            case 0xcd:
            case 0xce:
            case 0xcf:
                read();
                return true;

                // INT_SHORT = 0
            case 0xd4:
                return (256 * read() + read()) != 0;

                // INT_SHORT != 0
            case 0xd0:
            case 0xd1:
            case 0xd2:
            case 0xd3:
            case 0xd5:
            case 0xd6:
            case 0xd7:
                read();
                read();
                return true;

            case 'I':
                return parseInt() != 0;

            case 0xd8:
            case 0xd9:
            case 0xda:
            case 0xdb:
            case 0xdc:
            case 0xdd:
            case 0xde:
            case 0xdf:

            case 0xe0:
            case 0xe1:
            case 0xe2:
            case 0xe3:
            case 0xe4:
            case 0xe5:
            case 0xe6:
            case 0xe7:
            case 0xe8:
            case 0xe9:
            case 0xea:
            case 0xeb:
            case 0xec:
            case 0xed:
            case 0xee:
            case 0xef:
                return tag != BC_LONG_ZERO;

                // LONG_BYTE = 0
            case 0xf8:
                return read() != 0;

                // LONG_BYTE != 0
            case 0xf0:
            case 0xf1:
            case 0xf2:
            case 0xf3:
            case 0xf4:
            case 0xf5:
            case 0xf6:
            case 0xf7:
            case 0xf9:
            case 0xfa:
            case 0xfb:
            case 0xfc:
            case 0xfd:
            case 0xfe:
            case 0xff:
                read();
                return true;

                // INT_SHORT = 0
            case 0x3c:
                return (256 * read() + read()) != 0;

                // INT_SHORT != 0
            case 0x38:
            case 0x39:
            case 0x3a:
            case 0x3b:
            case 0x3d:
            case 0x3e:
            case 0x3f:
                read();
                read();
                return true;

            case BC_LONG_INT:
                return (0x1000000L * read() + 0x10000L * read() + 0x100 * read() + read()) != 0;

            case BC_LONG:
                return parseLong() != 0;

            case BC_DOUBLE_ZERO:
                return false;

            case BC_DOUBLE_ONE:
                return true;

            case BC_DOUBLE_BYTE:
                return read() != 0;

            case BC_DOUBLE_SHORT:
                return (0x100 * read() + read()) != 0;

            case BC_DOUBLE_MILL: {
                int mills = parseInt();

                return mills != 0;
            }

            case BC_DOUBLE:
                return parseDouble() != 0.0;

            case BC_NULL:
                return false;

            default:
                throw expect("boolean", tag);
        }
    }

    /**
     * Reads a short
     *
     * <pre>
     * I b32 b24 b16 b8
     * </pre>
     */
    public short readShort() throws IOException {
        return (short) readInt();
    }

    /**
     * Reads an integer
     *
     * <pre>
     * I b32 b24 b16 b8
     * </pre>
     */
    @Override
    public final int readInt() throws IOException {
        // int tag = _offset < _length ? (_buffer[_offset++] & 0xff) : read();
        int tag = read();

        switch (tag) {
            case BC_NULL:
                return 0;

            case BC_FALSE:
                return 0;

            case BC_TRUE:
                return 1;

                // direct integer
            case 0x80:
            case 0x81:
            case 0x82:
            case 0x83:
            case 0x84:
            case 0x85:
            case 0x86:
            case 0x87:
            case 0x88:
            case 0x89:
            case 0x8a:
            case 0x8b:
            case 0x8c:
            case 0x8d:
            case 0x8e:
            case 0x8f:

            case 0x90:
            case 0x91:
            case 0x92:
            case 0x93:
            case 0x94:
            case 0x95:
            case 0x96:
            case 0x97:
            case 0x98:
            case 0x99:
            case 0x9a:
            case 0x9b:
            case 0x9c:
            case 0x9d:
            case 0x9e:
            case 0x9f:

            case 0xa0:
            case 0xa1:
            case 0xa2:
            case 0xa3:
            case 0xa4:
            case 0xa5:
            case 0xa6:
            case 0xa7:
            case 0xa8:
            case 0xa9:
            case 0xaa:
            case 0xab:
            case 0xac:
            case 0xad:
            case 0xae:
            case 0xaf:

            case 0xb0:
            case 0xb1:
            case 0xb2:
            case 0xb3:
            case 0xb4:
            case 0xb5:
            case 0xb6:
            case 0xb7:
            case 0xb8:
            case 0xb9:
            case 0xba:
            case 0xbb:
            case 0xbc:
            case 0xbd:
            case 0xbe:
            case 0xbf:
                return tag - BC_INT_ZERO;

                /* byte int */
            case 0xc0:
            case 0xc1:
            case 0xc2:
            case 0xc3:
            case 0xc4:
            case 0xc5:
            case 0xc6:
            case 0xc7:
            case 0xc8:
            case 0xc9:
            case 0xca:
            case 0xcb:
            case 0xcc:
            case 0xcd:
            case 0xce:
            case 0xcf:
                return ((tag - BC_INT_BYTE_ZERO) << 8) + read();

                /* short int */
            case 0xd0:
            case 0xd1:
            case 0xd2:
            case 0xd3:
            case 0xd4:
            case 0xd5:
            case 0xd6:
            case 0xd7:
                return ((tag - BC_INT_SHORT_ZERO) << 16) + 256 * read() + read();

            case 'I':
            case BC_LONG_INT:
                return ((read() << 24) + (read() << 16) + (read() << 8) + read());

                // direct long
            case 0xd8:
            case 0xd9:
            case 0xda:
            case 0xdb:
            case 0xdc:
            case 0xdd:
            case 0xde:
            case 0xdf:

            case 0xe0:
            case 0xe1:
            case 0xe2:
            case 0xe3:
            case 0xe4:
            case 0xe5:
            case 0xe6:
            case 0xe7:
            case 0xe8:
            case 0xe9:
            case 0xea:
            case 0xeb:
            case 0xec:
            case 0xed:
            case 0xee:
            case 0xef:
                return tag - BC_LONG_ZERO;

                /* byte long */
            case 0xf0:
            case 0xf1:
            case 0xf2:
            case 0xf3:
            case 0xf4:
            case 0xf5:
            case 0xf6:
            case 0xf7:
            case 0xf8:
            case 0xf9:
            case 0xfa:
            case 0xfb:
            case 0xfc:
            case 0xfd:
            case 0xfe:
            case 0xff:
                return ((tag - BC_LONG_BYTE_ZERO) << 8) + read();

                /* short long */
            case 0x38:
            case 0x39:
            case 0x3a:
            case 0x3b:
            case 0x3c:
            case 0x3d:
            case 0x3e:
            case 0x3f:
                return ((tag - BC_LONG_SHORT_ZERO) << 16) + 256 * read() + read();

            case BC_LONG:
                return (int) parseLong();

            case BC_DOUBLE_ZERO:
                return 0;

            case BC_DOUBLE_ONE:
                return 1;

                // case LONG_BYTE:
            case BC_DOUBLE_BYTE:
                return (byte) (offset < length ? buffer[offset++] : read());

                // case INT_SHORT:
                // case LONG_SHORT:
            case BC_DOUBLE_SHORT:
                return (short) (256 * read() + read());

            case BC_DOUBLE_MILL: {
                int mills = parseInt();

                return (int) (0.001 * mills);
            }

            case BC_DOUBLE:
                return (int) parseDouble();

            default:
                throw expect("integer", tag);
        }
    }

    /**
     * Reads a long
     *
     * <pre>
     * L b64 b56 b48 b40 b32 b24 b16 b8
     * </pre>
     */
    @Override
    public long readLong() throws IOException {
        int tag = read();

        switch (tag) {
            case BC_NULL:
                return 0;

            case BC_FALSE:
                return 0;

            case BC_TRUE:
                return 1;

                // direct integer
            case 0x80:
            case 0x81:
            case 0x82:
            case 0x83:
            case 0x84:
            case 0x85:
            case 0x86:
            case 0x87:
            case 0x88:
            case 0x89:
            case 0x8a:
            case 0x8b:
            case 0x8c:
            case 0x8d:
            case 0x8e:
            case 0x8f:

            case 0x90:
            case 0x91:
            case 0x92:
            case 0x93:
            case 0x94:
            case 0x95:
            case 0x96:
            case 0x97:
            case 0x98:
            case 0x99:
            case 0x9a:
            case 0x9b:
            case 0x9c:
            case 0x9d:
            case 0x9e:
            case 0x9f:

            case 0xa0:
            case 0xa1:
            case 0xa2:
            case 0xa3:
            case 0xa4:
            case 0xa5:
            case 0xa6:
            case 0xa7:
            case 0xa8:
            case 0xa9:
            case 0xaa:
            case 0xab:
            case 0xac:
            case 0xad:
            case 0xae:
            case 0xaf:

            case 0xb0:
            case 0xb1:
            case 0xb2:
            case 0xb3:
            case 0xb4:
            case 0xb5:
            case 0xb6:
            case 0xb7:
            case 0xb8:
            case 0xb9:
            case 0xba:
            case 0xbb:
            case 0xbc:
            case 0xbd:
            case 0xbe:
            case 0xbf:
                return tag - BC_INT_ZERO;

                /* byte int */
            case 0xc0:
            case 0xc1:
            case 0xc2:
            case 0xc3:
            case 0xc4:
            case 0xc5:
            case 0xc6:
            case 0xc7:
            case 0xc8:
            case 0xc9:
            case 0xca:
            case 0xcb:
            case 0xcc:
            case 0xcd:
            case 0xce:
            case 0xcf:
                return ((tag - BC_INT_BYTE_ZERO) << 8) + read();

                /* short int */
            case 0xd0:
            case 0xd1:
            case 0xd2:
            case 0xd3:
            case 0xd4:
            case 0xd5:
            case 0xd6:
            case 0xd7:
                return ((tag - BC_INT_SHORT_ZERO) << 16) + 256 * read() + read();

                // case LONG_BYTE:
            case BC_DOUBLE_BYTE:
                return (byte) (offset < length ? buffer[offset++] : read());

                // case INT_SHORT:
                // case LONG_SHORT:
            case BC_DOUBLE_SHORT:
                return (short) (256 * read() + read());

            case BC_INT:
            case BC_LONG_INT:
                return parseInt();

                // direct long
            case 0xd8:
            case 0xd9:
            case 0xda:
            case 0xdb:
            case 0xdc:
            case 0xdd:
            case 0xde:
            case 0xdf:

            case 0xe0:
            case 0xe1:
            case 0xe2:
            case 0xe3:
            case 0xe4:
            case 0xe5:
            case 0xe6:
            case 0xe7:
            case 0xe8:
            case 0xe9:
            case 0xea:
            case 0xeb:
            case 0xec:
            case 0xed:
            case 0xee:
            case 0xef:
                return tag - BC_LONG_ZERO;

                /* byte long */
            case 0xf0:
            case 0xf1:
            case 0xf2:
            case 0xf3:
            case 0xf4:
            case 0xf5:
            case 0xf6:
            case 0xf7:
            case 0xf8:
            case 0xf9:
            case 0xfa:
            case 0xfb:
            case 0xfc:
            case 0xfd:
            case 0xfe:
            case 0xff:
                return ((tag - BC_LONG_BYTE_ZERO) << 8) + read();

                /* short long */
            case 0x38:
            case 0x39:
            case 0x3a:
            case 0x3b:
            case 0x3c:
            case 0x3d:
            case 0x3e:
            case 0x3f:
                return ((tag - BC_LONG_SHORT_ZERO) << 16) + 256 * read() + read();

            case 'L':
                return parseLong();

            case BC_DOUBLE_ZERO:
                return 0;

            case BC_DOUBLE_ONE:
                return 1;

            case BC_DOUBLE_MILL: {
                int mills = parseInt();

                return (long) (0.001 * mills);
            }

            case BC_DOUBLE:
                return (long) parseDouble();

            default:
                throw expect("long", tag);
        }
    }

    /**
     * Reads a float
     *
     * <pre>
     * D b64 b56 b48 b40 b32 b24 b16 b8
     * </pre>
     */
    public float readFloat() throws IOException {
        return (float) readDouble();
    }

    /**
     * Reads a double
     *
     * <pre>
     * D b64 b56 b48 b40 b32 b24 b16 b8
     * </pre>
     */
    @Override
    public double readDouble() throws IOException {
        int tag = read();

        switch (tag) {
            case BC_NULL:
                return 0;

            case BC_FALSE:
                return 0;

            case BC_TRUE:
                return 1;

                // direct integer
            case 0x80:
            case 0x81:
            case 0x82:
            case 0x83:
            case 0x84:
            case 0x85:
            case 0x86:
            case 0x87:
            case 0x88:
            case 0x89:
            case 0x8a:
            case 0x8b:
            case 0x8c:
            case 0x8d:
            case 0x8e:
            case 0x8f:

            case 0x90:
            case 0x91:
            case 0x92:
            case 0x93:
            case 0x94:
            case 0x95:
            case 0x96:
            case 0x97:
            case 0x98:
            case 0x99:
            case 0x9a:
            case 0x9b:
            case 0x9c:
            case 0x9d:
            case 0x9e:
            case 0x9f:

            case 0xa0:
            case 0xa1:
            case 0xa2:
            case 0xa3:
            case 0xa4:
            case 0xa5:
            case 0xa6:
            case 0xa7:
            case 0xa8:
            case 0xa9:
            case 0xaa:
            case 0xab:
            case 0xac:
            case 0xad:
            case 0xae:
            case 0xaf:

            case 0xb0:
            case 0xb1:
            case 0xb2:
            case 0xb3:
            case 0xb4:
            case 0xb5:
            case 0xb6:
            case 0xb7:
            case 0xb8:
            case 0xb9:
            case 0xba:
            case 0xbb:
            case 0xbc:
            case 0xbd:
            case 0xbe:
            case 0xbf:
                return tag - 0x90;

                /* byte int */
            case 0xc0:
            case 0xc1:
            case 0xc2:
            case 0xc3:
            case 0xc4:
            case 0xc5:
            case 0xc6:
            case 0xc7:
            case 0xc8:
            case 0xc9:
            case 0xca:
            case 0xcb:
            case 0xcc:
            case 0xcd:
            case 0xce:
            case 0xcf:
                return ((tag - BC_INT_BYTE_ZERO) << 8) + read();

                /* short int */
            case 0xd0:
            case 0xd1:
            case 0xd2:
            case 0xd3:
            case 0xd4:
            case 0xd5:
            case 0xd6:
            case 0xd7:
                return ((tag - BC_INT_SHORT_ZERO) << 16) + 256 * read() + read();

            case BC_INT:
            case BC_LONG_INT:
                return parseInt();

                // direct long
            case 0xd8:
            case 0xd9:
            case 0xda:
            case 0xdb:
            case 0xdc:
            case 0xdd:
            case 0xde:
            case 0xdf:

            case 0xe0:
            case 0xe1:
            case 0xe2:
            case 0xe3:
            case 0xe4:
            case 0xe5:
            case 0xe6:
            case 0xe7:
            case 0xe8:
            case 0xe9:
            case 0xea:
            case 0xeb:
            case 0xec:
            case 0xed:
            case 0xee:
            case 0xef:
                return tag - BC_LONG_ZERO;

                /* byte long */
            case 0xf0:
            case 0xf1:
            case 0xf2:
            case 0xf3:
            case 0xf4:
            case 0xf5:
            case 0xf6:
            case 0xf7:
            case 0xf8:
            case 0xf9:
            case 0xfa:
            case 0xfb:
            case 0xfc:
            case 0xfd:
            case 0xfe:
            case 0xff:
                return ((tag - BC_LONG_BYTE_ZERO) << 8) + read();

                /* short long */
            case 0x38:
            case 0x39:
            case 0x3a:
            case 0x3b:
            case 0x3c:
            case 0x3d:
            case 0x3e:
            case 0x3f:
                return ((tag - BC_LONG_SHORT_ZERO) << 16) + 256 * read() + read();

            case 'L':
                return (double) parseLong();

            case BC_DOUBLE_ZERO:
                return 0;

            case BC_DOUBLE_ONE:
                return 1;

            case BC_DOUBLE_BYTE:
                return (byte) (offset < length ? buffer[offset++] : read());

            case BC_DOUBLE_SHORT:
                return (short) (256 * read() + read());

            case BC_DOUBLE_MILL: {
                int mills = parseInt();

                return 0.001 * mills;
            }

            case BC_DOUBLE:
                return parseDouble();

            default:
                throw expect("double", tag);
        }
    }

    /**
     * Reads a date.
     *
     * <pre>
     * T b64 b56 b48 b40 b32 b24 b16 b8
     * </pre>
     */
    @Override
    public long readUTCDate() throws IOException {
        int tag = read();

        if (tag == BC_DATE) {
            return parseLong();
        } else if (tag == BC_DATE_MINUTE) {
            return parseInt() * 60000L;
        } else {
            throw expect("date", tag);
        }
    }

    /**
     * Reads a byte from the stream.
     */
    public int readChar() throws IOException {
        if (chunkLength > 0) {
            chunkLength--;
            if (chunkLength == 0 && isLastChunk) {
                chunkLength = END_OF_DATA;
            }

            return parseUTF8Char();
        } else if (chunkLength == END_OF_DATA) {
            chunkLength = 0;
            return -1;
        }

        int tag = read();

        switch (tag) {
            case BC_NULL:
                return -1;

            case BC_STRING:
            case BC_STRING_CHUNK:
                isLastChunk = tag == BC_STRING;
                chunkLength = (read() << 8) + read();

                chunkLength--;
                int value = parseUTF8Char();

                // special code so successive read byte won't
                // be read as a single object.
                if (chunkLength == 0 && isLastChunk) {
                    chunkLength = END_OF_DATA;
                }

                return value;

            default:
                throw expect("char", tag);
        }
    }

    /**
     * Reads a byte array from the stream.
     */
    public int readString(char[] buffer, int offset, int length) throws IOException {
        int readLength = 0;

        if (chunkLength == END_OF_DATA) {
            chunkLength = 0;
            return -1;
        } else if (chunkLength == 0) {
            int tag = read();

            switch (tag) {
                case BC_NULL:
                    return -1;

                case BC_STRING:
                case BC_STRING_CHUNK:
                    isLastChunk = tag == BC_STRING;
                    chunkLength = (read() << 8) + read();
                    break;

                case 0x00:
                case 0x01:
                case 0x02:
                case 0x03:
                case 0x04:
                case 0x05:
                case 0x06:
                case 0x07:
                case 0x08:
                case 0x09:
                case 0x0a:
                case 0x0b:
                case 0x0c:
                case 0x0d:
                case 0x0e:
                case 0x0f:

                case 0x10:
                case 0x11:
                case 0x12:
                case 0x13:
                case 0x14:
                case 0x15:
                case 0x16:
                case 0x17:
                case 0x18:
                case 0x19:
                case 0x1a:
                case 0x1b:
                case 0x1c:
                case 0x1d:
                case 0x1e:
                case 0x1f:
                    isLastChunk = true;
                    chunkLength = tag - 0x00;
                    break;

                case 0x30:
                case 0x31:
                case 0x32:
                case 0x33:
                    isLastChunk = true;
                    chunkLength = (tag - 0x30) * 256 + read();
                    break;

                default:
                    throw expect("string", tag);
            }
        }

        while (length > 0) {
            if (chunkLength > 0) {
                buffer[offset++] = (char) parseUTF8Char();
                chunkLength--;
                length--;
                readLength++;
            } else if (isLastChunk) {
                if (readLength == 0) {
                    return -1;
                } else {
                    chunkLength = END_OF_DATA;
                    return readLength;
                }
            } else {
                int tag = read();

                switch (tag) {
                    case BC_STRING:
                    case BC_STRING_CHUNK:
                        isLastChunk = tag == BC_STRING;
                        chunkLength = (read() << 8) + read();
                        break;

                    case 0x00:
                    case 0x01:
                    case 0x02:
                    case 0x03:
                    case 0x04:
                    case 0x05:
                    case 0x06:
                    case 0x07:
                    case 0x08:
                    case 0x09:
                    case 0x0a:
                    case 0x0b:
                    case 0x0c:
                    case 0x0d:
                    case 0x0e:
                    case 0x0f:

                    case 0x10:
                    case 0x11:
                    case 0x12:
                    case 0x13:
                    case 0x14:
                    case 0x15:
                    case 0x16:
                    case 0x17:
                    case 0x18:
                    case 0x19:
                    case 0x1a:
                    case 0x1b:
                    case 0x1c:
                    case 0x1d:
                    case 0x1e:
                    case 0x1f:
                        isLastChunk = true;
                        chunkLength = tag - 0x00;
                        break;

                    case 0x30:
                    case 0x31:
                    case 0x32:
                    case 0x33:
                        isLastChunk = true;
                        chunkLength = (tag - 0x30) * 256 + read();
                        break;

                    default:
                        throw expect("string", tag);
                }
            }
        }

        if (readLength == 0) {
            return -1;
        } else if (chunkLength > 0 || !isLastChunk) {
            return readLength;
        } else {
            chunkLength = END_OF_DATA;
            return readLength;
        }
    }

    /**
     * Reads a string
     *
     * <pre>
     * S b16 b8 string value
     * </pre>
     */
    @Override
    public String readString() throws IOException {
        int tag = read();

        switch (tag) {
            case BC_NULL:
                return null;
            case BC_TRUE:
                return "true";
            case BC_FALSE:
                return "false";

                // direct integer
            case 0x80:
            case 0x81:
            case 0x82:
            case 0x83:
            case 0x84:
            case 0x85:
            case 0x86:
            case 0x87:
            case 0x88:
            case 0x89:
            case 0x8a:
            case 0x8b:
            case 0x8c:
            case 0x8d:
            case 0x8e:
            case 0x8f:

            case 0x90:
            case 0x91:
            case 0x92:
            case 0x93:
            case 0x94:
            case 0x95:
            case 0x96:
            case 0x97:
            case 0x98:
            case 0x99:
            case 0x9a:
            case 0x9b:
            case 0x9c:
            case 0x9d:
            case 0x9e:
            case 0x9f:

            case 0xa0:
            case 0xa1:
            case 0xa2:
            case 0xa3:
            case 0xa4:
            case 0xa5:
            case 0xa6:
            case 0xa7:
            case 0xa8:
            case 0xa9:
            case 0xaa:
            case 0xab:
            case 0xac:
            case 0xad:
            case 0xae:
            case 0xaf:

            case 0xb0:
            case 0xb1:
            case 0xb2:
            case 0xb3:
            case 0xb4:
            case 0xb5:
            case 0xb6:
            case 0xb7:
            case 0xb8:
            case 0xb9:
            case 0xba:
            case 0xbb:
            case 0xbc:
            case 0xbd:
            case 0xbe:
            case 0xbf:
                return String.valueOf((tag - 0x90));

                /* byte int */
            case 0xc0:
            case 0xc1:
            case 0xc2:
            case 0xc3:
            case 0xc4:
            case 0xc5:
            case 0xc6:
            case 0xc7:
            case 0xc8:
            case 0xc9:
            case 0xca:
            case 0xcb:
            case 0xcc:
            case 0xcd:
            case 0xce:
            case 0xcf:
                return String.valueOf(((tag - BC_INT_BYTE_ZERO) << 8) + read());

                /* short int */
            case 0xd0:
            case 0xd1:
            case 0xd2:
            case 0xd3:
            case 0xd4:
            case 0xd5:
            case 0xd6:
            case 0xd7:
                return String.valueOf(((tag - BC_INT_SHORT_ZERO) << 16) + 256 * read() + read());

            case BC_INT:
            case BC_LONG_INT:
                return String.valueOf(parseInt());

                // direct long
            case 0xd8:
            case 0xd9:
            case 0xda:
            case 0xdb:
            case 0xdc:
            case 0xdd:
            case 0xde:
            case 0xdf:

            case 0xe0:
            case 0xe1:
            case 0xe2:
            case 0xe3:
            case 0xe4:
            case 0xe5:
            case 0xe6:
            case 0xe7:
            case 0xe8:
            case 0xe9:
            case 0xea:
            case 0xeb:
            case 0xec:
            case 0xed:
            case 0xee:
            case 0xef:
                return String.valueOf(tag - BC_LONG_ZERO);

                /* byte long */
            case 0xf0:
            case 0xf1:
            case 0xf2:
            case 0xf3:
            case 0xf4:
            case 0xf5:
            case 0xf6:
            case 0xf7:
            case 0xf8:
            case 0xf9:
            case 0xfa:
            case 0xfb:
            case 0xfc:
            case 0xfd:
            case 0xfe:
            case 0xff:
                return String.valueOf(((tag - BC_LONG_BYTE_ZERO) << 8) + read());

                /* short long */
            case 0x38:
            case 0x39:
            case 0x3a:
            case 0x3b:
            case 0x3c:
            case 0x3d:
            case 0x3e:
            case 0x3f:
                return String.valueOf(((tag - BC_LONG_SHORT_ZERO) << 16) + 256 * read() + read());

            case BC_LONG:
                return String.valueOf(parseLong());

            case BC_DOUBLE_ZERO:
                return "0.0";

            case BC_DOUBLE_ONE:
                return "1.0";

            case BC_DOUBLE_BYTE:
                return String.valueOf((byte) (offset < length ? buffer[offset++] : read()));

            case BC_DOUBLE_SHORT:
                return String.valueOf(((short) (256 * read() + read())));

            case BC_DOUBLE_MILL: {
                int mills = parseInt();

                return String.valueOf(0.001 * mills);
            }

            case BC_DOUBLE:
                return String.valueOf(parseDouble());

            case BC_STRING:
            case BC_STRING_CHUNK:
                isLastChunk = tag == BC_STRING;
                chunkLength = (read() << 8) + read();

                sbuf.setLength(0);
                int ch;

                while ((ch = parseChar()) >= 0) {
                    sbuf.append((char) ch);
                }

                return sbuf.toString();

                // 0-byte string
            case 0x00:
            case 0x01:
            case 0x02:
            case 0x03:
            case 0x04:
            case 0x05:
            case 0x06:
            case 0x07:
            case 0x08:
            case 0x09:
            case 0x0a:
            case 0x0b:
            case 0x0c:
            case 0x0d:
            case 0x0e:
            case 0x0f:

            case 0x10:
            case 0x11:
            case 0x12:
            case 0x13:
            case 0x14:
            case 0x15:
            case 0x16:
            case 0x17:
            case 0x18:
            case 0x19:
            case 0x1a:
            case 0x1b:
            case 0x1c:
            case 0x1d:
            case 0x1e:
            case 0x1f:
                isLastChunk = true;
                chunkLength = tag - 0x00;

                sbuf.setLength(0);

                while ((ch = parseChar()) >= 0) {
                    sbuf.append((char) ch);
                }

                return sbuf.toString();

            case 0x30:
            case 0x31:
            case 0x32:
            case 0x33:
                isLastChunk = true;
                chunkLength = (tag - 0x30) * 256 + read();

                sbuf.setLength(0);

                while ((ch = parseChar()) >= 0) {
                    sbuf.append((char) ch);
                }

                return sbuf.toString();

            default:
                throw expect("string", tag);
        }
    }

    /**
     * Reads a byte array
     *
     * <pre>
     * B b16 b8 data value
     * </pre>
     */
    @Override
    public byte[] readBytes() throws IOException {
        int tag = read();

        switch (tag) {
            case BC_NULL:
                return null;

            case BC_BINARY:
            case BC_BINARY_CHUNK:
                isLastChunk = tag == BC_BINARY;
                chunkLength = (read() << 8) + read();

                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                int data;
                while ((data = parseByte()) >= 0) {
                    bos.write(data);
                }

                return bos.toByteArray();

            case 0x20:
            case 0x21:
            case 0x22:
            case 0x23:
            case 0x24:
            case 0x25:
            case 0x26:
            case 0x27:
            case 0x28:
            case 0x29:
            case 0x2a:
            case 0x2b:
            case 0x2c:
            case 0x2d:
            case 0x2e:
            case 0x2f: {
                isLastChunk = true;
                chunkLength = tag - 0x20;

                byte[] buffer = new byte[chunkLength];

                int offset = 0;
                while (offset < chunkLength) {
                    int sublen = read(buffer, 0, chunkLength - offset);

                    if (sublen <= 0) break;

                    offset += sublen;
                }

                return buffer;
            }

            case 0x34:
            case 0x35:
            case 0x36:
            case 0x37: {
                isLastChunk = true;
                chunkLength = (tag - 0x34) * 256 + read();

                byte[] buffer = new byte[chunkLength];

                int offset = 0;
                while (offset < chunkLength) {
                    int sublen = read(buffer, 0, chunkLength - offset);

                    if (sublen <= 0) {
                        break;
                    }

                    offset += sublen;
                }

                return buffer;
            }

            default:
                throw expect("bytes", tag);
        }
    }

    /**
     * Reads a byte from the stream.
     */
    public int readByte() throws IOException {
        if (chunkLength > 0) {
            chunkLength--;
            if (chunkLength == 0 && isLastChunk) {
                chunkLength = END_OF_DATA;
            }

            return read();
        } else if (chunkLength == END_OF_DATA) {
            chunkLength = 0;
            return -1;
        }

        int tag = read();

        switch (tag) {
            case BC_NULL:
                return -1;

            case BC_BINARY:
            case BC_BINARY_CHUNK: {
                isLastChunk = tag == BC_BINARY;
                chunkLength = (read() << 8) + read();

                int value = parseByte();

                // special code so successive read byte won't
                // be read as a single object.
                if (chunkLength == 0 && isLastChunk) {
                    chunkLength = END_OF_DATA;
                }

                return value;
            }

            case 0x20:
            case 0x21:
            case 0x22:
            case 0x23:
            case 0x24:
            case 0x25:
            case 0x26:
            case 0x27:
            case 0x28:
            case 0x29:
            case 0x2a:
            case 0x2b:
            case 0x2c:
            case 0x2d:
            case 0x2e:
            case 0x2f: {
                isLastChunk = true;
                chunkLength = tag - 0x20;

                int value = parseByte();

                // special code so successive read byte won't
                // be read as a single object.
                if (chunkLength == 0) {
                    chunkLength = END_OF_DATA;
                }

                return value;
            }

            case 0x34:
            case 0x35:
            case 0x36:
            case 0x37: {
                isLastChunk = true;
                chunkLength = (tag - 0x34) * 256 + read();

                int value = parseByte();

                // special code so successive read byte won't
                // be read as a single object.
                if (chunkLength == 0) {
                    chunkLength = END_OF_DATA;
                }

                return value;
            }

            default:
                throw expect("binary", tag);
        }
    }

    /**
     * Reads a byte array from the stream.
     */
    public int readBytes(byte[] buffer, int offset, int length) throws IOException {
        int readLength = 0;

        if (chunkLength == END_OF_DATA) {
            chunkLength = 0;
            return -1;
        } else if (chunkLength == 0) {
            int tag = read();

            switch (tag) {
                case BC_NULL:
                    return -1;

                case BC_BINARY:
                case BC_BINARY_CHUNK:
                    isLastChunk = tag == BC_BINARY;
                    chunkLength = (read() << 8) + read();
                    break;

                case 0x20:
                case 0x21:
                case 0x22:
                case 0x23:
                case 0x24:
                case 0x25:
                case 0x26:
                case 0x27:
                case 0x28:
                case 0x29:
                case 0x2a:
                case 0x2b:
                case 0x2c:
                case 0x2d:
                case 0x2e:
                case 0x2f: {
                    isLastChunk = true;
                    chunkLength = tag - 0x20;
                    break;
                }

                case 0x34:
                case 0x35:
                case 0x36:
                case 0x37: {
                    isLastChunk = true;
                    chunkLength = (tag - 0x34) * 256 + read();
                    break;
                }

                default:
                    throw expect("binary", tag);
            }
        }

        while (length > 0) {
            if (chunkLength > 0) {
                buffer[offset++] = (byte) read();
                chunkLength--;
                length--;
                readLength++;
            } else if (isLastChunk) {
                if (readLength == 0) return -1;
                else {
                    chunkLength = END_OF_DATA;
                    return readLength;
                }
            } else {
                int tag = read();

                switch (tag) {
                    case BC_BINARY:
                    case BC_BINARY_CHUNK:
                        isLastChunk = tag == BC_BINARY;
                        chunkLength = (read() << 8) + read();
                        break;

                    default:
                        throw expect("binary", tag);
                }
            }
        }

        if (readLength == 0) {
            return -1;
        } else if (chunkLength > 0 || !isLastChunk) {
            return readLength;
        } else {
            chunkLength = END_OF_DATA;
            return readLength;
        }
    }

    /**
     * Reads an object from the input stream with an expected type.
     */
    @Override
    public Object readObject(Class cl) throws IOException {
        if (cl == null || cl == Object.class) {
            return readObject();
        }

        int tag = offset < length ? (buffer[offset++] & 0xff) : read();

        switch (tag) {
            case BC_NULL:
                return null;

            case BC_MAP_UNTYPED: {
                Deserializer reader = findSerializerFactory().getDeserializer(cl);

                return reader.readMap(this);
            }

            case BC_MAP: {
                String type = readType();

                // hessian/3bb3
                if ("".equals(type)) {
                    Deserializer reader;
                    reader = findSerializerFactory().getDeserializer(cl);

                    return reader.readMap(this);
                } else {
                    Deserializer reader;
                    reader = findSerializerFactory().getObjectDeserializer(type, cl);

                    return reader.readMap(this);
                }
            }

            case BC_OBJECT_DEF: {
                readObjectDefinition(cl);

                return readObject(cl);
            }

            case 0x60:
            case 0x61:
            case 0x62:
            case 0x63:
            case 0x64:
            case 0x65:
            case 0x66:
            case 0x67:
            case 0x68:
            case 0x69:
            case 0x6a:
            case 0x6b:
            case 0x6c:
            case 0x6d:
            case 0x6e:
            case 0x6f: {
                int ref = tag - 0x60;
                int size = classDefs.size();

                if (ref < 0 || size <= ref)
                    throw new HessianProtocolException("'" + ref + "' is an unknown class definition");

                ObjectDefinition def = classDefs.get(ref);

                return readObjectInstance(cl, def);
            }

            case BC_OBJECT: {
                int ref = readInt();
                int size = classDefs.size();

                if (ref < 0 || size <= ref)
                    throw new HessianProtocolException("'" + ref + "' is an unknown class definition");

                ObjectDefinition def = classDefs.get(ref);

                return readObjectInstance(cl, def);
            }

            case BC_LIST_VARIABLE: {
                String type = readType();

                Deserializer reader;
                reader = findSerializerFactory().getListDeserializer(type, cl);

                Object v = reader.readList(this, -1);

                return v;
            }

            case BC_LIST_FIXED: {
                String type = readType();
                int length = readInt();

                Deserializer reader;
                reader = findSerializerFactory().getListDeserializer(type, cl);

                Object v = reader.readLengthList(this, length);

                return v;
            }

            case 0x70:
            case 0x71:
            case 0x72:
            case 0x73:
            case 0x74:
            case 0x75:
            case 0x76:
            case 0x77: {
                int length = tag - 0x70;

                String type = readType();

                Deserializer reader;
                reader = findSerializerFactory().getListDeserializer(type, cl);

                Object v = reader.readLengthList(this, length);

                return v;
            }

            case BC_LIST_VARIABLE_UNTYPED: {
                Deserializer reader;
                reader = findSerializerFactory().getListDeserializer(null, cl);

                Object v = reader.readList(this, -1);

                return v;
            }

            case BC_LIST_FIXED_UNTYPED: {
                int length = readInt();

                Deserializer reader;
                reader = findSerializerFactory().getListDeserializer(null, cl);

                Object v = reader.readLengthList(this, length);

                return v;
            }

            case 0x78:
            case 0x79:
            case 0x7a:
            case 0x7b:
            case 0x7c:
            case 0x7d:
            case 0x7e:
            case 0x7f: {
                int length = tag - 0x78;

                Deserializer reader;
                reader = findSerializerFactory().getListDeserializer(null, cl);

                Object v = reader.readLengthList(this, length);

                return v;
            }

            case BC_REF: {
                int ref = readInt();

                return refs.get(ref);
            }
        }

        if (tag >= 0) {
            offset--;
        }

        // hessian/3b2i vs hessian/3406
        // return readObject();
        return findSerializerFactory().getDeserializer(cl).readObject(this);
    }

    /**
     * Reads an arbitrary object from the input stream when the type
     * is unknown.
     */
    @Override
    public Object readObject() throws IOException {
        int tag = offset < length ? (buffer[offset++] & 0xff) : read();

        switch (tag) {
            case BC_NULL:
                return null;

            case BC_TRUE:
                return Boolean.TRUE;

            case BC_FALSE:
                return Boolean.FALSE;

                // direct integer
            case 0x80:
            case 0x81:
            case 0x82:
            case 0x83:
            case 0x84:
            case 0x85:
            case 0x86:
            case 0x87:
            case 0x88:
            case 0x89:
            case 0x8a:
            case 0x8b:
            case 0x8c:
            case 0x8d:
            case 0x8e:
            case 0x8f:

            case 0x90:
            case 0x91:
            case 0x92:
            case 0x93:
            case 0x94:
            case 0x95:
            case 0x96:
            case 0x97:
            case 0x98:
            case 0x99:
            case 0x9a:
            case 0x9b:
            case 0x9c:
            case 0x9d:
            case 0x9e:
            case 0x9f:

            case 0xa0:
            case 0xa1:
            case 0xa2:
            case 0xa3:
            case 0xa4:
            case 0xa5:
            case 0xa6:
            case 0xa7:
            case 0xa8:
            case 0xa9:
            case 0xaa:
            case 0xab:
            case 0xac:
            case 0xad:
            case 0xae:
            case 0xaf:

            case 0xb0:
            case 0xb1:
            case 0xb2:
            case 0xb3:
            case 0xb4:
            case 0xb5:
            case 0xb6:
            case 0xb7:
            case 0xb8:
            case 0xb9:
            case 0xba:
            case 0xbb:
            case 0xbc:
            case 0xbd:
            case 0xbe:
            case 0xbf:
                return Integer.valueOf(tag - BC_INT_ZERO);

                /* byte int */
            case 0xc0:
            case 0xc1:
            case 0xc2:
            case 0xc3:
            case 0xc4:
            case 0xc5:
            case 0xc6:
            case 0xc7:
            case 0xc8:
            case 0xc9:
            case 0xca:
            case 0xcb:
            case 0xcc:
            case 0xcd:
            case 0xce:
            case 0xcf:
                return Integer.valueOf(((tag - BC_INT_BYTE_ZERO) << 8) + read());

                /* short int */
            case 0xd0:
            case 0xd1:
            case 0xd2:
            case 0xd3:
            case 0xd4:
            case 0xd5:
            case 0xd6:
            case 0xd7:
                return Integer.valueOf(((tag - BC_INT_SHORT_ZERO) << 16) + 256 * read() + read());

            case BC_INT:
                return Integer.valueOf(parseInt());

                // direct long
            case 0xd8:
            case 0xd9:
            case 0xda:
            case 0xdb:
            case 0xdc:
            case 0xdd:
            case 0xde:
            case 0xdf:

            case 0xe0:
            case 0xe1:
            case 0xe2:
            case 0xe3:
            case 0xe4:
            case 0xe5:
            case 0xe6:
            case 0xe7:
            case 0xe8:
            case 0xe9:
            case 0xea:
            case 0xeb:
            case 0xec:
            case 0xed:
            case 0xee:
            case 0xef:
                return Long.valueOf(tag - BC_LONG_ZERO);

                /* byte long */
            case 0xf0:
            case 0xf1:
            case 0xf2:
            case 0xf3:
            case 0xf4:
            case 0xf5:
            case 0xf6:
            case 0xf7:
            case 0xf8:
            case 0xf9:
            case 0xfa:
            case 0xfb:
            case 0xfc:
            case 0xfd:
            case 0xfe:
            case 0xff:
                return Long.valueOf(((tag - BC_LONG_BYTE_ZERO) << 8) + read());

                /* short long */
            case 0x38:
            case 0x39:
            case 0x3a:
            case 0x3b:
            case 0x3c:
            case 0x3d:
            case 0x3e:
            case 0x3f:
                return Long.valueOf(((tag - BC_LONG_SHORT_ZERO) << 16) + 256 * read() + read());

            case BC_LONG_INT:
                return Long.valueOf(parseInt());

            case BC_LONG:
                return Long.valueOf(parseLong());

            case BC_DOUBLE_ZERO:
                return Double.valueOf(0);

            case BC_DOUBLE_ONE:
                return Double.valueOf(1);

            case BC_DOUBLE_BYTE:
                return Double.valueOf((byte) read());

            case BC_DOUBLE_SHORT:
                return Double.valueOf((short) (256 * read() + read()));

            case BC_DOUBLE_MILL: {
                int mills = parseInt();

                return Double.valueOf(0.001 * mills);
            }

            case BC_DOUBLE:
                return Double.valueOf(parseDouble());

            case BC_DATE:
                return new Date(parseLong());

            case BC_DATE_MINUTE:
                return new Date(parseInt() * 60000L);

            case BC_STRING_CHUNK:
            case BC_STRING: {
                isLastChunk = tag == BC_STRING;
                chunkLength = (read() << 8) + read();

                sbuf.setLength(0);

                parseString(sbuf);

                return sbuf.toString();
            }

            case 0x00:
            case 0x01:
            case 0x02:
            case 0x03:
            case 0x04:
            case 0x05:
            case 0x06:
            case 0x07:
            case 0x08:
            case 0x09:
            case 0x0a:
            case 0x0b:
            case 0x0c:
            case 0x0d:
            case 0x0e:
            case 0x0f:

            case 0x10:
            case 0x11:
            case 0x12:
            case 0x13:
            case 0x14:
            case 0x15:
            case 0x16:
            case 0x17:
            case 0x18:
            case 0x19:
            case 0x1a:
            case 0x1b:
            case 0x1c:
            case 0x1d:
            case 0x1e:
            case 0x1f: {
                isLastChunk = true;
                chunkLength = tag - 0x00;

                sbuf.setLength(0);

                parseString(sbuf);

                return sbuf.toString();
            }

            case 0x30:
            case 0x31:
            case 0x32:
            case 0x33: {
                isLastChunk = true;
                chunkLength = (tag - 0x30) * 256 + read();

                sbuf.setLength(0);

                parseString(sbuf);

                return sbuf.toString();
            }

            case BC_BINARY_CHUNK:
            case BC_BINARY: {
                isLastChunk = tag == BC_BINARY;
                chunkLength = (read() << 8) + read();

                int data;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                while ((data = parseByte()) >= 0) {
                    bos.write(data);
                }

                return bos.toByteArray();
            }

            case 0x20:
            case 0x21:
            case 0x22:
            case 0x23:
            case 0x24:
            case 0x25:
            case 0x26:
            case 0x27:
            case 0x28:
            case 0x29:
            case 0x2a:
            case 0x2b:
            case 0x2c:
            case 0x2d:
            case 0x2e:
            case 0x2f: {
                isLastChunk = true;
                int len = tag - 0x20;
                chunkLength = 0;

                byte[] data = new byte[len];

                for (int i = 0; i < len; i++) {
                    data[i] = (byte) read();
                }

                return data;
            }

            case 0x34:
            case 0x35:
            case 0x36:
            case 0x37: {
                isLastChunk = true;
                int len = (tag - 0x34) * 256 + read();
                chunkLength = 0;

                byte[] buffer = new byte[len];

                for (int i = 0; i < len; i++) {
                    buffer[i] = (byte) read();
                }

                return buffer;
            }

            case BC_LIST_VARIABLE: {
                // variable length list
                String type = readType();

                return findSerializerFactory().readList(this, -1, type);
            }

            case BC_LIST_VARIABLE_UNTYPED: {
                return findSerializerFactory().readList(this, -1, null);
            }

            case BC_LIST_FIXED: {
                // fixed length lists
                String type = readType();
                int length = readInt();

                Deserializer reader;
                reader = findSerializerFactory().getListDeserializer(type, null);

                return reader.readLengthList(this, length);
            }

            case BC_LIST_FIXED_UNTYPED: {
                // fixed length lists
                int length = readInt();

                Deserializer reader;
                reader = findSerializerFactory().getListDeserializer(null, null);

                return reader.readLengthList(this, length);
            }

                // compact fixed list
            case 0x70:
            case 0x71:
            case 0x72:
            case 0x73:
            case 0x74:
            case 0x75:
            case 0x76:
            case 0x77: {
                // fixed length lists
                String type = readType();
                int length = tag - 0x70;

                Deserializer reader;
                reader = findSerializerFactory().getListDeserializer(type, null);

                return reader.readLengthList(this, length);
            }

                // compact fixed untyped list
            case 0x78:
            case 0x79:
            case 0x7a:
            case 0x7b:
            case 0x7c:
            case 0x7d:
            case 0x7e:
            case 0x7f: {
                // fixed length lists
                int length = tag - 0x78;

                Deserializer reader;
                reader = findSerializerFactory().getListDeserializer(null, null);

                return reader.readLengthList(this, length);
            }

            case BC_MAP_UNTYPED: {
                return findSerializerFactory().readMap(this, null);
            }

            case BC_MAP: {
                String type = readType();

                return findSerializerFactory().readMap(this, type);
            }

            case BC_OBJECT_DEF: {
                readObjectDefinition(null);

                return readObject();
            }

            case 0x60:
            case 0x61:
            case 0x62:
            case 0x63:
            case 0x64:
            case 0x65:
            case 0x66:
            case 0x67:
            case 0x68:
            case 0x69:
            case 0x6a:
            case 0x6b:
            case 0x6c:
            case 0x6d:
            case 0x6e:
            case 0x6f: {
                int ref = tag - 0x60;

                if (classDefs.size() <= ref)
                    throw error("No classes defined at reference '" + Integer.toHexString(tag) + "'");

                ObjectDefinition def = classDefs.get(ref);

                return readObjectInstance(null, def);
            }

            case BC_OBJECT: {
                int ref = readInt();

                if (classDefs.size() <= ref) {
                    throw error("Illegal object reference #" + ref);
                }

                ObjectDefinition def = classDefs.get(ref);

                return readObjectInstance(null, def);
            }

            case BC_REF: {
                int ref = readInt();

                return refs.get(ref);
            }

            default:
                if (tag < 0) {
                    throw new EOFException("readObject: unexpected end of file");
                } else {
                    throw error("readObject: unknown code " + codeName(tag));
                }
        }
    }

    /**
     * Reads an object definition:
     *
     * <pre>
     * O string <int> (string)* <value>*
     * </pre>
     */
    private void readObjectDefinition(Class<?> cl) throws IOException {
        String type = readString();
        int len = readInt();

        SerializerFactory factory = findSerializerFactory();

        Deserializer reader = factory.getObjectDeserializer(type, null);

        Object[] fields = reader.createFields(len);
        String[] fieldNames = new String[len];

        for (int i = 0; i < len; i++) {
            String name = readString();

            fields[i] = reader.createField(name);
            fieldNames[i] = name;
        }

        ObjectDefinition def = new ObjectDefinition(type, reader, fields, fieldNames);

        classDefs.add(def);
    }

    private Object readObjectInstance(Class<?> cl, ObjectDefinition def) throws IOException {
        String type = def.getType();
        Deserializer reader = def.getReader();
        Object[] fields = def.getFields();

        SerializerFactory factory = findSerializerFactory();

        if (cl != reader.getType() && cl != null) {
            reader = factory.getObjectDeserializer(type, cl);

            return reader.readObject(this, def.getFieldNames());
        } else {
            return reader.readObject(this, fields);
        }
    }

    /**
     * Reads a remote object.
     */
    @Override
    public Object readRemote() throws IOException {
        String type = readType();
        String url = readString();

        return resolveRemote(type, url);
    }

    /**
     * Reads a reference.
     */
    @Override
    public Object readRef() throws IOException {
        int value = parseInt();

        return refs.get(value);
    }

    /**
     * Reads the start of a list.
     */
    @Override
    public int readListStart() throws IOException {
        return read();
    }

    /**
     * Reads the start of a list.
     */
    @Override
    public int readMapStart() throws IOException {
        return read();
    }

    /**
     * Returns true if this is the end of a list or a map.
     */
    @Override
    public boolean isEnd() throws IOException {
        int code;

        if (offset < length) {
            code = (buffer[offset] & 0xff);
        } else {
            code = read();

            if (code >= 0) {
                offset--;
            }
        }

        return (code < 0 || code == 'Z');
    }

    /**
     * Reads the end byte.
     */
    @Override
    public void readEnd() throws IOException {
        int code = offset < length ? (buffer[offset++] & 0xff) : read();

        if (code != BC_END) {
            if (code < 0) {
                throw error("unexpected end of file");
            } else {
                throw error("unknown code:" + codeName(code));
            }
        }
    }

    /**
     * Reads the end byte.
     */
    @Override
    public void readMapEnd() throws IOException {
        int code = offset < length ? (buffer[offset++] & 0xff) : read();

        if (code != BC_END) {
            throw error("expected end of map ('Z') at '" + codeName(code) + "'");
        }
    }

    /**
     * Reads the end byte.
     */
    @Override
    public void readListEnd() throws IOException {
        int code = offset < length ? (buffer[offset++] & 0xff) : read();

        if (code != BC_END) {
            throw error("expected end of list ('Z') at '" + codeName(code) + "'");
        }
    }

    /**
     * Adds a list/map reference.
     */
    @Override
    public int addRef(Object ref) {
        if (refs == null) {
            refs = new ArrayList<>();
        }

        refs.add(ref);

        return refs.size() - 1;
    }

    /**
     * Adds a list/map reference.
     */
    @Override
    public void setRef(int i, Object ref) {
        refs.set(i, ref);
    }

    /**
     * Resets the references for streaming.
     */
    @Override
    public void resetReferences() {
        refs.clear();
    }

    public void reset() {
        resetReferences();

        classDefs.clear();
        types.clear();
    }

    public void resetBuffer() {
        int offset = this.offset;
        this.offset = 0;

        int length = this.length;
        this.length = 0;

        if (length > 0 && offset != length) {
            throw new IllegalStateException("offset=" + offset + " length=" + length);
        }
    }

    public Object readStreamingObject() throws IOException {
        if (refs != null) {
            refs.clear();
        }

        return readObject();
    }

    /**
     * Resolves a remote object.
     */
    public Object resolveRemote(String type, String url) throws IOException {
        HessianRemoteResolver resolver = getRemoteResolver();

        if (resolver != null) {
            return resolver.lookup(type, url);
        } else {
            return new HessianRemote(type, url);
        }
    }

    /**
     * Parses a type from the stream.
     *
     * <pre>
     * type ::= string
     * type ::= int
     * </pre>
     */
    @Override
    public String readType() throws IOException {
        int code = offset < length ? (buffer[offset++] & 0xff) : read();
        offset--;

        switch (code) {
            case 0x00:
            case 0x01:
            case 0x02:
            case 0x03:
            case 0x04:
            case 0x05:
            case 0x06:
            case 0x07:
            case 0x08:
            case 0x09:
            case 0x0a:
            case 0x0b:
            case 0x0c:
            case 0x0d:
            case 0x0e:
            case 0x0f:

            case 0x10:
            case 0x11:
            case 0x12:
            case 0x13:
            case 0x14:
            case 0x15:
            case 0x16:
            case 0x17:
            case 0x18:
            case 0x19:
            case 0x1a:
            case 0x1b:
            case 0x1c:
            case 0x1d:
            case 0x1e:
            case 0x1f:

            case 0x30:
            case 0x31:
            case 0x32:
            case 0x33:
            case BC_STRING_CHUNK:
            case BC_STRING: {
                String type = readString();

                if (types == null) {
                    types = new ArrayList<>();
                }

                types.add(type);

                return type;
            }

            default: {
                int ref = readInt();

                if (types.size() <= ref) {
                    throw new IndexOutOfBoundsException(
                            "type ref #" + ref + " is greater than the number of valid types (" + types.size() + ")");
                }

                return types.get(ref);
            }
        }
    }

    /**
     * Parses the length for an array
     *
     * <pre>
     * l b32 b24 b16 b8
     * </pre>
     */
    @Override
    public int readLength() {
        throw new UnsupportedOperationException();
    }

    /**
     * Parses a 32-bit integer value from the stream.
     *
     * <pre>
     * b32 b24 b16 b8
     * </pre>
     */
    private int parseInt() throws IOException {
        int offset = this.offset;

        if (offset + 3 < length) {
            byte[] buffer = this.buffer;

            int b32 = buffer[offset + 0] & 0xff;
            int b24 = buffer[offset + 1] & 0xff;
            int b16 = buffer[offset + 2] & 0xff;
            int b8 = buffer[offset + 3] & 0xff;

            this.offset = offset + 4;

            return (b32 << 24) + (b24 << 16) + (b16 << 8) + b8;
        } else {
            int b32 = read();
            int b24 = read();
            int b16 = read();
            int b8 = read();

            return (b32 << 24) + (b24 << 16) + (b16 << 8) + b8;
        }
    }

    /**
     * Parses a 64-bit long value from the stream.
     *
     * <pre>
     * b64 b56 b48 b40 b32 b24 b16 b8
     * </pre>
     */
    private long parseLong() throws IOException {
        long b64 = read();
        long b56 = read();
        long b48 = read();
        long b40 = read();
        long b32 = read();
        long b24 = read();
        long b16 = read();
        long b8 = read();

        return ((b64 << 56) + (b56 << 48) + (b48 << 40) + (b40 << 32) + (b32 << 24) + (b24 << 16) + (b16 << 8) + b8);
    }

    /**
     * Parses a 64-bit double value from the stream.
     *
     * <pre>
     * b64 b56 b48 b40 b32 b24 b16 b8
     * </pre>
     */
    private double parseDouble() throws IOException {
        long bits = parseLong();

        return Double.longBitsToDouble(bits);
    }

    org.w3c.dom.Node parseXML() throws IOException {
        throw new UnsupportedOperationException();
    }

    private void parseString(StringBuilder sbuf) throws IOException {
        while (true) {
            if (chunkLength <= 0) {
                if (!parseChunkLength()) {
                    return;
                }
            }

            int length = chunkLength;
            chunkLength = 0;

            while (length-- > 0) {
                sbuf.append((char) parseUTF8Char());
            }
        }
    }

    /**
     * Reads a character from the underlying stream.
     */
    private int parseChar() throws IOException {
        while (chunkLength <= 0) {
            if (!parseChunkLength()) {
                return -1;
            }
        }

        chunkLength--;

        return parseUTF8Char();
    }

    private boolean parseChunkLength() throws IOException {
        if (isLastChunk) {
            return false;
        }

        int code = offset < length ? (buffer[offset++] & 0xff) : read();

        switch (code) {
            case BC_STRING_CHUNK:
                isLastChunk = false;

                chunkLength = (read() << 8) + read();
                break;

            case BC_STRING:
                isLastChunk = true;

                chunkLength = (read() << 8) + read();
                break;

            case 0x00:
            case 0x01:
            case 0x02:
            case 0x03:
            case 0x04:
            case 0x05:
            case 0x06:
            case 0x07:
            case 0x08:
            case 0x09:
            case 0x0a:
            case 0x0b:
            case 0x0c:
            case 0x0d:
            case 0x0e:
            case 0x0f:

            case 0x10:
            case 0x11:
            case 0x12:
            case 0x13:
            case 0x14:
            case 0x15:
            case 0x16:
            case 0x17:
            case 0x18:
            case 0x19:
            case 0x1a:
            case 0x1b:
            case 0x1c:
            case 0x1d:
            case 0x1e:
            case 0x1f:
                isLastChunk = true;
                chunkLength = code - 0x00;
                break;

            case 0x30:
            case 0x31:
            case 0x32:
            case 0x33:
                isLastChunk = true;
                chunkLength = (code - 0x30) * 256 + read();
                break;

            default:
                throw expect("string", code);
        }

        return true;
    }

    /**
     * Parses a single UTF8 character.
     */
    private int parseUTF8Char() throws IOException {
        int ch = offset < length ? (buffer[offset++] & 0xff) : read();

        if (ch < 0x80) {
            return ch;
        } else if ((ch & 0xe0) == 0xc0) {
            int ch1 = read();

            return ((ch & 0x1f) << 6) + (ch1 & 0x3f);
        } else if ((ch & 0xf0) == 0xe0) {
            int ch1 = read();
            int ch2 = read();

            return ((ch & 0x0f) << 12) + ((ch1 & 0x3f) << 6) + (ch2 & 0x3f);
        } else {
            throw error("bad utf-8 encoding at " + codeName(ch));
        }
    }

    /**
     * Reads a byte from the underlying stream.
     */
    private int parseByte() throws IOException {
        while (chunkLength <= 0) {
            if (isLastChunk) {
                return -1;
            }

            int code = read();

            switch (code) {
                case BC_BINARY_CHUNK:
                    isLastChunk = false;

                    chunkLength = (read() << 8) + read();
                    break;

                case BC_BINARY:
                    isLastChunk = true;

                    chunkLength = (read() << 8) + read();
                    break;

                case 0x20:
                case 0x21:
                case 0x22:
                case 0x23:
                case 0x24:
                case 0x25:
                case 0x26:
                case 0x27:
                case 0x28:
                case 0x29:
                case 0x2a:
                case 0x2b:
                case 0x2c:
                case 0x2d:
                case 0x2e:
                case 0x2f:
                    isLastChunk = true;

                    chunkLength = code - 0x20;
                    break;

                case 0x34:
                case 0x35:
                case 0x36:
                case 0x37:
                    isLastChunk = true;
                    chunkLength = (code - 0x34) * 256 + read();
                    break;

                default:
                    throw expect("byte[]", code);
            }
        }

        chunkLength--;

        return read();
    }

    /**
     * Reads bytes based on an input stream.
     */
    @Override
    public InputStream readInputStream() throws IOException {
        int tag = read();

        switch (tag) {
            case BC_NULL:
                return null;

            case BC_BINARY:
            case BC_BINARY_CHUNK:
                isLastChunk = tag == BC_BINARY;
                chunkLength = (read() << 8) + read();
                break;

            case 0x20:
            case 0x21:
            case 0x22:
            case 0x23:
            case 0x24:
            case 0x25:
            case 0x26:
            case 0x27:
            case 0x28:
            case 0x29:
            case 0x2a:
            case 0x2b:
            case 0x2c:
            case 0x2d:
            case 0x2e:
            case 0x2f:
                isLastChunk = true;
                chunkLength = tag - 0x20;
                break;

            case 0x34:
            case 0x35:
            case 0x36:
            case 0x37:
                isLastChunk = true;
                chunkLength = (tag - 0x34) * 256 + read();
                break;

            default:
                throw expect("binary", tag);
        }

        return new ReadInputStream();
    }

    /**
     * Reads bytes from the underlying stream.
     */
    int read(byte[] buffer, int offset, int length) throws IOException {
        int readLength = 0;

        while (length > 0) {
            while (chunkLength <= 0) {
                if (isLastChunk) return readLength == 0 ? -1 : readLength;

                int code = read();

                switch (code) {
                    case BC_BINARY_CHUNK:
                        isLastChunk = false;

                        chunkLength = (read() << 8) + read();
                        break;

                    case BC_BINARY:
                        isLastChunk = true;

                        chunkLength = (read() << 8) + read();
                        break;

                    case 0x20:
                    case 0x21:
                    case 0x22:
                    case 0x23:
                    case 0x24:
                    case 0x25:
                    case 0x26:
                    case 0x27:
                    case 0x28:
                    case 0x29:
                    case 0x2a:
                    case 0x2b:
                    case 0x2c:
                    case 0x2d:
                    case 0x2e:
                    case 0x2f:
                        isLastChunk = true;
                        chunkLength = code - 0x20;
                        break;

                    case 0x34:
                    case 0x35:
                    case 0x36:
                    case 0x37:
                        isLastChunk = true;
                        chunkLength = (code - 0x34) * 256 + read();
                        break;

                    default:
                        throw expect("byte[]", code);
                }
            }

            int sublen = chunkLength;
            if (length < sublen) {
                sublen = length;
            }

            if (this.length <= this.offset && !readBuffer()) {
                return -1;
            }

            if (this.length - this.offset < sublen) {
                sublen = this.length - this.offset;
            }

            System.arraycopy(this.buffer, this.offset, buffer, offset, sublen);

            this.offset += sublen;

            offset += sublen;
            readLength += sublen;
            length -= sublen;
            chunkLength -= sublen;
        }

        return readLength;
    }

    /**
     * Normally, shouldn't be called externally, but needed for QA, e.g.
     * ejb/3b01.
     */
    public final int read() throws IOException {
        if (length <= offset && !readBuffer()) {
            return -1;
        }

        return buffer[offset++] & 0xff;
    }

    protected void unread() {
        if (offset <= 0) {
            throw new IllegalStateException();
        }

        offset--;
    }

    private boolean readBuffer() throws IOException {
        byte[] buffer = this.buffer;
        int offset = this.offset;
        int length = this.length;

        if (offset < length) {
            System.arraycopy(buffer, offset, buffer, 0, length - offset);
            offset = length - offset;
        } else {
            offset = 0;
        }

        int len = is.read(buffer, offset, SIZE - offset);

        if (len <= 0) {
            this.length = offset;
            this.offset = 0;

            return offset > 0;
        }

        this.length = offset + len;
        this.offset = 0;

        return true;
    }

    public Reader getReader() {
        return null;
    }

    protected IOException expect(String expect, int ch) throws IOException {
        if (ch < 0) {
            return error("expected " + expect + " at end of file");
        } else {
            offset--;

            try {
                int offset = this.offset;
                String context = buildDebugContext(buffer, 0, length, offset);

                Object obj = readObject();

                if (obj != null) {
                    return error("expected " + expect
                            + " at 0x" + Integer.toHexString(ch & 0xff)
                            + " " + obj.getClass().getName() + " (" + obj + ")"
                            + "\n  " + context + "");
                } else return error("expected " + expect + " at 0x" + Integer.toHexString(ch & 0xff) + " null");
            } catch (Exception e) {
                log.debug(e.toString(), e);

                return error("expected " + expect + " at 0x" + Integer.toHexString(ch & 0xff));
            }
        }
    }

    private String buildDebugContext(byte[] buffer, int offset, int length, int errorOffset) {
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        for (int i = 0; i < errorOffset; i++) {
            int ch = buffer[offset + i];
            addDebugChar(sb, ch);
        }
        sb.append("] ");
        addDebugChar(sb, buffer[offset + errorOffset]);
        sb.append(" [");
        for (int i = errorOffset + 1; i < length; i++) {
            int ch = buffer[offset + i];
            addDebugChar(sb, ch);
        }
        sb.append("]");

        return sb.toString();
    }

    private void addDebugChar(StringBuilder sb, int ch) {
        if (ch >= 0x20 && ch < 0x7f) {
            sb.append((char) ch);
        } else if (ch == '\n') {
            sb.append((char) ch);
        } else {
            sb.append(String.format("\\x%02x", ch & 0xff));
        }
    }

    protected String codeName(int ch) {
        if (ch < 0) {
            return "end of file";
        } else {
            return "0x" + Integer.toHexString(ch & 0xff) + " (" + (char) +ch + ")";
        }
    }

    protected IOException error(String message) {
        return new HessianProtocolException(message);
    }

    public void free() {
        reset();
    }

    @Override
    public void close() throws IOException {
        InputStream is = this.is;
        this.is = null;

        if (isCloseStreamOnClose && is != null) {
            is.close();
        }
    }

    class ReadInputStream extends InputStream {
        boolean isClosed = false;

        public int read() throws IOException {
            if (isClosed) {
                return -1;
            }

            int ch = parseByte();
            if (ch < 0) {
                isClosed = true;
            }

            return ch;
        }

        public int read(byte[] buffer, int offset, int length) throws IOException {
            if (isClosed) {
                return -1;
            }

            int len = HessianDecoder.this.read(buffer, offset, length);
            if (len < 0) {
                isClosed = true;
            }

            return len;
        }

        @Override
        public void close() throws IOException {
            while (read() >= 0) {}
        }
    }
    ;

    static final class ObjectDefinition {
        private final String type;
        private final Deserializer reader;
        private final Object[] fields;
        private final String[] fieldNames;

        ObjectDefinition(String type, Deserializer reader, Object[] fields, String[] fieldNames) {
            this.type = type;
            this.reader = reader;
            this.fields = fields;
            this.fieldNames = fieldNames;
        }

        String getType() {
            return type;
        }

        Deserializer getReader() {
            return reader;
        }

        Object[] getFields() {
            return fields;
        }

        String[] getFieldNames() {
            return fieldNames;
        }
    }
}
