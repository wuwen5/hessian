package com.caucho.hessian.test;

/**
 * The Test service is a quick sanity check service.  Developers of a
 * new Hessian implementation can use this service as an initial test.
 *
 * http://hessian.caucho.com/test/test2
 */
interface TestHessian2 {
    /**
     * trivial null method call
     *<pre>
     * H x02 x00
     * C
     *   x0a methodNull
     *   x90
     * </pre>
     * <pre>
     * R N
     * </pre>
     */
    void methodNull();

    //
    // result values
    //

    /**
     * Result of null
     *<pre>
     * R N
     * </pre>
     */
    void replyNull();

    //
    // boolean
    //

    /**
     * Boolean true
     *<pre>
     * T
     * </pre>
     */
    Object replyTrue();

    /**
     * Boolean false
     *<pre>
     * F
     * </pre>
     */
    Object replyFalse();

    //
    // integers
    //

    /**
     * Result of integer 0
     *<pre>
     * R x90
     * </pre>
     */
    int replyInt_0();

    /**
     * Result of integer 1
     *<pre>
     * R x91
     * </pre>
     */
    int replyInt_1();

    /**
     * Result of integer 47
     *<pre>
     * R xbf
     * </pre>
     */
    int replyInt_47();

    /**
     * Result of integer -16
     *<pre>
     * R x80
     * </pre>
     */
    int replyInt_m16();

    // two byte integers

    /**
     * Result of integer 0x30
     *<pre>
     * R xc8 x30
     * </pre>
     */
    int replyInt_0x30();

    /**
     * Result of integer x7ff
     *<pre>
     * R xcf xff
     * </pre>
     */
    int replyInt_0x7ff();

    /**
     * Result of integer -17
     *<pre>
     * R xc7 xef
     * </pre>
     */
    int replyInt_m17();

    /**
     * Result of integer -0x800
     *
     * <pre>
     * R xc0 x00
     * </pre>
     */
    int replyInt_m0x800();

    /**
     * Result of integer 0x800
     *
     * <pre>
     * R xd4 x08 x00
     * </pre>
     */
    int replyInt_0x800();

    /**
     * Result of integer 0x3ffff
     *
     * <pre>
     * R xd7 xff xff
     * </pre>
     */
    int replyInt_0x3ffff();

    /**
     * Result of integer -0x801
     *<pre>
     * R xd3 xf8 x00
     * </pre>
     */
    int replyInt_m0x801();

    /**
     * Result of integer m0x40000
     *
     * <pre>
     * R xd0 x00 x00
     * </pre>
     */
    int replyInt_m0x40000();

    // 5 byte integers

    /**
     * Result of integer 0x40000
     *
     * <pre>
     * R I x00 x04 x00 x00
     * </pre>
     */
    int replyInt_0x40000();

    /**
     * Result of integer 0x7fffffff
     *
     * <pre>
     * R I x7f xff xff xff
     * </pre>
     */
    int replyInt_0x7fffffff();

    /**
     * Result of integer m0x40001
     *
     * <pre>
     * R I xff xf3 xff xf
     * </pre>
     */
    int replyInt_m0x40001();

    /**
     * Result of integer -0x80000000
     *
     * <pre>
     * R I x80 x00 x00 x00
     * </pre>
     */
    int replyInt_m0x80000000();

    //
    // longs
    //

    /**
     * Result of long 0
     *
     * <pre>
     * R xe0
     * </pre>
     */
    long replyLong_0();

    /**
     * Result of long 1
     *
     * <pre>
     * R xe1
     * </pre>
     */
    long replyLong_1();

    /**
     * Result of long 15
     *
     * <pre>
     * R xef
     * </pre>
     */
    long replyLong_15();

    /**
     * Result of long -8
     *
     * <pre>
     * R xd8
     * </pre>
     */
    long replyLong_m8();

    // two byte longs

    /**
     * Result of long 0x10
     *
     * <pre>
     * R xf8 x10
     * </pre>
     */
    long replyLong_0x10();

    /**
     * Result of long x7ff
     *
     * <pre>
     * R xff xff
     * </pre>
     */
    long replyLong_0x7ff();

    /**
     * Result of long -9
     *
     * <pre>
     * R xf7 xf7
     * </pre>
     */
    long replyLong_m9();

