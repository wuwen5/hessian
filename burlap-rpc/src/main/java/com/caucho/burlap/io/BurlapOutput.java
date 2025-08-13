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

package com.caucho.burlap.io;

import io.github.wuwen5.hessian.io.Serializer;
import io.github.wuwen5.hessian.io.SerializerFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.TimeZone;

/**
 * Output stream for Burlap requests, compatible with microedition
 * Java.  It only uses classes and types available in JDK.
 *
 * <p>Since BurlapOutput does not depend on any classes other than
 * in the JDK, it can be extracted independently into a smaller package.
 *
 * <p>BurlapOutput is unbuffered, so any client needs to provide
 * its own buffering.
 *
 * <pre>
 * OutputStream os = ...; // from http connection
 * BurlapOutput out = new BurlapOutput(os);
 * String value;
 *
 * out.startCall("hello");  // start hello call
 * out.writeString("arg1"); // write a string argument
 * out.completeCall();      // complete the call
 * </pre>
 */
public class BurlapOutput extends AbstractBurlapOutput {
    /**
     * the output stream
     */
    protected OutputStream os;
    /**
     * map of references
     */
    private IdentityHashMap _refs;

    private Date date;
    private Calendar utcCalendar;
    private Calendar localCalendar;
    /**
     * Creates a new Burlap output stream, initialized with an
     * underlying output stream.
     *
     * @param os the underlying output stream.
     */
    public BurlapOutput(OutputStream os) {
        init(os);
    }

    /**
     * Creates an uninitialized Burlap output stream.
     */
    public BurlapOutput() {}

    /**
     * Initializes the output
     */
    @Override
    public void init(OutputStream os) {
        this.os = os;

        _refs = null;

        if (serializerFactory == null) {
            serializerFactory = new SerializerFactory();
        }
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
     * Starts the method call.  Clients would use <code>startCall</code>
     * instead of <code>call</code> if they wanted finer control over
     * writing the arguments, or needed to write headers.
     *<pre>
     * &lt;burlap:call&gt;
     * &lt;method&gt;method-name&lt;/method&gt;
     * </pre>
     * @param method the method name to call.
     */
    @Override
    public void startCall(String method) throws IOException {
        print("<burlap:call><method>");
        print(method);
        print("</method>");
    }

    /**
     * Starts the method call.  Clients would use <code>startCall</code>
     * instead of <code>call</code> if they wanted finer control over
     * writing the arguments, or needed to write headers.
     *<pre>
     * &lt;method&gt;method-name&lt;/method&gt;
     * </pre>
     */
    public void startCall() throws IOException {
        print("<burlap:call>");
    }

    /**
     * Writes the method for a call.
     *<pre>
     * &lt;method&gt;value&lt;/method&gt;
     * </pre>
     * @param method the method name to call.
     */
    public void writeMethod(String method) throws IOException {
        print("<method>");
        print(method);
        print("</method>");
    }

    /**
     * Completes.
     *<pre>
     * &lt;/burlap:call&gt;
     * </pre>
     */
    public void completeCall() throws IOException {
        print("</burlap:call>");
    }

    /**
     * Starts the reply
     *
     * <p>A successful completion will have a single value:
     *
     * <pre>
     * r
     * </pre>
     */
    public void startReply() throws IOException {
        print("<burlap:reply>");
    }

    /**
     * Completes reading the reply
     *
     * <p>A successful completion will have a single value:
     *
     * <pre>
     * &lt;/burlap:reply&gt;
     * </pre>
     */
    public void completeReply() throws IOException {
        print("</burlap:reply>");
    }

    /**
     * Writes a header name.  The header value must immediately follow.
     *<pre>
     * &lt;header&gt;foo&lt;/header&gt;&lt;int&gt;value&lt;/int&gt;
     * </pre>
     */
    public void writeHeader(String name) throws IOException {
        print("<header>");
        printString(name);
        print("</header>");
    }

    /**
     * Writes a fault.  The fault will be written
     * as a descriptive string followed by an object:
     *<pre>
     * &lt;fault&gt;
     * &lt;string&gt;code
     * &lt;string&gt;the fault code
     *
     * &lt;string&gt;message
     * &lt;string&gt;the fault mesage
     *
     * &lt;string&gt;detail
     * &lt;map&gt;t\x00\xnnjavax.ejb.FinderException
     *     ...
     * &lt;/map&gt;
     * &lt;/fault&gt;
     * </pre>
     * @param code the fault code, a three digit
     */
    public void writeFault(String code, String message, Object detail) throws IOException {
        print("<fault>");
        writeString("code");
        writeString(code);

        writeString("message");
        writeString(message);

        if (detail != null) {
            writeString("detail");
            writeObject(detail);
        }
        print("</fault>");
    }

    /**
     * Writes any object to the output stream.
     */
    @Override
    public void writeObject(Object object) throws IOException {
        if (object == null) {
            writeNull();
            return;
        }

        Serializer serializer;

        serializer = serializerFactory.getSerializer(object.getClass());

        serializer.writeObject(object, this);
    }

    /**
     * Writes the list header to the stream.  List writers will call
     * <code>writeListBegin</code> followed by the list contents and then
     * call <code>writeListEnd</code>.
     *<pre>
     * &lt;list&gt;
     *   &lt;type&gt;java.util.ArrayList&lt;/type&gt;
     *   &lt;length>3&lt;/length&gt;
     *   &lt;int&gt;1&lt;/int&gt;
     *   &lt;int&gt;2&lt;/int&gt;
     *   &lt;int&gt;3&lt;/int&gt;
     * &lt;/list&gt;
     * </pre>
     */
    @Override
    public boolean writeListBegin(int length, String type) throws IOException {
        print("<list><type>");

        if (type != null) {
            print(type);
        }

        print("</type><length>");
        print(length);
        print("</length>");

        return true;
    }

    /**
     * Writes the tail of the list to the stream.
     */
    @Override
    public void writeListEnd() throws IOException {
        print("</list>");
    }

    /**
     * Writes the map header to the stream.  Map writers will call
     * <code>writeMapBegin</code> followed by the map contents and then
     * call <code>writeMapEnd</code>.
     *<pre>
     * &lt;map&gt;
     *   &lt;type&gt;type&lt;/type&gt;
     *   (&lt;key&gt; &lt;value&gt;)*
     * &lt;/map&gt;
     * </pre>
     */
    @Override
    public void writeMapBegin(String type) throws IOException {
        print("<map><type>");
        if (type != null) print(type);

        print("</type>");
    }

    /**
     * Writes the tail of the map to the stream.
     */
    @Override
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
        print(type);
        print("</type><string>");
        print(url);
        print("</string></remote>");
    }

