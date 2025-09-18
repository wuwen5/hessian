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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import lombok.Setter;

/**
 * Abstract output stream for Hessian requests.
 *
 * <pre>
 * OutputStream os = ...; // from http connection
 * AbstractOutput out = new HessianSerializerOutput(os);
 * String value;
 * </pre>
 */
public abstract class AbstractHessianEncoder implements Closeable {
    /**
     * serializer factory
     */
    private Hessian2SerializerFactory defaultSerializerFactory;

    /**
     * serializer factory
     * -- SETTER --
     *  Sets the serializer factory.
     *
     */
    @Setter
    protected Hessian2SerializerFactory serializerFactory;

    private byte[] byteBuffer;

    /**
     * Gets the serializer factory.
     */
    public Hessian2SerializerFactory getSerializerFactory() {
        // the default serializer factory cannot be modified by external
        // callers
        if (serializerFactory == defaultSerializerFactory) {
            serializerFactory = new Hessian2SerializerFactory();
        }

        return serializerFactory;
    }

    /**
     * Gets the serializer factory.
     */
    protected final Hessian2SerializerFactory findSerializerFactory() {
        Hessian2SerializerFactory factory = serializerFactory;

        if (factory == null) {
            factory = Hessian2SerializerFactory.createDefault();
            defaultSerializerFactory = factory;
            serializerFactory = factory;
        }

        return factory;
    }

    /**
     * Initialize the output with a new underlying stream.
     */
    public void init(OutputStream os) {}

    public boolean setUnshared(boolean isUnshared) {
        throw new UnsupportedOperationException(getClass().getSimpleName());
    }

    /**
     * Writes a boolean value to the stream.  The boolean will be written
     * with the following syntax:
     *<pre>
     * T
     * F
     * </pre>
     *
     * @throws IOException if an I/O error occurs.
     * @param value the boolean value to write.
     */
    public abstract void writeBoolean(boolean value) throws IOException;

    /**
     * Writes an integer value to the stream.  The integer will be written
     * with the following syntax:
     *<pre>
     * I b32 b24 b16 b8
     * </pre>
     *
     * @throws IOException if an I/O error occurs.
     * @param value the integer value to write.
     */
    public abstract void writeInt(int value) throws IOException;

    /**
     * Writes a long value to the stream.  The long will be written
     * with the following syntax:
     *<pre>
     * L b64 b56 b48 b40 b32 b24 b16 b8
     * </pre>
     *
     * @throws IOException if an I/O error occurs.
     * @param value the long value to write.
     */
    public abstract void writeLong(long value) throws IOException;

    /**
     * Writes a double value to the stream.  The double will be written
     * with the following syntax:
     *<pre>
     * D b64 b56 b48 b40 b32 b24 b16 b8
     * </pre>
     *
     * @throws IOException if an I/O error occurs.
     * @param value the double value to write.
     */
    public abstract void writeDouble(double value) throws IOException;

    /**
     * Writes a date to the stream.
     *<pre>
     * T  b64 b56 b48 b40 b32 b24 b16 b8
     * </pre>
     *
     * @throws IOException if an I/O error occurs.
     * @param time the date in milliseconds from the epoch in UTC
     */
    public abstract void writeUTCDate(long time) throws IOException;

    /**
     * Writes a null value to the stream.
     * The null will be written with the following syntax
     *<pre>
     * N
     * </pre>
     * @throws IOException if an I/O error occurs.
     */
    public abstract void writeNull() throws IOException;

    /**
     * Writes a string value to the stream using UTF-8 encoding.
     * The string will be written with the following syntax:
     * <p>
     * <pre>
     * S b16 b8 string-value
     * </pre>
     * <p>
     * If the value is null, it will be written as
     * <p>
     * <pre>
     * N
     * </pre>
     *
     * @throws IOException if an I/O error occurs
     * @param value the string value to write.
     */
    public abstract void writeString(String value) throws IOException;

    /**
     * Writes a string value to the stream using UTF-8 encoding.
     * The string will be written with the following syntax:
     *<pre>
     * S b16 b8 string-value
     * </pre>
     * <p>
     * If the value is null, it will be written as
     *<pre>
     * N
     * </pre>
     *
     * @throws IOException if an I/O error occurs
     * @param buffer the string value to write.
     * @param offset the offset of the string value to write.
     * @param length the length of the string value to write.
     */
    public abstract void writeString(char[] buffer, int offset, int length) throws IOException;

    /**
     * Writes a byte array to the stream.
     * The array will be written with the following syntax:
     *<pre>
     * B b16 b18 bytes
     * </pre>
     * <p>
     * If the value is null, it will be written as
     *<pre>
     * N
     * </pre>
     *
     * @throws IOException if an I/O error occurs
     * @param buffer the string value to write.
     */
    public abstract void writeBytes(byte[] buffer) throws IOException;

    /**
     * Writes a byte array to the stream.
     * The array will be written with the following syntax:
     *<pre>
     * B b16 b18 bytes
     * </pre>
     * <p>
     * If the value is null, it will be written as
     *<pre>
     * N
     * </pre>
     *
     * @throws IOException if an I/O error occurs
     * @param buffer the byte array to write.
     * @param offset the offset of the first byte to write.
     * @param length the number of bytes to write.
     */
    public abstract void writeBytes(byte[] buffer, int offset, int length) throws IOException;