    /**
     * Result of long -0x800
     *
     * <pre>
     * R xf0 x00
     * </pre>
     */
    long replyLong_m0x800();

    /**
     * Result of long 0x800
     *
     * <pre>
     * R x3c x08 x00
     * </pre>
     */
    long replyLong_0x800();

    /**
     * Result of long 0x3ffff
     *
     * <pre>
     * R x3f xff xff
     * </pre>
     */
    long replyLong_0x3ffff();

    /**
     * Result of long -0x801
     *
     * <pre>
     * R x3b xf7 xff
     * </pre>
     */
    long replyLong_m0x801();

    /**
     * Result of long m0x40000
     *
     * <pre>
     * R x38 x00 x00
     * </pre>
     */
    long replyLong_m0x40000();

    // 5 byte longs

    /**
     * Result of long 0x40000
     *
     * <pre>
     * R x59 x00 x04 x00 x00
     * </pre>
     */
    long replyLong_0x40000();

    /**
     * Result of long 0x7fffffff
     *
     * <pre>
     * R x59 x7f xff xff xff
     * </pre>
     */
    long replyLong_0x7fffffff();

    /**
     * Result of long m0x40001
     *
     * <pre>
     * R x59 xff xf3 xff xf
     * </pre>
     */
    long replyLong_m0x40001();

    /**
     * Result of long -0x80000000
     *
     * <pre>
     * R x59 x80 x00 x00 x00
     * </pre>
     */
    long replyLong_m0x80000000();

    /**
     * Result of long 0x80000000
     *
     * <pre>
     * R L x00 x00 x00 x00 x80 x00 x00 x00
     * </pre>
     */
    long replyLong_0x80000000();

    /**
     * Result of long -0x80000001
     *
     * <pre>
     * R L xff xff xff xff x7f xff xff xff
     * </pre>
     */
    long replyLong_m0x80000001();

    //
    // doubles
    //

    /**
     * Result of double 0.0
     *
     * <pre>
     * R x5b
     * </pre>
     */
    double replyDouble_0_0();

    /**
     * Result of double 1.0
     *
     * <pre>
     * R x5c
     * </pre>
     */
    double replyDouble_1_0();

    /**
     * Result of double 2.0
     *
     * <pre>
     * R x5d x02
     * </pre>
     */
    double replyDouble_2_0();

    /**
     * Result of double 127.0
     *
     * <pre>
     * R x5d x7f
     * </pre>
     */
    double replyDouble_127_0();

    /**
     * Result of double -128.0
     *
     * <pre>
     * R x5d x80
     * </pre>
     */
    double replyDouble_m128_0();

    /**
     * Result of double 128.0
     *
     * <pre>
     * R x5e x00 x80
     * </pre>
     */
    double replyDouble_128_0();

    /**
     * Result of double -129.0
     *
     * <pre>
     * R x5e xff x7f
     * </pre>
     */
    double replyDouble_m129_0();

    /**
     * Result of double 32767.0
     *
     * <pre>
     * R x5e x7f xff
     * </pre>
     */
    double replyDouble_32767_0();

    /**
     * Result of double -32768.0
     *
     * <pre>
     * R x5e x80 x80
     * </pre>
     */
    double replyDouble_m32768_0();

    /**
     * Result of double 0.001
     *
     * <pre>
     * R x5f x00 x00 x00 x01
     * </pre>
     */
    double replyDouble_0_001();

    /**
     * Result of double -0.001
     *
     * <pre>
     * R x5f xff xff xff xff
     * </pre>
     */
    double replyDouble_m0_001();

    /**
     * Result of double 65.536
     *
     * <pre>
     * R x5f x00 x01 x00 x00
     * </pre>
     */
    double replyDouble_65_536();

    /**
     * Result of double 3.14159
     *
     * <pre>
     * D x40 x09 x21 xf9 xf0 x1b x86 x6e
     * </pre>
     */
    double replyDouble_3_14159();

    //
    // date
    //

    /**
     * date 0 (01-01-1970 00:00 GMT)
     *
     * <pre>
     * x4a x00 x00 x00 x00
     * </pre>
     */
    Object replyDate_0();

    /**
     * Date by millisecond (05-08-1998 07:51:31.000 GMT)
     *
     * <pre>
     * x4a x00 x00 x00 xd0 x4b x92 x84 xb8
     * </pre>
     */
    Object replyDate_1();