    /**
     * Writes a boolean value to the stream.  The boolean will be written
     * with the following syntax:
     *<pre>
     * &lt;boolean&gt;0&lt;/boolean&gt;
     * &lt;boolean&gt;1&lt;/boolean&gt;
     * </pre>
     *
     * @param value the boolean value to write.
     */
    @Override
    public void writeBoolean(boolean value) throws IOException {
        if (value) {
            print("<boolean>1</boolean>");
        } else {
            print("<boolean>0</boolean>");
        }
    }

    /**
     * Writes an integer value to the stream.  The integer will be written
     * with the following syntax:
     *<pre>
     * &lt;int&gt;int value&lt;/int&gt;
     * </pre>
     *
     * @param value the integer value to write.
     */
    @Override
    public void writeInt(int value) throws IOException {
        print("<int>");
        print(value);
        print("</int>");
    }

    /**
     * Writes a long value to the stream.  The long will be written
     * with the following syntax:
     *<pre>
     * &lt;long&gt;int value&lt;/long&gt;
     * </pre>
     * @param value the long value to write.
     */
    @Override
    public void writeLong(long value) throws IOException {
        print("<long>");
        print(value);
        print("</long>");
    }

    /**
     * Writes a double value to the stream.  The double will be written
     * with the following syntax:
     *<pre>
     * &lt;double&gt;value&lt;/double&gt;
     * </pre>
     * @param value the double value to write.
     */
    @Override
    public void writeDouble(double value) throws IOException {
        print("<double>");
        print(value);
        print("</double>");
    }

