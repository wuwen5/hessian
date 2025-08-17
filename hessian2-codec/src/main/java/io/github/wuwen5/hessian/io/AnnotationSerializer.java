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

package io.github.wuwen5.hessian.io;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Serializing a Java annotation
 */
public class AnnotationSerializer extends AbstractSerializer {

    private Class<?> annType;
    private Method[] methods;
    private MethodSerializer[] methodSerializers;

    public AnnotationSerializer(Class<?> annType) {
        if (!Annotation.class.isAssignableFrom(annType)) {
            throw new IllegalStateException(
                    annType.getName() + " is invalid because it is not a java.lang.annotation.Annotation");
        }
    }

    @Override
    public void writeObject(Object obj, AbstractHessianEncoder out) throws IOException {
        if (out.addRef(obj)) {
            return;
        }

        init(((Annotation) obj).annotationType());

        int ref = out.writeObjectBegin(annType.getName());

        if (ref == -1) {
            writeDefinition20(out);
            out.writeObjectBegin(annType.getName());
        }

        writeInstance(obj, out);
    }

    private void writeDefinition20(AbstractHessianEncoder out) throws IOException {
        out.writeClassFieldLength(methods.length);

        for (Method method : methods) {
            out.writeString(method.getName());
        }
    }

    @Override
    public void writeInstance(Object obj, AbstractHessianEncoder out) throws IOException {
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];

            methodSerializers[i].serialize(out, obj, method);
        }
    }

    private void init(Class<?> cl) {
        synchronized (this) {
            if (annType != null) {
                return;
            }

            annType = cl;

            this.methods = Arrays.stream(annType.getDeclaredMethods())
                    .filter(m -> m.getParameterTypes().length == 0
                            && !"hashCode".equals(m.getName())
                            && !"toString".equals(m.getName())
                            && !"annotationType".equals(m.getName()))
                    .toArray(Method[]::new);

            methodSerializers = new MethodSerializer[this.methods.length];

            for (int i = 0; i < this.methods.length; i++) {
                methodSerializers[i] = getMethodSerializer(this.methods[i].getReturnType());
            }
        }
    }
}
