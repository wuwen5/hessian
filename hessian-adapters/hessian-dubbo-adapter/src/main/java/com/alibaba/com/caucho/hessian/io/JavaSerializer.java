package com.alibaba.com.caucho.hessian.io;

/**
 * @author wuwen
 */
public class JavaSerializer extends io.github.wuwen5.hessian.io.JavaSerializer implements Serializer {

    public JavaSerializer(Class<?> cl) {
        super(cl);
    }

    public JavaSerializer(Class<?> cl, ClassLoader loader) {
        super(cl);
    }

    public static Serializer create(Class<?> cl) {
        return io.github.wuwen5.hessian.io.JavaSerializer.create(cl)::writeObject;
    }
}