    /**
     * Writes a date to the stream.
     *<pre>
     * &lt;date&gt;iso8901&lt;/date&gt;
     * </pre>
     * @param time the date in milliseconds from the epoch in UTC
     */
    @Override
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
     * Writes a null value to the stream.
     * The null will be written with the following syntax
     *<pre>
     * &lt;null&gt;&lt;/null&gt;
     * </pre>
     */
    @Override
    public void writeNull() throws IOException {
        print("<null></null>");
    }

    /**
     * Writes a string value to the stream using UTF-8 encoding.
     * The string will be written with the following syntax:
     *<pre>
     * &lt;string&gt;string-value&lt;/string&gt;
     * </pre>
     * If the value is null, it will be written as
     *<pre>
     * &lt;null&gt;&lt;/null&gt;
     * </pre>
     * @param value the string value to write.
     */
    @Override
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
     * Writes a string value to the stream using UTF-8 encoding.
     * The string will be written with the following syntax:
     *<pre>
     * S b16 b8 string-value
     * </pre>
     * If the value is null, it will be written as
     *<pre>
     * N
     * </pre>
     */
    @Override
    public void writeString(char[] buffer, int offset, int length) throws IOException {
        if (buffer == null) {
            print("<null></null>");
        } else {
            print("<string>");
            printString(buffer, offset, length);
            print("</string>");
        }
    }

    /**
     * Writes a byte array to the stream.
     * The array will be written with the following syntax:
     *<pre>
     * &lt;base64&gt;bytes&lt;/base64&gt;
     * </pre>
     * If the value is null, it will be written as
     *<pre>
     * &lt;null&gt;&lt;/null&gt;
     * </pre>
     * @param buffer the byte array to write.
     */
    @Override
    public void writeBytes(byte[] buffer) throws IOException {
        if (buffer == null) {
            print("<null></null>");
        } else {
            writeBytes(buffer, 0, buffer.length);
        }
    }
    /**
     * Writes a byte array to the stream.
     * The array will be written with the following syntax:
     *<pre>
     * &lt;base64&gt;bytes&lt;/base64&gt;
     * </pre>
     * If the value is null, it will be written as
     *<pre>
     * &lt;null&gt;&lt;/null&gt;
     * </pre>
     */
    @Override
    public void writeBytes(byte[] buffer, int offset, int length) throws IOException {
        if (buffer == null) {
            print("<null></null>");
        } else {
            print("<base64>");

            int i = 0;
            for (; i + 2 < length; i += 3) {
                if (i != 0 && (i & 0x3f) == 0) print('\n');

                int v = (((buffer[offset + i] & 0xff) << 16)
                        + ((buffer[offset + i + 1] & 0xff) << 8)
                        + (buffer[offset + i + 2] & 0xff));

                print(encode(v >> 18));
                print(encode(v >> 12));
                print(encode(v >> 6));
                print(encode(v));
            }

            if (i + 1 < length) {
                int v = (((buffer[offset + i] & 0xff) << 8) + (buffer[offset + i + 1] & 0xff));

                print(encode(v >> 10));
                print(encode(v >> 4));
                print(encode(v << 2));
                print('=');
            } else if (i < length) {
                int v = buffer[offset + i] & 0xff;

                print(encode(v >> 2));
                print(encode(v << 4));
                print('=');
                print('=');
            }

            print("</base64>");
        }
    }

    /**
     * Writes a byte buffer to the stream.
     */
    @Override
    public void writeByteBufferStart() {
        throw new UnsupportedOperationException();
    }