    /**
     * Date by minute (05-08-1998 07:51:00.000 GMT)
     *
     * <pre>
     * x4b x00 xe3 x83 x8f
     * </pre>
     */
    Object replyDate_2();

    //
    // string length
    //

    /**
     * A zero-length string
     *
     * <pre>
     * x00
     * </pre>
     */
    String replyString_0();

    /**
     * A null string
     *
     * <pre>
     * N
     * </pre>
     */
    String replyString_null();

    /**
     * A one-length string
     *
     * <pre>
     * x01 a
     * </pre>
     */
    String replyString_1();

    /**
     * A 31-length string
     *
     * <pre>
     * x0f 0123456789012345678901234567890
     * </pre>
     */
    String replyString_31();

    /**
     * A 32-length string
     *
     * <pre>
     * x30 x02 01234567890123456789012345678901
     * </pre>
     */
    String replyString_32();

    /**
     * A 1023-length string
     *
     * <pre>
     * x33 xff 000 01234567890123456789012345678901...
     * </pre>
     */
    String replyString_1023();

    /**
     * A 1024-length string
     *
     * <pre>
     * S x04 x00 000 01234567890123456789012345678901...
     * </pre>
     */
    String replyString_1024();

    /**
     * A 65536-length string
     *
     * <pre>
     * R x80 x00 000 ...
     * S x04 x00 000 01234567890123456789012345678901...
     * </pre>
     */
    String replyString_65536();

    //
    // binary length
    //

    /**
     * A zero-length binary
     *
     * <pre>
     * x20
     * </pre>
     */
    Object replyBinary_0();

    /**
     * A null string
     *
     * <pre>
     * N
     * </pre>
     */
    Object replyBinary_null();

    /**
     * A one-length string
     *
     * <pre>
     * x01 0
     * </pre>
     */
    Object replyBinary_1();

    /**
     * A 15-length binary
     *
     * <pre>
     * x2f 0123456789012345
     * </pre>
     */
    Object replyBinary_15();

    /**
     * A 16-length binary
     *
     * <pre>
     * x34 x10 01234567890123456789012345678901
     * </pre>
     */
    Object replyBinary_16();

    /**
     * A 1023-length binary
     *
     * <pre>
     * x37 xff 000 01234567890123456789012345678901...
     * </pre>
     */
    Object replyBinary_1023();

    /**
     * A 1024-length binary
     *
     * <pre>
     * B x04 x00 000 01234567890123456789012345678901...
     * </pre>
     */
    Object replyBinary_1024();

    /**
     * A 65536-length binary
     *
     * <pre>
     * A x80 x00 000 ...
     * B x04 x00 000 01234567890123456789012345678901...
     * </pre>
     */
    Object replyBinary_65536();

    //
    // lists
    //

    /**
     * Zero-length untyped list
     *
     * <pre>
     * x78
     * </pre>
     */
    Object replyUntypedFixedList_0();

    /**
     * 1-length untyped list
     *
     * <pre>
     * x79 x01 1
     * </pre>
     */
    Object replyUntypedFixedList_1();

    /**
     * 7-length untyped list
     *
     * <pre>
     * x7f x01 1 x01 2 x01 3 x01 4 x01 5 x01 6 x01 7
     * </pre>
     */
    Object replyUntypedFixedList_7();

    /**
     * 8-length untyped list
     *
     * <pre>
     * X x98 x01 1 x01 2 x01 3 x01 4 x01 5 x01 6 x01 7 x01 8
     * </pre>
     */
    Object replyUntypedFixedList_8();

    /**
     * Zero-length typed list (String array)
     *
     * <pre>
     * x70 x07 [string
     * </pre>
     */
    Object replyTypedFixedList_0();

    /**
     * 1-length typed list (String array)
     *
     * <pre>
     * x71 x07 [string x01 1
     * </pre>
     */
    Object replyTypedFixedList_1();

    /**
     * 7-length typed list (String array)
     *
     * <pre>
     * x77 x07 [string x01 1 x01 2 x01 3 x01 4 x01 5 x01 6 x01 7
     * </pre>
     */
    Object replyTypedFixedList_7();

