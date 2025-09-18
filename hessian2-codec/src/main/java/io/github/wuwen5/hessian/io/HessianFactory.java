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

import io.github.wuwen5.hessian.util.HessianFreeList;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory for creating HessianInput and HessianOutput streams.
 */
@Slf4j
public class HessianFactory {

    @Setter
    private Hessian2SerializerFactory serializerFactory;

    private final Hessian2SerializerFactory defaultSerializerFactory;

    protected final HessianFreeList<HessianEncoder> freeHessian2Output = new HessianFreeList<>(32);

    protected final HessianFreeList<HessianDecoder> freeHessian2Input = new HessianFreeList<>(32);

    public HessianFactory() {
        defaultSerializerFactory = Hessian2SerializerFactory.createDefault();
        serializerFactory = defaultSerializerFactory;
    }

    public Hessian2SerializerFactory getSerializerFactory() {
        // the default serializer factory cannot be modified by external
        // callers
        if (serializerFactory == defaultSerializerFactory) {
            serializerFactory = new Hessian2SerializerFactory();
        }

        return serializerFactory;
    }

    /**
     * Enable whitelist deserialization mode. Only classes matching the whitelist
     * will be allowed.
     */
    public void setWhitelist(boolean isWhitelist) {
        getSerializerFactory().getClassFactory().setWhitelist(isWhitelist);
    }

    /**
     * Allow a class or package based on a pattern.
     * <p>
     * Examples: "java.util.*", "com.foo.io.Bean"
     */
    public void allow(String pattern) {
        getSerializerFactory().getClassFactory().allow(pattern);
    }

    /**
     * Deny a class or package based on a pattern.
     * <p>
     * Examples: "java.util.*", "com.foo.io.Bean"
     */
    public void deny(String pattern) {
        getSerializerFactory().getClassFactory().deny(pattern);
    }

    /**
     * Creates a new Hessian 2.0 deserializer.
     */
    public HessianDecoder createHessian2Input(InputStream is) {
        HessianDecoder in = freeHessian2Input.allocate();

        if (in == null) {
            in = new HessianDecoder(is);
            in.setSerializerFactory(getSerializerFactory());
        } else {
            in.init(is);
        }

        return in;
    }

    /**
     * Frees a Hessian 2.0 deserializer
     */
    public void freeHessian2Input(HessianDecoder in) {
        if (in == null) {
            return;
        }

        in.free();

        freeHessian2Input.free(in);
    }

    /**
     * Creates a new Hessian 2.0 deserializer.
     */
    public Hessian2StreamingInput createHessian2StreamingInput(InputStream is) {
        Hessian2StreamingInput in = new Hessian2StreamingInput(is);
        in.setSerializerFactory(getSerializerFactory());

        return in;
    }

    /**
     * Frees a Hessian 2.0 deserializer
     */
    public void freeHessian2StreamingInput(Hessian2StreamingInput in) {}

    /**
     * Creates a new Hessian 2.0 serializer.
     */
    public HessianEncoder createHessian2Output(OutputStream os) {
        HessianEncoder out = createHessian2Output();

        out.init(os);

        return out;
    }

    /**
     * Creates a new Hessian 2.0 serializer.
     */
    public HessianEncoder createHessian2Output() {
        HessianEncoder out = freeHessian2Output.allocate();

        if (out == null) {
            out = new HessianEncoder();

            out.setSerializerFactory(getSerializerFactory());
        }

        return out;
    }

    /**
     * Frees a Hessian 2.0 serializer
     */
    public void freeHessian2Output(HessianEncoder out) {
        if (out == null) {
            return;
        }

        out.free();

        freeHessian2Output.free(out);
    }

    /**
     * Creates a new Hessian 2.0 serializer.
     */
    public Hessian2StreamingOutput createHessian2StreamingOutput(OutputStream os) {
        HessianEncoder out = createHessian2Output(os);

        return new Hessian2StreamingOutput(out);
    }

    /**
     * Frees a Hessian 2.0 serializer
     */
    public void freeHessian2StreamingOutput(Hessian2StreamingOutput out) {
        if (out == null) {
            return;
        }

        freeHessian2Output(out.getHessian2Output());
    }

    public OutputStream createHessian2DebugOutput(OutputStream os, Consumer<String> log) {
        HessianDebugOutputStream out = new HessianDebugOutputStream(os, log);

        out.startTop2();

        return out;
    }
}
