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

package com.caucho.hessian.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Output stream for Hessian 2 requests.
 *
 * <p>Since HessianOutput does not depend on any classes other than
 * in the JDK, it can be extracted independently into a smaller package.
 *
 * <p>HessianOutput is unbuffered, so any client needs to provide
 * its own buffering.
 *
 * <pre>
 * OutputStream os = ...; // from http connection
 * Hessian2Output out = new Hessian2Output(os);
 * String value;
 *
 * out.startCall("hello", 1); // start hello call
 * out.writeString("arg1");   // write a string argument
 * out.completeCall();        // complete the call
 * </pre>
 */
public class HessianRpcOutput extends Hessian2Output implements AbstractHessianOutput {
    public HessianRpcOutput() {
        super();
    }

    /**
     * Creates a new Hessian output stream, initialized with an
     * underlying output stream.
     *
     * @param os the underlying output stream.
     */
    public HessianRpcOutput(OutputStream os) {
        super(os);
    }

    @Override
    public void setSerializerFactory(SerializerFactory factory) {
        super.setSerializerFactory(factory);
    }

    /**
     * Writes a complete method call.
     */
    @Override
    public void call(String method, Object[] args) throws IOException {
        writeVersion();

        int length = args != null ? args.length : 0;

        startCall(method, length);

        for (int i = 0; i < length; i++) {
            writeObject(args[i]);
        }

        completeCall();

        flush();
    }

    /**
     * Writes the call tag.  This would be followed by the
     * method and the arguments
     *<pre>
     * C
     * </pre>
     */
    @Override
    public void startCall() throws IOException {
        flushIfFull();

        buffer[offset++] = (byte) 'C';
    }

    /**
     * Starts the method call.  Clients would use <code>startCall</code>
     * instead of <code>call</code> if they wanted finer control over
     * writing the arguments, or needed to write headers.
     *<pre>
     * C
     * string # method name
     * int    # arg count
     * </pre>
     * @param method the method name to call.
     */
    @Override
    public void startCall(String method, int length) throws IOException {
        int offset = this.offset;

        if (SIZE < offset + 32) {
            flushBuffer();
            offset = this.offset;
        }

        byte[] buffer = this.buffer;

        buffer[this.offset++] = (byte) 'C';

        writeString(method);
        writeInt(length);
    }

    /**
     * Writes the method tag.
     *<pre>
     * string
     * </pre>
     * @param method the method name to call.
     */
    @Override
    public void writeMethod(String method) throws IOException {
        writeString(method);
    }

    /**
     * Completes.
     *<pre>
     * z
     * </pre>
     */
    @Override
    public void completeCall() throws IOException {
        /*
        flushIfFull();

        _buffer[_offset++] = (byte) 'Z';
        */
    }

    /**
     * Starts the reply
     *
     * <p>A successful completion will have a single value:
     *
     * <pre>
     * R
     * </pre>
     */
    @Override
    public void startReply() throws IOException {
        writeVersion();

        flushIfFull();

        buffer[offset++] = (byte) 'R';
    }

    /**
     * Starts an envelope.
     * <pre>
     * E major minor
     * m b16 b8 method-name
     * </pre>
     *
     * @param method the method name to call.
     */
    public void startEnvelope(String method) throws IOException {
        int offset = this.offset;

        if (SIZE < offset + 32) {
            flushBuffer();
            offset = this.offset;
        }

        buffer[this.offset++] = (byte) 'E';

        writeString(method);
    }

    /**
     * Completes an envelope.
     *
     * <p>A successful completion will have a single value:
     *
     * <pre>
     * Z
     * </pre>
     */
    public void completeEnvelope() throws IOException {
        flushIfFull();

        buffer[offset++] = (byte) 'Z';
    }
}
