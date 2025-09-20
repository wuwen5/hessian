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

import com.caucho.hessian.HessianUnshared;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.WeakHashMap;
import lombok.extern.slf4j.Slf4j;
import sun.misc.Unsafe;

/**
 * Serializing an object for known object types.
 */
@Slf4j
public class UnsafeSerializer extends AbstractSerializer {
    private static boolean isEnabled;
    private static final Unsafe UNSAFE;

    private static final WeakHashMap<Class<?>, SoftReference<UnsafeSerializer>> SERIALIZER_MAP = new WeakHashMap<>();

    private Field[] fields;
    private FieldSerializer[] fieldSerializers;

    public static boolean isEnabled() {
        return isEnabled;
    }

    public UnsafeSerializer(Class<?> cl) {
        introspect(cl);
    }

    public static UnsafeSerializer create(Class<?> cl) {
        synchronized (SERIALIZER_MAP) {
            SoftReference<UnsafeSerializer> baseRef = SERIALIZER_MAP.get(cl);

            UnsafeSerializer base = baseRef != null ? baseRef.get() : null;

            if (base == null) {
                if (cl.isAnnotationPresent(HessianUnshared.class)
                        || cl.isAnnotationPresent(io.github.wuwen5.hessian.HessianUnshared.class)) {
                    base = new UnsafeUnsharedSerializer(cl);
                } else {
                    base = new UnsafeSerializer(cl);
                }

                baseRef = new SoftReference<>(base);
                SERIALIZER_MAP.put(cl, baseRef);
            }

            return base;
        }
    }

    protected void introspect(Class<?> cl) {
        ArrayList<Field> primitiveFields = new ArrayList<>();
        ArrayList<Field> compoundFields = new ArrayList<>();

        for (; cl != null; cl = cl.getSuperclass()) {

            for (Field field : cl.getDeclaredFields()) {
                if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                if (field.getType().isPrimitive()
                        || (field.getType().getName().startsWith("java.lang.")
                                && !field.getType().equals(Object.class))) {
                    primitiveFields.add(field);
                } else {
                    compoundFields.add(field);
                }
            }
        }

        ArrayList<Field> fieldArrayList = new ArrayList<>();
        fieldArrayList.addAll(primitiveFields);
        fieldArrayList.addAll(compoundFields);
        Collections.reverse(fieldArrayList);

        this.fields = new Field[fieldArrayList.size()];
        fieldArrayList.toArray(this.fields);

        fieldSerializers = new FieldSerializer[this.fields.length];

        for (int i = 0; i < this.fields.length; i++) {
            fieldSerializers[i] = getFieldSerializer(this.fields[i]);
        }
    }

    @Override
    public void writeObject(Object obj, AbstractHessianEncoder out) throws IOException {
        if (out.addRef(obj)) {
            return;
        }

        Class<?> cl = obj.getClass();

        int ref = out.writeObjectBegin(cl.getName());

        if (ref >= 0) {
            writeInstance(obj, out);
        } else if (ref == -1) {
            writeDefinition20(out);
            out.writeObjectBegin(cl.getName());
            writeInstance(obj, out);
        }
    }

    @Override
    protected void writeObject10(Object obj, AbstractHessianEncoder out) throws IOException {
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];

            out.writeString(field.getName());

