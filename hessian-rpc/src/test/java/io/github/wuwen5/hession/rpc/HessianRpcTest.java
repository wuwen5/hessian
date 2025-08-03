package io.github.wuwen5.hession.rpc;

import com.caucho.hessian.client.HessianProxyFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author wuwen
 */
public class HessianRpcTest {

    static Server server;
    static int port = 8081;
    static String url = "http://localhost:" + port + "/hello";

    @BeforeAll
    public static void startServer() throws Exception {
        server = new Server(port);
        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(new ServletHolder(new BasicService()), "/hello");
        server.setHandler(handler);
        server.start();
    }

    @AfterAll
    public static void stopServer() throws Exception {
        if (server != null) server.stop();
    }

    @Test
    void testHello() throws IOException {

        HessianProxyFactory factory = new HessianProxyFactory();
        BasicAPI client = (BasicAPI) factory.create(BasicAPI.class, url);

        String result = client.hello("test");
        assertEquals("Hello test", result);
    }

    @Test
    void testDivideByZero() throws MalformedURLException {
        HessianProxyFactory factory = new HessianProxyFactory();
        BasicAPI client = (BasicAPI) factory.create(BasicAPI.class, url);

        // tes exception handling for division by zero
        ArithmeticException exception = assertThrows(ArithmeticException.class,
                () -> client.divide(10, 0));

        assertEquals("Division by zero", exception.getMessage());
    }

}
