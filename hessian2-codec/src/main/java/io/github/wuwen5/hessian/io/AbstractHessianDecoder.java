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
import java.io.Reader;

/**
 * Abstract base class for Hessian requests.  Hessian users should only
 * need to use the methods in this class.
 */
public abstract class AbstractHessianDecoder implements Closeable {
    private Hessian2RemoteResolver resolver;
    private byte[] buffer;

    /**
     * Initialize the Hessian stream with the underlying input stream.
     */
    public void init(InputStream is) {}

    /**
     * Sets the resolver used to lookup remote objects.
     */
    public void setRemoteResolver(Hessian2RemoteResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Sets the resolver used to lookup remote objects.
     */
    public Hessian2RemoteResolver getRemoteResolver() {
        return resolver;
    }

    /**
     * Reads a boolean
     *
     * <pre>
     * T
     * F
     * </pre>
     * @throws IOException if an I/O error occurs
     * @return the boolean value
     */
    public abstract boolean readBoolean() throws IOException;

    /**
     * Reads a null
     *
     * <pre>
     * N
     * </pre>
     * @throws IOException if an I/O error occurs
     */
    public abstract void readNull() throws IOException;

    /**
     * Reads an integer
     *
     * <pre>
     * I b32 b24 b16 b8
     * </pre>
     * @throws IOException if an I/O error occurs
     * @return the integer value
     */
    public abstract int readInt() throws IOException;

    /**
     * Reads a long
     *
     * <pre>
     * L b64 b56 b48 b40 b32 b24 b16 b8
     * </pre>
     * @throws IOException if an I/O error occurs
     * @return the long value
     */
    public abstract long readLong() throws IOException;

    /**
     * Reads a double.
     *
     * <pre>
     * D b64 b56 b48 b40 b32 b24 b16 b8
     * </pre>
     * @throws IOException if an I/O error occurs
     * @return the double value
     */
    public abstract double readDouble() throws IOException;

    /**
     * Reads a date.
     *
     * <pre>
     * T b64 b56 b48 b40 b32 b24 b16 b8
     * </pre>
     * @throws IOException if an I/O error occurs
     * @return the date value in milliseconds since the epoch
     */
    public abstract long readUTCDate() throws IOException;

    /**
     * Reads a string encoded in UTF-8
     *
     * <pre>
     * s b16 b8 non-final string chunk
     * S b16 b8 final string chunk
     * </pre>
     * @throws IOException if an I/O error occurs
     * @return the string value
     */
    public abstract String readString() throws IOException;

    /**
     * Starts reading a string.  All the characters must be read before
     * calling the next method.  The actual characters will be read with
     * the reader's read() or read(char [], int, int).
     *
     * <pre>
     * s b16 b8 non-final string chunk
     * S b16 b8 final string chunk
     * </pre>
     * @throws IOException if an I/O error occurs
     * @return a Reader for reading the string
     */
    public abstract Reader getReader() throws IOException;

    /**
     * Starts reading a byte array using an input stream.  All the bytes
     * must be read before calling the following method.
     *
     * <pre>
     * b b16 b8 non-final binary chunk
     * B b16 b8 final binary chunk
     * </pre>
     * @throws IOException if an I/O error occurs
     * @return an InputStream for reading the bytes
     */
    public abstract InputStream readInputStream() throws IOException;

    /**
     * Reads data to an output stream.
     *
     * <pre>
     * b b16 b8 non-final binary chunk
     * B b16 b8 final binary chunk
     * </pre>
     */
    public boolean readToOutputStream(OutputStream os) throws IOException {

        try (InputStream is = readInputStream()) {

            if (is == null) {
                return false;
            }

            if (buffer == null) {
                buffer = new byte[256];
            }

            int len;

            while ((len = is.read(buffer, 0, buffer.length)) > 0) {
                os.write(buffer, 0, len);
            }

            return true;
        }
    }

    /**
     * Reads a byte array.
     *
     * <pre>
     * b b16 b8 non-final binary chunk
     * B b16 b8 final binary chunk
     * </pre>
     * @throws IOException if an I/O error occurs
     * @return the byte array
     */
    public abstract byte[] readBytes() throws IOException;

    /**
     * Reads an arbitrary object from the input stream.
     *
     * @throws IOException if an I/O error occurs
     * @param expectedClass the expected class if the protocol doesn't supply it.
     * @return the object read from the stream
     */
    public abstract Object readObject(Class<?> expectedClass) throws IOException;

    /**
     * Reads an arbitrary object from the input stream.
     * @throws IOException if an I/O error occurs
     * @return the object read from the stream
     */
    public abstract Object readObject() throws IOException;

    /**
     * Reads a remote object reference to the stream.  The type is the
     * type of the remote interface.
     *<pre>
     * <code>
     * 'r' 't' b16 b8 type url
     * </code>
     * </pre>
     * @throws IOException if an I/O error occurs
     * @return the remote object read from the stream
     */
    public abstract Object readRemote() throws IOException;

    /**
     * Reads a reference
     *
     * <pre>
     * R b32 b24 b16 b8
     * </pre>
     * @throws IOException if an I/O error occurs
     * @return the object reference read from the stream
     */
    public abstract Object readRef() throws IOException;

    /**
     * Adds an object reference.
     * @throws IOException if an I/O error occurs
     * @param obj the object to add as a reference
     * @return the reference index
     */
    public abstract int addRef(Object obj) throws IOException;

    /**
     * Sets an object reference.
     * @throws IOException if an I/O error occurs
     * @param i the reference index
     * @param obj the object to set as a reference
     */
    public abstract void setRef(int i, Object obj) throws IOException;

    /**
     * Resets the references for streaming.
     */
    public void resetReferences() {}

    /**
     * Reads the start of a list
     * @throws IOException if an I/O error occurs
     * @return the length of the list, or -1 if the list is unbounded
     */
    public abstract int readListStart() throws IOException;

    /**
     * Reads the length of a list.
     * @throws IOException if an I/O error occurs
     * @return the length of a list.
     */
    public abstract int readLength() throws IOException;

    /**
     * Reads the start of a map
     * @throws IOException if an I/O error occurs
     * @return the start of a map
     */
    public abstract int readMapStart() throws IOException;

    /**
     * Reads an object type.
     * @throws IOException if an I/O error occurs
     * @return an object type.
     */
    public abstract String readType() throws IOException;

    /**
     * Returns true if the data has ended.
     * @throws IOException if an I/O error occurs
     * @return true if the data has ended.
     */
    public abstract boolean isEnd() throws IOException;

    /**
     * Read the end byte
     * @throws IOException if an I/O error occurs
     */
    public abstract void readEnd() throws IOException;

    /**
     * Read the end byte
     * @throws IOException if an I/O error occurs
     */
    public abstract void readMapEnd() throws IOException;

    /**
     * Read the end byte
     * @throws IOException if an I/O error occurs
     */
    public abstract void readListEnd() throws IOException;
}
