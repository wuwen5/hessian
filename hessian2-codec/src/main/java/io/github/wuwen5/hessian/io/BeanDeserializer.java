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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Serializing an object for known object types.
 */
@Slf4j
public class BeanDeserializer extends AbstractMapDeserializer {
    private final Class<?> type;
    private final Map<String, Method> methodMap;
    private final Method readResolve;
    private Constructor<?> constructor;
    private Object[] constructorArgs;

    public BeanDeserializer(Class<?> cl) {
        type = cl;
        methodMap = getMethodMap(cl);

        readResolve = getReadResolve(cl);

        Constructor<?>[] constructors = cl.getConstructors();
        int bestLength = Integer.MAX_VALUE;

        for (Constructor<?> c : constructors) {
            if (c.getParameterTypes().length < bestLength) {
                this.constructor = c;
                bestLength = this.constructor.getParameterTypes().length;
            }
        }

        if (constructor != null) {
            Class<?>[] params = constructor.getParameterTypes();
            constructorArgs = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                constructorArgs[i] = getParamArg(params[i]);
            }
        }
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public Object readMap(AbstractHessianDecoder in) throws IOException {
        try {
            Object obj = instantiate();

            return readMap(in, obj);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOExceptionWrapper(e);
        }
    }

    @Override
    public Object readObject(AbstractHessianDecoder in, Object[] fields) throws IOException {
        try {
            Object obj = instantiate();
            int ref = in.addRef(obj);

            String[] fieldNames = (String[]) fields;
            for (String fieldName : fieldNames) {
                Object value = in.readObject(methodMap.get(fieldName).getParameterTypes()[0]);

                Method method = methodMap.get(fieldName);
                if (method != null) {
                    method.invoke(obj, value);
                } else {
                    in.readObject();
                }
            }

            Object resolve = resolve(obj);

            if (obj != resolve) {
                in.setRef(ref, resolve);
            }

            return resolve;
        } catch (Exception e) {
            throw new IOExceptionWrapper(e);
        }
    }

    public Object readMap(AbstractHessianDecoder in, Object obj) throws IOException {
        try {
            int ref = in.addRef(obj);

            while (!in.isEnd()) {
                Object key = in.readObject();

                Method method = methodMap.get(key);

                if (method != null) {
                    Object value = in.readObject(method.getParameterTypes()[0]);

                    method.invoke(obj, value);
                } else {
                    in.readObject();
                }
            }

            in.readMapEnd();

            Object resolve = resolve(obj);

            if (obj != resolve) {
                in.setRef(ref, resolve);
            }

            return resolve;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOExceptionWrapper(e);
        }
    }

    private Object resolve(Object obj) {
        // if there's a readResolve method, call it
        try {
            if (readResolve != null) {
                return readResolve.invoke(obj);
            }
        } catch (Exception ignored) {
        }

        return obj;
    }

    protected Object instantiate() throws ReflectiveOperationException {
        return constructor.newInstance(constructorArgs);
    }

    /**
     * Returns the readResolve method
     */
    protected Method getReadResolve(Class<?> cl) {
        for (; cl != null; cl = cl.getSuperclass()) {
            Method[] methods = cl.getDeclaredMethods();

            for (Method method : methods) {
                if ("readResolve".equals(method.getName()) && method.getParameterTypes().length == 0) {
                    return method;
                }
            }
        }

        return null;
    }

    /**
     * Checks if the method is a candidate write method.
     * Reference has been made to the implementation of org.springframework.beans.ExtendedBeanInfo.
     * <p>
     * register static and/or non-void returning setter methods. For example:
     * <pre>
     *   public class Bean {
     *
     *       private Foo foo;
     *
     *       public Foo getFoo() {
     *           return this.foo;
     *       }
     *
     *       public Bean setFoo(Foo foo) {
     *           this.foo = foo;
     *           return this;
     *       }
     *   }
     *   </pre>
     *
     * @param method the method to check
     * @return true if the method is a candidate write method
     */
    private static boolean isCandidateWriteMethod(Method method) {
        String methodName = method.getName();
        int nParams = method.getParameterCount();
        return (methodName.length() > 3
                && methodName.startsWith("set")
                && Modifier.isPublic(method.getModifiers())
                && (nParams == 1 || (nParams == 2 && int.class == method.getParameterTypes()[0])));
    }

    /**
     * Creates a map of the classes fields.
     */
    protected Map<String, Method> getMethodMap(Class<?> cl) {
        Map<String, Method> mMap = new HashMap<>();

        for (; cl != null; cl = cl.getSuperclass()) {
            Method[] methods = cl.getDeclaredMethods();

            for (Method method : methods) {

                if (isCandidateWriteMethod(method)) {

                    String name = method.getName();

                    name = name.substring(3);

                    int j = 0;
                    for (; j < name.length() && Character.isUpperCase(name.charAt(j)); j++) {}

                    if (j == 1) {
                        name = name.substring(0, j).toLowerCase(Locale.ENGLISH) + name.substring(j);
                    } else if (j > 1) {
                        name = name.substring(0, j - 1).toLowerCase(Locale.ENGLISH) + name.substring(j - 1);
                    }

                    mMap.put(name, method);
                }
            }
        }

        return mMap;
    }

    /**
     * Creates a map of the classes fields.
     */
    private static Object getParamArg(Class<?> cl) {
        if (!cl.isPrimitive()) {
            return null;
        } else if (boolean.class.equals(cl)) {
            return Boolean.FALSE;
        } else if (byte.class.equals(cl)) {
            return (byte) 0;
        } else if (short.class.equals(cl)) {
            return (short) 0;
        } else if (char.class.equals(cl)) {
            return (char) 0;
        } else if (int.class.equals(cl)) {
            return 0;
        } else if (long.class.equals(cl)) {
            return 0L;
        } else if (float.class.equals(cl)) {
            return (float) 0;
        } else if (double.class.equals(cl)) {
            return (double) 0;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public boolean isReadResolve() {
        return true;
    }
}