    /**
     * Writes a byte buffer to the stream.
     *<pre>
     * b b16 b18 bytes
     * </pre>
     */
    @Override
    public void writeByteBufferPart(byte[] buffer, int offset, int length) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Writes a byte buffer to the stream.
     *<pre>
     * b b16 b18 bytes
     * </pre>
     */
    @Override
    public void writeByteBufferEnd(byte[] buffer, int offset, int length) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Encodes a digit
     */
    private char encode(int d) {
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
     * Writes a reference.
     *<pre>
     * &lt;ref&gt;int&lt;/ref&gt;
     * </pre>
     * @param value the integer value to write.
     */
    @Override
    public void writeRef(int value) throws IOException {
        print("<ref>");
        print(value);
        print("</ref>");
    }

    /**
     * If the object has already been written, just write its ref.
     *
     * @return true if we're writing a ref.
     */
    @Override
    public boolean addRef(Object object) throws IOException {
        if (_refs == null) {
            _refs = new IdentityHashMap();
        }

        Integer ref = (Integer) _refs.get(object);

        if (ref != null) {
            int value = ref.intValue();

            writeRef(value);
            return true;
        } else {
            _refs.put(object, new Integer(_refs.size()));

            return false;
        }
    }

    @Override
    public int getRef(Object obj) {
        if (_refs == null) {
            return -1;
        }

        Integer ref = (Integer) _refs.get(obj);

        if (ref != null) {
            return ref;
        } else {
            return -1;
        }
    }

    /**
     * Removes a reference.
     */
    @Override
    public boolean removeRef(Object obj) {
        if (_refs != null) {
            _refs.remove(obj);

            return true;
        } else {
            return false;
        }
    }

    /**
     * Replaces a reference from one object to another.
     */
    @Override
    public boolean replaceRef(Object oldRef, Object newRef) throws IOException {
        Integer value = (Integer) _refs.remove(oldRef);

        if (value != null) {
            _refs.put(newRef, value);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Prints a string to the stream, encoded as UTF-8
     *
     * @param v the string to print.
     */
    public void printString(String v) throws IOException {
        printString(v, 0, v.length());
    }

    /**
     * Prints a string to the stream, encoded as UTF-8
     *
     * @param v the string to print.
     */
    public void printString(String v, int offset, int length) throws IOException {
        for (int i = 0; i < length; i++) {
            char ch = v.charAt(i + offset);

            if (ch == '<') {
                os.write('&');
                os.write('#');
                os.write('6');
                os.write('0');
                os.write(';');
            } else if (ch == '&') {
                os.write('&');
                os.write('#');
                os.write('3');
                os.write('8');
                os.write(';');
            } else if (ch < 0x80) os.write(ch);
            else if (ch < 0x800) {
                os.write(0xc0 + ((ch >> 6) & 0x1f));
                os.write(0x80 + (ch & 0x3f));
            } else {
                os.write(0xe0 + ((ch >> 12) & 0xf));
                os.write(0x80 + ((ch >> 6) & 0x3f));
                os.write(0x80 + (ch & 0x3f));
            }
        }
    }

    /**
     * Prints a string to the stream, encoded as UTF-8
     *
     * @param v the string to print.
     */
    public void printString(char[] v, int offset, int length) throws IOException {
        for (int i = 0; i < length; i++) {
            char ch = v[i + offset];

            if (ch < 0x80) os.write(ch);
            else if (ch < 0x800) {
                os.write(0xc0 + ((ch >> 6) & 0x1f));
                os.write(0x80 + (ch & 0x3f));
            } else {
                os.write(0xe0 + ((ch >> 12) & 0xf));
                os.write(0x80 + ((ch >> 6) & 0x3f));
                os.write(0x80 + (ch & 0x3f));
            }
        }
    }

    /**
     * Prints a date.
     *
     * @param calendar the calendar to print.
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

        int ms = calendar.get(Calendar.MILLISECOND);
        os.write('.');
        os.write((char) ('0' + (ms / 100 % 10)));
        os.write((char) ('0' + (ms / 10 % 10)));
        os.write((char) ('0' + (ms % 10)));

        os.write('Z');
    }

    /**
     * Prints a char to the stream.
     *
     * @param v the char to print.
     */
    protected void print(char v) throws IOException {
        os.write(v);
    }

    /**
     * Prints an integer to the stream.
     *
     * @param v the integer to print.
     */
    protected void print(int v) throws IOException {
        print(String.valueOf(v));
    }

    /**
     * Prints a long to the stream.
     *
     * @param v the long to print.
     */
    protected void print(long v) throws IOException {
        print(String.valueOf(v));
    }

    /**
     * Prints a double to the stream.
     *
     * @param v the double to print.
     */
    protected void print(double v) throws IOException {
        print(String.valueOf(v));
    }

    /**
     * Prints a string as ascii to the stream.  Used for tags, etc.
     * that are known to the ascii.
     *
     * @param s the ascii string to print.
     */
    protected void print(String s) throws IOException {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            int ch = s.charAt(i);

            os.write(ch);
        }
    }

    @Override
    public void close() throws IOException {}
}
