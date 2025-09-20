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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Debugging input stream for Hessian requests.
 */
@Slf4j
public class HessianDebugState implements Hessian2Constants {

    private final PrintWriter dbg;

    private State state;
    private final List<State> stateStack = new ArrayList<>();

    private final List<ObjectDef> objectDefList = new ArrayList<>();

    private final ArrayList<String> typeDefList = new ArrayList<>();

    private int refId;
    private boolean isNewline = true;
    private boolean isObject = false;
    private int column;

    @Getter
    @Setter
    private int depth = 0;

    /**
     * Creates an uninitialized Hessian input stream.
     */
    public HessianDebugState(PrintWriter dbg) {
        this.dbg = dbg;

        state = new InitialState();
    }

    public void startTop2() {
        state = new Top2State();
    }

    public void startStreaming() {
        state = new StreamingState(new InitialState(), false);
    }

    /**
     * Reads a character.
     */
    public void next(int ch) {
        state = state.next(ch);
    }

    void pushStack(State state) {
        stateStack.add(state);
    }

    State popStack() {
        return stateStack.remove(stateStack.size() - 1);
    }

    void println() {
        if (!isNewline) {
            dbg.println();
            dbg.flush();
        }

        isNewline = true;
        column = 0;
    }

    abstract class State {
        State next;

        State() {}

        State(State next) {
            this.next = next;
        }

        abstract State next(int ch);

        @SuppressWarnings("unused")
        boolean isShift(Object value) {
            return false;
        }

        @SuppressWarnings("unused")
        State shift(Object value) {
            return this;
        }

        int depth() {
            if (next != null) {
                return next.depth();
            } else {
                return HessianDebugState.this.getDepth();
            }
        }

        void printIndent(int depth) {
            if (isNewline) {
                for (int i = column; i < depth() + depth; i++) {
                    dbg.print(" ");
                    column++;
                }
            }
        }

        void print(String string) {
            print(0, string);
        }

        void print(int depth, String string) {
            printIndent(depth);

            dbg.print(string);
            isNewline = false;
            isObject = false;

            int p = string.lastIndexOf('\n');
            if (p > 0) {
                column = string.length() - p - 1;
            } else {
                column += string.length();
            }
        }

        void println(String string) {
            println(0, string);
        }

        void println(int depth, String string) {
            printIndent(depth);

            dbg.println(string);
            dbg.flush();
            isNewline = true;
            isObject = false;
            column = 0;
        }

        void println() {
            if (!isNewline) {
                dbg.println();
                dbg.flush();
            }

            isNewline = true;
            isObject = false;
            column = 0;
        }

        void printObject(String string) {
            if (isObject) {
                println();
            }

            printIndent(0);

            dbg.print(string);
            dbg.flush();

            column += string.length();

            isNewline = false;
            isObject = true;
        }

