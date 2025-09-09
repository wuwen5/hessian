package com.alibaba.com.caucho.hessian.io;

/**
 * @author wuwen
 */
public class ClassFactory extends io.github.wuwen5.hessian.io.ClassFactory {

    private final io.github.wuwen5.hessian.io.ClassFactory delegate;

    public ClassFactory(io.github.wuwen5.hessian.io.ClassFactory factory) {
        super(null);
        this.delegate = factory;
    }

    @Override
    public void allow(String pattern) {
        delegate.allow(pattern);
    }

    @Override
    public Class<?> load(String className) throws ClassNotFoundException {
        return delegate.load(className);
    }

    @Override
    public void setWhitelist(boolean isWhitelist) {
        delegate.setWhitelist(isWhitelist);
    }

    @Override
    public void deny(String pattern) {
        delegate.deny(pattern);
    }
}
