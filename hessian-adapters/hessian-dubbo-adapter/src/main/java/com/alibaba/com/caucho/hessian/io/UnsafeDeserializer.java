package com.alibaba.com.caucho.hessian.io;

/**
 * @author wuwen
 */
public class UnsafeDeserializer extends io.github.wuwen5.hessian.io.UnsafeDeserializer implements Deserializer {
    public UnsafeDeserializer(Class<?> cl, FieldDeserializer2Factory fieldFactory) {
        super(cl, fieldFactory);
    }
}
