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

package com.caucho.hessian.io;

import java.io.Closeable;
import java.io.IOException;


/**
 * Abstract output stream for Hessian requests.
 *
 * <pre>
 * OutputStream os = ...; // from http connection
 * AbstractOutput out = new HessianSerializerOutput(os);
 * String value;
 *
 * out.startCall("hello");  // start hello call
 * out.writeString("arg1"); // write a string argument
 * out.completeCall();      // complete the call
 * </pre>
 */
public interface AbstractHessianOutput extends Closeable {
    /**
     * Starts the method call:
     *
     * <code><pre>
     * C
     * </pre></code>
     */
    void startCall() throws IOException;

    /**
     * Starts the method call:
     *
     * <code><pre>
     * C string int
     * </pre></code>
     *
     * @param method the method name to call.
     */
    void startCall(String method, int length) throws IOException;

    /**
     * Writes the method tag.
     *
     * <code><pre>
     * string
     * </pre></code>
     *
     * @param method the method name to call.
     */
    void writeMethod(String method) throws IOException;

    /**
     * Completes the method call:
     *
     * <code><pre>
     * </pre></code>
     */
    void completeCall() throws IOException;

    /**
     * Writes a boolean value to the stream.  The boolean will be written
     * with the following syntax:
     *
     * <code><pre>
     * T
     * F
     * </pre></code>
     *
     * @param value the boolean value to write.
     */
    void writeBoolean(boolean value) throws IOException;

    /**
     * Writes an integer value to the stream.  The integer will be written
     * with the following syntax:
     *
     * <code><pre>
     * I b32 b24 b16 b8
     * </pre></code>
     *
     * @param value the integer value to write.
     */
    void writeInt(int value) throws IOException;

    /**
     * Writes a long value to the stream.  The long will be written
     * with the following syntax:
     *
     * <code><pre>
     * L b64 b56 b48 b40 b32 b24 b16 b8
     * </pre></code>
     *
     * @param value the long value to write.
     */
    void writeLong(long value) throws IOException;

    /**
     * Writes a double value to the stream.  The double will be written
     * with the following syntax:
     *
     * <code><pre>
     * D b64 b56 b48 b40 b32 b24 b16 b8
     * </pre></code>
     *
     * @param value the double value to write.
     */
    void writeDouble(double value) throws IOException;

    /**
     * Writes a date to the stream.
     *
     * <code><pre>
     * T  b64 b56 b48 b40 b32 b24 b16 b8
     * </pre></code>
     *
     * @param time the date in milliseconds from the epoch in UTC
     */
    void writeUTCDate(long time) throws IOException;

    /**
     * Writes a null value to the stream.
     * The null will be written with the following syntax
     *
     * <code><pre>
     * N
     * </pre></code>
     *
     * @param value the string value to write.
     */
    void writeNull() throws IOException;

    /**
     * Writes a string value to the stream using UTF-8 encoding.
     * The string will be written with the following syntax:
     *
     * <code><pre>
     * S b16 b8 string-value
     * </pre></code>
     * <p>
     * If the value is null, it will be written as
     *
     * <code><pre>
     * N
     * </pre></code>
     *
     * @param value the string value to write.
     */
    void writeString(String value) throws IOException;

    /**
     * Writes a string value to the stream using UTF-8 encoding.
     * The string will be written with the following syntax:
     *
     * <code><pre>
     * S b16 b8 string-value
     * </pre></code>
     * <p>
     * If the value is null, it will be written as
     *
     * <code><pre>
     * N
     * </pre></code>
     *
     * @param value the string value to write.
     */
    void writeString(char[] buffer, int offset, int length) throws IOException;

    /**
     * Writes a byte array to the stream.
     * The array will be written with the following syntax:
     *
     * <code><pre>
     * B b16 b18 bytes
     * </pre></code>
     * <p>
     * If the value is null, it will be written as
     *
     * <code><pre>
     * N
     * </pre></code>
     *
     * @param value the string value to write.
     */
    void writeBytes(byte[] buffer) throws IOException;

    /**
     * Writes a byte array to the stream.
     * The array will be written with the following syntax:
     *
     * <code><pre>
     * B b16 b18 bytes
     * </pre></code>
     * <p>
     * If the value is null, it will be written as
     *
     * <code><pre>
     * N
     * </pre></code>
     *
     * @param value the string value to write.
     */
    void writeBytes(byte[] buffer, int offset, int length) throws IOException;

    /**
     * Writes a byte buffer to the stream.
     */
    void writeByteBufferStart() throws IOException;

    /**
     * Writes a byte buffer to the stream.
     *
     * <code><pre>
     * b b16 b18 bytes
     * </pre></code>
     *
     * @param value the string value to write.
     */
    void writeByteBufferPart(byte[] buffer,
                             int offset,
                             int length) throws IOException;

    /**
     * Writes the last chunk of a byte buffer to the stream.
     *
     * <code><pre>
     * b b16 b18 bytes
     * </pre></code>
     */
    void writeByteBufferEnd(byte[] buffer,
                            int offset,
                            int length) throws IOException;

    /**
     * Writes a generic object to the output stream.
     */
    void writeObject(Object object) throws IOException;

    /**
     * Sets the serializer factory.
     */
    void setSerializerFactory(SerializerFactory factory);

    /**
     * Writes a complete method call.
     */
    void call(String method, Object[] args) throws IOException;

    void flush() throws IOException;

    void writeReply(Object o) throws IOException;

    /**
     * Writes a fault.  The fault will be written
     * as a descriptive string followed by an object:
     *
     * <code><pre>
     * f
     * &lt;string>code
     * &lt;string>the fault code
     *
     * &lt;string>message
     * &lt;string>the fault mesage
     *
     * &lt;string>detail
     * mt\x00\xnnjavax.ejb.FinderException
     *     ...
     * z
     * z
     * </pre></code>
     *
     * @param code the fault code, a three digit
     */
    void writeFault(String code, String message, Object detail) throws IOException;
}
