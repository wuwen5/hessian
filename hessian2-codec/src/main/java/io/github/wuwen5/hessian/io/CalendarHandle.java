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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Handle for a calendar object.
 */
public class CalendarHandle implements java.io.Serializable, HessianHandle {
    private Class<?> type;
    private final Date date;

    public CalendarHandle(Class<?> type, long time) {
        if (!GregorianCalendar.class.equals(type)) {
            this.type = type;
        }

        this.date = new Date(time);
    }

    Object readResolve() {
        try {
            Calendar cal;

            if (this.type != null) {
                cal = (Calendar) this.type.getDeclaredConstructor().newInstance();
            } else {
                cal = new GregorianCalendar();
            }

            cal.setTimeInMillis(this.date.getTime());

            return cal;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to create Calendar instance for type: " + type, e);
        }
    }
}