    /**
     * 8-length typed list (String array)
     *
     * <pre>
     * V x07 [stringx98 x01 1 x01 2 x01 3 x01 4 x01 5 x01 6 x01 7 x01 8
     * </pre>
     */
    Object replyTypedFixedList_8();

    //
    // untyped maps
    //

    /**
     * zero-length untyped map
     *
     * <pre>
     * H Z
     * </pre>
     */
    Object replyUntypedMap_0();

    /**
     * untyped map with string key
     *
     * <pre>
     * H x01 a x90 Z
     * </pre>
     */
    Object replyUntypedMap_1();

    /**
     * untyped map with int key
     *
     * <pre>
     * H x90 x01 a x91 x01 b Z
     * </pre>
     */
    Object replyUntypedMap_2();

    /**
     * untyped map with list key
     *
     * <pre>
     * H x71 x01 a x90 Z
     * </pre>
     */
    Object replyUntypedMap_3();

    //
    // typed maps
    //

    /**
     * zero-length typed map
     *
     * <pre>
     * M x13 java.lang.Hashtable Z
     * </pre>
     */
    Object replyTypedMap_0();

    /**
     * untyped map with string key
     *
     * <pre>
     * M x13 java.lang.Hashtable x01 a x90 Z
     * </pre>
     */
    Object replyTypedMap_1();

    /**
     * typed map with int key
     *
     * <pre>
     * M x13 java.lang.Hashtable x90 x01 a x91 x01 b Z
     * </pre>
     */
    Object replyTypedMap_2();

    /**
     * typed map with list key
     *
     * <pre>
     * M x13 java.lang.Hashtable x71 x01 a x90 Z
     * </pre>
     */
    Object replyTypedMap_3();

    //
    // objects
    //

    /**
     * Returns a single object
     *
     * <pre>
     * C x1a com.caucho.hessian.test.A0 x90 x60
     * </pre>
     */
    Object replyObject_0();

    /**
     * Returns 16 object types
     *
     * <pre>
     * X xa0
     *  C x1a com.caucho.hessian.test.A0 x90 x60
     *  C x1a com.caucho.hessian.test.A1 x90 x61
     *  C x1a com.caucho.hessian.test.A2 x90 x62
     *  C x1a com.caucho.hessian.test.A3 x90 x63
     *  C x1a com.caucho.hessian.test.A4 x90 x64
     *  C x1a com.caucho.hessian.test.A5 x90 x65
     *  C x1a com.caucho.hessian.test.A6 x90 x66
     *  C x1a com.caucho.hessian.test.A7 x90 x67
     *  C x1a com.caucho.hessian.test.A8 x90 x68
     *  C x1a com.caucho.hessian.test.A9 x90 x69
     *  C x1b com.caucho.hessian.test.A10 x90 x6a
     *  C x1b com.caucho.hessian.test.A11 x90 x6b
     *  C x1b com.caucho.hessian.test.A12 x90 x6c
     *  C x1b com.caucho.hessian.test.A13 x90 x6d
     *  C x1b com.caucho.hessian.test.A14 x90 x6e
     *  C x1b com.caucho.hessian.test.A15 x90 x6f
     *  C x1b com.caucho.hessian.test.A16 x90 O xa0
     */
    Object replyObject_16();

    /**
     * Simple object with one field
     *
     * <pre>
     * C x22 com.caucho.hessian.test.TestObject x91 x06 _value x60 x90
     * </pre>
     */
    Object replyObject_1();

    /**
     * Simple two objects with one field
     *
     * <pre>
     * x7a
     *   C x22 com.caucho.hessian.test.TestObject x91 x06 _value
     *   x60 x90
     *   x60 x91
     * </pre>
     */
    Object replyObject_2();

    /**
     * Simple repeated object
     *
     * <pre>
     * x7a
     *   C x22 com.caucho.hessian.test.TestObject x91 x06 _value
     *   x60 x90
     *   Q x91
     * </pre>
     */
    Object replyObject_2a();

    /**
     * Two object with equals
     *
     * <pre>
     * x7a
     *   C x22 com.caucho.hessian.test.TestObject x91 x06 _value
     *   x60 x90
     *   x60 x90
     * </pre>
     */
    Object replyObject_2b();

    /**
     * Circular object
     *
     * <pre>
     * C x20 com.caucho.hessian.test.TestCons x91 x06 _first x05 _rest
     *   x60 x01 a Q \x90x
     * </pre>
     */
    Object replyObject_3();

