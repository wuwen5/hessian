package io.github.wuwen5.hession.rpc;

/**
 * @author wuwen
 */
public interface BasicAPI {
    String hello(String name);
    int divide(int a, int b) throws ArithmeticException;
}
