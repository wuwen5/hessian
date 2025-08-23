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

package io.github.wuwen5.hessian.io;

import java.io.IOException;
import java.util.*;

/**
 * Deserializing a JDK 1.2 Collection.
 */
public class CollectionDeserializer extends AbstractListDeserializer {
    private final Class<?> type;

    public CollectionDeserializer(Class<?> type) {
        this.type = type;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public Object readList(AbstractHessianDecoder in, int length) throws IOException {
        Collection<Object> list = createList();

        in.addRef(list);

        while (!in.isEnd()) {
            list.add(in.readObject());
        }

        in.readEnd();

        return list;
    }

    @Override
    public Object readLengthList(AbstractHessianDecoder in, int length) throws IOException {
        Collection<Object> list = createList();

        in.addRef(list);

        for (; length > 0; length--) {
            list.add(in.readObject());
        }

        return list;
    }

    private Collection<Object> createList() throws IOException {
        Collection<Object> list = null;

        if (type == null) {
            list = new ArrayList<>();
        } else if (!type.isInterface()) {
            try {
                list = (Collection) type.getDeclaredConstructor().newInstance();
            } catch (Exception ignored) {
            }
        }

        if (list == null) {
            if (SortedSet.class.isAssignableFrom(type)) {
                list = new TreeSet<>();
            } else if (Set.class.isAssignableFrom(type)) {
                list = new HashSet<>();
            } else if (List.class.isAssignableFrom(type)) {
                list = new ArrayList<>();
            } else if (Collection.class.isAssignableFrom(type)) {
                list = new ArrayList<>();
            } else {
                try {
                    list = (Collection) type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new IOExceptionWrapper(e);
                }
            }
        }

        return list;
    }

    public String toString() {
        return getClass().getSimpleName() + "[" + type + "]";
    }
}