            fieldSerializers[i].serialize(out, obj);
        }

        out.writeMapEnd();
    }

    private void writeDefinition20(AbstractHessianEncoder out) throws IOException {
        out.writeClassFieldLength(fields.length);

        for (Field field : fields) {
            out.writeString(field.getName());
        }
    }

    @Override
    public final void writeInstance(Object obj, AbstractHessianEncoder out) throws IOException {
        try {
            for (FieldSerializer fieldSerializer : this.fieldSerializers) {
                fieldSerializer.serialize(out, obj);
            }
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    e.getMessage() + "\n class: " + obj.getClass().getName() + " (object=" + obj + ")", e);
        } catch (IOException e) {
            throw new IOExceptionWrapper(
                    e.getMessage() + "\n class: " + obj.getClass().getName() + " (object=" + obj + ")", e);
        }
    }

    private static FieldSerializer getFieldSerializer(Field field) {
        Class<?> type = field.getType();

        if (boolean.class.equals(type)) {
            return new BooleanFieldSerializer(field);
        } else if (byte.class.equals(type)) {
            return new ByteFieldSerializer(field);
        } else if (char.class.equals(type)) {
            return new CharFieldSerializer(field);
        } else if (short.class.equals(type)) {
            return new ShortFieldSerializer(field);
        } else if (int.class.equals(type)) {
            return new IntFieldSerializer(field);
        } else if (long.class.equals(type)) {
            return new LongFieldSerializer(field);
        } else if (double.class.equals(type)) {
            return new DoubleFieldSerializer(field);
        } else if (float.class.equals(type)) {
            return new FloatFieldSerializer(field);
        } else if (String.class.equals(type)) {
            return new StringFieldSerializer(field);
        } else if (java.util.Date.class.equals(type)
                || java.sql.Date.class.equals(type)
                || java.sql.Timestamp.class.equals(type)
                || java.sql.Time.class.equals(type)) {
            return new DateFieldSerializer(field);
        } else {
            return new ObjectFieldSerializer(field);
        }
    }

    abstract static class FieldSerializer {
        abstract void serialize(AbstractHessianEncoder out, Object obj) throws IOException;
    }

    static final class ObjectFieldSerializer extends FieldSerializer {
        private final Field field;
        private final long offset;

        ObjectFieldSerializer(Field field) {
            this.field = field;
            offset = UNSAFE.objectFieldOffset(field);

            if (offset == Unsafe.INVALID_FIELD_OFFSET) throw new IllegalStateException();
        }

        @Override
        void serialize(AbstractHessianEncoder out, Object obj) throws IOException {
            try {
                Object value = UNSAFE.getObject(obj, offset);

                out.writeObject(value);
            } catch (RuntimeException e) {
                throw new IllegalStateException(
                        e.getMessage() + "\n field: "
                                + field.getDeclaringClass().getName()
                                + '.' + field.getName(),
                        e);
            } catch (IOException e) {
                throw new IOExceptionWrapper(
                        e.getMessage() + "\n field: "
                                + field.getDeclaringClass().getName()
                                + '.' + field.getName(),
                        e);
            }
        }
    }

    static final class BooleanFieldSerializer extends FieldSerializer {
        private final long offset;

        BooleanFieldSerializer(Field field) {
            offset = UNSAFE.objectFieldOffset(field);

            if (offset == Unsafe.INVALID_FIELD_OFFSET) {
                throw new IllegalStateException();
            }
        }

        @Override
        void serialize(AbstractHessianEncoder out, Object obj) throws IOException {
            boolean value = UNSAFE.getBoolean(obj, offset);

            out.writeBoolean(value);
        }
    }

    static final class ByteFieldSerializer extends FieldSerializer {
        private final long offset;

        ByteFieldSerializer(Field field) {
            offset = UNSAFE.objectFieldOffset(field);

            if (offset == Unsafe.INVALID_FIELD_OFFSET) {
                throw new IllegalStateException();
            }
        }

        @Override
        void serialize(AbstractHessianEncoder out, Object obj) throws IOException {
            int value = UNSAFE.getByte(obj, offset);

            out.writeInt(value);
        }
    }

    static final class CharFieldSerializer extends FieldSerializer {
        private final long offset;

        CharFieldSerializer(Field field) {
            offset = UNSAFE.objectFieldOffset(field);

            if (offset == Unsafe.INVALID_FIELD_OFFSET) {
                throw new IllegalStateException();
            }
        }

        @Override
        void serialize(AbstractHessianEncoder out, Object obj) throws IOException {
            char value = UNSAFE.getChar(obj, offset);

            out.writeString(String.valueOf(value));
        }
    }

    static final class ShortFieldSerializer extends FieldSerializer {
        private final long offset;

        ShortFieldSerializer(Field field) {
            offset = UNSAFE.objectFieldOffset(field);

            if (offset == Unsafe.INVALID_FIELD_OFFSET) {
                throw new IllegalStateException();
            }
        }

        @Override
        void serialize(AbstractHessianEncoder out, Object obj) throws IOException {
            int value = UNSAFE.getShort(obj, offset);

            out.writeInt(value);
        }
    }

    static final class IntFieldSerializer extends FieldSerializer {
        private final long offset;

        IntFieldSerializer(Field field) {
            offset = UNSAFE.objectFieldOffset(field);

            if (offset == Unsafe.INVALID_FIELD_OFFSET) {
                throw new IllegalStateException();
            }
        }

        @Override
        void serialize(AbstractHessianEncoder out, Object obj) throws IOException {
            int value = UNSAFE.getInt(obj, offset);

            out.writeInt(value);
        }
    }

    static final class LongFieldSerializer extends FieldSerializer {
        private final long offset;

        LongFieldSerializer(Field field) {
            offset = UNSAFE.objectFieldOffset(field);

            if (offset == Unsafe.INVALID_FIELD_OFFSET) {
                throw new IllegalStateException();
            }
        }

        @Override
        void serialize(AbstractHessianEncoder out, Object obj) throws IOException {
            long value = UNSAFE.getLong(obj, offset);

            out.writeLong(value);
        }
    }

    static final class FloatFieldSerializer extends FieldSerializer {
        private final long offset;

        FloatFieldSerializer(Field field) {
            offset = UNSAFE.objectFieldOffset(field);

            if (offset == Unsafe.INVALID_FIELD_OFFSET) {
                throw new IllegalStateException();
            }
        }

        @Override
        void serialize(AbstractHessianEncoder out, Object obj) throws IOException {
            double value = UNSAFE.getFloat(obj, offset);

            out.writeDouble(value);
        }
    }

    static final class DoubleFieldSerializer extends FieldSerializer {
        private final long offset;

        DoubleFieldSerializer(Field field) {
            offset = UNSAFE.objectFieldOffset(field);

            if (offset == Unsafe.INVALID_FIELD_OFFSET) throw new IllegalStateException();
        }

        @Override
        void serialize(AbstractHessianEncoder out, Object obj) throws IOException {
            double value = UNSAFE.getDouble(obj, offset);

            out.writeDouble(value);
        }
    }

    static final class StringFieldSerializer extends FieldSerializer {
        private final long offset;

        StringFieldSerializer(Field field) {
            offset = UNSAFE.objectFieldOffset(field);

            if (offset == Unsafe.INVALID_FIELD_OFFSET) {
                throw new IllegalStateException();
            }
        }

        @Override
        void serialize(AbstractHessianEncoder out, Object obj) throws IOException {
            String value = (String) UNSAFE.getObject(obj, offset);

            out.writeString(value);
        }
    }

    static final class DateFieldSerializer extends FieldSerializer {
        private final long offset;

        DateFieldSerializer(Field field) {
            offset = UNSAFE.objectFieldOffset(field);

            if (offset == Unsafe.INVALID_FIELD_OFFSET) {
                throw new IllegalStateException();
            }
        }

        @Override
        void serialize(AbstractHessianEncoder out, Object obj) throws IOException {
            java.util.Date value = (java.util.Date) UNSAFE.getObject(obj, offset);

            if (value == null) {
                out.writeNull();
            } else {
                out.writeUTCDate(value.getTime());
            }
        }
    }

    static {
        boolean isEnabled = false;
        Unsafe unsafe = null;

        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = null;
            for (Field field : unsafeClass.getDeclaredFields()) {
                if ("theUnsafe".equals(field.getName())) {
                    theUnsafe = field;
                }
            }

            if (theUnsafe != null) {
                theUnsafe.setAccessible(true);
                unsafe = (Unsafe) theUnsafe.get(null);
            }

            isEnabled = unsafe != null;

            String unsafeProp = System.getProperty("com.caucho.hessian.unsafe");

            if ("false".equals(unsafeProp)) {
                isEnabled = false;
            }
        } catch (Throwable e) {
            log.error(e.toString(), e);
        }

        UNSAFE = unsafe;
        UnsafeSerializer.isEnabled = isEnabled;
    }
}
