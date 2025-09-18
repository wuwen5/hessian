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

package com.caucho.hessian.io;

import io.github.wuwen5.hessian.io.AbstractHessianDecoder;
import io.github.wuwen5.hessian.io.Hessian2SerializerFactory;
import io.github.wuwen5.hessian.io.HessianFactory;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HessianInputFactory {

    private final HessianFactory factory = new HessianFactory();

    public void setSerializerFactory(Hessian2SerializerFactory factory) {
        this.factory.setSerializerFactory(factory);
    }

    public Hessian2SerializerFactory getSerializerFactory() {
        return factory.getSerializerFactory();
    }

    public HeaderType readHeader(InputStream is) throws IOException {
        int code = is.read();

        int major = is.read();
        int minor = is.read();

        log.debug("Hessian header: code={}, major={}, minor={}", (char) code, major, minor);

        switch (code) {
            case -1:
                throw new IOException("Unexpected end of file for Hessian message");
            case 'H':
                return HeaderType.HESSIAN_2;
            default:
                throw new IOException(
                        (char) code + " 0x" + Integer.toHexString(code) + " is an unknown Hessian2 message code.");
        }
    }

    public AbstractHessianDecoder open(InputStream is) throws IOException {
        int code = is.read();

        int major = is.read();
        int minor = is.read();
        log.debug("Hessian header: code={}, major={}, minor={}", (char) code, major, minor);

        switch (code) {
            case 'c':
            case 'C':
            case 'r':
            case 'R':
                if (major >= 2) {
                    return factory.createHessian2Input(is);
                } else {
                    throw new IOException("major version " + major + " is not supported for Hessian 2.0 messages");
                }

            default:
                throw new IOException((char) code + " is an unknown Hessian message code.");
        }
    }

    public enum HeaderType {
        HESSIAN_2,
        REPLY_2;
    }
}
