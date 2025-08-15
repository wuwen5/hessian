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

package com.caucho.hessian.io;

import io.github.wuwen5.hessian.io.HessianProtocolException;
import java.io.Closeable;
import java.io.IOException;

/**
 * Abstract base class for Hessian requests.  Hessian users should only
 * need to use the methods in this class.
 *
 * <pre>
 * AbstractHessianInput in = ...; // get input
 * String value;
 *
 * in.startReply();         // read reply header
 * value = in.readString(); // read string value
 * in.completeReply();      // read reply footer
 * </pre>
 */
public interface AbstractHessianInput extends Closeable {

    /**
     * Starts reading the call
     *
     * <pre>
     * c major minor
     * </pre>
     */
    default int readCall() throws IOException {
        int tag = read();

        if (tag != 'C') {
            String message = "expected hessian call ('C') at "
                    + (tag < 0 ? "end of file" : "0x" + Integer.toHexString(tag & 0xff) + " (" + (char) +tag + ")");
            if (getMethod() != null) {
                throw new HessianProtocolException(getMethod() + ": " + message);
            } else {
                throw new HessianProtocolException(message);
            }
        }

        return 0;
    }

    /**
     * Normally, shouldn't be called externally, but needed for QA, e.g.
     * ejb/3b01.
     */
    int read() throws IOException;

    /**
     * For backward compatibility with HessianSkeleton
     */
    default void skipOptionalCall() {}

    /**
     * Reads a header, returning null if there are no headers.
     *
     * <pre>
     * H b16 b8 value
     * </pre>
     */
    default String readHeader() throws IOException {
        return null;
    }

    /**
     * Starts reading the call
     *
     * <p>A successful completion will have a single value:
     *
     * <pre>
     * m b16 b8 method
     * </pre>
     */
    String readMethod() throws IOException;

    /**
     * Reads the number of method arguments
     *
     * @return -1 for a variable length (hessian 1.0)
     */
    default int readMethodArgLength() throws IOException {
        return -1;
    }

    /**
     * Completes reading the call
     *
     * <p>The call expects the following protocol data
     *
     * <pre>
     * Z
     * </pre>
     */
    default void completeCall() {}

    /**
     * Reads a reply as an object.
     * If the reply has a fault, throws the exception.
     */
    Object readReply(Class<?> expectedClass) throws Throwable;

    /**
     * Starts reading the reply
     *
     * <p>A successful completion will have a single value:
     *
     * <pre>
     * r
     * v
     * </pre>
     */
    void startReply() throws Throwable;

    /**
     * Starts reading the body of the reply, i.e. after the 'r' has been
     * parsed.
     */
    default void startReplyBody() {}

    /**
     * Completes reading the call
     *
     * <p>A successful completion will have a single value:
     *
     * <pre>
     * z
     * </pre>
     */
    default void completeReply() throws IOException {}

    /**
     * Reads a boolean
     *
     * <pre>
     * T
     * F
     * </pre>
     */
    boolean readBoolean() throws IOException;

    /**
     * Reads a null
     *
     * <pre>
     * N
     * </pre>
     */
    void readNull() throws IOException;

    /**
     * Reads an integer
     *
     * <pre>
     * I b32 b24 b16 b8
     * </pre>
     */
    int readInt() throws IOException;

    /**
     * Reads a long
     *
     * <pre>
     * L b64 b56 b48 b40 b32 b24 b16 b8
     * </pre>
     */
    long readLong() throws IOException;

    /**
     * Reads a double.
     *
     * <pre>
     * D b64 b56 b48 b40 b32 b24 b16 b8
     * </pre>
     */
    double readDouble() throws IOException;

    /**
     * Reads a date.
     *
     * <pre>
     * T b64 b56 b48 b40 b32 b24 b16 b8
     * </pre>
     */
    long readUTCDate() throws IOException;

    /**
     * Reads a string encoded in UTF-8
     *
     * <pre>
     * s b16 b8 non-final string chunk
     * S b16 b8 final string chunk
     * </pre>
     */
    String readString() throws IOException;

    byte[] readBytes() throws IOException;

    /**
     * Reads an arbitrary object from the input stream.
     *
     * @param expectedClass the expected class if the protocol doesn't supply it.
     */
    Object readObject(Class expectedClass) throws IOException;

    /**
     * Reads an arbitrary object from the input stream.
     */
    Object readObject() throws IOException;

    /**
     * Sets the resolver used to lookup remote objects.
     */
    void setRemoteResolver(HessianRemoteResolver resolver);

    /**
     * Sets the serializer factory.
     */
    void setSerializerFactory(SerializerFactory ser);

    /**
     * Returns true if the data has ended.
     */
    boolean isEnd() throws IOException;

    /**
     * Read the end byte
     */
    void readMapEnd() throws IOException;

    /**
     * Returns the call's method
     */
    String getMethod();
}
