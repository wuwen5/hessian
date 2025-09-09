package com.alibaba.com.caucho.hessian.io;

import io.github.wuwen5.hessian.io.UnsafeDeserializer;
import io.github.wuwen5.hessian.io.UnsafeSerializer;

public class SerializerFactory extends io.github.wuwen5.hessian.io.SerializerFactory {

    protected Serializer _defaultSerializer = (Serializer) defaultSerializer;

    private io.github.wuwen5.hessian.io.SerializerFactory delegate;

    public SerializerFactory() {}

    public SerializerFactory(ClassLoader cl) {
        super(cl);
    }

    public boolean isEnableUnsafeSerializer() {
        return (UnsafeSerializer.isEnabled() && UnsafeDeserializer.isEnabled());
    }

    public SerializerFactory(io.github.wuwen5.hessian.io.SerializerFactory serializerFactory) {
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
