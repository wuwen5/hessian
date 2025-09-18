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

package com.alibaba.com.caucho.hessian.io;

import io.github.wuwen5.hessian.io.Hessian2SerializerFactory;
import io.github.wuwen5.hessian.io.UnsafeDeserializer;
import io.github.wuwen5.hessian.io.UnsafeSerializer;

public class SerializerFactory extends Hessian2SerializerFactory {

    protected Serializer _defaultSerializer = (Serializer) defaultSerializer;

    private Hessian2SerializerFactory delegate;

    public SerializerFactory() {}

    public SerializerFactory(ClassLoader cl) {
        super(cl);
    }

    public boolean isEnableUnsafeSerializer() {
        return (UnsafeSerializer.isEnabled() && UnsafeDeserializer.isEnabled());
    }

    public SerializerFactory(Hessian2SerializerFactory serializerFactory) {
        this.delegate = serializerFactory;
    }

    @Override
    public ClassFactory getClassFactory() {
        io.github.wuwen5.hessian.io.ClassFactory classFactory = super.getClassFactory();
        return new ClassFactory(classFactory);
    }

    @Override
    public FieldDeserializer2Factory getFieldDeserializerFactory() {
        return new FieldDeserializer2Factory(super.getFieldDeserializerFactory());
    }

    @Override
    protected Serializer getDefaultSerializer(Class<?> cl) {
        // 返回桥接接口类型，避免类型不匹配
        // 由子类Hessian2SerializerFactory实现
        return null;
    }

    @Override
    protected Deserializer getDefaultDeserializer(Class<?> cl) {
        // 返回桥接接口类型，避免类型不匹配
        // 由子类Hessian2SerializerFactory实现
        return null;
    }
}