        protected State nextObject(int ch) {
            switch (ch) {
                case -1:
                    println();
                    return this;

                case 'N':
                    if (isShift(null)) {
                        return shift(null);
                    } else {
                        printObject("null");
                        return this;
                    }

                case 'T':
                    if (isShift(Boolean.TRUE)) {
                        return shift(Boolean.TRUE);
                    } else {
                        printObject("true");
                        return this;
                    }

                case 'F':
                    if (isShift(Boolean.FALSE)) {
                        return shift(Boolean.FALSE);
                    } else {
                        printObject("false");
                        return this;
                    }

                case 0x80:
                case 0x81:
                case 0x82:
                case 0x83:
                case 0x84:
                case 0x85:
                case 0x86:
                case 0x87:
                case 0x88:
                case 0x89:
                case 0x8a:
                case 0x8b:
                case 0x8c:
                case 0x8d:
                case 0x8e:
                case 0x8f:

                case 0x90:
                case 0x91:
                case 0x92:
                case 0x93:
                case 0x94:
                case 0x95:
                case 0x96:
                case 0x97:
                case 0x98:
                case 0x99:
                case 0x9a:
                case 0x9b:
                case 0x9c:
                case 0x9d:
                case 0x9e:
                case 0x9f:

                case 0xa0:
                case 0xa1:
                case 0xa2:
                case 0xa3:
                case 0xa4:
                case 0xa5:
                case 0xa6:
                case 0xa7:
                case 0xa8:
                case 0xa9:
                case 0xaa:
                case 0xab:
                case 0xac:
                case 0xad:
                case 0xae:
                case 0xaf:

                case 0xb0:
                case 0xb1:
                case 0xb2:
                case 0xb3:
                case 0xb4:
                case 0xb5:
                case 0xb6:
                case 0xb7:
                case 0xb8:
                case 0xb9:
                case 0xba:
                case 0xbb:
                case 0xbc:
                case 0xbd:
                case 0xbe:
                case 0xbf: {
                    Integer value = ch - 0x90;

                    if (isShift(value)) {
                        return shift(value);
                    } else {
                        printObject(value.toString());
                        return this;
                    }
                }

                case 0xc0:
                case 0xc1:
                case 0xc2:
                case 0xc3:
                case 0xc4:
                case 0xc5:
                case 0xc6:
                case 0xc7:
                case 0xc8:
                case 0xc9:
                case 0xca:
                case 0xcb:
                case 0xcc:
                case 0xcd:
                case 0xce:
                case 0xcf:
                    return new IntegerState(this, "int", ch - 0xc8, 3);

                case 0xd0:
                case 0xd1:
                case 0xd2:
                case 0xd3:
                case 0xd4:
                case 0xd5:
                case 0xd6:
                case 0xd7:
                    return new IntegerState(this, "int", ch - 0xd4, 2);

                case 'I':
                    return new IntegerState(this, "int");

                case 0xd8:
                case 0xd9:
                case 0xda:
                case 0xdb:
                case 0xdc:
                case 0xdd:
                case 0xde:
                case 0xdf:
                case 0xe0:
                case 0xe1:
                case 0xe2:
                case 0xe3:
                case 0xe4:
                case 0xe5:
                case 0xe6:
                case 0xe7:
                case 0xe8:
                case 0xe9:
                case 0xea:
                case 0xeb:
                case 0xec:
                case 0xed:
                case 0xee:
                case 0xef: {
                    Long value = (long) (ch - 0xe0);

                    if (isShift(value)) {
                        return shift(value);
                    } else {
                        printObject(value + "L");
                        return this;
                    }
                }

                case 0xf0:
                case 0xf1:
                case 0xf2:
                case 0xf3:
                case 0xf4:
                case 0xf5:
                case 0xf6:
                case 0xf7:
                case 0xf8:
                case 0xf9:
                case 0xfa:
                case 0xfb:
                case 0xfc:
                case 0xfd:
                case 0xfe:
                case 0xff:
                    return new LongState(this, "long", (long) ch - 0xf8, 7);

                case 0x38:
                case 0x39:
                case 0x3a:
                case 0x3b:
                case 0x3c:
                case 0x3d:
                case 0x3e:
                case 0x3f:
                    return new LongState(this, "long", (long) ch - 0x3c, 6);

                case BC_LONG_INT:
                    return new LongState(this, "long", 0, 4);

                case 'L':
                    return new LongState(this, "long");

                case 0x5b:
                case 0x5c: {
                    Double value = (double) (ch - 0x5b);

                    if (isShift(value)) {
                        return shift(value);
                    } else {
                        printObject(value.toString());
                        return this;
                    }
                }

                case 0x5d:
                    return new DoubleIntegerState(this, 3);

                case 0x5e:
                    return new DoubleIntegerState(this, 2);

                case 0x5f:
                    return new MillsState(this);

                case 'D':
                    return new DoubleState(this);

                case 'Q':
                    return new RefState(this);

                case BC_DATE:
                    return new DateState(this);

                case BC_DATE_MINUTE:
                    return new DateState(this, true);

                case 0x00: {
                    String value = "\"\"";

                    if (isShift(value)) return shift(value);
                    else {
                        printObject(value);
                        return this;
                    }
                }

                case 0x01:
                case 0x02:
                case 0x03:
                case 0x04:
                case 0x05:
                case 0x06:
                case 0x07:
                case 0x08:
                case 0x09:
                case 0x0a:
                case 0x0b:
                case 0x0c:
                case 0x0d:
                case 0x0e:
                case 0x0f:

                case 0x10:
                case 0x11:
                case 0x12:
                case 0x13:
                case 0x14:
                case 0x15:
                case 0x16:
                case 0x17:
                case 0x18:
                case 0x19:
                case 0x1a:
                case 0x1b:
                case 0x1c:
                case 0x1d:
                case 0x1e:
                case 0x1f:
                    return new StringState(this, 'S', ch);

                case 0x30:
                case 0x31:
                case 0x32:
                case 0x33:
                    return new StringState(this, 'S', ch - 0x30, true);

                case 'R':
                    return new StringState(this, 'S', false);

                case 'S':
                    return new StringState(this, 'S', true);

                case 0x20: {
                    String value = "binary(0)";

                    if (isShift(value)) {
                        return shift(value);
                    } else {
                        printObject(value);
                        return this;
                    }
                }

                case 0x21:
                case 0x22:
                case 0x23:
                case 0x24:
                case 0x25:
                case 0x26:
                case 0x27:
                case 0x28:
                case 0x29:
                case 0x2a:
                case 0x2b:
                case 0x2c:
                case 0x2d:
                case 0x2e:
                case 0x2f:
                    return new BinaryState(this, 'B', ch - 0x20);

                case 0x34:
                case 0x35:
                case 0x36:
                case 0x37:
                    return new BinaryState(this, 'B', ch - 0x34, true);

                case 'A':
                    return new BinaryState(this, 'B', false);

                case 'B':
                    return new BinaryState(this, 'B', true);

                case 'M':
                    return new MapState(this, refId++);

                case 'H':
                    return new MapState(this, refId++, false);

                case BC_LIST_VARIABLE:
                    return new ListState(this, refId++, true);

                case BC_LIST_VARIABLE_UNTYPED:
                    return new ListState(this, refId++, false);

                case BC_LIST_FIXED:
                    return new CompactListState(this, refId++, true);

                case BC_LIST_FIXED_UNTYPED:
                    return new CompactListState(this, refId++, false);

                case 0x70:
                case 0x71:
                case 0x72:
                case 0x73:
                case 0x74:
                case 0x75:
                case 0x76:
                case 0x77:
                    return new CompactListState(this, refId++, true, ch - 0x70);

                case 0x78:
                case 0x79:
                case 0x7a:
                case 0x7b:
                case 0x7c:
                case 0x7d:
                case 0x7e:
                case 0x7f:
                    return new CompactListState(this, refId++, false, ch - 0x78);

                case 'C':
                    return new ObjectDefState(this);

                case 0x60:
                case 0x61:
                case 0x62:
                case 0x63:
                case 0x64:
                case 0x65:
                case 0x66:
                case 0x67:
                case 0x68:
                case 0x69:
                case 0x6a:
                case 0x6b:
                case 0x6c:
                case 0x6d:
                case 0x6e:
                case 0x6f:
                    return new ObjectState(this, refId++, ch - 0x60);

                case 'O':
                    return new ObjectState(this, refId++);

                default:
                    return this;
            }
        }
    }

