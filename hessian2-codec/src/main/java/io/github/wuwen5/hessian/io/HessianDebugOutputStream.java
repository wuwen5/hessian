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

package io.github.wuwen5.hessian.io;

import io.github.wuwen5.hessian.LineFlushingWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

/**
 * Debugging output stream for Hessian requests.
 */
@Slf4j
public class HessianDebugOutputStream extends OutputStream {

    private OutputStream os;

    private final HessianDebugState state;

    /**
     * Creates an uninitialized Hessian input stream.
     */
    public HessianDebugOutputStream(OutputStream os, PrintWriter dbg) {
        this.os = os;

        state = new HessianDebugState(dbg);
    }

    /**
     * Creates an uninitialized Hessian input stream.
     */
    public HessianDebugOutputStream(OutputStream os, Consumer<String> logger) {
        this(os, new PrintWriter(new LineFlushingWriter(logger)));
    }

    /**
     * Creates an uninitialized Hessian input stream.
     */
    public HessianDebugOutputStream(Consumer<String> logger) {
        this(null, new PrintWriter(new LineFlushingWriter(logger)));
    }

    public void initPacket(OutputStream os) {
        this.os = os;
    }

    public void startTop2() {
        state.startTop2();
    }

    public void startStreaming() {
        state.startStreaming();
    }

    /**
     * Writes a character.
     */
    @Override
    public void write(int ch) throws IOException {
        ch = ch & 0xff;

        os.write(ch);

        try {
            state.next(ch);
        } catch (Exception e) {
            log.warn(e.toString(), e);
        }
    }

    @Override
    public void flush() throws IOException {
        os.flush();
    }

    /**
     * closes the stream.
     */
    @Override
    public void close() throws IOException {
        OutputStream los = this.os;
        this.os = null;

        if (los != null) {
            state.next(-1);
            los.close();
        }

        state.println();
    }
}