    //
    // arguments
    //

    /**
     * Null
     *
     * <pre>
     * N
     * </pre>
     */
    Object argNull(Object v);

    //
    // boolean
    //

    /**
     * Boolean true
     *
     * <pre>
     * T
     * </pre>
     */
    Object argTrue(Object v);

    /**
     * Boolean false
     *
     * <pre>
     * F
     * </pre>
     */
    Object argFalse(Object v);

    //
    // integer
    //

    /**
     * Integer 0
     *
     * <pre>
     * x90
     * </pre>
     */
    Object argInt_0(Object v);

    /**
     * Integer 1
     *
     * <pre>
     * x91
     * </pre>
     */
    Object argInt_1(Object v);

    /**
     * integer 47
     *
     * <pre>
     * xbf
     * </pre>
     */
    Object argInt_47(Object v);

    /**
     * Result of integer -16
     *
     * <pre>
     * R x80
     * </pre>
     */
    Object argInt_m16(Object v);

    // two byte integers

    /**
     * Integer 0x30
     *
     * <pre>
     * xc8 x30
     * </pre>
     */
    Object argInt_0x30(Object v);

    /**
     * Result of integer x7ff
     *
     * <pre>
     * xcf xff
     * </pre>
     */
    Object argInt_0x7ff(Object v);

    /**
     * integer -17
     *
     * <pre>
     * xc7 xef
     * </pre>
     */
    Object argInt_m17(Object v);

    /**
     * Integer -0x800
     *
     * <pre>
     * xc0 x00
     * </pre>
     */
    Object argInt_m0x800(Object v);

    /**
     * Integer 0x800
     *
     * <pre>
     * xd4 x08 x00
     * </pre>
     */
    Object argInt_0x800(Object v);

    /**
     * Integer 0x3ffff
     *
     * <pre>
     * xd7 xff xff
     * </pre>
     */
    Object argInt_0x3ffff(Object v);

    /**
     * Integer -0x801
     *
     * <pre>
     * xd3 xf8 x00
     * </pre>
     */
    Object argInt_m0x801(Object v);

    /**
     * Integer m0x40000
     *
     * <pre>
     * xd0 x00 x00
     * </pre>
     */
    Object argInt_m0x40000(Object v);

    // 5 byte integers

    /**
     * integer 0x40000
     *
     * <pre>
     * I x00 x04 x00 x00
     * </pre>
     */
    Object argInt_0x40000(Object v);

    /**
     * Integer 0x7fffffff
     *
     * <pre>
     * I x7f xff xff xff
     * </pre>
     */
    Object argInt_0x7fffffff(Object v);

    /**
     * Integer m0x40001
     *
     * <pre>
     * I xff xfb xff xff
     * </pre>
     */
    Object argInt_m0x40001(Object v);

    /**
     * Result of integer -0x80000000
     *
     * <pre>
     * I x80 x00 x00 x00
     * </pre>
     */
    Object argInt_m0x80000000(Object v);

    //
    // longs
    //

    /**
     * long 0
     *
     * <pre>
     * xe0
     * </pre>
     */
    Object argLong_0(Object v);

    /**
     * long 1
     *
     * <pre>
     * xe1
     * </pre>
     */
    Object argLong_1(Object v);

    /**
     * long 15
     *
     * <pre>
     * xef
     * </pre>
     */
    Object argLong_15(Object v);

    /**
     * long -8
     *
     * <pre>
     * xd8
     * </pre>
     */
    Object argLong_m8(Object v);

    // two byte longs

    /**
     * long 0x10
     *
     * <pre>
     * xf8 x10
     * </pre>
     */
    Object argLong_0x10(Object v);

    /**
     * long x7ff
     *
     * <pre>
     * xff xff
     * </pre>
     */
    Object argLong_0x7ff(Object v);

    /**
     * long -9
     *
     * <pre>
     * xf7 xf7
     * </pre>
     */
    Object argLong_m9(Object v);

    /**
     * long -0x800
     *
     * <pre>
     * xf0 x00
     * </pre>
     */
    Object argLong_m0x800(Object v);

    /**
     * long 0x800
     *
     * <pre>
     * x3c x08 x00
     * </pre>
     */
    Object argLong_0x800(Object v);

