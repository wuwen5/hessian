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

import io.github.wuwen5.hessian.io.beans.BasicTypeBean;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CalendarTest extends SerializeTestBase {
    @Test
    void testCustomCalendar() throws IOException {
        BasicTypeBean.MyCalendar myCalendar = new BasicTypeBean.MyCalendar();

        BasicTypeBean.MyCalendar result = baseHessian2Serialize(myCalendar);

        Assertions.assertEquals(myCalendar, result);
    }

    @Test
    void testCalendar() throws IOException {
        Calendar calendar = Calendar.getInstance();
        Calendar result = baseHessian2Serialize(calendar);
        Assertions.assertEquals(calendar, result);
    }

    @Test
    void testJavaBean() throws IOException {

        Calendar calendar = Calendar.getInstance();
        BasicTypeBean.MyCalendar myCalendar = new BasicTypeBean.MyCalendar();
        JavaBean bean = new JavaBean().setCalendar(calendar).setMyCalendar(myCalendar);
        JavaBean result = baseHessian2Serialize(bean);
        Assertions.assertEquals(calendar, result.getCalendar());
        Assertions.assertEquals(myCalendar, result.getMyCalendar());
    }

    @Setter
    @Getter
    @Accessors(chain = true)
    static class JavaBean implements Serializable {
        private Calendar calendar;
        private BasicTypeBean.MyCalendar myCalendar;
        private Calendar nullCalendar;
    }
}
