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

import io.github.wuwen5.hessian.HessianException;
import io.github.wuwen5.hessian.io.net.InetAddressDeserializer;
import io.github.wuwen5.hessian.io.net.InetAddressSerializer;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The classloader-specific Factory for returning serialization
 */
public class ContextSerializerFactory {
    private static final Logger log = Logger.getLogger(ContextSerializerFactory.class.getName());

    private static final WeakHashMap<ClassLoader, SoftReference<ContextSerializerFactory>> CONTEXT_REF_MAP =
            new WeakHashMap<>();

    private static final ClassLoader SYSTEM_CLASS_LOADER;

    private static Map<String, HessianSerializer> staticSerializerMap;
    private static Map<String, HessianDeserializer> staticDeserializerMap;
    private static Map<String, HessianDeserializer> staticClassNameMap;

    private ContextSerializerFactory parent;
    private final WeakReference<ClassLoader> loaderRef;

    private final Set<String> serializerFiles = new HashSet<>();
    private final Set<String> deserializerFiles = new HashSet<>();

    private final Map<String, HessianSerializer> serializerClassMap = new HashMap<>();

    private final ConcurrentMap<String, HessianSerializer> customSerializerMap = new ConcurrentHashMap<>();

    private final Map<Class<?>, HessianSerializer> serializerInterfaceMap = new HashMap<>();

    private final Map<String, HessianDeserializer> deserializerClassMap = new HashMap<>();

    private final Map<String, HessianDeserializer> deserializerClassNameMap = new HashMap<>();

    private final ConcurrentMap<String, HessianDeserializer> customDeserializerMap = new ConcurrentHashMap<>();

    private final Map<String, HessianDeserializer> deserializerInterfaceMap = new HashMap<>();

    public ContextSerializerFactory(ContextSerializerFactory parent, ClassLoader loader) {
        if (loader == null) {
            loader = SYSTEM_CLASS_LOADER;
        }

        loaderRef = new WeakReference<>(loader);

        init();
    }

    public static ContextSerializerFactory create() {
        return create(Thread.currentThread().getContextClassLoader());
    }

    public static ContextSerializerFactory create(ClassLoader loader) {
        synchronized (CONTEXT_REF_MAP) {
            SoftReference<ContextSerializerFactory> factoryRef = CONTEXT_REF_MAP.get(loader);

            ContextSerializerFactory factory = null;

            if (factoryRef != null) {
                factory = factoryRef.get();
            }

            if (factory == null) {
                ContextSerializerFactory parent = null;

                if (loader != null) {
                    parent = create(loader.getParent());
                }

                factory = new ContextSerializerFactory(parent, loader);
                factoryRef = new SoftReference<>(factory);

                CONTEXT_REF_MAP.put(loader, factoryRef);
            }

            return factory;
        }
    }

    public ClassLoader getClassLoader() {

        if (this.loaderRef != null) {
            return this.loaderRef.get();
        } else {
            return null;
        }
    }

    /**
     * Returns the serializer for a given class.
     */
    public HessianSerializer getSerializer(String className) {
        HessianSerializer serializer = serializerClassMap.get(className);

        if (serializer == AbstractSerializer.NULL) {
            return null;
        } else {
            return serializer;
        }
    }

    /**
     * Returns a custom serializer the class
     *
     * @param cl the class of the object that needs to be serialized.
     *
     * @return a serializer object for the serialization.
     */
    public HessianSerializer getCustomSerializer(Class<?> cl) {
        HessianSerializer serializer = customSerializerMap.get(cl.getName());

        if (serializer == AbstractSerializer.NULL) {
            return null;
        } else if (serializer != null) {
            return serializer;
        }

        try {
            Class<?> serClass = Class.forName(cl.getName() + "HessianSerializer", false, cl.getClassLoader());

            HessianSerializer ser =
                    (HessianSerializer) serClass.getDeclaredConstructor().newInstance();

            customSerializerMap.put(cl.getName(), ser);

            return ser;
        } catch (ClassNotFoundException e) {
            log.log(Level.ALL, e.toString(), e);
        } catch (Exception e) {
            throw new HessianException(e);
        }

        customSerializerMap.put(cl.getName(), AbstractSerializer.NULL);

        return null;
    }

    /**
     * Returns the deserializer for a given class.
     */
    public HessianDeserializer getDeserializer(String className) {
        HessianDeserializer deserializer = deserializerClassMap.get(className);

        if (deserializer != null && deserializer != BaseDeserializer.NULL) {
            return deserializer;
        }

        deserializer = deserializerInterfaceMap.get(className);

        if (deserializer != null && deserializer != BaseDeserializer.NULL) {
            return deserializer;
        }

        return null;
    }