    /**
     * long 0x3ffff
     *
     * <pre>
     * x3f xff xff
     * </pre>
     */
    Object argLong_0x3ffff(Object v);

    /**
     * long -0x801
     *
     * <pre>
     * x3b xf7 xff
     * </pre>
     */
    Object argLong_m0x801(Object v);

    /**
     * long m0x40000
     *
     * <pre>
     * x38 x00 x00
     * </pre>
     */
    Object argLong_m0x40000(Object v);

    // 5 byte longs

    /**
     * long 0x40000
     *
     * <pre>
     * x59 x00 x04 x00 x00
     * </pre>
     */
    Object argLong_0x40000(Object v);

    /**
     * long 0x7fffffff
     *
     * <pre>
     * x59 x7f xff xff xff
     * </pre>
     */
    Object argLong_0x7fffffff(Object v);

    /**
     * long m0x40001
     *
     * <pre>
     * x59 xff xfb xff xf
     * </pre>
     */
    Object argLong_m0x40001(Object v);

    /**
     * long -0x80000000
     *
     * <pre>
     * x59 x80 x00 x00 x00
     * </pre>
     */
    Object argLong_m0x80000000(Object v);

    /**
     * Result of long 0x80000000
     *
     * <pre>
     * L x00 x00 x00 x00 x80 x00 x00 x00
     * </pre>
     */
    Object argLong_0x80000000(Object v);

    /**
     * Result of long -0x80000001
     *
     * <pre>
     * L xff xff xff xff x7f xff xff xff
     * </pre>
     */
    Object argLong_m0x80000001(Object v);

    //
    // doubles
    //

    /**
     * double 0.0
     *
     * <pre>
     * x5b
     * </pre>
     */
    Object argDouble_0_0(Object v);

    /**
     * double 1.0
     *
     * <pre>
     * x5c
     * </pre>
     */
    Object argDouble_1_0(Object v);

    /**
     * double 2.0
     *
     * <pre>
     * x5d x02
     * </pre>
     */
    Object argDouble_2_0(Object v);

    /**
     * double 127.0
     *
     * <pre>
     * x5d x7f
     * </pre>
     */
    Object argDouble_127_0(Object v);

    /**
     * double -128.0
     *
     * <pre>
     * x5d x80
     * </pre>
     */
    Object argDouble_m128_0(Object v);

    /**
     * double 128.0
     *
     * <pre>
     * x5e x00 x80
     * </pre>
     */
    Object argDouble_128_0(Object v);

    /**
     * double -129.0
     *
     * <pre>
     * x5e xff x7f
     * </pre>
     */
    Object argDouble_m129_0(Object v);

    /**
     * double 32767.0
     *
     * <pre>
     * x5e x7f xff
     * </pre>
     */
    Object argDouble_32767_0(Object v);

    /**
     * Double -32768.0
     *
     * <pre>
     * x5e x80 x80
     * </pre>
     */
    Object argDouble_m32768_0(Object v);

    /**
     * double 0.001
     *
     * <pre>
     * x5f x00 x00 x00 x01
     * </pre>
     */
    Object argDouble_0_001(Object v);

    /**
     * double -0.001
     *
     * <pre>
     * x5f xff xff xff xff
     * </pre>
     */
    Object argDouble_m0_001(Object v);

    /**
     * double 65.536
     *
     * <pre>
     * x5f x00 x01 x00 x00
     * </pre>
     */
    Object argDouble_65_536(Object v);

    /**
     * Result of double 3.14159
     *
     * <pre>
     * D x40 x09 x21 xf9 xf0 x1b x86 x6e
     * </pre>
     */
    Object argDouble_3_14159(Object v);

    //
    // date
    //

    /**
     * date 0 (01-01-1970 00:00 GMT)
     *
     * <pre>
     * x4a x00 x00 x00 x00
     * </pre>
     */
    Object argDate_0(Object v);

    /**
     * Date by millisecond (05-08-1998 07:51 GMT)
     *
     * <pre>
     * x4a x00 x00 x00 xd0 x4b x92 x84 xb8
     * </pre>
     */
    Object argDate_1(Object v);

    /**
     * Date by minute (05-08-1998 07:51 GMT)
     *
     * <pre>
     * x4b x00 xe3 x83 x8f
     * </pre>
     */
    Object argDate_2(Object v);