    class InitialState extends State {
        @Override
        State next(int ch) {
            return nextObject(ch);
        }
    }

    class Top2State extends State {
        @Override
        State next(int ch) {
            super.println();

            if (ch == 'R') {
                return new Reply2State(this);
            } else if (ch == 'F') {
                return new Fault2State(this);
            } else if (ch == 'C') {
                return new Call2State(this);
            } else if (ch == 'H') {
                return new Hessian2State(this);
            } else if (ch == 'r') {
                throw new IllegalStateException("unexpected 'r' in top2 state");
            } else if (ch == 'c') {
                throw new IllegalStateException("unexpected 'c' in top2 state");
            } else {
                return nextObject(ch);
            }
        }
    }

    class IntegerState extends State {
        String typeCode;

        int length;
        int value;

        IntegerState(State next, String typeCode) {
            super(next);

            this.typeCode = typeCode;
        }

        IntegerState(State next, String typeCode, int value, int length) {
            super(next);

            this.typeCode = typeCode;

            this.value = value;
            this.length = length;
        }

        @Override
        State next(int ch) {
            value = 256 * value + (ch & 0xff);

            if (++length == 4) {

                if (next.isShift(value)) {
                    return next.shift(value);
                } else {
                    printObject(String.valueOf(value));

                    return next;
                }
            } else {
                return this;
            }
        }
    }

    class LongState extends State {
        String typeCode;

        int length;
        long value;

        LongState(State next, String typeCode) {
            super(next);

            this.typeCode = typeCode;
        }

        LongState(State next, String typeCode, long value, int length) {
            super(next);

            this.typeCode = typeCode;

            this.value = value;
            this.length = length;
        }

        @Override
        State next(int ch) {
            value = 256 * value + (ch & 0xff);

            if (++length == 8) {

                if (next.isShift(value)) {
                    return next.shift(value);
                } else {
                    printObject(value + "L");

                    return next;
                }
            } else {
                return this;
            }
        }
    }

    class DoubleIntegerState extends State {
        int length;
        int value;
        boolean isFirst = true;

        DoubleIntegerState(State next, int length) {
            super(next);

            this.length = length;
        }