    /**
     * Writes a byte buffer to the stream.
     * @throws IOException if an I/O error occurs
     */
    public abstract void writeByteBufferStart() throws IOException;

    /**
     * Writes a byte buffer to the stream.
     *<pre>
     * b b16 b18 bytes
     * </pre>
     *
     * @throws IOException if an I/O error occurs
     * @param buffer the byte buffer to write.
     * @param offset the offset of the buffer to write.
     * @param length the length of the buffer to write.
     */
    public abstract void writeByteBufferPart(byte[] buffer, int offset, int length) throws IOException;

    /**
     * Writes the last chunk of a byte buffer to the stream.
     *<pre>
     * b b16 b18 bytes
     * </pre>
     *
     * @throws IOException if an I/O error occurs
     * @param buffer the buffer to write.
     * @param offset the offset of the buffer to write.
     * @param length the length of the buffer to write.
     */
    public abstract void writeByteBufferEnd(byte[] buffer, int offset, int length) throws IOException;

    /**
     * Writes a full output stream.
     */
    public void writeByteStream(InputStream is) throws IOException {
        writeByteBufferStart();

        if (byteBuffer == null) {
            byteBuffer = new byte[1024];
        }

        byte[] buffer = byteBuffer;

        int len;
        while ((len = is.read(buffer, 0, buffer.length)) > 0) {
            if (len < buffer.length) {
                int len2 = is.read(buffer, len, buffer.length - len);

                if (len2 < 0) {
                    writeByteBufferEnd(buffer, 0, len);
                    return;
                }

                len += len2;
            }

            writeByteBufferPart(buffer, 0, len);
        }

        writeByteBufferEnd(buffer, 0, 0);
    }

    /**
     * Writes a reference.
     * <p><pre>
     * Q int
     * </pre>
     *
     * @throws IOException if an I/O error occurs
     * @param value the integer value to write.
     */
    protected abstract void writeRef(int value) throws IOException;

    /**
     * Replaces a reference from one object to another.
     * @throws IOException if an I/O error occurs
     * @param newRef the new reference object
     * @param oldRef the old reference object
     * @return true if the reference was replaced
     */
    public abstract boolean replaceRef(Object oldRef, Object newRef) throws IOException;

    /**
     * Adds an object to the reference list.  If the object already exists,
     * writes the reference, otherwise, the caller is responsible for
     * the serialization.
     * <p><pre>
     * R b32 b24 b16 b8
     * </pre>
     *
     * @throws IOException if an I/O error occurs
     * @param object the object to add as a reference.
     * @return true if the object has already been written.
     */
    public abstract boolean addRef(Object object) throws IOException;

    /**
     * Gets the reference for an object.
     * @param obj the object to get the reference for.
     * @return the reference for the object.
     */
    public abstract int getRef(Object obj);

    /**
     * Resets the references for streaming.
     */
    public void resetReferences() {}

    /**
     * Writes a generic object to the output stream.
     * @throws IOException if an I/O error occurs
     * @param object the object to write.
     */
    public abstract void writeObject(Object object) throws IOException;

    /**
     * Writes the list header to the stream.  List writers will call
     * <code>writeListBegin</code> followed by the list contents and then
     * call <code>writeListEnd</code>.
     * <p>
     *     <pre>
     * V
     *   x13 java.util.ArrayList   # type
     *   x93                       # length=3
     *   x91                       # 1
     *   x92                       # 2
     *   x93                       # 3
     * &lt;/list&gt;
     * </pre>
     * @throws IOException if an I/O error occurs
     * @param length the length of the list
     * @param type the type of the list
     * @return true for variable lists, false for fixed lists
     */
    public abstract boolean writeListBegin(int length, String type) throws IOException;

    /**
     * Writes the tail of the list to the stream.
     * @throws IOException if an I/O error occurs
     */
    public abstract void writeListEnd() throws IOException;

    /**
     * Writes the map header to the stream.  Map writers will call
     * <code>writeMapBegin</code> followed by the map contents and then
     * call <code>writeMapEnd</code>.
     * <p>
     * <pre>
     * M type (key value)* Z
     * </pre>
     * @throws IOException if an I/O error occurs
     * @param type the type of the map
     */
    public abstract void writeMapBegin(String type) throws IOException;

    /**
     * Writes the tail of the map to the stream.
     * @throws IOException if an I/O error occurs
     */
    public abstract void writeMapEnd() throws IOException;

    /**
     * Writes the object header to the stream (for Hessian 2.0), or a
     * Map for Hessian 1.0.  Object writers will call
     * <code>writeObjectBegin</code> followed by the map contents and then
     * call <code>writeObjectEnd</code>.
     * <p>
     * <pre>
     * C type int key*
     * C int value*
     * </pre>
     *
     * @return int if the object has already been defined.
     */
    public int writeObjectBegin(String type) throws IOException {
        writeMapBegin(type);

        return -2;
    }

    /**
     * Writes the end of the class.
     */
    public void writeClassFieldLength(int len) throws IOException {}

    /**
     * Writes the tail of the object to the stream.
     */
    public void writeObjectEnd() throws IOException {}

    public void flush() throws IOException {}
}
