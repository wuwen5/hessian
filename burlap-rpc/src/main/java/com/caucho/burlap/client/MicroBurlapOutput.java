/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2004 Caucho Technology, Inc.  All rights reserved.
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

package com.caucho.burlap.client;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

/**
 * Output stream for Burlap requests, compatible with microedition
 * Java.  It only uses classes and types available to J2ME.  In
 * particular, it does not have any support for the &lt;double> type.
 *
 * <p>MicroBurlapOutput does not depend on any classes other than
 * in J2ME, so it can be extracted independently into a smaller package.
 *
 * <p>MicroBurlapOutput is unbuffered, so any client needs to provide
 * its own buffering.
 *
 * <pre>
 * OutputStream os = ...; // from http connection
 * MicroBurlapOutput out = new MicroBurlapOutput(os);
 * String value;
 *
 * out.startCall("hello");  // start hello call
 * out.writeString("arg1"); // write a string argument
 * out.completeCall();      // complete the call
 * </pre>
 */
public class MicroBurlapOutput {
    private OutputStream os;
    private Date date;
    private Calendar utcCalendar;
    private Calendar localCalendar;

    /**
     * Creates a new Burlap output stream, initialized with an
     * underlying output stream.
     *
     * @param os the underlying output stream.
     */
    public MicroBurlapOutput(OutputStream os) {
        init(os);
    }

    /**
     * Creates an uninitialized Burlap output stream.
     */
    public MicroBurlapOutput() {}

    public void init(OutputStream os) {
        this.os = os;
    }

    /**
     * Writes a complete method call.
     */
    public void call(String method, Object[] args) throws IOException {
        startCall(method);

        if (args != null) {
            for (Object arg : args) {
                writeObject(arg);
            }
        }

        completeCall();
    }

    /**
     * Writes the method call:
     *<pre>
     * &lt;burlap:request&gt;
     *   &lt;method&gt;add&lt;/method&gt;
     * </pre>
     * @param method the method name to call.
     */
    public void startCall(String method) throws IOException {
        print("<burlap:call><method>");
        print(method);
        print("</method>");
    }

    /**
     * Writes the method call:
     *<pre>
     * &lt;/burlap:request&gt;
     * </pre>
     */
    public void completeCall() throws IOException {
        print("</burlap:call>");
    }

    /**
     * Writes a boolean value to the stream.  The boolean will be written
     * with the following syntax:
     *<pre>
     * &lt;boolean&gt;1&lt;/boolean&gt;
     * </pre>
     * @param value the boolean value to write.
     */
    public void writeBoolean(boolean value) throws IOException {
        print("<boolean>");
        printInt(value ? 1 : 0);
        print("</boolean>");
    }

    /**
     * Writes an integer value to the stream.  The integer will be written
     * with the following syntax:
     *<pre>
     * &lt;int&gt;123&lt;/int&gt;
     * </pre>
     * @param value the integer value to write.
     */
    public void writeInt(int value) throws IOException {
        print("<int>");
        printInt(value);
        print("</int>");
    }

    /**
     * Writes a long value to the stream.  The long will be written
     * with the following syntax:
     *<pre>
     * &lt;long&gt;123&lt;/long&gt;
     * </pre>
     * @param value the long value to write.
     */
    public void writeLong(long value) throws IOException {
        print("<long>");
        printLong(value);
        print("</long>");
    }

    /**
     * Writes a null value to the stream.
     * The null will be written with the following syntax
     *<pre>
     * &lt;null&gt;&lt;/null&gt;
     * </pre>
     */
    public void writeNull() throws IOException {
        print("<null></null>");
    }

    /**
     * Writes a string value to the stream using UTF-8 encoding.
     * The string will be written with the following syntax:
     *<pre>
     * &lt;string&gt;12.3e10&lt;/string&gt;
     * </pre>
     * If the value is null, it will be written as
     *<pre>
     * &lt;null&gt;&lt;/null&gt;
     * </pre>
     *
     * @param value the string value to write.
     */
    public void writeString(String value) throws IOException {
        if (value == null) {
            print("<null></null>");
        } else {
            print("<string>");
            printString(value);
            print("</string>");
        }
    }

    /**
     * Writes a byte array to the stream using base64 encoding.
     * The array will be written with the following syntax:
     *<pre>
     * &lt;base64&gt;dJmO==&lt;/base64&gt;
     * </pre>
     * If the value is null, it will be written as
     *<pre>
     * &lt;null&gt;&lt;/null&gt;
     * </pre>
     */
    public void writeBytes(byte[] buffer, int offset, int length) throws IOException {
        if (buffer == null) {
            print("<null></null>");
        } else {
            print("<base64>");
            printBytes(buffer, offset, length);
            print("</base64>");
        }
    }