        @Override
        State next(int ch) {
            if (isFirst) {
                value = (byte) ch;
            } else {
                value = 256 * value + (ch & 0xff);
            }

            isFirst = false;

            if (++length == 4) {

                if (next.isShift(value)) {
                    return next.shift(value);
                } else {
                    printObject(String.valueOf(value));

                    return next;
                }
            } else {
                return this;
            }
        }
    }

    class RefState extends State {
        String typeCode;

        int length;
        int value;

        RefState(State next) {
            super(next);
        }

        RefState(State next, String typeCode) {
            super(next);

            this.typeCode = typeCode;
        }

        RefState(State next, String typeCode, int value, int length) {
            super(next);

            this.typeCode = typeCode;

            this.value = value;
            this.length = length;
        }

        @Override
        boolean isShift(Object o) {
            return true;
        }

        @Override
        State shift(Object o) {
            super.println("ref #" + o);

            return next;
        }

        @Override
        State next(int ch) {
            return nextObject(ch);
        }
    }

    class DateState extends State {
        int length;
        long value;
        boolean isMinute;

        DateState(State next) {
            super(next);
        }

        DateState(State next, boolean isMinute) {
            super(next);

            length = 4;
            this.isMinute = isMinute;
        }

        @Override
        State next(int ch) {
            value = 256 * value + (ch & 0xff);

            if (++length == 8) {
                java.util.Date d;

                if (isMinute) {
                    d = new java.util.Date(this.value * 60000L);
                } else {
                    d = new java.util.Date(this.value);
                }

                if (next.isShift(d)) {
                    return next.shift(d);
                } else {
                    printObject(d.toString());

                    return next;
                }
            } else {
                return this;
            }
        }
    }

    class DoubleState extends State {
        int length;
        long value;

        DoubleState(State next) {
            super(next);
        }

        @Override
        State next(int ch) {
            value = 256 * value + (ch & 0xff);

            if (++length == 8) {
                Double v = Double.longBitsToDouble(this.value);

                if (next.isShift(v)) {
                    return next.shift(v);
                } else {
                    printObject(v.toString());

                    return next;
                }
            } else {
                return this;
            }
        }
    }

    class MillsState extends State {
        int length;
        int value;

        MillsState(State next) {
            super(next);
        }

        @Override
        State next(int ch) {
            value = 256 * value + (ch & 0xff);

            if (++length == 4) {
                Double v = 0.001 * this.value;

                if (next.isShift(v)) {
                    return next.shift(v);
                } else {
                    printObject(v.toString());

                    return next;
                }
            } else {
                return this;
            }
        }
    }

    class StringState extends State {
        private static final int TOP = 0;
        private static final int UTF_2_1 = 1;
        private static final int UTF_3_1 = 2;
        private static final int UTF_3_2 = 3;

        char typeCode;

        StringBuilder value = new StringBuilder();
        int lengthIndex;
        int length;
        boolean isLastChunk;

        int utfState;
        char ch;

        StringState(State next, char typeCode, boolean isLastChunk) {
            super(next);

            this.typeCode = typeCode;
            this.isLastChunk = isLastChunk;
        }

        StringState(State next, char typeCode, int length) {
            super(next);

            this.typeCode = typeCode;
            isLastChunk = true;
            this.length = length;
            lengthIndex = 2;
        }

        StringState(State next, char typeCode, int length, boolean isLastChunk) {
            super(next);

            this.typeCode = typeCode;
            this.isLastChunk = isLastChunk;
            this.length = length;
            lengthIndex = 1;
        }

