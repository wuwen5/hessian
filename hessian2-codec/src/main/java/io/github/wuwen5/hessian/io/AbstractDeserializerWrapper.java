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
public abstract class AbstractDeserializerWrapper implements HessianDeserializer {
    protected abstract HessianDeserializer getDelegate();

    @Override
    public Class<?> getType() {
        return getDelegate().getType();
    }

    @Override
    public boolean isReadResolve() {
        return getDelegate().isReadResolve();
    }

    @Override
    public Object readObject(AbstractHessianDecoder in) throws IOException {
        return getDelegate().readObject(in);
    }

    @Override
    public Object readList(AbstractHessianDecoder in, int length) throws IOException {
        return getDelegate().readList(in, length);
    }

    @Override
    public Object readLengthList(AbstractHessianDecoder in, int length) throws IOException {
        return getDelegate().readLengthList(in, length);
    }

    @Override
    public Object readMap(AbstractHessianDecoder in) throws IOException {
        return getDelegate().readMap(in);
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
        return getDelegate().createFields(len);
    }

    /**
     * Creates a field value class. The default
     * implementation returns the String.
     *
     * @param name number of items in the array
     * @return the new empty array
     */
    @Override
    public Object createField(String name) {
        return getDelegate().createField(name);
    }

    @Override
    public Object readObject(AbstractHessianDecoder in, String[] fieldNames) throws IOException {
        return getDelegate().readObject(in, fieldNames);
    }

    /**
     * Reads an object instance from the input stream
     */
    @Override
    public Object readObject(AbstractHessianDecoder in, Object[] fields) throws IOException {
        return getDelegate().readObject(in, fields);
    }
}