    /**
     * Writes a date to the stream using ISO8609.
     *<pre>
     * &lt;date&gt;19980508T095131Z&lt;/date&gt;
     * </pre>
     * @param time the date in milliseconds from the epoch in UTC timezone
     */
    public void writeUTCDate(long time) throws IOException {
        print("<date>");
        if (utcCalendar == null) {
            utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            date = new Date();
        }

        date.setTime(time);
        utcCalendar.setTime(date);

        printDate(utcCalendar);
        print("</date>");
    }

    /**
     * Writes a date to the stream using ISO8609.
     *<pre>
     * &lt;date&gt;19980508T095131Z&lt;/date&gt;
     * </pre>
     */
    public void writeLocalDate(long time) throws IOException {
        print("<date>");
        if (localCalendar == null) {
            localCalendar = Calendar.getInstance();
            date = new Date();
        }

        date.setTime(time);
        localCalendar.setTime(date);

        printDate(localCalendar);
        print("</date>");
    }

    /**
     * Writes a reference.
     *<pre>
     * &lt;ref&gt;123&lt;/ref&gt;
     * </pre>
     * @param value the integer value to write.
     */
    public void writeRef(int value) throws IOException {
        print("<ref>");
        printInt(value);
        print("</ref>");
    }

    /**
     * Writes a generic object.  writeObject understands the following types:
     *
     * <ul>
     * <li>null
     * <li>java.lang.String
     * <li>java.lang.Boolean
     * <li>java.lang.Integer
     * <li>java.lang.Long
     * <li>java.util.Date
     * <li>byte[]
     * <li>java.util.Vector
     * <li>java.util.Hashtable
     * </ul>
     *
     * Unknown objects will call <code>writeCustomObject</code>.
     */
    public void writeObject(Object object) throws IOException {
        if (object == null) {
            writeNull();
        } else if (object instanceof String) {
            writeString((String) object);
        } else if (object instanceof Boolean) {
            writeBoolean(((Boolean) object).booleanValue());
        } else if (object instanceof Integer) {
            writeInt(((Integer) object).intValue());
        } else if (object instanceof Long) {
            writeLong(((Long) object).longValue());
        } else if (object instanceof Date) {
            writeUTCDate(((Date) object).getTime());
        } else if (object instanceof byte[]) {
            byte[] data = (byte[]) object;
            writeBytes(data, 0, data.length);
        } else if (object instanceof Vector) {
            Vector vector = (Vector) object;

            int size = vector.size();
            writeListBegin(size, null);
            for (int i = 0; i < size; i++) {
                writeObject(vector.elementAt(i));
            }

            writeListEnd();
        } else if (object instanceof Hashtable) {
            Hashtable hashtable = (Hashtable) object;

            writeMapBegin(null);
            Enumeration e = hashtable.keys();
            while (e.hasMoreElements()) {
                Object key = e.nextElement();
                Object value = hashtable.get(key);

                writeObject(key);
                writeObject(value);
            }
            writeMapEnd();
        } else {
            writeCustomObject(object);
        }
    }

    /**
     * Applications which override this can do custom serialization.
     *
     * @param object the object to write.
     */
    public void writeCustomObject(Object object) throws IOException {
        throw new IOException("unexpected object: " + object);
    }

    /**
     * Writes the list header to the stream.  List writers will call
     * <code>writeListBegin</code> followed by the list contents and then
     * call <code>writeListEnd</code>.
     *<pre>
     * &lt;list&gt;
     *   &lt;type&gt;java.util.ArrayList&lt;/type&gt;
     *   &lt;length&gt;3&lt;/length&gt;
     *   &lt;int&gt;1&lt;/int&gt;
     *   &lt;int&gt;2&lt;/int&gt;
     *   &lt;int&gt;3&lt;/int&gt;
     * &lt;/list&gt;
     * </pre>
     */
    public void writeListBegin(int length, String type) throws IOException {
        print("<list><type>");
        if (type != null) {
            print(type);
        }
        print("</type><length>");
        printInt(length);
        print("</length>");
    }

    /**
     * Writes the tail of the list to the stream.
     */
    public void writeListEnd() throws IOException {
        print("</list>");
    }

    /**
     * Writes the map header to the stream.  Map writers will call
     * <code>writeMapBegin</code> followed by the map contents and then
     * call <code>writeMapEnd</code>.
     *<pre>
     * &lt;map&gt;
     *   &lt;type&gt;java.util.Hashtable&lt;/type&gt;
     *   &lt;string&gt;a&lt;/string;&lt;int&gt;1&lt;/int&gt;
     *   &lt;string&gt;b&lt;/string;&lt;int&gt;2&lt;/int&gt;
     *   &lt;string&gt;c&lt;/string;&lt;int&gt;3&lt;/int&gt;
     * &lt;/map&gt;
     * </pre>
     */
    public void writeMapBegin(String type) throws IOException {
        print("<map><type>");
        if (type != null) {
            print(type);
        }
        print("</type>");
    }

