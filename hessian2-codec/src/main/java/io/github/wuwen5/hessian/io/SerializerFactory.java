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

import com.caucho.hessian.io.HessianRemoteObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.SneakyThrows;

/**
 * Factory for returning serialization methods.
 */
public class SerializerFactory extends AbstractSerializerFactory {
    private static final Logger log = Logger.getLogger(SerializerFactory.class.getName());

    private static final ClassLoader SYSTEM_CLASS_LOADER;

    private static final Map<String, Deserializer> STATIC_TYPE_MAP;

    private static final WeakHashMap<ClassLoader, SoftReference<SerializerFactory>> DEFAULT_FACTORY_REF_MAP =
            new WeakHashMap<>();

    private final ContextSerializerFactory contextFactory;
    private final WeakReference<ClassLoader> loaderRef;

    protected Serializer defaultSerializer;

    /**
     * Additional factories
     */
    protected List<AbstractSerializerFactory> factories = new ArrayList<>();

    protected CollectionSerializer collectionSerializer;
    protected MapSerializer mapSerializer;

    private Deserializer hashMapDeserializer;
    private Deserializer arrayListDeserializer;
    private final ConcurrentMap<Class<?>, Serializer> cachedSerializerMap = new ConcurrentHashMap<>(8);
    private final ConcurrentMap<Class<?>, Deserializer> cachedDeserializerMap = new ConcurrentHashMap<>(8);
    private final ConcurrentMap<String, Deserializer> cachedTypeDeserializerMap = new ConcurrentHashMap<>();

    private boolean isAllowNonSerializable;
    private final boolean isEnableUnsafeSerializer = (UnsafeSerializer.isEnabled() && UnsafeDeserializer.isEnabled());

    private final FieldDeserializer2Factory fieldDeserializer2Factory;

    private ClassFactory classFactory;

    public SerializerFactory() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public SerializerFactory(ClassLoader loader) {
        loaderRef = new WeakReference<>(loader);

        contextFactory = ContextSerializerFactory.create(loader);

        if (isEnableUnsafeSerializer) {
            fieldDeserializer2Factory = new FieldDeserializer2FactoryUnsafe();
        } else {
            fieldDeserializer2Factory = new FieldDeserializer2Factory();
        }
    }

