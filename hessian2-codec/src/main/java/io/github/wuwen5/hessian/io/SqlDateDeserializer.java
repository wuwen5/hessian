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

import io.github.wuwen5.hessian.HessianException;
import java.io.IOException;
import java.lang.reflect.Constructor;

/**
 * Deserializing a string valued object
 */
public class SqlDateDeserializer extends BaseDeserializer {
    private final Class<?> cl;
    private final Constructor<?> constructor;

    public SqlDateDeserializer(Class<?> cl) {
        try {
            this.cl = cl;
            constructor = cl.getConstructor(long.class);
        } catch (NoSuchMethodException e) {
            throw new HessianException(e);
        }
    }

    @Override
    public Class<?> getType() {
        return cl;
    }

    @Override
    public Object readMap(AbstractHessianDecoder in) throws IOException {
        int ref = in.addRef(null);

        long initValue = Long.MIN_VALUE;

        while (!in.isEnd()) {
            String key = in.readString();

            if ("value".equals(key)) {
                initValue = in.readUTCDate();
            } else {
                in.readString();
            }
        }

        in.readMapEnd();

        Object value = create(initValue);

        in.setRef(ref, value);

        return value;
    }

    @Override
    public Object readObject(AbstractHessianDecoder in, Object[] fields) throws IOException {
        String[] fieldNames = (String[]) fields;

        int ref = in.addRef(null);

        long initValue = Long.MIN_VALUE;

        for (String key : fieldNames) {
            if ("value".equals(key)) {
                initValue = in.readUTCDate();
            } else {
                in.readObject();
            }
        }

        Object value = create(initValue);

        in.setRef(ref, value);

        return value;
    }

    private Object create(long initValue) throws IOException {
        if (initValue == Long.MIN_VALUE) throw new IOException(cl.getName() + " expects name.");

        try {
            return constructor.newInstance(new Long(initValue));
        } catch (Exception e) {
            throw new IOExceptionWrapper(e);
        }
    }
}
