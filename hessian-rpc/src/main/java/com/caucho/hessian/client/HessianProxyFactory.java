/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2004 Caucho Technology, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Caucho Technology (http://www.caucho.com/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Hessian", "Resin", and "Caucho" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    info@caucho.com.
 *
 * 5. Products derived from this software may not be called "Resin"
 *    nor may "Resin" appear in their names without prior written
 *    permission of Caucho Technology.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL CAUCHO TECHNOLOGY OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author Scott Ferguson
 */

package com.caucho.hessian.client;

import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.HessianRemoteObject;
import com.caucho.hessian.io.HessianRemoteResolver;
import com.caucho.hessian.io.SerializerFactory;
import com.caucho.services.client.ServiceProxyFactory;
import io.github.wuwen5.hessian.io.HessianDebugInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import lombok.Getter;
import lombok.Setter;

/**
 * Factory for creating Hessian client stubs.  The returned stub will
 * call the remote object for all methods.
 *
 * <pre>
 * String url = "<a href="http://localhost:8080/ejb/hello">...</a>";
 * HelloHome hello = (HelloHome) factory.create(HelloHome.class, url);
 * </pre>
 * <p>
 * After creation, the stub can be like a regular Java class.  Because
 * it makes remote calls, it can throw more exceptions than a Java class.
 * In particular, it may throw protocol exceptions.
 * <p>
 * The factory can also be configured as a JNDI resource.  The factory
 * expects to parameters: "type" and "url", corresponding to the two
 * arguments to <code>create</code>
 * <p>
 * In Resin 3.0, the above example would be configured as:
 * <pre>
 * &lt;reference&gt;
 *   &lt;jndi-name&gt;hessian/hello&lt;/jndi-name&gt;
 *   &lt;factory&gt;com.caucho.hessian.client.HessianProxyFactory&lt;/factory&gt;
 *   &lt;init-param url="http://localhost:8080/ejb/hello"/&gt;
 *   &lt;init-param type="test.HelloHome"/&gt;
 * &lt;/reference&gt;
 * </pre>
 * <p>
 * To get the above resource, use JNDI as follows:
 * <pre>
 * Context ic = new InitialContext();
 * HelloHome hello = (HelloHome) ic.lookup("java:comp/env/hessian/hello");
 *
 * System.out.println("Hello: " + hello.helloWorld());
 * </pre>
 *
 * <h2>Authentication</h2>
 *
 * <p>The proxy can use HTTP basic authentication if the user and the
 * password are set.
 */
@SuppressWarnings("JavadocLinkAsPlainText")
public class HessianProxyFactory implements ServiceProxyFactory, ObjectFactory {

    private final ClassLoader loader;

    /**
     * -- SETTER --
     *  Sets the serializer factory.
     */
    @Setter
    private SerializerFactory serializerFactory;

    private HessianConnectionFactory connFactory;

    private final HessianRemoteResolver resolver;

    private String user;
    private String password;
    private String basicAuth;

    @Getter
    @Setter
    private boolean overloadEnabled;

    @Getter
    @Setter
    private boolean chunkedPost = true;

    @Getter
    @Setter
    private boolean debug;

    @Getter
    @Setter
    private long readTimeout = -1;

    @Getter
    @Setter
    private long connectTimeout = -1;

    /**
     * Creates the new proxy factory.
     */
    public HessianProxyFactory() {
        this(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Creates the new proxy factory.
     */
    public HessianProxyFactory(ClassLoader loader) {
        this.loader = loader;
        resolver = new HessianProxyResolver(this);
    }

    /**
     * Sets the user.
     */
    public void setUser(String user) {
        this.user = user;
        basicAuth = null;
    }

    /**
     * Sets the password.
     */
    public void setPassword(String password) {
        this.password = password;
        basicAuth = null;
    }

    public String getBasicAuth() {
        if (basicAuth != null) {
            return basicAuth;
        } else if (user != null && password != null) {
            return "Basic "
                    + Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
        } else {
            return null;
        }
    }

    /**
     * Sets the connection factory to use when connecting
     * to the Hessian service.
     */
    public void setConnectionFactory(HessianConnectionFactory factory) {
        connFactory = factory;
    }

    /**
     * Returns the connection factory to be used for the HTTP request.
     */
    public HessianConnectionFactory getConnectionFactory() {
        if (connFactory == null) {
            connFactory = createHessianConnectionFactory();
            connFactory.setHessianProxyFactory(this);
        }

        return connFactory;
    }

    /**
     * Returns the remote resolver.
     */
    public HessianRemoteResolver getRemoteResolver() {
        return resolver;
    }

    /**
     * Gets the serializer factory.
     */
    public SerializerFactory getSerializerFactory() {
        if (serializerFactory == null) {
            serializerFactory = new SerializerFactory(loader);
        }

        return serializerFactory;
    }

    protected HessianConnectionFactory createHessianConnectionFactory() {
        String className = System.getProperty(HessianConnectionFactory.class.getName());

        HessianConnectionFactory factory;

        try {
            if (className != null) {
                ClassLoader loader = Thread.currentThread().getContextClassLoader();

                Class<?> cl = Class.forName(className, false, loader);

                factory = (HessianConnectionFactory) cl.getDeclaredConstructor().newInstance();

                return factory;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new HessianURLConnectionFactory();
    }

    /**
     * Creates a new proxy with the specified URL.  The API class uses
     * the java.api.class value from _hessian_
     *
     * @param url the URL where the client object is located.
     * @return a proxy to the object with the specified interface.
     */
    public Object create(String url) throws MalformedURLException, ClassNotFoundException {
        HessianMetaInfoAPI metaInfo;

        metaInfo = (HessianMetaInfoAPI) create(HessianMetaInfoAPI.class, url);

        String apiClassName = (String) metaInfo._hessian_getAttribute("java.api.class");

        if (apiClassName == null) {
            throw new HessianRuntimeException(url + " has an unknown api.");
        }

        Class<?> apiClass = Class.forName(apiClassName, false, loader);

        return create(apiClass, url);
    }

    /**
     * Creates a new proxy with the specified URL.  The returned object
     * is a proxy with the interface specified by api.
     *
     * <pre>
     * String url = "http://localhost:8080/ejb/hello");
     * HelloHome hello = (HelloHome) factory.create(HelloHome.class, url);
     * </pre>
     *
     * @param api     the interface the proxy class needs to implement
     * @param urlName the URL where the client object is located.
     * @return a proxy to the object with the specified interface.
     */
    public Object create(Class api, String urlName) throws MalformedURLException {
        return create(api, urlName, loader);
    }

    /**
     * Creates a new proxy with the specified URL.  The returned object
     * is a proxy with the interface specified by api.
     *
     * <pre>
     * String url = "http://localhost:8080/ejb/hello");
     * HelloHome hello = (HelloHome) factory.create(HelloHome.class, url);
     * </pre>
     *
     * @param api     the interface the proxy class needs to implement
     * @param urlName the URL where the client object is located.
     * @return a proxy to the object with the specified interface.
     */
    public Object create(Class<?> api, String urlName, ClassLoader loader) throws MalformedURLException {
        URL url = new URL(urlName);

        return create(api, url, loader);
    }

    /**
     * Creates a new proxy with the specified URL.  The returned object
     * is a proxy with the interface specified by api.
     *
     * <pre>
     * String url = "http://localhost:8080/ejb/hello");
     * HelloHome hello = (HelloHome) factory.create(HelloHome.class, url);
     * </pre>
     *
     * @param api the interface the proxy class needs to implement
     * @param url the URL where the client object is located.
     * @return a proxy to the object with the specified interface.
     */
    public Object create(Class<?> api, URL url, ClassLoader loader) {
        if (api == null) {
            throw new NullPointerException("api must not be null for HessianProxyFactory.create()");
        }
        InvocationHandler handler = null;

        handler = new HessianProxy(url, this, api);

        return Proxy.newProxyInstance(loader, new Class[] {api, HessianRemoteObject.class}, handler);
    }

    public AbstractHessianInput getHessianInput(InputStream is) {
        return getHessian2Input(is);
    }

    public AbstractHessianInput getHessian2Input(InputStream is) {
        AbstractHessianInput in;

        if (debug) {
            is = new HessianDebugInputStream(is, new PrintWriter(System.out));
        }

        in = new Hessian2Input(is);

        in.setRemoteResolver(getRemoteResolver());

        in.setSerializerFactory(getSerializerFactory());

        return in;
    }

    public AbstractHessianOutput getHessianOutput(OutputStream os) {
        AbstractHessianOutput out = new Hessian2Output(os);

        out.setSerializerFactory(getSerializerFactory());

        return out;
    }

    /**
     * JNDI object factory so the proxy can be used as a resource.
     */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
            throws Exception {
        Reference ref = (Reference) obj;

        String api = null;
        String url = null;

        for (int i = 0; i < ref.size(); i++) {
            RefAddr addr = ref.get(i);

            String type = addr.getType();
            String value = (String) addr.getContent();

            if ("type".equals(type)) {
                api = value;
            } else if ("url".equals(type)) {
                url = value;
            } else if ("user".equals(type)) {
                setUser(value);
            } else if ("password".equals(type)) {
                setPassword(value);
            }
        }

        if (url == null) {
            throw new NamingException("`url' must be configured for HessianProxyFactory.");
        }
        // XXX: could use meta protocol to grab this
        if (api == null) {
            throw new NamingException("`type' must be configured for HessianProxyFactory.");
        }

        Class<?> apiClass = Class.forName(api, false, loader);

        return create(apiClass, url);
    }
}
