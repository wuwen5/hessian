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

package io.github.wuwen5.hessian.io;

import io.github.wuwen5.hessian.HessianException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;

/**
 * Serializing an object.
 */
@Slf4j
public abstract class AbstractSerializer implements Serializer {
    public static final NullSerializer NULL = new NullSerializer();

    /**
     * Writes the object.
     * @param obj the object to serialize
     * @param out hessian encoder
     * @throws IOException if an error occurs
     */
    @Override
    public void writeObject(Object obj, AbstractHessianEncoder out) throws IOException {
        if (out.addRef(obj)) {
            return;
        }

        try {
            Object replace = writeReplace(obj);

            if (replace != null) {

                out.writeObject(replace);

                out.replaceRef(replace, obj);

                return;
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new HessianException(e);
        }

        Class<?> cl = getClass(obj);

        int ref = out.writeObjectBegin(cl.getName());

        if (ref < -1) {
            writeObject10(obj, out);
        } else {
            if (ref == -1) {
                writeDefinition20(cl, out);

                out.writeObjectBegin(cl.getName());
            }

            writeInstance(obj, out);
        }
    }

    protected Object writeReplace(Object obj) {
        return null;
    }

    protected Class<?> getClass(Object obj) {
        return obj.getClass();
    }

    protected void writeObject10(Object obj, AbstractHessianEncoder out) throws IOException {
        throw new UnsupportedOperationException(getClass().getName());
    }

    protected void writeDefinition20(Class<?> cl, AbstractHessianEncoder out) throws IOException {
        throw new UnsupportedOperationException(getClass().getName());
    }

    protected void writeInstance(Object obj, AbstractHessianEncoder out) throws IOException {
        throw new UnsupportedOperationException(getClass().getName());
    }

    /**
     * The NullSerializer exists as a marker for the factory classes so
     * they save a null result.
     */
    static final class NullSerializer extends AbstractSerializer {
        @Override
        public void writeObject(Object obj, AbstractHessianEncoder out) throws IOException {
            throw new IllegalStateException(getClass().getName());
        }
    }

    enum MethodSerializer {

        /**
         * Default method serializer
         */
        DEFAULT {
            @Override
            void serialize(AbstractHessianEncoder out, Object obj, Method method) {
                Object value = null;

                try {
                    value = method.invoke(obj);
                } catch (InvocationTargetException e) {
                    throw error(method, e.getCause());
                } catch (IllegalAccessException e) {
                    log.debug(e.toString(), e);
                }

                try {
                    out.writeObject(value);
                } catch (Exception e) {
                    throw error(method, e);
                }
            }
        },
        /**
         * Boolean method serializer
         */
        BOOLEAN {
            @Override
            void serialize(AbstractHessianEncoder out, Object obj, Method method) throws IOException {
                boolean value = false;
                try {
                    value = (Boolean) method.invoke(obj);
                } catch (InvocationTargetException e) {
                    throw error(method, e.getCause());
                } catch (IllegalAccessException e) {
                    log.debug(e.toString(), e);
                }
                out.writeBoolean(value);
            }
        },
        INT {
            @Override
            void serialize(AbstractHessianEncoder out, Object obj, Method method) throws IOException {
                int value = 0;

                try {
                    value = Integer.parseInt(method.invoke(obj).toString());
                } catch (InvocationTargetException e) {
                    throw error(method, e.getCause());
                } catch (IllegalAccessException e) {
                    log.debug(e.toString(), e);
                }

                out.writeInt(value);
            }
        },
        LONG {
            @Override
            void serialize(AbstractHessianEncoder out, Object obj, Method method) throws IOException {
                long value = 0;

                try {
                    value = (Long) method.invoke(obj);
                } catch (InvocationTargetException e) {
                    throw error(method, e.getCause());
                } catch (IllegalAccessException e) {
                    log.debug(e.toString(), e);
                }

                out.writeLong(value);
            }
        },
        DOUBLE {
            @Override
            void serialize(AbstractHessianEncoder out, Object obj, Method method) throws IOException {
                double value = 0;

                try {
                    value = Double.parseDouble(method.invoke(obj).toString());
                } catch (InvocationTargetException e) {
                    throw error(method, e.getCause());
                } catch (IllegalAccessException e) {
                    log.debug(e.toString(), e);
                }

                out.writeDouble(value);
            }
        },
        STRING {
            @Override
            void serialize(AbstractHessianEncoder out, Object obj, Method method) throws IOException {
                String value = null;

                try {
                    value = (String) method.invoke(obj);
                } catch (InvocationTargetException e) {
                    throw error(method, e.getCause());
                } catch (IllegalAccessException e) {
                    log.debug(e.toString(), e);
                }

                out.writeString(value);
            }
        },
        DATE {
            @Override
            void serialize(AbstractHessianEncoder out, Object obj, Method method) throws IOException {
                java.util.Date value = null;

                try {
                    value = (java.util.Date) method.invoke(obj);
                } catch (InvocationTargetException e) {
                    throw error(method, e.getCause());
                } catch (IllegalAccessException e) {
                    log.debug(e.toString(), e);
                }

                if (value == null) {
                    out.writeNull();
                } else {
                    out.writeUTCDate(value.getTime());
                }
            }
        };

        /**
         * serialize
         * @param out hessian encoder
         * @param obj the object to serialize
         * @param method the read method to be called
         * @throws IOException if an error occurs
         */
        abstract void serialize(AbstractHessianEncoder out, Object obj, Method method) throws IOException;

        static RuntimeException error(Method method, Throwable cause) {
            String msg = (method.getDeclaringClass().getSimpleName() + "." + method.getName() + "(): " + cause);

            return new HessianMethodSerializationException(msg, cause);
        }
    }

    static MethodSerializer getMethodSerializer(Class<?> type) {
        if (int.class.equals(type) || byte.class.equals(type) || short.class.equals(type)) {
            return MethodSerializer.INT;
        } else if (long.class.equals(type)) {
            return MethodSerializer.LONG;
        } else if (double.class.equals(type) || float.class.equals(type)) {
            return MethodSerializer.DOUBLE;
        } else if (boolean.class.equals(type)) {
            return MethodSerializer.BOOLEAN;
        } else if (String.class.equals(type)) {
            return MethodSerializer.STRING;
        } else if (java.util.Date.class.equals(type)
                || java.sql.Date.class.equals(type)
                || java.sql.Timestamp.class.equals(type)
                || java.sql.Time.class.equals(type)) {
            return MethodSerializer.DATE;
        } else {
            return MethodSerializer.DEFAULT;
        }
    }
}
