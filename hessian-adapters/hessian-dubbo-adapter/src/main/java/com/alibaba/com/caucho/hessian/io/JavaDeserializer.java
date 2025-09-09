package com.alibaba.com.caucho.hessian.io;

import io.github.wuwen5.hessian.io.FieldDeserializer2Factory;

/**
 * @author wuwen
 */
public class JavaDeserializer extends io.github.wuwen5.hessian.io.JavaDeserializer implements Deserializer {
    public JavaDeserializer(Class<?> cl, FieldDeserializer2Factory fieldFactory) {
        super(cl, fieldFactory);
    }

    public JavaDeserializer(Class<?> cl) {
        this(cl, FieldDeserializer2Factory.create());
    }
}
