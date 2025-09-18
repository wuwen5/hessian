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
 * Deserializing an object. Custom deserializers should extend
 * from AbstractDeserializer to avoid issues with signature
 * changes.
 */
public interface HessianDeserializer {
    Class<?> getType();

    boolean isReadResolve();

    Object readObject(AbstractHessianDecoder in) throws IOException;

    Object readList(AbstractHessianDecoder in, int length) throws IOException;

    Object readLengthList(AbstractHessianDecoder in, int length) throws IOException;

    Object readMap(AbstractHessianDecoder in) throws IOException;

    /**
     * Creates an empty array for the deserializers field
     * entries.
     *
     * @param len number of fields to be read
     * @return empty array of the proper field type.
     */
    Object[] createFields(int len);

    /**
     * Returns the deserializer's field reader for the given name.
     *
     * @param name the field name
     * @return the deserializer's internal field reader
     */
    Object createField(String name);

    /**
     * Reads the object from the input stream, given the field
     * definition.
     *
     * @param in the input stream
     * @param fields the deserializer's own field marshal
     * @return the new object
     * @throws IOException if an I/O error occurs
     */
    Object readObject(AbstractHessianDecoder in, Object[] fields) throws IOException;

    Object readObject(AbstractHessianDecoder in, String[] fieldNames) throws IOException;
}
