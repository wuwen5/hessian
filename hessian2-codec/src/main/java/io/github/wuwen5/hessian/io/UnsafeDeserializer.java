/*
 * Copyright (c) 2001-2008 Caucho Technology, Inc.  All rights reserved.
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

import java.lang.reflect.Field;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import sun.misc.Unsafe;

/**
 * Serializing an object for known object types.
 */
@Slf4j
public class UnsafeDeserializer extends AbstractFieldDeserializer {

    @Getter
    private static boolean isEnabled;

    @SuppressWarnings("restriction")
    private static Unsafe unsafe;

    public UnsafeDeserializer(Class<?> cl, FieldDeserializer2Factory fieldFactory) {
        super(cl, fieldFactory);
    }

    @Override
    @SuppressWarnings("restriction")
    protected Object instantiate() throws InstantiationException {
        return unsafe.allocateInstance(type);
    }

    static {
        boolean isEnabled = false;

        try {
            Class<?> unsafe = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = null;
            for (Field field : unsafe.getDeclaredFields()) {
                if ("theUnsafe".equals(field.getName())) {
                    theUnsafe = field;
                }
            }

            if (theUnsafe != null) {
                theUnsafe.setAccessible(true);
                UnsafeDeserializer.unsafe = (Unsafe) theUnsafe.get(null);
            }

            isEnabled = UnsafeDeserializer.unsafe != null;

            String unsafeProp = System.getProperty("com.caucho.hessian.unsafe");

            if ("false".equals(unsafeProp)) {
                isEnabled = false;
            }
        } catch (Throwable e) {
            log.trace(e.toString(), e);
        }

        UnsafeDeserializer.isEnabled = isEnabled;
    }
}
