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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Deserializing a JDK 1.2 Class.
 */
public class ClassDeserializer extends AbstractMapDeserializer {
    private static final Map<String, Class<?>> PRIM_CLASSES = new HashMap<>();

    private ClassLoader loader;

    public ClassDeserializer(ClassLoader loader) {
        this.loader = loader;
    }

    @Override
    public Class<?> getType() {
        return Class.class;
    }

    @Override
    public Object readMap(AbstractHessianDecoder in) throws IOException {
        int ref = in.addRef(null);

        String name = null;

        while (!in.isEnd()) {
            String key = in.readString();

            if ("name".equals(key)) {
                name = in.readString();
            } else {
                in.readObject();
            }
        }

        in.readMapEnd();

        Object value = create(name);

        in.setRef(ref, value);

        return value;
    }

    @Override
    public Object readObject(AbstractHessianDecoder in, Object[] fields) throws IOException {
        String[] fieldNames = (String[]) fields;

        int ref = in.addRef(null);

        String name = null;

        for (String fieldName : fieldNames) {
            if ("name".equals(fieldName)) {
                name = in.readString();
            } else {
                in.readObject();
            }
        }

        Object value = create(name);

        in.setRef(ref, value);

        return value;
    }

    Object create(String name) throws IOException {
        if (name == null) {
            throw new IOException("Serialized Class expects name.");
        }

        Class<?> cl = PRIM_CLASSES.get(name);

        if (cl != null) {
            return cl;
        }

        try {
            if (loader != null) {
                return Class.forName(name, false, loader);
            } else {
                return Class.forName(name);
            }
        } catch (Exception e) {
            throw new IOExceptionWrapper(e);
        }
    }

    static {
        PRIM_CLASSES.put("void", void.class);
        PRIM_CLASSES.put("boolean", boolean.class);
        PRIM_CLASSES.put("java.lang.Boolean", Boolean.class);
        PRIM_CLASSES.put("byte", byte.class);
        PRIM_CLASSES.put("java.lang.Byte", Byte.class);
        PRIM_CLASSES.put("char", char.class);
        PRIM_CLASSES.put("java.lang.Character", Character.class);
        PRIM_CLASSES.put("short", short.class);
        PRIM_CLASSES.put("java.lang.Short", Short.class);
        PRIM_CLASSES.put("int", int.class);
        PRIM_CLASSES.put("java.lang.Integer", Integer.class);
        PRIM_CLASSES.put("long", long.class);
        PRIM_CLASSES.put("java.lang.Long", Long.class);
        PRIM_CLASSES.put("float", float.class);
        PRIM_CLASSES.put("java.lang.Float", Float.class);
        PRIM_CLASSES.put("double", double.class);
        PRIM_CLASSES.put("java.lang.Double", Double.class);
        PRIM_CLASSES.put("java.lang.String", String.class);
    }
}