        @Override
        State next(int ch) {
            if (lengthIndex < 2) {
                length = 256 * length + (ch & 0xff);

                if (++lengthIndex == 2 && length == 0 && isLastChunk) {
                    if (next.isShift(value.toString())) {
                        return next.shift(value.toString());
                    } else {
                        printObject("\"" + value + "\"");
                        return next;
                    }
                } else {
                    return this;
                }
            } else if (length == 0) {
                if (ch == 's' || ch == 'x') {
                    isLastChunk = false;
                    lengthIndex = 0;
                    return this;
                } else if (ch == 'S' || ch == 'X') {
                    isLastChunk = true;
                    lengthIndex = 0;
                    return this;
                } else if (ch == 0x00) {
                    if (next.isShift(value.toString())) {
                        return next.shift(value.toString());
                    } else {
                        printObject("\"" + value + "\"");
                        return next;
                    }
                } else if (0x00 <= ch && ch < 0x20) {
                    isLastChunk = true;
                    lengthIndex = 2;
                    length = ch & 0xff;
                    return this;
                } else if (0x30 <= ch && ch < 0x34) {
                    isLastChunk = true;
                    lengthIndex = 1;
                    length = (ch - 0x30);
                    return this;
                } else {
                    super.println(this + " " + (char) ch + ": unexpected character");
                    return next;
                }
            }

            switch (utfState) {
                case TOP:
                    if (ch < 0x80) {
                        length--;

                        value.append((char) ch);
                    } else if (ch < 0xe0) {
                        this.ch = (char) ((ch & 0x1f) << 6);
                        utfState = UTF_2_1;
                    } else {
                        this.ch = (char) ((ch & 0xf) << 12);
                        utfState = UTF_3_1;
                    }
                    break;

                case UTF_2_1:
                case UTF_3_2:
                    this.ch += ch & 0x3f;
                    value.append(this.ch);
                    length--;
                    utfState = TOP;
                    break;

                case UTF_3_1:
                    this.ch += (char) ((ch & 0x3f) << 6);
                    utfState = UTF_3_2;
                    break;
                default:
                    break;
            }

            if (length == 0 && isLastChunk) {
                if (next.isShift(value.toString())) {
                    return next.shift(value.toString());
                } else {
                    printObject("\"" + value + "\"");

                    return next;
                }
            } else {
                return this;
            }
        }
    }

    class BinaryState extends State {
        char typeCode;

        int totalLength;

        int lengthIndex;
        int length;
        boolean isLastChunk;

        BinaryState(State next, char typeCode, boolean isLastChunk) {
            super(next);

            this.typeCode = typeCode;
            this.isLastChunk = isLastChunk;
        }

        BinaryState(State next, char typeCode, int length) {
            super(next);

            this.typeCode = typeCode;
            isLastChunk = true;
            this.length = length;
            lengthIndex = 2;
        }

        BinaryState(State next, char typeCode, int length, boolean isLastChunk) {
            super(next);

            this.typeCode = typeCode;
            this.isLastChunk = isLastChunk;
            this.length = length;
            lengthIndex = 1;
        }

        @Override
        State next(int ch) {
            if (lengthIndex < 2) {
                length = 256 * length + (ch & 0xff);

                if (++lengthIndex == 2 && length == 0 && isLastChunk) {
                    String value = getBinaryValue();

                    if (next.isShift(value)) {
                        return next.shift(value);
                    } else {
                        printObject(value);
                        return next;
                    }
                } else {
                    return this;
                }
            } else if (length == 0) {
                if (ch == 'b' || ch == 'A') {
                    isLastChunk = false;
                    lengthIndex = 0;
                    return this;
                } else if (ch == 'B') {
                    isLastChunk = true;
                    lengthIndex = 0;
                    return this;
                } else if (ch == 0x20) {
                    String value = getBinaryValue();

                    if (next.isShift(value)) {
                        return next.shift(value);
                    } else {
                        printObject(value);
                        return next;
                    }
                } else if (0x20 <= ch && ch < 0x30) {
                    isLastChunk = true;
                    lengthIndex = 2;
                    length = (ch & 0xff) - 0x20;
                    return this;
                } else {
                    super.println(this + " 0x" + Integer.toHexString(ch) + " " + (char) ch + ": unexpected character");
                    return next;
                }
            }

            length--;
            totalLength++;

            if (length == 0 && isLastChunk) {
                String value = getBinaryValue();

                if (next.isShift(value)) {
                    return next.shift(value);
                } else {
                    printObject(value);

                    return next;
                }
            } else {
                return this;
            }
        }

        private String getBinaryValue() {
            return "binary(" + totalLength + ")";
        }
    }

    class MapState extends State {
        private static final int TYPE = 0;
        private static final int KEY = 1;
        private static final int VALUE = 2;

        private final int refId;

        private int state;
        private int valueDepth;
        private boolean hasData;

        MapState(State next, int refId) {
            super(next);

            this.refId = refId;
            state = TYPE;
        }

        MapState(State next, int refId, boolean isType) {
            super(next);

            this.refId = refId;

            if (isType) state = TYPE;
            else {
                printObject("map (#" + this.refId + ")");
                state = VALUE;
            }
        }

        @Override
        boolean isShift(Object value) {
            return state == TYPE;
        }

