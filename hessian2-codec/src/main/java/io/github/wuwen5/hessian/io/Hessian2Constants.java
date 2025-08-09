/*
 * Copyright (c) 2001-2008 Caucho Technology, Inc.  All rights reserved.
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

package io.github.wuwen5.hessian.io;

public interface Hessian2Constants {
    /**
     * final chunk
     */
    int BC_BINARY = 'B';
    /**
     * non-final chunk
     */
    int BC_BINARY_CHUNK = 'A';
    /**
     * 1-byte length binary
     */
    int BC_BINARY_DIRECT = 0x20;

    int BINARY_DIRECT_MAX = 0x0f;
    /**
     * 2-byte length binary
     */
    int BC_BINARY_SHORT = 0x34;
    /**
     * 0-1023 binary
     */
    int BINARY_SHORT_MAX = 0x3ff;

    /**
     * object/class definition
     */
    int BC_CLASS_DEF = 'C';

    /**
     * 64-bit millisecond UTC date
     */
    int BC_DATE = 0x4a;
    /**
     * 32-bit minute UTC date
     */
    int BC_DATE_MINUTE = 0x4b;

    /**
     * IEEE 64-bit double
     */
    int BC_DOUBLE = 'D';

    int BC_DOUBLE_ZERO = 0x5b;
    int BC_DOUBLE_ONE = 0x5c;
    int BC_DOUBLE_BYTE = 0x5d;
    int BC_DOUBLE_SHORT = 0x5e;
    int BC_DOUBLE_MILL = 0x5f;

    /**
     * boolean false
     */
    int BC_FALSE = 'F';

    /**
     * 32-bit int
     */
    int BC_INT = 'I';

    int INT_DIRECT_MIN = -0x10;
    int INT_DIRECT_MAX = 0x2f;
    int BC_INT_ZERO = 0x90;

    int INT_BYTE_MIN = -0x800;
    int INT_BYTE_MAX = 0x7ff;
    int BC_INT_BYTE_ZERO = 0xc8;

    int BC_END = 'Z';

    int INT_SHORT_MIN = -0x40000;
    int INT_SHORT_MAX = 0x3ffff;
    int BC_INT_SHORT_ZERO = 0xd4;

    int BC_LIST_VARIABLE = 0x55;
    int BC_LIST_FIXED = 'V';
    int BC_LIST_VARIABLE_UNTYPED = 0x57;
    int BC_LIST_FIXED_UNTYPED = 0x58;

    int BC_LIST_DIRECT = 0x70;
    int BC_LIST_DIRECT_UNTYPED = 0x78;
    int LIST_DIRECT_MAX = 0x7;

    /**
     * 64-bit signed integer
     */
    int BC_LONG = 'L';

    long LONG_DIRECT_MIN = -0x08;
    long LONG_DIRECT_MAX = 0x0f;
    int BC_LONG_ZERO = 0xe0;

    long LONG_BYTE_MIN = -0x800;
    long LONG_BYTE_MAX = 0x7ff;
    int BC_LONG_BYTE_ZERO = 0xf8;

    int LONG_SHORT_MIN = -0x40000;
    int LONG_SHORT_MAX = 0x3ffff;
    int BC_LONG_SHORT_ZERO = 0x3c;

    int BC_LONG_INT = 0x59;

    int BC_MAP = 'M';
    int BC_MAP_UNTYPED = 'H';

    int BC_NULL = 'N';

    int BC_OBJECT = 'O';
    int BC_OBJECT_DEF = 'C';

    int BC_OBJECT_DIRECT = 0x60;
    int OBJECT_DIRECT_MAX = 0x0f;

    int BC_REF = 0x51;

    /**
     * final string
     */
    int BC_STRING = 'S';
    /**
     * non-final string
     */
    int BC_STRING_CHUNK = 'R';

    int BC_STRING_DIRECT = 0x00;
    int STRING_DIRECT_MAX = 0x1f;
    int BC_STRING_SHORT = 0x30;
    int STRING_SHORT_MAX = 0x3ff;

    int BC_TRUE = 'T';

    int P_PACKET_CHUNK = 0x4f;
    int P_PACKET = 'P';

    int P_PACKET_DIRECT = 0x80;
    int PACKET_DIRECT_MAX = 0x7f;

    int P_PACKET_SHORT = 0x70;
    int PACKET_SHORT_MAX = 0xfff;
}
