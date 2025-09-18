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

/**
 * Deserializing an object.
 */
public class BaseDeserializer implements HessianDeserializer {
    public static final NullDeserializer NULL = new NullDeserializer();

    @Override
    public Class<?> getType() {
        return Object.class;
    }

    @Override
    public boolean isReadResolve() {
        return false;
    }

    @Override
    public Object readObject(AbstractHessianDecoder in) throws IOException {
        Object obj = in.readObject();

        String className = getClass().getName();

        if (obj != null) {
            throw error(className + ": unexpected object " + obj.getClass().getName() + " (" + obj + ")");
        } else {
            throw error(className + ": unexpected null value");
        }
    }

    @Override
    public Object readList(AbstractHessianDecoder in, int length) throws IOException {
        throw new UnsupportedOperationException(String.valueOf(this));
    }

    @Override
    public Object readLengthList(AbstractHessianDecoder in, int length) throws IOException {
        throw new UnsupportedOperationException(String.valueOf(this));
    }

    @Override
    public Object readMap(AbstractHessianDecoder in) throws IOException {
        return readObject(in);
    }

    /**
     * Creates the field array for a class. The default
     * implementation returns a String[] array.
     *
     * @param len number of items in the array
     * @return the new empty array
     */
    @Override
    public Object[] createFields(int len) {
        return new String[len];
    }

    /**
     * Creates a field value class. The default
     * implementation returns the String.
     *
     * @param name the field name
     * @return the new empty array
     */
    @Override
    public Object createField(String name) {
        return name;
    }

    @Override
    public Object readObject(AbstractHessianDecoder in, String[] fieldNames) throws IOException {
        return readObject(in, (Object[]) fieldNames);
    }

    /**
     * Reads an object instance from the input stream
     */
    @Override
    public Object readObject(AbstractHessianDecoder in, Object[] fields) throws IOException {
        throw new UnsupportedOperationException(toString());
    }

    protected HessianProtocolException error(String msg) {
        return new HessianProtocolException(msg);
    }

    protected String codeName(int ch) {
        if (ch < 0) {
            return "end of file";
        } else {
            return "0x" + Integer.toHexString(ch & 0xff);
        }
    }

    /**
     * The NullDeserializer exists as a marker for the factory classes so
     * they save a null result.
     */
    static final class NullDeserializer extends BaseDeserializer {}
}