        @Override
        State shift(Object type) {
            if (state == TYPE) {
                if (type instanceof String) {
                    typeDefList.add((String) type);
                } else if (type instanceof Integer) {
                    int iValue = (Integer) type;

                    if (iValue >= 0 && iValue < typeDefList.size()) type = typeDefList.get(iValue);
                }

                printObject("map " + type + " (#" + refId + ")");

                state = VALUE;

                return this;
            } else {
                printObject(this + " unknown shift state= " + state + " type=" + type);

                return this;
            }
        }

        @Override
        int depth() {
            if (state == TYPE) {
                return next.depth();
            } else if (state == KEY) {
                return next.depth() + 2;
            } else {
                return valueDepth;
            }
        }

        @Override
        State next(int ch) {
            switch (state) {
                case TYPE:
                    return nextObject(ch);

                case VALUE:
                    if (ch == 'Z') {
                        if (hasData) {
                            super.println();
                        }

                        return next;
                    } else {
                        if (hasData) {
                            super.println();
                        }

                        hasData = true;
                        state = KEY;

                        return nextObject(ch);
                    }

                case KEY:
                    print(" => ");
                    isObject = false;
                    valueDepth = column;

                    state = VALUE;

                    return nextObject(ch);

                default:
                    throw new IllegalStateException();
            }
        }
    }

    class ObjectDefState extends State {
        private static final int STATE_TYPE = 1;
        private static final int STATE_COUNT = 2;
        private static final int STATE_FIELD = 3;

        private int state;
        private int count;

        private final List<String> fields = new ArrayList<>();

        ObjectDefState(State next) {
            super(next);

            state = STATE_TYPE;
        }

        @Override
        boolean isShift(Object value) {
            return true;
        }

        @Override
        State shift(Object object) {
            if (state == STATE_TYPE) {
                String type = (String) object;

                print("/* defun " + type + " [");

                objectDefList.add(new ObjectDef(type, fields));

                state = STATE_COUNT;
            } else if (state == STATE_COUNT) {
                count = (Integer) object;

                state = STATE_FIELD;
            } else if (state == STATE_FIELD) {
                String field = (String) object;

                count--;

                fields.add(field);

                if (fields.size() == 1) {
                    print(field);
                } else {
                    print(", " + field);
                }
            } else {
                throw new UnsupportedOperationException();
            }

            return this;
        }

        @Override
        int depth() {
            if (state <= STATE_TYPE) {
                return next.depth();
            } else {
                return next.depth() + 2;
            }
        }

        @Override
        State next(int ch) {
            switch (state) {
                case STATE_TYPE:

                case STATE_COUNT:
                    return nextObject(ch);

                case STATE_FIELD:
                    if (count == 0) {
                        super.println("] */");
                        next.printIndent(0);

                        return next.nextObject(ch);
                    } else {
                        return nextObject(ch);
                    }

                default:
                    throw new IllegalStateException();
            }
        }
    }

    class ObjectState extends State {
        private static final int TYPE = 0;
        private static final int FIELD = 1;

        private final int refId;

        private int state;
        private ObjectDef def;
        private int count;
        private int fieldDepth;

        ObjectState(State next, int refId) {
            super(next);

            this.refId = refId;
            state = TYPE;
        }

        ObjectState(State next, int refId, int def) {
            super(next);

            this.refId = refId;
            state = FIELD;

            if (def < 0 || objectDefList.size() <= def) {
                log.warn("{} {} is an unknown object type", this, def);

                super.println(this + " object unknown  (#" + this.refId + ")");
            }

            this.def = objectDefList.get(def);

            if (isObject) {
                super.println();
            }

            super.println("object " + this.def.getType() + " (#" + this.refId + ")");
        }

        @Override
        boolean isShift(Object value) {
            return state == TYPE;
        }

        @Override
        State shift(Object object) {
            if (state == TYPE) {

                this.def = objectDefList.get((Integer) object);

                super.println("object " + this.def.getType() + " (#" + refId + ")");

                state = FIELD;

                if (this.def.getFields().isEmpty()) {
                    return next;
                }
            }

            return this;
        }

        @Override
        int depth() {
            if (state <= TYPE) {
                return next.depth();
            } else {
                return fieldDepth;
            }
        }

        @Override
        State next(int ch) {
            switch (state) {
                case TYPE:
                    return nextObject(ch);

                case FIELD:
                    if (def.getFields().size() <= count) {
                        return next.next(ch);
                    }

                    fieldDepth = next.depth() + 2;
                    super.println();
                    print(def.getFields().get(count++) + ": ");

                    fieldDepth = column;

                    isObject = false;
                    return nextObject(ch);

                default:
                    throw new IllegalStateException();
            }
        }
    }

