/*
 * Copyright (c) 2001-2004 Caucho Technology, Inc.  All rights reserved.
 *
 * The Apache Software License, Version 1.1
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
 * 4. The names "Burlap", "Resin", and "Caucho" must not be used to
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

package io.github.wuwen5.hessian.io;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.logging.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Serializing an object for known object types.
 */
@Slf4j
public class BeanSerializer extends AbstractSerializer {

    private final Method[] methods;
    private final String[] names;

    private Object writeReplaceFactory;
    private Method writeReplace;

    public BeanSerializer(Class<?> cl, ClassLoader loader) {
        introspectWriteReplace(cl, loader);

        List<Method> primitiveMethods = new ArrayList<>();
        List<Method> compoundMethods = new ArrayList<>();

        for (; cl != null; cl = cl.getSuperclass()) {
            Method[] methods = cl.getDeclaredMethods();

            for (Method method : methods) {
                if (Modifier.isStatic(method.getModifiers())) {
                    continue;
                }

                if (method.getParameterTypes().length != 0) {
                    continue;
                }

                String name = method.getName();

                if (!name.startsWith("get")) {
                    continue;
                }

                Class<?> type = method.getReturnType();

                if (type.equals(void.class)) {
                    continue;
                }

                if (findSetter(methods, name, type) == null) {
                    continue;
                }

                // XXX: could parameterize the handler to only deal with public
                method.setAccessible(true);

                if (type.isPrimitive() || type.getName().startsWith("java.lang.") && !type.equals(Object.class)) {
                    primitiveMethods.add(method);
                } else {
                    compoundMethods.add(method);
                }
            }
        }

        List<Method> methodList = new ArrayList<>();
        methodList.addAll(primitiveMethods);
        methodList.addAll(compoundMethods);

        methodList.sort(new MethodNameCmp());

        methods = new Method[methodList.size()];
        methodList.toArray(methods);

        names = new String[methods.length];

        for (int i = 0; i < methods.length; i++) {
            String name = methods[i].getName();

            name = name.substring(3);

            int j = 0;
            for (; j < name.length() && Character.isUpperCase(name.charAt(j)); j++) {}

            if (j == 1) {
                name = name.substring(0, j).toLowerCase(Locale.ENGLISH) + name.substring(j);
            } else if (j > 1) {
                name = name.substring(0, j - 1).toLowerCase(Locale.ENGLISH) + name.substring(j - 1);
            }

            names[i] = name;
        }
    }

    private void introspectWriteReplace(Class<?> cl, ClassLoader loader) {
        try {
            String className = cl.getName() + "HessianSerializer";

            Class<?> serializerClass = Class.forName(className, false, loader);

            Object serializerObject = serializerClass.newInstance();

            Method writeReplace = getWriteReplace(serializerClass, cl);

            if (writeReplace != null) {
                writeReplaceFactory = serializerObject;
                this.writeReplace = writeReplace;

                return;
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Exception e) {
            log.trace(e.toString(), e);
        }

        writeReplace = getWriteReplace(cl);
    }

    /**
     * Returns the writeReplace method
     */
    protected Method getWriteReplace(Class<?> cl) {
        for (; cl != null; cl = cl.getSuperclass()) {
            Method[] methods = cl.getDeclaredMethods();

            for (Method method : methods) {
                if ("writeReplace".equals(method.getName()) && method.getParameterTypes().length == 0) {
                    return method;
                }
            }
        }

        return null;
    }

    /**
     * Returns the writeReplace method
     */
    protected Method getWriteReplace(Class<?> cl, Class<?> param) {
        for (; cl != null; cl = cl.getSuperclass()) {
            for (Method method : cl.getDeclaredMethods()) {
                if ("writeReplace".equals(method.getName())
                        && method.getParameterTypes().length == 1
                        && param.equals(method.getParameterTypes()[0])) {
                    return method;
                }
            }
        }

        return null;
    }

    @Override
    public void writeObject(Object obj, AbstractHessianEncoder out) throws IOException {
        if (out.addRef(obj)) {
            return;
        }

        Class<?> cl = obj.getClass();

        try {
            if (writeReplace != null) {
                Object repl;

                if (writeReplaceFactory != null) {
                    repl = writeReplace.invoke(writeReplaceFactory, obj);
                } else {
                    repl = writeReplace.invoke(obj);
                }

                out.writeObject(repl);

                out.replaceRef(repl, obj);

                return;
            }
        } catch (Exception e) {
            log.trace(e.toString(), e);
        }

        int ref = out.writeObjectBegin(cl.getName());

        if (ref < -1) {
            // Hessian 1.1 uses a map

            for (int i = 0; i < methods.length; i++) {
                Object value = null;

                try {
                    value = methods[i].invoke(obj, (Object[]) null);
                } catch (Exception e) {
                    log.debug(e.toString(), e);
                }

                out.writeString(names[i]);

                out.writeObject(value);
            }

            out.writeMapEnd();
        } else {
            if (ref == -1) {
                out.writeInt(names.length);

                for (String name : names) {
                    out.writeString(name);
                }

                out.writeObjectBegin(cl.getName());
            }

            for (Method method : methods) {
                Object value = null;

                try {
                    value = method.invoke(obj, (Object[]) null);
                } catch (Exception e) {
                    log.trace(e.toString(), e);
                }

                out.writeObject(value);
            }
        }
    }

    /**
     * Finds any matching setter.
     */
    private Method findSetter(Method[] methods, String getterName, Class<?> arg) {
        String setterName = "set" + getterName.substring(3);

        for (Method method : methods) {
            if (method.getName().equals(setterName) && method.getReturnType().equals(void.class)) {
                Class<?>[] params = method.getParameterTypes();

                if (params.length == 1 && params[0].equals(arg)) {
                    return method;
                }
            }
        }

        return null;
    }

    static class MethodNameCmp implements Comparator<Method> {
        @Override
        public int compare(Method a, Method b) {
            return a.getName().compareTo(b.getName());
        }
    }
}