    //
    // string length
    //

    /**
     * A zero-length string
     *
     * <pre>
     * x00
     * </pre>
     */
    Object argString_0(Object v);

    /**
     * A one-length string
     *
     * <pre>
     * x01 a
     * </pre>
     */
    Object argString_1(Object v);

    /**
     * A 31-length string
     *
     * <pre>
     * x0f 0123456789012345678901234567890
     * </pre>
     */
    Object argString_31(Object v);

    /**
     * A 32-length string
     *
     * <pre>
     * x30 x02 01234567890123456789012345678901
     * </pre>
     */
    Object argString_32(Object v);

    /**
     * A 1023-length string
     *
     * <pre>
     * x33 xff 000 01234567890123456789012345678901...
     * </pre>
     */
    Object argString_1023(Object v);

    /**
     * A 1024-length string
     *
     * <pre>
     * S x04 x00 000 01234567890123456789012345678901...
     * </pre>
     */
    Object argString_1024(Object v);

    /**
     * A 65536-length string
     *
     * <pre>
     * R x80 x00 000 ...
     * S x04 x00 000 01234567890123456789012345678901...
     * </pre>
     */
    Object argString_65536(Object v);

    //
    // binary length
    //

    /**
     * A zero-length binary
     *
     * <pre>
     * x20
     * </pre>
     */
    Object argBinary_0(Object v);

    /**
     * A one-length string
     *
     * <pre>
     * x21 0
     * </pre>
     */
    Object argBinary_1(Object v);

    /**
     * A 15-length binary
     *
     * <pre>
     * x2f 0123456789012345
     * </pre>
     */
    Object argBinary_15(Object v);

    /**
     * A 16-length binary
     *
     * <pre>
     * x34 x10 01234567890123456789012345678901
     * </pre>
     */
    Object argBinary_16(Object v);

    /**
     * A 1023-length binary
     *
     * <pre>
     * x37 xff 000 01234567890123456789012345678901...
     * </pre>
     */
    Object argBinary_1023(Object v);

    /**
     * A 1024-length binary
     *
     * <pre>
     * B x04 x00 000 01234567890123456789012345678901...
     * </pre>
     */
    Object argBinary_1024(Object v);

    /**
     * A 65536-length binary
     *
     * <pre>
     * A x80 x00 000 ...
     * B x04 x00 000 01234567890123456789012345678901...
     * </pre>
     */
    Object argBinary_65536(Object v);

    //
    // lists
    //

    /**
     * Zero-length untyped list
     *
     * <pre>
     * x78
     * </pre>
     */
    Object argUntypedFixedList_0(Object v);

    /**
     * 1-length untyped list
     *
     * <pre>
     * x79 x01 1
     * </pre>
     */
    Object argUntypedFixedList_1(Object v);

    /**
     * 7-length untyped list
     *
     * <pre>
     * x7f x01 1 x01 2 x01 3 x01 4 x01 5 x01 6 x01 7
     * </pre>
     */
    Object argUntypedFixedList_7(Object v);

    /**
     * 8-length untyped list
     *
     * <pre>
     * X x98 x01 1 x01 2 x01 3 x01 4 x01 5 x01 6 x01 7 x01 8
     * </pre>
     */
    Object argUntypedFixedList_8(Object v);

    /**
     * Zero-length typed list (String array)
     *
     * <pre>
     * x70 x07 [string
     * </pre>
     */
    Object argTypedFixedList_0(Object v);

    /**
     * 1-length typed list (String array)
     *
     * <pre>
     * x71 x07 [string x01 1
     * </pre>
     */
    Object argTypedFixedList_1(Object v);

    /**
     * 7-length typed list (String array)
     *
     * <pre>
     * x77 x07 [string x01 1 x01 2 x01 3 x01 4 x01 5 x01 6 x01 7
     * </pre>
     */
    Object argTypedFixedList_7(Object v);

    /**
     * 8-length typed list (String array)
     *
     * <pre>
     * V x07 [stringx98 x01 1 x01 2 x01 3 x01 4 x01 5 x01 6 x01 7 x01 8
     * </pre>
     */
    Object argTypedFixedList_8(Object v);

    //
    // untyped maps
    //

