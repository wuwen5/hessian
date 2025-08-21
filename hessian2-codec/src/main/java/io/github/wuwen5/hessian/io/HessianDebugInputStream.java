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

import io.github.wuwen5.hessian.LineFlushingWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.function.Consumer;

/**
 * Debugging input stream for Hessian requests.
 */
public class HessianDebugInputStream extends InputStream {
    private InputStream is;

    private final HessianDebugState state;

    /**
     * Creates an uninitialized Hessian input stream.
     */
    public HessianDebugInputStream(InputStream is, OutputStream os) {
        this(is, new PrintWriter(os));
    }

    /**
     * Creates an uninitialized Hessian input stream.
     */
    public HessianDebugInputStream(InputStream is, PrintWriter dbg) {
        this.is = is;

        if (dbg == null) {
            dbg = new PrintWriter(System.out);
        }

        state = new HessianDebugState(dbg);
    }

    /**
     * Creates an uninitialized Hessian input stream.
     */
    public HessianDebugInputStream(InputStream is, Consumer<String> logger) {
        this(is, new PrintWriter(new LineFlushingWriter(logger)));
    }

    /**
     * Creates an uninitialized Hessian input stream.
     */
    public HessianDebugInputStream(Consumer<String> logger) {
        this(null, logger);
    }

    public void initPacket(InputStream is) {
        this.is = is;
    }

    public void startTop2() {
        state.startTop2();
    }

    public void startStreaming() {
        state.startStreaming();
    }

    public void setDepth(int depth) {
        state.setDepth(depth);
    }

    /**
     * Reads a character.
     */
    @Override
    public int read() throws IOException {
        int ch;

        if (this.is == null) {
            return -1;
        } else {
            ch = this.is.read();
        }

        state.next(ch);

        return ch;
    }

    /**
     * closes the stream.
     */
    @Override
    public void close() throws IOException {
        InputStream lis = this.is;
        this.is = null;

        if (lis != null) {
            lis.close();
        }

        state.println();
    }
}
