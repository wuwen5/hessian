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

package io.github.wuwen5.hessian.io.time;

import io.github.wuwen5.hessian.io.AbstractHessianEncoder;
import io.github.wuwen5.hessian.io.AbstractSerializer;
import java.io.IOException;
import java.time.OffsetDateTime;

public class OffsetDateTimeSerializer extends AbstractSerializer {

    @Override
    public void writeObject(Object obj, AbstractHessianEncoder out) throws IOException {
        if (out.addRef(obj)) {
            return;
        }

        final String className = "java.time.OffsetDateTime";
        OffsetDateTime offsetDateTime = (OffsetDateTime) obj;

        int ref = out.writeObjectBegin(className);

        if (ref == -1) {
            out.writeClassFieldLength(3);

            out.writeString("date");
            out.writeString("time");
            out.writeString("offset");

            out.writeObjectBegin(className);
        }

        out.writeLong(offsetDateTime.toLocalDate().toEpochDay());
        out.writeLong(offsetDateTime.toLocalTime().toNanoOfDay());
        out.writeInt(offsetDateTime.getOffset().getTotalSeconds());
    }
}
