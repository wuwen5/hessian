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

/**
 * Factory for returning serialization methods.
 */
public interface ISerializerFactory {

    /**
     * Returns the serializer for a class.
     *
     * @param cl the class of the object that needs to be serialized.
     *
     * @return a serializer object for the serialization.
     * @throws HessianProtocolException when a serializer cannot be found.
     */
    HessianSerializer getSerializer(Class<?> cl) throws HessianProtocolException;

    /**
     * Returns the deserializer for a class.
     *
     * @param cl the class of the object that needs to be deserialized.
     *
     * @return a deserializer object for the serialization.
     * @throws HessianProtocolException when a deserializer cannot be found.
     */
    HessianDeserializer getDeserializer(Class<?> cl) throws HessianProtocolException;
}
