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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import lombok.extern.slf4j.Slf4j;

/**
 * Serializing an object for known object types.
 */
@Slf4j
public class FieldDeserializer2Factory {
    public static FieldDeserializer2Factory create() {
        boolean isEnableUnsafeSerializer = (UnsafeSerializer.isEnabled() && UnsafeDeserializer.isEnabled());

        if (isEnableUnsafeSerializer) {
            return new FieldDeserializer2FactoryUnsafe();
        } else {
            return new FieldDeserializer2Factory();
        }
    }

    /**
     * Creates a map of the classes fields.
     */
    FieldDeserializer2 create(Field field) {
        if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
            return NullFieldDeserializer.DESER;
        }

        // XXX: could parameterize the handler to only deal with public
        try {
            field.setAccessible(true);
        } catch (Throwable e) {
            log.trace("Unable to set field to accessible", e);
        }

        Class<?> type = field.getType();
        FieldDeserializer2 deser;

        if (String.class.equals(type)) {
            deser = new StringFieldDeserializer(field);
        } else if (byte.class.equals(type)) {
            deser = new ByteFieldDeserializer(field);
        } else if (short.class.equals(type)) {
            deser = new ShortFieldDeserializer(field);
        } else if (int.class.equals(type)) {
            deser = new IntFieldDeserializer(field);
        } else if (long.class.equals(type)) {
            deser = new LongFieldDeserializer(field);
        } else if (float.class.equals(type)) {
            deser = new FloatFieldDeserializer(field);
        } else if (double.class.equals(type)) {
            deser = new DoubleFieldDeserializer(field);
        } else if (boolean.class.equals(type)) {
            deser = new BooleanFieldDeserializer(field);
        } else if (java.sql.Date.class.equals(type)) {
            deser = new SqlDateFieldDeserializer(field);
        } else if (java.sql.Timestamp.class.equals(type)) {
            deser = new SqlTimestampFieldDeserializer(field);
        } else if (java.sql.Time.class.equals(type)) {
            deser = new SqlTimeFieldDeserializer(field);
        } else {
            deser = new ObjectFieldDeserializer(field);
        }

