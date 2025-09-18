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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import sun.misc.Unsafe;

/**
 * Serializing an object for known object types.
 */
@Slf4j
public class UnsafeDeserializer extends AbstractMapDeserializer {

    private static boolean isEnabled;

    @SuppressWarnings("restriction")
    private static Unsafe unsafe;

    private final Class<?> type;
    private final Map<String, FieldDeserializer> fieldMap;
    private final Method readResolve;

    public UnsafeDeserializer(Class<?> cl, FieldDeserializer2Factory fieldFactory) {
        type = cl;
        fieldMap = getFieldMap(cl, fieldFactory);

        readResolve = getReadResolve(cl);

        if (readResolve != null) {
            readResolve.setAccessible(true);
        }
    }

    public static boolean isEnabled() {
        return isEnabled;
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
            reader = FieldDeserializer2FactoryUnsafe.NullFieldDeserializer.DESER;
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

            if (obj != resolve) in.setRef(ref, resolve);

            return resolve;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOExceptionWrapper(e);
        }
    }

    public Object readObject(AbstractHessianDecoder in, Object obj, FieldDeserializer[] fields) throws IOException {
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

    @SuppressWarnings("restriction")
    protected Object instantiate() throws InstantiationException {
        return unsafe.allocateInstance(type);
    }

    /**
     * Creates a map of the classes fields.
     */
    protected Map<String, FieldDeserializer> getFieldMap(Class<?> cl, FieldDeserializer2Factory fieldFactory) {
        Map<String, FieldDeserializer> map = new HashMap<>();

        for (; cl != null; cl = cl.getSuperclass()) {
            Field[] fields = cl.getDeclaredFields();
            for (Field field : fields) {
                if (!Modifier.isTransient(field.getModifiers())
                        && !Modifier.isStatic(field.getModifiers())
                        && map.get(field.getName()) == null) {
                    FieldDeserializer deser = fieldFactory.create(field);

                    map.put(field.getName(), deser);
                }
            }
        }

        return map;
    }

    static void logDeserializeError(Field field, Object obj, Object value, Throwable e) throws IOException {
        String fieldName = (field.getDeclaringClass().getName() + "." + field.getName());

        if (e instanceof HessianFieldException) {
            throw (HessianFieldException) e;
        } else if (e instanceof IOException) {
            throw new HessianFieldException(fieldName + ": " + e.getMessage(), e);
        }

        if (value != null) {
            throw new HessianFieldException(
                    fieldName + ": " + value.getClass().getName() + " (" + value + ")" + " cannot be assigned to '"
                            + field.getType().getName() + "'",
                    e);
        } else {
            throw new HessianFieldException(
                    fieldName + ": " + field.getType().getName() + " cannot be assigned from null", e);
        }
    }

    static {
        boolean isEnabled = false;

        try {
            Class<?> unsafe = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = null;
            for (Field field : unsafe.getDeclaredFields()) {
                if ("theUnsafe".equals(field.getName())) {
                    theUnsafe = field;
                }
            }

            if (theUnsafe != null) {
                theUnsafe.setAccessible(true);
                UnsafeDeserializer.unsafe = (Unsafe) theUnsafe.get(null);
            }

            isEnabled = UnsafeDeserializer.unsafe != null;

            String unsafeProp = System.getProperty("com.caucho.hessian.unsafe");

            if ("false".equals(unsafeProp)) {
                isEnabled = false;
            }
        } catch (Throwable e) {
            log.trace(e.toString(), e);
        }

        UnsafeDeserializer.isEnabled = isEnabled;
    }
}