    public static SerializerFactory createDefault() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        synchronized (DEFAULT_FACTORY_REF_MAP) {
            SoftReference<SerializerFactory> factoryRef = DEFAULT_FACTORY_REF_MAP.get(loader);

            SerializerFactory factory = null;

            if (factoryRef != null) {
                factory = factoryRef.get();
            }

            if (factory == null) {
                factory = new SerializerFactory();

                factoryRef = new SoftReference<>(factory);

                DEFAULT_FACTORY_REF_MAP.put(loader, factoryRef);
            }

            return factory;
        }
    }

    public ClassLoader getClassLoader() {
        return loaderRef.get();
    }

    /**
     * Set true if the collection serializer should send the java type.
     */
    public void setSendCollectionType(boolean isSendType) {
        if (collectionSerializer == null) {
            collectionSerializer = new CollectionSerializer();
        }

        collectionSerializer.setSendJavaType(isSendType);

        if (mapSerializer == null) {
            mapSerializer = new MapSerializer();
        }

        mapSerializer.setSendJavaType(isSendType);
    }

    /**
     * Adds a factory.
     */
    public void addFactory(AbstractSerializerFactory factory) {
        factories.add(factory);
    }

    /**
     * If true, non-serializable objects are allowed.
     */
    public void setAllowNonSerializable(boolean allow) {
        isAllowNonSerializable = allow;
    }

    /**
     * If true, non-serializable objects are allowed.
     */
    public boolean isAllowNonSerializable() {
        return isAllowNonSerializable;
    }

    /**
     * Returns the serializer for a class.
     *
     * @param cl the class of the object that needs to be serialized.
     *
     * @return a serializer object for the serialization.
     */
    public Serializer getObjectSerializer(Class<?> cl) throws HessianProtocolException {
        Serializer serializer = getSerializer(cl);

        if (serializer instanceof ObjectSerializer) {
            return ((ObjectSerializer) serializer).getObjectSerializer();
        } else {
            return serializer;
        }
    }

    public Class<?> loadSerializedClass(String className) throws ClassNotFoundException {
        return getClassFactory().load(className);
    }

    public ClassFactory getClassFactory() {
        synchronized (this) {
            if (classFactory == null) {
                classFactory = new ClassFactory(getClassLoader());
            }

            return classFactory;
        }
    }

    public FieldDeserializer2Factory getFieldDeserializerFactory() {
        return fieldDeserializer2Factory;
    }

    /**
     * Returns the serializer for a class.
     *
     * @param cl the class of the object that needs to be serialized.
     *
     * @return a serializer object for the serialization.
     */
    @Override
    public Serializer getSerializer(Class<?> cl) throws HessianProtocolException {
        Serializer serializer = cachedSerializerMap.get(cl);

        if (serializer != null) {
            return serializer;
        }

        return cachedSerializerMap.computeIfAbsent(cl, this::loadSerializer);
    }

    @SneakyThrows
    protected Serializer loadSerializer(Class<?> cl) {
        Serializer serializer;

        for (int i = 0; factories != null && i < factories.size(); i++) {
            AbstractSerializerFactory factory = factories.get(i);

            serializer = factory.getSerializer(cl);

            if (serializer != null) {
                return serializer;
            }
        }

        serializer = contextFactory.getSerializer(cl.getName());

        if (serializer != null) {
            return serializer;
        }

        ClassLoader loader = cl.getClassLoader();

        if (loader == null) {
            loader = SYSTEM_CLASS_LOADER;
        }

        ContextSerializerFactory factory = ContextSerializerFactory.create(loader);

        serializer = factory.getCustomSerializer(cl);

        if (serializer != null) {
            return serializer;
        }

        if (HessianRemoteObject.class.isAssignableFrom(cl)) {
            return new RemoteSerializer();
        } // TODO
        //    else if (BurlapRemoteObject.class.isAssignableFrom(cl)) {
        //      return new RemoteSerializer();
        //    }
        else if (InetAddress.class.isAssignableFrom(cl)) {
            return InetAddressSerializer.create();
        } else if (JavaSerializer.getWriteReplace(cl) != null) {
            Serializer baseSerializer = getDefaultSerializer(cl);

            return new WriteReplaceSerializer(cl, getClassLoader(), baseSerializer);
        } else if (Map.class.isAssignableFrom(cl)) {
            if (mapSerializer == null) {
                mapSerializer = new MapSerializer();
            }

            return mapSerializer;
        } else if (Collection.class.isAssignableFrom(cl)) {
            if (collectionSerializer == null) {
                collectionSerializer = new CollectionSerializer();
            }

            return collectionSerializer;
        } else if (cl.isArray()) {
            return new ArraySerializer();
        } else if (Throwable.class.isAssignableFrom(cl)) {
            return new ThrowableSerializer(getDefaultSerializer(cl));
        } else if (InputStream.class.isAssignableFrom(cl)) {
            return new InputStreamSerializer();
        } else if (Iterator.class.isAssignableFrom(cl)) {
            return IteratorSerializer.create();
        } else if (Calendar.class.isAssignableFrom(cl)) {
            return CalendarSerializer.SER;
        } else if (Enumeration.class.isAssignableFrom(cl)) {
            return EnumerationSerializer.create();
        } else if (Enum.class.isAssignableFrom(cl)) {
            return new EnumSerializer(cl);
        } else if (Annotation.class.isAssignableFrom(cl)) {
            return new AnnotationSerializer(cl);
        }

        return getDefaultSerializer(cl);
    }

    /**
     * Returns the default serializer for a class that isn't matched
     * directly.  Application can override this method to produce
     * bean-style serialization instead of field serialization.
     *
     * @param cl the class of the object that needs to be serialized.
     *
     * @return a serializer object for the serialization.
     */
    protected Serializer getDefaultSerializer(Class<?> cl) {
        if (defaultSerializer != null) {
            return defaultSerializer;
        }

        if (!Serializable.class.isAssignableFrom(cl) && !isAllowNonSerializable) {
            throw new IllegalStateException(
                    "Serialized class " + cl.getName() + " must implement java.io.Serializable");
        }

        if (isEnableUnsafeSerializer && JavaSerializer.getWriteReplace(cl) == null) {
            return UnsafeSerializer.create(cl);
        } else {
            return JavaSerializer.create(cl);
        }
    }

    /**
     * Returns the deserializer for a class.
     *
     * @param cl the class of the object that needs to be deserialized.
     *
     * @return a deserializer object for the serialization.
     */
    @Override
    public Deserializer getDeserializer(Class<?> cl) throws HessianProtocolException {
        Deserializer deserializer = cachedDeserializerMap.get(cl);

        if (deserializer != null) {
            return deserializer;
        }

        return cachedDeserializerMap.computeIfAbsent(cl, this::loadDeserializer);
    }

    @SneakyThrows
    protected Deserializer loadDeserializer(Class<?> cl) {
        Deserializer deserializer = null;

        for (int i = 0; deserializer == null && factories != null && i < factories.size(); i++) {
            AbstractSerializerFactory factory = factories.get(i);

            deserializer = factory.getDeserializer(cl);
        }

        if (deserializer != null) {
            return deserializer;
        }

        // XXX: need test
        deserializer = contextFactory.getDeserializer(cl.getName());

        if (deserializer != null) {
            return deserializer;
        }

        ContextSerializerFactory factory;

        if (cl.getClassLoader() != null) {
            factory = ContextSerializerFactory.create(cl.getClassLoader());
        } else {
            factory = ContextSerializerFactory.create(SYSTEM_CLASS_LOADER);
        }

        deserializer = factory.getDeserializer(cl.getName());

        if (deserializer != null) {
            return deserializer;
        }

        deserializer = factory.getCustomDeserializer(cl);

        if (deserializer != null) {
            return deserializer;
        }

        if (Collection.class.isAssignableFrom(cl)) {
            deserializer = new CollectionDeserializer(cl);
        } else if (Map.class.isAssignableFrom(cl)) {
            deserializer = new MapDeserializer(cl);
        } else if (Iterator.class.isAssignableFrom(cl)) {
            deserializer = IteratorDeserializer.create();
        } else if (Annotation.class.isAssignableFrom(cl)) {
            deserializer = new AnnotationDeserializer(cl);
        } else if (cl.isInterface()) {
            deserializer = new ObjectDeserializer(cl);
        } else if (cl.isArray()) {
            deserializer = new ArrayDeserializer(cl.getComponentType());
        } else if (Enumeration.class.isAssignableFrom(cl)) {
            deserializer = EnumerationDeserializer.create();
        } else if (Enum.class.isAssignableFrom(cl)) {
            deserializer = new EnumDeserializer(cl);
        } else if (Class.class.equals(cl)) {
            deserializer = new ClassDeserializer(getClassLoader());
        } else if (java.util.BitSet.class.equals(cl)) {
            deserializer = new BitSetDeserializer(fieldDeserializer2Factory);
        } else {
            deserializer = getDefaultDeserializer(cl);
        }

        return deserializer;
    }

    /**
     * Returns a custom serializer the class
     *
     * @param cl the class of the object that needs to be serialized.
     *
     * @return a serializer object for the serialization.
     */
    protected Deserializer getCustomDeserializer(Class<?> cl) {
        try {
            Class<?> serClass = Class.forName(cl.getName() + "HessianDeserializer", false, cl.getClassLoader());

            return (Deserializer) serClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            log.log(Level.FINEST, e.toString(), e);

            return null;
        } catch (Exception e) {
            log.log(Level.FINE, e.toString(), e);

            return null;
        }
    }

    /**
     * Returns the default serializer for a class that isn't matched
     * directly.  Application can override this method to produce
     * bean-style serialization instead of field serialization.
     *
     * @param cl the class of the object that needs to be serialized.
     *
     * @return a serializer object for the serialization.
     */
    protected Deserializer getDefaultDeserializer(Class<?> cl) {
        if (InputStream.class.equals(cl)) {
            return InputStreamDeserializer.DESER;
        }

        if (isEnableUnsafeSerializer) {
            return new UnsafeDeserializer(cl, fieldDeserializer2Factory);
        } else {
            return new JavaDeserializer(cl, fieldDeserializer2Factory);
        }
    }

    /**
     * Reads the object as a list.
     */
    public Object readList(AbstractHessianDecoder in, int length, String type) throws IOException {
        Deserializer deserializer = getDeserializer(type);

        if (deserializer != null) {
            return deserializer.readList(in, length);
        } else {
            return new CollectionDeserializer(ArrayList.class).readList(in, length);
        }
    }

    /**
     * Reads the object as a map.
     */
    public Object readMap(AbstractHessianDecoder in, String type) throws IOException {
        Deserializer deserializer = getDeserializer(type);

        if (deserializer != null) {
            return deserializer.readMap(in);
        } else if (hashMapDeserializer != null) {
            return hashMapDeserializer.readMap(in);
        } else {
            hashMapDeserializer = new MapDeserializer(HashMap.class);

            return hashMapDeserializer.readMap(in);
        }
    }

    /**
     * Reads the object as a map.
     */
    public Object readObject(AbstractHessianDecoder in, String type, String[] fieldNames) throws IOException {
        Deserializer deserializer = getDeserializer(type);

        if (deserializer != null) {
            return deserializer.readObject(in, fieldNames);
        } else if (hashMapDeserializer != null) {
            return hashMapDeserializer.readObject(in, fieldNames);
        } else {
            hashMapDeserializer = new MapDeserializer(HashMap.class);

            return hashMapDeserializer.readObject(in, fieldNames);
        }
    }

    /**
     * Reads the object as a map.
     */
    public Deserializer getObjectDeserializer(String type, Class<?> cl) throws HessianProtocolException {
        Deserializer reader = getObjectDeserializer(type);

        if (cl == null
                || cl.equals(reader.getType())
                || cl.isAssignableFrom(reader.getType())
                || reader.isReadResolve()
                || HessianHandle.class.isAssignableFrom(reader.getType())) {
            return reader;
        }

        if (log.isLoggable(Level.FINE)) {
            log.fine("hessian: expected deserializer '" + cl.getName() + "' at '" + type + "' ("
                    + reader.getType().getName() + ")");
        }

        return getDeserializer(cl);
    }

    /**
     * Reads the object as a map.
     */
    public Deserializer getObjectDeserializer(String type) throws HessianProtocolException {
        Deserializer deserializer = getDeserializer(type);

        if (deserializer != null) {
            return deserializer;
        } else if (hashMapDeserializer != null) {
            return hashMapDeserializer;
        } else {
            hashMapDeserializer = new MapDeserializer(HashMap.class);

            return hashMapDeserializer;
        }
    }

    /**
     * Reads the object as a map.
     */
    public Deserializer getListDeserializer(String type, Class<?> cl) throws HessianProtocolException {
        Deserializer reader = getListDeserializer(type);

        if (cl == null || cl.equals(reader.getType()) || cl.isAssignableFrom(reader.getType())) {
            return reader;
        }

        if (log.isLoggable(Level.FINE)) {
            log.fine("hessian: expected '" + cl.getName() + "' at '" + type + "' ("
                    + reader.getType().getName() + ")");
        }

        return getDeserializer(cl);
    }

    /**
     * Reads the object as a map.
     */
    public Deserializer getListDeserializer(String type) throws HessianProtocolException {
        Deserializer deserializer = getDeserializer(type);

        if (deserializer != null) {
            return deserializer;
        } else if (arrayListDeserializer != null) {
            return arrayListDeserializer;
        } else {
            arrayListDeserializer = new CollectionDeserializer(ArrayList.class);

            return arrayListDeserializer;
        }
    }

    /**
     * Returns a deserializer based on a string type.
     */
    public Deserializer getDeserializer(String type) throws HessianProtocolException {
        if (type == null || type.isEmpty()) {
            return null;
        }

        Deserializer deserializer = cachedTypeDeserializerMap.get(type);

        if (deserializer != null) {
            return deserializer;
        }

        deserializer = STATIC_TYPE_MAP.get(type);
        if (deserializer != null) {
            return deserializer;
        }

        if (type.startsWith("[")) {
            Deserializer subDeserializer = getDeserializer(type.substring(1));

            if (subDeserializer != null) {
                deserializer = new ArrayDeserializer(subDeserializer.getType());
            } else {
                deserializer = new ArrayDeserializer(Object.class);
            }
        } else {
            try {
                Class<?> cl = loadSerializedClass(type);

                deserializer = getDeserializer(cl);
            } catch (Exception e) {
                log.warning("Hessian/Burlap: '" + type + "' is an unknown class in " + getClassLoader() + ":\n" + e);

                log.log(Level.FINER, e.toString(), e);
            }
        }

        if (deserializer != null) {
            cachedTypeDeserializerMap.putIfAbsent(type, deserializer);
        }

        return deserializer;
    }

    private static void addBasic(Class<?> cl, String typeName, int type) {
        Deserializer deserializer = new BasicDeserializer(type);

        STATIC_TYPE_MAP.put(typeName, deserializer);
    }

    static {
        STATIC_TYPE_MAP = new HashMap<>();

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
        addBasic(StringBuilder.class, "string", BasicSerializer.STRING_BUILDER);
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
        addBasic(short[].class, "[short", BasicSerializer.SHORT_ARRAY);
        addBasic(int[].class, "[int", BasicSerializer.INTEGER_ARRAY);
        addBasic(long[].class, "[long", BasicSerializer.LONG_ARRAY);
        addBasic(float[].class, "[float", BasicSerializer.FLOAT_ARRAY);
        addBasic(double[].class, "[double", BasicSerializer.DOUBLE_ARRAY);
        addBasic(char[].class, "[char", BasicSerializer.CHARACTER_ARRAY);
        addBasic(String[].class, "[string", BasicSerializer.STRING_ARRAY);
        addBasic(Object[].class, "[object", BasicSerializer.OBJECT_ARRAY);

        Deserializer objectDeserializer = new JavaDeserializer(Object.class, new FieldDeserializer2Factory());
        STATIC_TYPE_MAP.put("object", objectDeserializer);
        STATIC_TYPE_MAP.put(HessianRemote.class.getName(), RemoteDeserializer.DESER);

        ClassLoader systemClassLoader = null;
        try {
            systemClassLoader = ClassLoader.getSystemClassLoader();
        } catch (Exception ignored) {
        }

        SYSTEM_CLASS_LOADER = systemClassLoader;
    }
}
