/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        HashMap<String, FieldDeserializer> fieldMap = new HashMap<>();

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
