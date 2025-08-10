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

import io.github.wuwen5.hessian.io.HessianServiceException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Input stream for Hessian requests.
 *
 * <p>HessianInput is unbuffered, so any client needs to provide
 * its own buffering.
 *
 * <pre>
 * InputStream is = ...; // from http connection
 * HessianInput in = new HessianInput(is);
 * String value;
 *
 * in.startReply();         // read reply header
 * value = in.readString(); // read string value
 * in.completeReply();      // read reply footer
 * </pre>
 */
@Slf4j
public class Hessian2Input extends io.github.wuwen5.hessian.io.Hessian2Input
        implements com.caucho.hessian.io.AbstractHessianInput {

    private static Field detailMessageField;

    /**
     * -- GETTER --
     * Returns any reply fault.
     */
    @Getter
    private Throwable replyFault;

    /**
     * the method for a call
     * -- GETTER --
     * Returns the calls method
     */
    @Getter
    private String method;

    public Hessian2Input() {
        super();
    }

    /**
     * Creates a new Hessian input stream, initialized with an
     * underlying input stream.
     *
     * @param is the underlying input stream.
     */
    public Hessian2Input(InputStream is) {
        super(is);
    }

    @Override
    public void setRemoteResolver(HessianRemoteResolver resolver) {
        super.setRemoteResolver(resolver);
    }

    @Override
    public void setSerializerFactory(SerializerFactory ser) {
        super.setSerializerFactory(ser);
    }

    /**
     * Starts reading the call
     *
     * <p>A successful completion will have a single value:
     *
     * <pre>
     * string
     * </pre>
     */
    @Override
    public String readMethod() throws IOException {
        method = readString();

        return method;
    }

    /**
     * Returns the number of method arguments
     *
     * <pre>
     * int
     * </pre>
     */
    @Override
    public int readMethodArgLength() throws IOException {
        return readInt();
    }

    /**
     * Reads a reply as an object.
     * If the reply has a fault, throws the exception.
     */
    @Override
    public Object readReply(Class expectedClass) throws Throwable {
        int tag = read();

        if (tag == 'R') {
            return readObject(expectedClass);
        } else if (tag == 'F') {
            HashMap map = (HashMap) readObject(HashMap.class);

            throw prepareFault(map);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append((char) tag);

            try {
                int ch;

                while ((ch = read()) >= 0) {
                    sb.append((char) ch);
                }
            } catch (IOException e) {
                log.debug(e.toString(), e);
            }

            throw error("expected hessian reply at " + codeName(tag) + "\n" + sb);
        }
    }

    /**
     * Starts reading the reply
     *
     * <p>A successful completion will have a single value:
     *
     * <pre>
     * r
     * </pre>
     */
    @Override
    public void startReply() throws Throwable {
        // XXX: for variable length (?)

        readReply(Object.class);
    }

    /**
     * Prepares the fault.
     */
    private Throwable prepareFault(HashMap fault) {
        Object detail = fault.get("detail");
        String message = (String) fault.get("message");

        if (detail instanceof Throwable) {
            replyFault = (Throwable) detail;

            Field detailMessageField = getDetailMessageField();

            if (message != null && detailMessageField != null) {
                try {
                    detailMessageField.set(replyFault, message);
                } catch (Throwable ignored) {
                }
            }

            return replyFault;
        } else {
            String code = (String) fault.get("code");

            replyFault = new HessianServiceException(message, code, detail);

            return replyFault;
        }
    }

    private static Field getDetailMessageField() {
        if (detailMessageField == null) {
            try {
                detailMessageField = Throwable.class.getDeclaredField("detailMessage");
                detailMessageField.setAccessible(true);
            } catch (Throwable ignored) {
            }
        }

        return detailMessageField;
    }
}
