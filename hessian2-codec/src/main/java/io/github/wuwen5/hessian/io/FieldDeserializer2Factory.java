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
    FieldDeserializer create(Field field) {
        if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
            return NullFieldDeserializer.DESER;
        }

        // XXX: could parameterize the handler to only deal with public
        try {
            field.setAccessible(true);
        } catch (RuntimeException e) {
            log.trace("Unable to set field to accessible", e);
        }

        Class<?> type = field.getType();
        FieldDeserializer deser;

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
    public static Object getParamArg(Class<?> cl) {
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

    static class NullFieldDeserializer implements FieldDeserializer {
        static NullFieldDeserializer DESER = new NullFieldDeserializer();

        @Override
        public void deserialize(AbstractHessianDecoder in, Object obj) throws IOException {
            in.readObject();
        }
    }

    static class ObjectFieldDeserializer implements FieldDeserializer {
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

    static class BooleanFieldDeserializer implements FieldDeserializer {
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

    static class ByteFieldDeserializer implements FieldDeserializer {
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

    static class ShortFieldDeserializer implements FieldDeserializer {
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

    static class IntFieldDeserializer implements FieldDeserializer {
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

    static class LongFieldDeserializer implements FieldDeserializer {
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

    static class FloatFieldDeserializer implements FieldDeserializer {
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

    static class DoubleFieldDeserializer implements FieldDeserializer {
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

    static class StringFieldDeserializer implements FieldDeserializer {
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

    static class SqlDateFieldDeserializer implements FieldDeserializer {
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

    static class SqlTimestampFieldDeserializer implements FieldDeserializer {
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

    static class SqlTimeFieldDeserializer implements FieldDeserializer {
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
