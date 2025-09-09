package com.alibaba.com.caucho.hessian.io;

import io.github.wuwen5.hessian.io.HessianEncoder;
import java.io.OutputStream;

/**
 * @author wuwen
 */
public class Hessian2Output extends HessianEncoder {
    public Hessian2Output(OutputStream os) {
        super(os);
    }

    public void setSerializerFactory(SerializerFactory serializerFactory) {
        super.setSerializerFactory(serializerFactory);
    }
}
