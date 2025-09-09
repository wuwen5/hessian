package com.alibaba.com.caucho.hessian.io;

import io.github.wuwen5.hessian.io.AbstractHessianEncoder;
import io.github.wuwen5.hessian.io.Serializer;
import java.io.IOException;

/**
 * @author wuwen
 */
public abstract class UnsafeSerializer implements Serializer {

    public static UnsafeSerializer create(Class<?> cl) {
        io.github.wuwen5.hessian.io.UnsafeSerializer unsafeSerializer =
                io.github.wuwen5.hessian.io.UnsafeSerializer.create(cl);
        return new UnsafeSerializer() {
            @Override
            public void writeObject(Object obj, AbstractHessianEncoder out) throws IOException {
                unsafeSerializer.writeObject(obj, out);
            }
        };
    }
}