    /**
     * Returns a custom deserializer the class
     *
     * @param cl the class of the object that needs to be deserialized.
     *
     * @return a deserializer object for the deserialization.
     */
    public HessianDeserializer getCustomDeserializer(Class<?> cl) {
        HessianDeserializer deserializer = customDeserializerMap.get(cl.getName());

        if (deserializer == BaseDeserializer.NULL) {
            return null;
        } else if (deserializer != null) {
            return deserializer;
        }

        try {
            Class<?> serClass = Class.forName(cl.getName() + "HessianDeserializer", false, cl.getClassLoader());

            HessianDeserializer ser =
                    (HessianDeserializer) serClass.getDeclaredConstructor().newInstance();

            customDeserializerMap.put(cl.getName(), ser);

            return ser;
        } catch (ClassNotFoundException e) {
            log.log(Level.ALL, e.toString(), e);
        } catch (Exception e) {
            throw new HessianException(e);
        }

        customDeserializerMap.put(cl.getName(), BaseDeserializer.NULL);

        return null;
    }

    /**
     * Initialize the factory
     */
    private void init() {
        if (parent != null) {
            serializerFiles.addAll(parent.serializerFiles);
            deserializerFiles.addAll(parent.deserializerFiles);

            serializerClassMap.putAll(parent.serializerClassMap);
            deserializerClassMap.putAll(parent.deserializerClassMap);
        }

        if (parent == null) {
            serializerClassMap.putAll(staticSerializerMap);
            deserializerClassMap.putAll(staticDeserializerMap);
            deserializerClassNameMap.putAll(staticClassNameMap);
        }

        Map<Class<?>, Class<?>> classMap = new HashMap<>();
        initSerializerFiles("META-INF/hessian/serializers", serializerFiles, classMap, HessianSerializer.class);

        for (Map.Entry<Class<?>, Class<?>> entry : classMap.entrySet()) {
            try {
                HessianSerializer ser = (HessianSerializer)
                        entry.getValue().getDeclaredConstructor().newInstance();

                if (entry.getKey().isInterface()) {
                    serializerInterfaceMap.put(entry.getKey(), ser);
                } else {
                    serializerClassMap.put(entry.getKey().getName(), ser);
                }
            } catch (Exception e) {
                throw new HessianException(e);
            }
        }

        classMap = new HashMap<>();
        initSerializerFiles("META-INF/hessian/deserializers", deserializerFiles, classMap, HessianDeserializer.class);

        for (Map.Entry<Class<?>, Class<?>> entry : classMap.entrySet()) {
            try {
                HessianDeserializer ser = (HessianDeserializer)
                        entry.getValue().getDeclaredConstructor().newInstance();

                if (entry.getKey().isInterface()) {
                    deserializerInterfaceMap.put(entry.getKey().getName(), ser);
                } else {
                    deserializerClassMap.put(entry.getKey().getName(), ser);
                }
            } catch (Exception e) {
                throw new HessianException(e);
            }
        }
    }