    /**
     * Writes the tail of the map to the stream.
     */
    public void writeMapEnd() throws IOException {
        print("</map>");
    }

    /**
     * Writes a remote object reference to the stream.  The type is the
     * type of the remote interface.
     *<pre>
     * &lt;remote&gt;
     *   &lt;type&gt;test.account.Account&lt;/type&gt;
     *   &lt;string&gt;http://caucho.com/foo;ejbid=bar&lt;/string&gt;
     * &lt;/remote&gt;
     * </pre>
     */
    public void writeRemote(String type, String url) throws IOException {
        print("<remote><type>");
        if (type != null) {
            print(type);
        }
        print("</type><string>");
        print(url);
        print("</string></remote>");
    }

    /**
     * Prints an integer to the stream.
     *
     * @param v the integer to print.
     */
    public void printInt(int v) throws IOException {
        print(String.valueOf(v));
    }

    /**
     * Prints a long to the stream.
     *
     * @param v the long to print.
     */
    public void printLong(long v) throws IOException {
        print(String.valueOf(v));
    }

    /**
     * Prints a string to the stream, properly encoded.
     *
     * @param v the string to print.
     */
    public void printString(String v) throws IOException {
        int len = v.length();

        for (int i = 0; i < len; i++) {
            char ch = v.charAt(i);

            switch (ch) {
                case '<':
                    print("&lt;");
                    break;

                case '&':
                    print("&amp;");
                    break;

                case '\r':
                    print("&#13;");
                    break;

                default:
                    if (ch < 0x80) os.write(ch);
                    else if (ch < 0x800) {
                        os.write(0xc0 + ((ch >> 6) & 0x1f));
                        os.write(0x80 + (ch & 0x3f));
                    } else {
                        os.write(0xe0 + ((ch >> 12) & 0xf));
                        os.write(0x80 + ((ch >> 6) & 0x3f));
                        os.write(0x80 + (ch & 0x3f));
                    }
                    break;
            }
        }
    }

    /**
     * Prints a byte array to the stream, properly encoded  in base64.
     *
     * @param data the bytes to print.
     */
    public void printBytes(byte[] data, int offset, int length) throws IOException {
        int i;

        for (; length >= 3; length -= 3) {
            int chunk = (((data[offset] & 0xff) << 16) + ((data[offset + 1] & 0xff) << 8) + (data[offset + 2] & 0xff));

            os.write(base64encode(chunk >> 18));
            os.write(base64encode(chunk >> 12));
            os.write(base64encode(chunk >> 6));
            os.write(base64encode(chunk));

            offset += 3;
        }

        if (length == 2) {
            int chunk = ((data[offset] & 0xff) << 8) + (data[offset + 1] & 0xff);

            os.write(base64encode(chunk >> 12));
            os.write(base64encode(chunk >> 6));
            os.write(base64encode(chunk));
            os.write('=');
        } else if (length == 1) {
            int chunk = data[offset] & 0xff;
            os.write(base64encode(chunk >> 6));
            os.write(base64encode(chunk));
            os.write('=');
            os.write('=');
        }
    }

    /**
     * Converts the digit to its base64 encoding.
     */
    public static char base64encode(int d) {
        d &= 0x3f;
        if (d < 26) {
            return (char) (d + 'A');
        } else if (d < 52) {
            return (char) (d + 'a' - 26);
        } else if (d < 62) {
            return (char) (d + '0' - 52);
        } else if (d == 62) {
            return '+';
        } else {
            return '/';
        }
    }

    /**
     * Prints a date.
     *
     */
    public void printDate(Calendar calendar) throws IOException {
        int year = calendar.get(Calendar.YEAR);

        os.write((char) ('0' + (year / 1000 % 10)));
        os.write((char) ('0' + (year / 100 % 10)));
        os.write((char) ('0' + (year / 10 % 10)));
        os.write((char) ('0' + (year % 10)));

        int month = calendar.get(Calendar.MONTH) + 1;
        os.write((char) ('0' + (month / 10 % 10)));
        os.write((char) ('0' + (month % 10)));

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        os.write((char) ('0' + (day / 10 % 10)));
        os.write((char) ('0' + (day % 10)));

        os.write('T');

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        os.write((char) ('0' + (hour / 10 % 10)));
        os.write((char) ('0' + (hour % 10)));

        int minute = calendar.get(Calendar.MINUTE);
        os.write((char) ('0' + (minute / 10 % 10)));
        os.write((char) ('0' + (minute % 10)));

        int second = calendar.get(Calendar.SECOND);
        os.write((char) ('0' + (second / 10 % 10)));
        os.write((char) ('0' + (second % 10)));

        os.write('Z');
    }

    /**
     * Prints a string as ascii to the stream.  Used for tags, etc.
     * that are known to the ascii.
     *
     * @param s the ascii string to print.
     */
    public void print(String s) throws IOException {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            int ch = s.charAt(i);

            os.write(ch);
        }
    }
}
