package io.github.wuwen5.hession.rpc;

import com.caucho.hessian.server.HessianServlet;


/**
 * @author wuwen
 */
public class BasicService extends HessianServlet implements BasicAPI {

    public String hello(String name)
    {
        return "Hello " + name;
    }

    @Override
    public int divide(int a, int b) throws ArithmeticException {
        if (b == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return a / b;
    }
}