    private void initSerializerFiles(
            String fileName, Set<String> fileList, Map<Class<?>, Class<?>> classMap, Class<?> type) {
        try {
            ClassLoader classLoader = getClassLoader();

            // on systems with the security manager enabled, the system classloader
            // is null
            if (classLoader == null) {
                return;
            }

            Enumeration<?> iter;

            iter = classLoader.getResources(fileName);
            while (iter.hasMoreElements()) {
                URL url = (URL) iter.nextElement();

                if (fileList.contains(url.toString())) {
                    continue;
                }

                fileList.add(url.toString());

                try (InputStream is = url.openStream()) {

                    Properties props = new Properties();
                    props.load(is);

                    for (Map.Entry<Object, Object> entry : props.entrySet()) {
                        String apiName = (String) entry.getKey();
                        String serializerName = (String) entry.getValue();

                        Class<?> apiClass;
                        Class<?> serializerClass;

                        try {
                            apiClass = Class.forName(apiName, false, classLoader);
                        } catch (ClassNotFoundException e) {
                            log.fine(url + ": " + apiName + " is not available in this context: " + getClassLoader());
                            continue;
                        }

                        try {
                            serializerClass = Class.forName(serializerName, false, classLoader);
                        } catch (ClassNotFoundException e) {
                            log.fine(url + ": " + serializerName + " is not available in this context: "
                                    + getClassLoader());
                            continue;
                        }

                        if (!type.isAssignableFrom(serializerClass)) {
                            throw new HessianException(url + ": " + serializerClass.getName()
                                    + " is invalid because it does not implement " + type.getName());
                        }

                        classMap.put(apiClass, serializerClass);
                    }
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new HessianException(e);
        }
    }

    private static void addBasic(Class<?> cl, String typeName, int type) {
        staticSerializerMap.put(cl.getName(), new BasicSerializer(type));

        HessianDeserializer deserializer = new BasicDeserializer(type);
        staticDeserializerMap.put(cl.getName(), deserializer);
        staticClassNameMap.put(typeName, deserializer);
    }

    static {
        staticSerializerMap = new HashMap<>();
        staticDeserializerMap = new HashMap<>();
        staticClassNameMap = new HashMap<>();

        FieldDeserializer2Factory fieldFactory = FieldDeserializer2Factory.create();

        addBasic(void.class, "void", BasicSerializer.NULL);

        addBasic(Boolean.class, "boolean", BasicSerializer.BOOLEAN);
        addBasic(Byte.class, "byte", BasicSerializer.BYTE);
        addBasic(Short.class, "short", BasicSerializer.SHORT);
        addBasic(Integer.class, "int", BasicSerializer.INTEGER);
        addBasic(Long.class, "long", BasicSerializer.LONG);
        addBasic(Float.class, "float", BasicSerializer.FLOAT);
        addBasic(Double.class, "double", BasicSerializer.DOUBLE);
        addBasic(Character.class, "char", BasicSerializer.CHARACTER_OBJECT);
        addBasic(String.class, "string", BasicSerializer.STRING);
        addBasic(Object.class, "object", BasicSerializer.OBJECT);
        addBasic(java.util.Date.class, "date", BasicSerializer.DATE);

        addBasic(boolean.class, "boolean", BasicSerializer.BOOLEAN);
        addBasic(byte.class, "byte", BasicSerializer.BYTE);
        addBasic(short.class, "short", BasicSerializer.SHORT);
        addBasic(int.class, "int", BasicSerializer.INTEGER);
        addBasic(long.class, "long", BasicSerializer.LONG);
        addBasic(float.class, "float", BasicSerializer.FLOAT);
        addBasic(double.class, "double", BasicSerializer.DOUBLE);
        addBasic(char.class, "char", BasicSerializer.CHARACTER);

        addBasic(boolean[].class, "[boolean", BasicSerializer.BOOLEAN_ARRAY);
        addBasic(byte[].class, "[byte", BasicSerializer.BYTE_ARRAY);
        staticSerializerMap.put(byte[].class.getName(), ByteArraySerializer.SER);
        addBasic(short[].class, "[short", BasicSerializer.SHORT_ARRAY);
        addBasic(int[].class, "[int", BasicSerializer.INTEGER_ARRAY);
        addBasic(long[].class, "[long", BasicSerializer.LONG_ARRAY);
        addBasic(float[].class, "[float", BasicSerializer.FLOAT_ARRAY);
        addBasic(double[].class, "[double", BasicSerializer.DOUBLE_ARRAY);
        addBasic(char[].class, "[char", BasicSerializer.CHARACTER_ARRAY);
        addBasic(String[].class, "[string", BasicSerializer.STRING_ARRAY);
        addBasic(Object[].class, "[object", BasicSerializer.OBJECT_ARRAY);

        HessianDeserializer objectDeserializer = new JavaDeserializer(Object.class, fieldFactory);
        staticDeserializerMap.put("object", objectDeserializer);
        staticClassNameMap.put("object", objectDeserializer);

        staticSerializerMap.put(Class.class.getName(), new ClassSerializer());

        staticDeserializerMap.put(Number.class.getName(), new BasicDeserializer(BasicSerializer.NUMBER));

        staticSerializerMap.put(Inet4Address.class.getName(), InetAddressSerializer.create());
        staticSerializerMap.put(Inet6Address.class.getName(), InetAddressSerializer.create());

        SqlDateSerializer sqlDateSerializer = new SqlDateSerializer();
        staticSerializerMap.put(java.sql.Date.class.getName(), sqlDateSerializer);
        staticSerializerMap.put(java.sql.Time.class.getName(), sqlDateSerializer);
        staticSerializerMap.put(java.sql.Timestamp.class.getName(), sqlDateSerializer);

        staticDeserializerMap.put(java.sql.Date.class.getName(), new SqlDateDeserializer(java.sql.Date.class));
        staticDeserializerMap.put(java.sql.Time.class.getName(), new SqlDateDeserializer(java.sql.Time.class));
        staticDeserializerMap.put(
                java.sql.Timestamp.class.getName(), new SqlDateDeserializer(java.sql.Timestamp.class));

        // hessian/3bb5
        staticDeserializerMap.put(StackTraceElement.class.getName(), new StackTraceElementDeserializer(fieldFactory));

        staticDeserializerMap.put(Inet4Address.class.getName(), new InetAddressDeserializer(Inet4Address.class));
        staticDeserializerMap.put(Inet6Address.class.getName(), new InetAddressDeserializer(Inet6Address.class));

        ClassLoader systemClassLoader = null;
        try {
            systemClassLoader = ClassLoader.getSystemClassLoader();
        } catch (Exception ignored) {
        }

        SYSTEM_CLASS_LOADER = systemClassLoader;
    }
}
