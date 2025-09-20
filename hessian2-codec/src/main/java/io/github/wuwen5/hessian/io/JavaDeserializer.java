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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * Serializing an object for known object types.
 */
public class JavaDeserializer extends AbstractMapDeserializer {
    private final Class<?> type;
    private final HashMap<?, FieldDeserializer> fieldMap;
    private final Method readResolve;
    private final Constructor<?> constructor;
    private final Object[] constructorArgs;

    public JavaDeserializer(Class<?> cl, FieldDeserializer2Factory fieldFactory) {
        type = cl;
        fieldMap = getFieldMap(cl, fieldFactory);

        readResolve = getReadResolve(cl);

        if (readResolve != null) {
            readResolve.setAccessible(true);
        }

        constructor = getConstructor(cl);
        constructorArgs = getConstructorArgs(constructor);
    }

    protected Constructor<?> getConstructor(Class<?> cl) {
        Constructor<?>[] constructors = cl.getDeclaredConstructors();
        long bestCost = Long.MAX_VALUE;

        Constructor<?> constructorVar = null;

        for (Constructor<?> value : constructors) {
            Class<?>[] param = value.getParameterTypes();
            long cost = getCost(param);

            if (cost < bestCost) {
                constructorVar = value;
                bestCost = cost;
            }
        }

        if (constructorVar != null) {
            constructorVar.setAccessible(true);
        }

        return constructorVar;
    }

    private static long getCost(Class<?>[] param) {
        long cost = 0;

        for (Class<?> aClass : param) {
            cost = 4 * cost;

            if (Object.class.equals(aClass)) {
                cost += 1;
            } else if (String.class.equals(aClass)) {
                cost += 2;
            } else if (int.class.equals(aClass)) {
                cost += 3;
            } else if (long.class.equals(aClass)) {
                cost += 4;
            } else if (aClass.isPrimitive()) {
                cost += 5;
            } else {
                cost += 6;
            }
        }

        if (cost < 0 || cost > (1L << 48)) {
            cost = 1L << 48;
        }

        cost += (long) param.length << 48;
        return cost;
    }

    protected Object[] getConstructorArgs(Constructor<?> constructor) {
        Object[] objects = null;

        if (constructor != null) {
            Class<?>[] params = constructor.getParameterTypes();
            objects = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                objects[i] = FieldDeserializer2Factory.getParamArg(params[i]);
            }
        }

        return objects;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public boolean isReadResolve() {
        return readResolve != null;
    }

    @Override
    public Object readMap(AbstractHessianDecoder in) throws IOException {
        try {
            Object obj = instantiate();

            return readMap(in, obj);
        } catch (IOException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IOExceptionWrapper(type.getName() + ":" + e.getMessage(), e);
        }
    }

    @Override
    public Object[] createFields(int len) {
        return new FieldDeserializer[len];
    }

    @Override
    public Object createField(String name) {
        Object reader = fieldMap.get(name);

        if (reader == null) {
            reader = FieldDeserializer2Factory.NullFieldDeserializer.DESER;
        }

        return reader;
    }

    @Override
    public Object readObject(AbstractHessianDecoder in, Object[] fields) throws IOException {
        try {
            Object obj = instantiate();

            return readObject(in, obj, (FieldDeserializer[]) fields);
        } catch (IOException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IOExceptionWrapper(type.getName() + ":" + e.getMessage(), e);
        }
    }

    @Override
    public Object readObject(AbstractHessianDecoder in, String[] fieldNames) throws IOException {
        try {
            Object obj = instantiate();

            return readObject(in, obj, fieldNames);
        } catch (IOException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IOExceptionWrapper(type.getName() + ":" + e.getMessage(), e);
        }
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

    public Object readMap(AbstractHessianDecoder in, Object obj) throws IOException {
        try {
            int ref = in.addRef(obj);

            while (!in.isEnd()) {
                Object key = in.readObject();

                FieldDeserializer deser = fieldMap.get(key);

                if (deser != null) {
                    deser.deserialize(in, obj);
                } else {
                    in.readObject();
                }
            }

            in.readMapEnd();

            Object resolve = resolve(in, obj);

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

    private Object readObject(AbstractHessianDecoder in, Object obj, FieldDeserializer[] fields) throws IOException {
        try {
            int ref = in.addRef(obj);

            for (FieldDeserializer reader : fields) {
                reader.deserialize(in, obj);
            }

            Object resolve = resolve(in, obj);

            if (obj != resolve) {
                in.setRef(ref, resolve);
            }

            return resolve;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOExceptionWrapper(obj.getClass().getName() + ":" + e, e);
        }
    }

    public Object readObject(AbstractHessianDecoder in, Object obj, String[] fieldNames) throws IOException {
        try {
            int ref = in.addRef(obj);

            for (String fieldName : fieldNames) {
                FieldDeserializer reader = fieldMap.get(fieldName);

                if (reader != null) {
                    reader.deserialize(in, obj);
                } else {
                    in.readObject();
                }
            }

            Object resolve = resolve(in, obj);

            if (obj != resolve) {
                in.setRef(ref, resolve);
            }

            return resolve;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOExceptionWrapper(obj.getClass().getName() + ":" + e, e);
        }
    }

    protected Object resolve(AbstractHessianDecoder in, Object obj) throws Exception {
        // if there's a readResolve method, call it
        try {
            if (readResolve != null) {
                return readResolve.invoke(obj);
            }
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            } else {
                throw e;
            }
        }

        return obj;
    }

    protected Object instantiate() throws HessianProtocolException {
        try {
            if (constructor != null) {
                return constructor.newInstance(constructorArgs);
            } else {
                return type.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            throw new HessianProtocolException("'" + type.getName() + "' could not be instantiated", e);
        }
    }

    /**
     * Creates a map of the classes fields.
     */
    protected HashMap<String, FieldDeserializer> getFieldMap(Class<?> cl, FieldDeserializer2Factory fieldFactory) {
        HashMap<String, FieldDeserializer> fieldMap = new HashMap<>(8);

        for (; cl != null; cl = cl.getSuperclass()) {
            Field[] fields = cl.getDeclaredFields();
            for (Field field : fields) {
                if (!Modifier.isTransient(field.getModifiers())
                        && !Modifier.isStatic(field.getModifiers())
                        && fieldMap.get(field.getName()) == null) {
                    FieldDeserializer deser = fieldFactory.create(field);

                    fieldMap.put(field.getName(), deser);
                }
            }
        }

        return fieldMap;
    }
}
