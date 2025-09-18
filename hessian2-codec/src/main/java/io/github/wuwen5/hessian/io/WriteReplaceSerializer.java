/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.wuwen5.hessian.io;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;

/**
 * Serializing an object for known object types.
 */
@Slf4j
public class WriteReplaceSerializer extends AbstractSerializer {
    private Object writeReplaceFactory;
    private Method writeReplace;
    private final HessianSerializer baseSerializer;

    public WriteReplaceSerializer(Class<?> cl, ClassLoader loader, HessianSerializer baseSerializer) {
        introspectWriteReplace(cl, loader);

        this.baseSerializer = baseSerializer;
    }

    private void introspectWriteReplace(Class<?> cl, ClassLoader loader) {
        try {
            String className = cl.getName() + "HessianSerializer";

            Class<?> serializerClass = Class.forName(className, false, loader);

            Object serializerObject = serializerClass.getDeclaredConstructor().newInstance();

            Method writeReplaceMethod = getWriteReplace(serializerClass, cl);

            if (writeReplaceMethod != null) {
                writeReplaceFactory = serializerObject;
                this.writeReplace = writeReplaceMethod;
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Exception e) {
            log.trace(e.toString(), e);
        }

        writeReplace = getWriteReplace(cl);
        if (writeReplace != null) {
            writeReplace.setAccessible(true);
        }
    }

    /**
     * Returns the writeReplace method
     */
    protected static Method getWriteReplace(Class<?> cl, Class<?> param) {
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

    /**
     * Returns the writeReplace method
     */
    protected static Method getWriteReplace(Class<?> cl) {
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

    @Override
    public void writeObject(Object obj, AbstractHessianEncoder out) throws IOException {
        int ref = out.getRef(obj);

        if (ref >= 0) {
            out.writeRef(ref);

            return;
        }

        Object repl;

        repl = writeReplace(obj);

        if (obj == repl) {
            if (log.isDebugEnabled()) {
                log.debug(
                        "{}: Hessian writeReplace error.  The writeReplace method ({}) must not return the same object: {}",
                        this,
                        writeReplace,
                        obj);
            }

            baseSerializer.writeObject(obj, out);

            return;
        }

        out.writeObject(repl);

        out.replaceRef(repl, obj);
    }

    @Override
    protected Object writeReplace(Object obj) {
        try {
            if (writeReplaceFactory != null) {
                return writeReplace.invoke(writeReplaceFactory, obj);
            } else {
                return writeReplace.invoke(obj);
            }
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getCause());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
