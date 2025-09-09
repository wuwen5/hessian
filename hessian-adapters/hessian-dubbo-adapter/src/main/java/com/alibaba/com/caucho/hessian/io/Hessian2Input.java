package com.alibaba.com.caucho.hessian.io;

import io.github.wuwen5.hessian.io.HessianDecoder;
import java.io.InputStream;

/**
 * @author wuwen
 */
public class Hessian2Input extends HessianDecoder {
    public Hessian2Input(InputStream is) {
        super(is);
    }

    public void setSerializerFactory(SerializerFactory serializerFactory) {
        super.setSerializerFactory(serializerFactory);
    }

    @Override
    public SerializerFactory getSerializerFactory() {
        return new SerializerFactory(super.getSerializerFactory());
    }
}