        return deser;
    }

    /**
     * Creates a map of the classes fields.
     */
    protected static Object getParamArg(Class<?> cl) {
        if (!cl.isPrimitive()) {
            return null;
        } else if (boolean.class.equals(cl)) {
            return Boolean.FALSE;
        } else if (byte.class.equals(cl)) {
            return new Byte((byte) 0);
        } else if (short.class.equals(cl)) {
            return new Short((short) 0);
        } else if (char.class.equals(cl)) {
            return new Character((char) 0);
        } else if (int.class.equals(cl)) {
            return Integer.valueOf(0);
        } else if (long.class.equals(cl)) {
            return Long.valueOf(0);
        } else if (float.class.equals(cl)) {
            return Float.valueOf(0);
        } else if (double.class.equals(cl)) {
            return Double.valueOf(0);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    static class NullFieldDeserializer implements FieldDeserializer2 {
        static NullFieldDeserializer DESER = new NullFieldDeserializer();

        @Override
        public void deserialize(AbstractHessianDecoder in, Object obj) throws IOException {
            in.readObject();
        }
    }

    static class ObjectFieldDeserializer implements FieldDeserializer2 {
        private final Field field;

        ObjectFieldDeserializer(Field field) {
            this.field = field;
        }

        @Override
        public void deserialize(AbstractHessianDecoder in, Object obj) throws IOException {
            Object value = null;

            try {
                value = in.readObject(field.getType());

                field.set(obj, value);
            } catch (Exception e) {
                logDeserializeError(field, obj, value, e);
            }
        }
    }

    static class BooleanFieldDeserializer implements FieldDeserializer2 {
        private final Field field;

        BooleanFieldDeserializer(Field field) {
            this.field = field;
        }

        @Override
        public void deserialize(AbstractHessianDecoder in, Object obj) throws IOException {
            boolean value = false;

            try {
                value = in.readBoolean();

                field.setBoolean(obj, value);
            } catch (Exception e) {
                logDeserializeError(field, obj, value, e);
            }
        }
    }

    static class ByteFieldDeserializer implements FieldDeserializer2 {
        private final Field field;

        ByteFieldDeserializer(Field field) {
            this.field = field;
        }

        @Override
        public void deserialize(AbstractHessianDecoder in, Object obj) throws IOException {
            int value = 0;

            try {
                value = in.readInt();

                field.setByte(obj, (byte) value);
            } catch (Exception e) {
                logDeserializeError(field, obj, value, e);
            }
        }
    }

    static class ShortFieldDeserializer implements FieldDeserializer2 {
        private final Field field;

        ShortFieldDeserializer(Field field) {
            this.field = field;
        }

        @Override
        public void deserialize(AbstractHessianDecoder in, Object obj) throws IOException {
            int value = 0;

            try {
                value = in.readInt();

                field.setShort(obj, (short) value);
            } catch (Exception e) {
                logDeserializeError(field, obj, value, e);
            }
        }
    }

    static class IntFieldDeserializer implements FieldDeserializer2 {
        private final Field field;

        IntFieldDeserializer(Field field) {
            this.field = field;
        }

        @Override
        public void deserialize(AbstractHessianDecoder in, Object obj) throws IOException {
            int value = 0;

            try {
                value = in.readInt();

                field.setInt(obj, value);
            } catch (Exception e) {
                logDeserializeError(field, obj, value, e);
            }
        }
    }

    static class LongFieldDeserializer implements FieldDeserializer2 {
        private final Field field;

        LongFieldDeserializer(Field field) {
            this.field = field;
        }

        @Override
        public void deserialize(AbstractHessianDecoder in, Object obj) throws IOException {
            long value = 0;

            try {
                value = in.readLong();

                field.setLong(obj, value);
            } catch (Exception e) {
                logDeserializeError(field, obj, value, e);
            }
        }
    }

    static class FloatFieldDeserializer implements FieldDeserializer2 {
        private final Field field;

        FloatFieldDeserializer(Field field) {
            this.field = field;
        }

        @Override
        public void deserialize(AbstractHessianDecoder in, Object obj) throws IOException {
            double value = 0;

            try {
                value = in.readDouble();

                field.setFloat(obj, (float) value);
            } catch (Exception e) {
                logDeserializeError(field, obj, value, e);
            }
        }
    }

    static class DoubleFieldDeserializer implements FieldDeserializer2 {
        private final Field field;

        DoubleFieldDeserializer(Field field) {
            this.field = field;
        }

        @Override
        public void deserialize(AbstractHessianDecoder in, Object obj) throws IOException {
            double value = 0;

            try {
                value = in.readDouble();

                field.setDouble(obj, value);
            } catch (Exception e) {
                logDeserializeError(field, obj, value, e);
            }
        }
    }

    static class StringFieldDeserializer implements FieldDeserializer2 {
        private final Field field;

        StringFieldDeserializer(Field field) {
            this.field = field;
        }

        @Override
        public void deserialize(AbstractHessianDecoder in, Object obj) throws IOException {
            String value = null;

            try {
                value = in.readString();

                field.set(obj, value);
            } catch (Exception e) {
                logDeserializeError(field, obj, value, e);
            }
        }
    }

    static class SqlDateFieldDeserializer implements FieldDeserializer2 {
        private final Field field;

        SqlDateFieldDeserializer(Field field) {
            this.field = field;
        }

        @Override
        public void deserialize(AbstractHessianDecoder in, Object obj) throws IOException {
            java.sql.Date value = null;

            try {
                java.util.Date date = (java.util.Date) in.readObject();

                if (date != null) {
                    value = new java.sql.Date(date.getTime());

                    field.set(obj, value);
                } else {
                    field.set(obj, null);
                }
            } catch (Exception e) {
                logDeserializeError(field, obj, value, e);
            }
        }
    }

    static class SqlTimestampFieldDeserializer implements FieldDeserializer2 {
        private final Field field;

        SqlTimestampFieldDeserializer(Field field) {
            this.field = field;
        }

        @Override
        public void deserialize(AbstractHessianDecoder in, Object obj) throws IOException {
            java.sql.Timestamp value = null;

            try {
                java.util.Date date = (java.util.Date) in.readObject();

                if (date != null) {
                    value = new java.sql.Timestamp(date.getTime());

                    field.set(obj, value);
                } else {
                    field.set(obj, null);
                }
            } catch (Exception e) {
                logDeserializeError(field, obj, value, e);
            }
        }
    }

    static class SqlTimeFieldDeserializer implements FieldDeserializer2 {
        private final Field field;

        SqlTimeFieldDeserializer(Field field) {
            this.field = field;
        }

        @Override
        public void deserialize(AbstractHessianDecoder in, Object obj) throws IOException {
            java.sql.Time value = null;

            try {
                java.util.Date date = (java.util.Date) in.readObject();

                if (date != null) {
                    value = new java.sql.Time(date.getTime());

                    field.set(obj, value);
                } else {
                    field.set(obj, null);
                }
            } catch (Exception e) {
                logDeserializeError(field, obj, value, e);
            }
        }
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
}
