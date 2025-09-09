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
package io.github.wuwen5.hessian.test.dubbo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.serialize.ObjectInput;
import org.apache.dubbo.common.serialize.ObjectOutput;
import org.apache.dubbo.common.serialize.Serialization;
import org.apache.dubbo.rpc.model.FrameworkModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Hessian2SerializationTest {

    @Test
    void testReadWriteObj() throws IOException, ClassNotFoundException {
        FrameworkModel frameworkModel = new FrameworkModel();
        Serialization serialization =
                frameworkModel.getExtensionLoader(Serialization.class).getExtension("hessian2");
        URL url = URL.valueOf("").setScopeModel(frameworkModel);

        BasicTypeBean basicTypeBean = BasicTypeBean.create();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutput objectOutput = serialization.serialize(url, outputStream);
        objectOutput.writeObject(basicTypeBean);
        objectOutput.flushBuffer();

        byte[] bytes = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        ObjectInput objectInput = serialization.deserialize(url, inputStream);
        Assertions.assertEquals(basicTypeBean, objectInput.readObject());

        basicTypeBean = BasicTypeBean.create();
        outputStream = new ByteArrayOutputStream();
        objectOutput = serialization.serialize(url, outputStream);
        objectOutput.writeObject(basicTypeBean);
        objectOutput.flushBuffer();

        bytes = outputStream.toByteArray();
        inputStream = new ByteArrayInputStream(bytes);
        objectInput = serialization.deserialize(url, inputStream);
        Assertions.assertEquals(basicTypeBean, objectInput.readObject());

        frameworkModel.destroy();
    }
}