    class ListState extends State {
        private static final int TYPE = 0;
        private static final int LENGTH = 1;
        private static final int VALUE = 2;

        private final int refId;

        private int state;
        private int count;
        private int valueDepth;

        ListState(State next, int refId, boolean isType) {
            super(next);

            this.refId = refId;

            if (isType) {
                state = TYPE;
            } else {
                printObject("list (#" + this.refId + ")");
                state = VALUE;
            }
        }

        @Override
        boolean isShift(Object value) {
            return state == TYPE || state == LENGTH;
        }

        @Override
        State shift(Object object) {
            if (state == TYPE) {
                Object type = object;

                if (type instanceof String) {
                    typeDefList.add((String) type);
                } else if (object instanceof Integer) {
                    int index = (Integer) object;

                    if (index >= 0 && index < typeDefList.size()) {
                        type = typeDefList.get(index);
                    } else {
                        type = "type-unknown(" + index + ")";
                    }
                }

                printObject("list " + type + "(#" + refId + ")");

                state = VALUE;

                return this;
            } else if (state == LENGTH) {
                state = VALUE;

                return this;
            } else {
                return this;
            }
        }

        @Override
        int depth() {
            if (state <= LENGTH) {
                return next.depth();
            } else if (state == VALUE) {
                return valueDepth;
            } else {
                return next.depth() + 2;
            }
        }

        @Override
        State next(int ch) {
            switch (state) {
                case TYPE:
                    return nextObject(ch);

                case VALUE:
                    if (ch == 'Z') {
                        if (count > 0) {
                            super.println();
                        }

                        return next;
                    } else {
                        valueDepth = next.depth() + 2;
                        super.println();
                        printObject(count++ + ": ");
                        valueDepth = column;
                        isObject = false;

                        return nextObject(ch);
                    }

                default:
                    throw new IllegalStateException();
            }
        }
    }

    class CompactListState extends State {
        private static final int STATE_TYPE = 0;
        private static final int STATE_LENGTH = 1;
        private static final int STATE_VALUE = 2;

        private final int refId;

        private final boolean isTyped;
        private boolean isLength;

        private int state;
        private int length;
        private int count;
        private int valueDepth;

        CompactListState(State next, int refId, boolean isTyped) {
            super(next);

            this.isTyped = isTyped;
            this.refId = refId;

            if (isTyped) {
                state = STATE_TYPE;
            } else {
                state = STATE_LENGTH;
            }
        }

        CompactListState(State next, int refId, boolean isTyped, int length) {
            super(next);

            this.isTyped = isTyped;
            this.refId = refId;
            this.length = length;

            isLength = true;

            if (isTyped) {
                state = STATE_TYPE;
            } else {
                printObject("list (#" + this.refId + ")");

                state = STATE_VALUE;
            }
        }

        @Override
        boolean isShift(Object value) {
            return state == STATE_TYPE || state == STATE_LENGTH;
        }

        @Override
        State shift(Object object) {
            if (state == STATE_TYPE) {
                Object type = object;

                if (object instanceof Integer) {
                    int index = (Integer) object;

                    if (index >= 0 && index < typeDefList.size()) {
                        type = typeDefList.get(index);
                    } else {
                        type = "type-unknown(" + index + ")";
                    }
                } else if (object instanceof String) {
                    typeDefList.add((String) object);
                }

                printObject("list " + type + " (#" + refId + ")");

                if (isLength) {
                    state = STATE_VALUE;

                    if (length == 0) {
                        return next;
                    }
                } else {
                    state = STATE_LENGTH;
                }

                return this;
            } else if (state == STATE_LENGTH) {
                length = (Integer) object;

                if (!isTyped) {
                    printObject("list (#" + refId + ")");
                }

                state = STATE_VALUE;

                if (length == 0) {
                    return next;
                } else {
                    return this;
                }
            } else {
                return this;
            }
        }

        @Override
        int depth() {
            if (state <= STATE_LENGTH) {
                return next.depth();
            } else if (state == STATE_VALUE) {
                return valueDepth;
            } else {
                return next.depth() + 2;
            }
        }

        @Override
        State next(int ch) {
            switch (state) {
                case STATE_TYPE:

                case STATE_LENGTH:
                    return nextObject(ch);

                case STATE_VALUE:
                    if (length <= count) {
                        return next.next(ch);
                    } else {
                        valueDepth = next.depth() + 2;
                        super.println();
                        printObject(count++ + ": ");
                        valueDepth = column;
                        isObject = false;

                        return nextObject(ch);
                    }

                default:
                    throw new IllegalStateException();
            }
        }
    }