    /**
     * zero-length untyped map
     *
     * <pre>
     * H Z
     * </pre>
     */
    Object argUntypedMap_0(Object v);

    /**
     * untyped map with string key
     *
     * <pre>
     * H x01 a x90 Z
     * </pre>
     */
    Object argUntypedMap_1(Object v);

    /**
     * untyped map with int key
     *
     * <pre>
     * H x90 x01 a x91 x01 b Z
     * </pre>
     */
    Object argUntypedMap_2(Object v);

    /**
     * untyped map with list key
     *
     * <pre>
     * H x71 x01 a x90 Z
     * </pre>
     */
    Object argUntypedMap_3(Object v);

    //
    // typed maps
    //

    /**
     * zero-length typed map
     *
     * <pre>
     * M x13 java.lang.Hashtable Z
     * </pre>
     */
    Object argTypedMap_0(Object v);

    /**
     * untyped map with string key
     *
     * <pre>
     * M x13 java.lang.Hashtable x01 a x90 Z
     * </pre>
     */
    Object argTypedMap_1(Object v);

    /**
     * typed map with int key
     *
     * <pre>
     * M x13 java.lang.Hashtable x90 x01 a x91 x01 b Z
     * </pre>
     */
    Object argTypedMap_2(Object v);

    /**
     * typed map with list key
     *
     * <pre>
     * M x13 java.lang.Hashtable x79 x01 a x90 Z
     * </pre>
     */
    Object argTypedMap_3(Object v);

    //
    // objects
    //

    /**
     * Returns a single object
     *
     * <pre>
     * C x1a com.caucho.hessian.test.A0 x90 x60
     * </pre>
     */
    Object argObject_0(Object v);

    /**
     * Returns 16 object types
     *
     * <pre>
     * X xa0
     *  C x1a com.caucho.hessian.test.A0 x90 x60
     *  C x1a com.caucho.hessian.test.A1 x90 x61
     *  C x1a com.caucho.hessian.test.A2 x90 x62
     *  C x1a com.caucho.hessian.test.A3 x90 x63
     *  C x1a com.caucho.hessian.test.A4 x90 x64
     *  C x1a com.caucho.hessian.test.A5 x90 x65
     *  C x1a com.caucho.hessian.test.A6 x90 x66
     *  C x1a com.caucho.hessian.test.A7 x90 x67
     *  C x1a com.caucho.hessian.test.A8 x90 x68
     *  C x1a com.caucho.hessian.test.A9 x90 x69
     *  C x1b com.caucho.hessian.test.A10 x90 x6a
     *  C x1b com.caucho.hessian.test.A11 x90 x6b
     *  C x1b com.caucho.hessian.test.A12 x90 x6c
     *  C x1b com.caucho.hessian.test.A13 x90 x6d
     *  C x1b com.caucho.hessian.test.A14 x90 x6e
     *  C x1b com.caucho.hessian.test.A15 x90 x6f
     *  C x1b com.caucho.hessian.test.A16 x90 O xa0
     */
    Object argObject_16(Object v);

    /**
     * Simple object with one field
     *
     * <pre>
     * C x30 x22 com.caucho.hessian.test.TestObject x91 x06 _value x60 x90
     * </pre>
     */
    Object argObject_1(Object v);

    /**
     * Simple two objects with one field
     *
     * <pre>
     * x7a
     *   C x30 x22 com.caucho.hessian.test.TestObject x91 x06 _value
     *   x60 x90
     *   x60 x91
     * </pre>
     */
    Object argObject_2(Object v);

    /**
     * Simple repeated object
     *
     * <pre>
     * x7a
     *   C x30 x22 com.caucho.hessian.test.TestObject x91 x06 _value
     *   x60 x90
     *   Q x91
     * </pre>
     */
    Object argObject_2a(Object v);

    /**
     * Two object with equals
     *
     * <pre>
     * x7a
     *   C x22 com.caucho.hessian.test.TestObject x91 x06 _value
     *   x60 x90
     *   x60 x90
     * </pre>
     */
    Object argObject_2b(Object v);

    /**
     * Circular object
     *
     * <pre>
     * C x20 com.caucho.hessian.test.TestCons x91 x06 _first x05 _rest
     *   x60 x01 a Q x90
     * </pre>
     */
    Object argObject_3(Object v);
}