    class Hessian2State extends State {
        private static final int STATE_MAJOR = 0;
        private static final int STATE_MINOR = 1;

        private int state;
        private int major;
        private int minor;

        Hessian2State(State next) {
            super(next);
        }

        @Override
        int depth() {
            return next.depth() + 2;
        }

        @Override
        State next(int ch) {
            switch (state) {
                case STATE_MAJOR:
                    major = ch;
                    state = STATE_MINOR;
                    return this;

                case STATE_MINOR:
                    minor = ch;
                    super.println(-2, "Hessian " + major + "." + minor);
                    return next;

                default:
                    throw new IllegalStateException();
            }
        }
    }

    class Call2State extends State {
        private static final int STATE_METHOD = 0;
        private static final int STATE_COUNT = 1;
        private static final int STATE_ARG = 2;

        private int state = STATE_METHOD;
        private int i;
        private int count;

        Call2State(State next) {
            super(next);
        }

        @Override
        int depth() {
            return next.depth() + 5;
        }

        @Override
        boolean isShift(Object value) {
            return state != STATE_ARG;
        }

        @Override
        State shift(Object object) {
            if (state == STATE_METHOD) {
                super.println(-5, "Call " + object);

                state = STATE_COUNT;
                return this;
            } else if (state == STATE_COUNT) {

                this.count = (Integer) object;

                state = STATE_ARG;

                if (this.count == 0) {
                    return next;
                } else {
                    return this;
                }
            } else {
                return this;
            }
        }

        @Override
        State next(int ch) {
            switch (state) {
                case STATE_COUNT:

                case STATE_METHOD:
                    return nextObject(ch);

                case STATE_ARG:
                    if (count <= i) {
                        super.println();
                        return next.next(ch);
                    } else {
                        super.println();
                        print(-3, i++ + ": ");

                        return nextObject(ch);
                    }

                default:
                    throw new IllegalStateException();
            }
        }
    }

    class Reply2State extends State {
        Reply2State(State next) {
            super(next);

            super.println(-2, "Reply");
        }

        @Override
        int depth() {
            return next.depth() + 2;
        }

        @Override
        State next(int ch) {
            if (ch < 0) {
                super.println();
                return next;
            } else {
                return nextObject(ch);
            }
        }
    }

    class Fault2State extends State {
        Fault2State(State next) {
            super(next);

            super.println(-2, "Fault");
        }

        @Override
        int depth() {
            return next.depth() + 2;
        }

        @Override
        State next(int ch) {
            return nextObject(ch);
        }
    }

    class StreamingState extends State {
        private long length;
        private int metaLength;
        private boolean isLast;
        private boolean isFirst = true;

        private boolean isLengthState;

        private State childState;

        StreamingState(State next, boolean isLast) {
            super(next);

            this.isLast = isLast;
            childState = new InitialState();
        }

        @Override
        State next(int ch) {
            if (metaLength > 0) {
                length = 256 * length + ch;
                metaLength--;

                if (metaLength == 0 && isFirst) {
                    if (isLast) {
                        super.println(-1, "--- packet-start(" + length + ")");
                    } else {
                        super.println(-1, "--- packet-start(fragment)");
                    }
                    isFirst = false;
                }

                return this;
            }

            if (length > 0) {
                length--;
                childState = childState.next(ch);

                return this;
            }

            if (!isLengthState) {
                isLengthState = true;

                if (isLast) {
                    super.println(-1, "");
                    super.println(-1, "--- packet-end");
                    refId = 0;

                    isFirst = true;
                }

                isLast = (ch & 0x80) == 0x00;
                isLengthState = true;
            } else {
                isLengthState = false;
                length = (ch & 0x7f);

                if (length == 0x7e) {
                    length = 0;
                    metaLength = 2;
                } else if (length == 0x7f) {
                    length = 0;
                    metaLength = 8;
                } else {
                    if (isFirst) {
                        if (isLast) {
                            super.println(-1, "--- packet-start(" + length + ")");
                        } else {
                            super.println(-1, "--- packet-start(fragment)");
                        }
                        isFirst = false;
                    }
                }
            }

            return this;
        }
    }

    static class ObjectDef {
        private final String type;
        private final List<String> fields;

        ObjectDef(String type, List<String> fields) {
            this.type = type;
            this.fields = fields;
        }

        String getType() {
            return type;
        }

        List<String> getFields() {
            return fields;
        }
    }
}
