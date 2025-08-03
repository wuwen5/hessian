# Hessian 2.0 Serialization Protocol

**Authors:**
- Scott Ferguson (Caucho Technology Inc.) - ferg@caucho.com
- Emil Ong (Caucho Technology Inc.) - emil@caucho.com  
  **Date:** August 2007

## Table of Contents

1. [Introduction](#introduction)
2. [Design Goals](#design-goals)
3. [Hessian Grammar](#hessian-grammar)
4. [Serialization](#serialization)
    - [4.1. binary data](#41-binary-data)
        - [4.1.1. Compact: short binary](#411-compact-short-binary)
        - [4.1.2. Binary Examples](#412-binary-examples)
    - [4.2. boolean](#42-boolean)
        - [4.2.1. Boolean Examples](#421-boolean-examples)
    - [4.3. date](#43-date)
        - [4.3.1. Compact: date in minutes](#431-compact-date-in-minutes)
        - [4.3.2. Date Examples](#432-date-examples)
    - [4.4. double](#44-double)
        - [4.4.1. Compact: double zero](#441-compact-double-zero)
        - [4.4.2. Compact: double one](#442-compact-double-one)
        - [4.4.3. Compact: double octet](#443-compact-double-octet)
        - [4.4.4. Compact: double short](#444-compact-double-short)
        - [4.4.5. Compact: double float](#445-compact-double-float)
        - [4.4.6. Double Examples](#446-double-examples)
    - [4.5. int](#45-int)
        - [4.5.1. Compact: single octet integers](#451-compact-single-octet-integers)
        - [4.5.2. Compact: two octet integers](#452-compact-two-octet-integers)
        - [4.5.3. Compact: three octet integers](#453-compact-three-octet-integers)
        - [4.5.4. Integer Examples](#454-integer-examples)
    - [4.6. list](#46-list)
        - [4.6.1. Compact: fixed length list](#461-compact-fixed-length-list)
        - [4.6.2. List examples](#462-list-examples)
    - [4.7. long](#47-long)
        - [4.7.1. Compact: single octet longs](#471-compact-single-octet-longs)
        - [4.7.2. Compact: two octet longs](#472-compact-two-octet-longs)
        - [4.7.3. Compact: three octet longs](#473-compact-three-octet-longs)
        - [4.7.4. Compact: four octet longs](#474-compact-four-octet-longs)
        - [4.7.5. Long Examples](#475-long-examples)
    - [4.8. map](#48-map)
        - [4.8.1. Map examples](#481-map-examples)
    - [4.9. null](#49-null)
    - [4.10. object](#410-object)
        - [4.10.1. Compact: class definition](#4101-compact-class-definition)
        - [4.10.2. Compact: object instantiation](#4102-compact-object-instantiation)
        - [4.10.3. Object examples](#4103-object-examples)
    - [4.11. ref](#411-ref)
        - [4.11.1. Ref Examples](#4111-ref-examples)
    - [4.12. string](#412-string)
        - [4.12.1. Compact: short strings](#4121-compact-short-strings)
        - [4.12.2. String Examples](#4122-string-examples)
    - [4.13. type](#413-type)
    - [4.14. Compact: type references](#414-compact-type-references)
5. [Reference Maps](#reference-maps)
    - [5.1. value reference](#51-value-reference)
    - [5.2. class reference](#52-class-reference)
    - [5.3. type reference](#53-type-reference)
6. [Bytecode map](#bytecode-map)

## Introduction

Hessian is a dynamically-typed, binary serialization and Web Services protocol designed for object-oriented transmission.

## Design Goals

Hessian is dynamically-typed, compact, and portable across languages.  
The Hessian protocol has the following design goals:
- It must self-describe the serialized types, i.e. not require
  external schema or interface definitions.
- It must be language-independent, including supporting
  scripting languages.
- It must be readable or writable in a single pass.
- It must be as compact as possible.
- It must be simple, so it can be effectively tested and implemented.
- It must be as fast as possible.
- It must support Unicode strings.
- It must support 8-bit binary data without escaping or using
  attachments.
- It must support encryption, compression, signature, and
  transaction context envelopes.

## Hessian Grammar
### Serialization Grammar
```text
           # starting production
top        ::= value

           # 8-bit binary data split into 64k chunks
binary     ::= x41 b1 b0 &lt;binary-data> binary # non-final chunk
           ::= 'B' b1 b0 &lt;binary-data>        # final chunk
           ::= [x20-x2f] &lt;binary-data>        # binary data of 
                                                 #  length 0-15
           ::= [x34-x37] &lt;binary-data>        # binary data of 
                                                 # length 0-1023

           # boolean true/false
boolean    ::= 'T'
           ::= 'F'

           # definition for an object (compact map)
class-def  ::= 'C' string int string*

           # time in UTC encoded as 64-bit long milliseconds since 
           #  epoch
date       ::= x4a b7 b6 b5 b4 b3 b2 b1 b0
           ::= x4b b3 b2 b1 b0       # minutes since epoch

           # 64-bit IEEE double
double     ::= 'D' b7 b6 b5 b4 b3 b2 b1 b0
           ::= x5b                   # 0.0
           ::= x5c                   # 1.0
           ::= x5d b0                # byte cast to double 
                                     #  (-128.0 to 127.0)
           ::= x5e b1 b0             # short cast to double
           ::= x5f b3 b2 b1 b0       # 32-bit mills

           # 32-bit signed integer
int        ::= 'I' b3 b2 b1 b0
           ::= [x80-xbf]             # -x10 to x3f
           ::= [xc0-xcf] b0          # -x800 to x7ff
           ::= [xd0-xd7] b1 b0       # -x40000 to x3ffff

           # list/vector
list       ::= x55 type value* 'Z'   # variable-length list
	   ::= 'V' type int value*   # fixed-length list
           ::= x57 value* 'Z'        # variable-length untyped list
           ::= x58 int value*        # fixed-length untyped list
	   ::= [x70-77] type value*  # fixed-length typed list
	   ::= [x78-7f] value*       # fixed-length untyped list

           # 64-bit signed long integer
long       ::= 'L' b7 b6 b5 b4 b3 b2 b1 b0
           ::= [xd8-xef]             # -x08 to x0f
           ::= [xf0-xff] b0          # -x800 to x7ff
           ::= [x38-x3f] b1 b0       # -x40000 to x3ffff
           ::= x59 b3 b2 b1 b0       # 32-bit integer cast to long

           # map/object
map        ::= 'M' type (value value)* 'Z'  # key, value map pairs
	   ::= 'H' (value value)* 'Z'       # untyped key, value

           # null value
null       ::= 'N'

           # Object instance
object     ::= 'O' int value*
	   ::= [x60-x6f] value*

           # value reference (e.g. circular trees and graphs)
ref        ::= x51 int            # reference to nth map/list/object

           # UTF-8 encoded character string split into 64k chunks
string     ::= x52 b1 b0 &lt;utf8-data> string  # non-final chunk
           ::= 'S' b1 b0 &lt;utf8-data>         # string of length 
                                             #  0-65535
           ::= [x00-x1f] &lt;utf8-data>         # string of length 
                                             #  0-31
           ::= [x30-x34] &lt;utf8-data>         # string of length 
                                             #  0-1023

           # map/list types for OO languages
type       ::= string                        # type name
           ::= int                           # type reference

           # main production
value      ::= null
           ::= binary
           ::= boolean
           ::= class-def value
           ::= date
           ::= double
           ::= int
           ::= list
           ::= long
           ::= map
           ::= object
           ::= ref
           ::= string
```

## Serialization

Hessian's object serialization has 8 primitive types:

- [raw binary data](#41-binary-data)
- [boolean](#42-boolean)
- [64-bit millisecond date](#43-date)
- [64-bit double](#44-double)
- [32-bit int](#45-int)
- [64-bit long](#47-long)
- [null](#49-null)
- [UTF8-encoded string](#412-string)

It has 3 recursive types:

- [list for lists and arrays](#46-list)
- [map for maps and dictionaries](#48-map)
- [object for objects](#410-object)

Finally, it has one special contruct:

- [ref for shared and circular object references.](#411-ref)

Hessian 2.0 has 3 internal reference maps:

- [An object/list reference map.](#51-value-reference)
- [An class definition reference map.](#52-class-reference)
- [A type (class name) reference map.](#53-type-reference)

### 4.1. binary data

Binary Grammar
```text
binary ::= x41 b1 b0 &lt;binary-data> binary
       ::= B b1 b0 &lt;binary-data>
       ::= [x20-x2f] &lt;binary-data>
       ::= [x34-x37] b0 &lt;binary-data>
```
Binary data is encoded in chunks.  The octet x42 ('B') encodes
the final chunk and x41 ('A') represents any non-final chunk.
Each chunk has a 16-bit unsigned length value.

len = 256 * b1 + b0

#### 4.1.1. Compact: short binary

Binary data with length less than 15 may be encoded by a single
octet length [x20-x2f].

len = code - 0x20

#### 4.1.2. Binary Examples

```text
x20               # zero-length binary data

x23 x01 x02 x03   # 3 octet data

B x10 x00 ....    # 4k final chunk of data

A x04 x00 ....    # 1k non-final chunk of data
```

### 4.2. boolean

Boolean Grammar

```text
boolean ::= T
        ::= F
```

The octet 'F' represents false and the octet T represents true.

#### 4.2.1. Boolean Examples

```text
T   # true
F   # false
```

### 4.3. date

Date Grammar

```text
date ::= x4a b7 b6 b5 b4 b3 b2 b1 b0
     ::= x4b b4 b3 b2 b1 b0
```

Date represented by a 64-bit long of milliseconds since
Jan 1 1970 00:00H, UTC.

#### 4.3.1. Compact: date in minutes

The second form contains a 32-bit int of minutes since
Jan 1 1970 00:00H, UTC.

#### 4.3.2. Date Examples

```text
x4a x00 x00 x00 xd0 x4b x92 x84 xb8   # 09:51:31 May 8, 1998 UTC
```

```text
x4b x4b x92 x0b xa0                 # 09:51:00 May 8, 1998 UTC
```

### 4.4. double

Double Grammar

```text
double ::= D b7 b6 b5 b4 b3 b2 b1 b0
       ::= x5b
       ::= x5c
       ::= x5d b0
       ::= x5e b1 b0
       ::= x5f b3 b2 b1 b0
```

A 64-bit IEEE floating pointer number.

#### 4.4.1. Compact: double zero
The double 0.0 can be represented by the octet x5b

#### 4.4.2. Compact: double one
The double 1.0 can be represented by the octet x5c

#### 4.4.3. Compact: double octet
Doubles between -128.0 and 127.0 with no fractional component
can be represented in two octets by casting the byte value to a
double.

value = (double) b0

#### 4.4.4. Compact: double short
Doubles between -32768.0 and 32767.0 with no fractional component
can be represented in three octets by casting the short value to a
double.

value = (double) (256 * b1 + b0)

#### 4.4.5. Compact: double float
Doubles which are equivalent to their 32-bit float representation can be represented as the 4-octet float and then cast to double.

### 4.4.6. Double Examples

```text
x5b          # 0.0
x5c          # 1.0

x5d x00      # 0.0
x5d x80      # -128.0
x5d x7f      # 127.0

x5e x00 x00  # 0.0
x5e x80 x00  # -32768.0
x5e x7f xff  # 32767.0

D x40 x28 x80 x00 x00 x00 x00 x00  # 12.25
```

### 4.5. int

Integer Grammar

```text
int ::= 'I' b3 b2 b1 b0
    ::= [x80-xbf]
    ::= [xc0-xcf] b0
    ::= [xd0-xd7] b1 b0
```

A 32-bit signed integer.  An integer is represented by the
octet x49 ('I') followed by the 4 octets of the integer
in big-endian order.

value = (b3 << 24) + (b2 << 16) + (b1 << 8) + b0;

#### 4.5.1. Compact: single octet integers

Integers between -16 and 47 can be encoded by a single octet in the range x80 to xbf.

value = code - 0x90

#### 4.5.2. Compact: two octet integers

Integers between -2048 and 2047 can be encoded in two octets with the leading byte in the range xc0 to xcf.

value = ((code - 0xc8) << 8) + b0;

#### 4.5.3. Compact: three octet integers

Integers between -262144 and 262143 can be encoded in three bytes with the leading byte in the range xd0 to xd7.

value = ((code - 0xd4) << 16) + (b1 << 8) + b0;

#### 4.5.4. Integer Examples

```text
x90                # 0
x80                # -16
xbf                # 47

xc8 x00            # 0
xc0 x00            # -2048
xc7 x00            # -256
xcf xff            # 2047

xd4 x00 x00        # 0
xd0 x00 x00        # -262144
xd7 xff xff        # 262143

I x00 x00 x00 x00  # 0
I x00 x00 x01 x2c  # 300
```

### 4.6. list

List Grammar

```text
list ::= x55 type value* 'Z'   # variable-length list
     ::= 'V' type int value*   # fixed-length list
     ::= x57 value* 'Z'        # variable-length untyped list
     ::= x58 int value*        # fixed-length untyped list
     ::= [x70-77] type value*  # fixed-length typed list
     ::= [x78-7f] value*       # fixed-length untyped list
```

An ordered list, like an array. The two list productions are a fixed-length list and a variable length list. Both lists have a type. The type string may be an arbitrary UTF-8 string understood by the service.

Each list item is added to the reference list to handle shared and circular elements. See the ref element.

Any parser expecting a list must also accept a null or a shared ref.

The valid values of type are not specified in this document and may depend on the specific application. For example, a server implemented in a language with static typing which exposes an Hessian interface can use the type information to instantiate the specific array type. On the other hand, a server written in a dynamicly-typed language would likely ignore the contents of type entirely and create a generic array.

#### 4.6.1. Compact: fixed length list

Hessian 2.0 allows a compact form of the list for successive lists of the same type where the length is known beforehand. The type and length are encoded by integers, where the type is a reference to an earlier specified type.

#### 4.6.2. List examples

Serialization of a typed int array: int[] = {0, 1}

```text
V                    # fixed length, typed list
  x04 [int           # encoding of int[] type
  x92                # length = 2
  x90                # integer 0
  x91                # integer 1
```

untyped variable-length list = {0, 1}

```text
x57                  # variable-length, untyped
  x90                # integer 0
  x91                # integer 1
  Z
```

fixed-length type

```text
x72                # typed list length=2
  x04 [int         # type for int[] (save as type #0)
  x90              # integer 0
  x91              # integer 1

x73                # typed list length = 3
  x90              # type reference to int[] (integer #0)
  x92              # integer 2
  x93              # integer 3
  x94              # integer 4
```
### 4.7. long

Long Grammar

```text
long ::= L b7 b6 b5 b4 b3 b2 b1 b0
     ::= [xd8-xef]
     ::= [xf0-xff] b0
     ::= [x38-x3f] b1 b0
     ::= x4c b3 b2 b1 b0
```
A 64-bit signed integer. An long is represented by the octet x4c ('L' ) followed by the 8-bytes of the integer in big-endian order.

#### 4.7.1. Compact: single octet longs

Longs between -8 and 15 are represented by a single octet in the range xd8 to xef.

value = (code - 0xe0)

#### 4.7.2. Compact: two octet longs

Longs between -2048 and 2047 are encoded in two octets with the leading byte in the range xf0 to xff.

value = ((code - 0xf8) << 8) + b0

#### 4.7.3. Compact: three octet longs

Longs between -262144 and 262143 are encoded in three octets with the leading byte in the range x38 to x3f.

value = ((code - 0x3c) << 16) + (b1 << 8) + b0

#### 4.7.4. Compact: four octet longs

Longs between which fit into 32-bits are encoded in five octets with the leading byte x4c.

value = (b3 << 24) + (b2 << 16) + (b1 << 8) + b0

#### 4.7.5. Long Examples

```text
xe0                  # 0
xd8                  # -8
xef                  # 15

xf8 x00              # 0
xf0 x00              # -2048
xf7 x00              # -256
xff xff              # 2047

x3c x00 x00          # 0
x38 x00 x00          # -262144
x3f xff xff          # 262143

x4c x00 x00 x00 x00  # 0
x4c x00 x00 x01 x2c  # 300

L x00 x00 x00 x00 x00 x00 x01 x2c  # 300
```

### 4.8. map

Map Grammar

```text
map        ::= M type (value value)* Z
```

Represents serialized maps and can represent objects. The type element describes the type of the map.

The type may be empty, i.e. a zero length. The parser is responsible for choosing a type if one is not specified. For objects, unrecognized keys will be ignored.

Each map is added to the reference list. Any time the parser expects a map, it must also be able to support a null or a ref.

The type is chosen by the service.

#### 4.8.1. Map examples

A sparse array

```text
map = new HashMap();
map.put(new Integer(1), "fee");
map.put(new Integer(16), "fie");
map.put(new Integer(256), "foe");

---

H           # untyped map (HashMap for Java)
  x91       # 1
  x03 fee   # "fee"

  xa0       # 16
  x03 fie   # "fie"

  xc9 x00   # 256
  x03 foe   # "foe"

  Z
```

Map Representation of a Java Object

```text
public class Car implements Serializable {
  String color = "aquamarine";
  String model = "Beetle";
  int mileage = 65536;
}

---
M
  x13 com.caucho.test.Car  # type

  x05 color                # color field
  x0a aquamarine

  x05 model                # model field
  x06 Beetle

  x07 mileage              # mileage field
  I x00 x01 x00 x00
  Z
```

### 4.9. null

Null Grammar

```text
null ::= N
```
Null represents a null pointer.

The octet `N` represents the null value.

### 4.10. object

Object Grammar

```text
class-def  ::= 'C' string int string*

object     ::= 'O' int value*
           ::= [x60-x6f] value*
```

#### 4.10.1.  Compact: class definition

Hessian 2.0 has a compact object form where the field names are only serialized once. Following objects only need to serialize their values.

The object definition includes a mandatory type string, the number of fields, and the field names. The object definition is stored in the object definition map and will be referenced by object instances with an integer reference.


#### 4.10.2.  Compact: object instantiation

Hessian 2.0 has a compact object form where the field names are only serialized once. Following objects only need to serialize their values.

The object instantiation creates a new object based on a previous definition. The integer value refers to the object definition.

#### 4.10.3.  Object examples

Object serialization

```text
class Car {
  String color;
  String model;
}

out.writeObject(new Car("red", "corvette"));
out.writeObject(new Car("green", "civic"));

---

C                        # object definition (#0)
  x0b example.Car        # type is example.Car
  x92                    # two fields
  x05 color              # color field name
  x05 model              # model field name

O                        # object def (long form)
  x90                    # object definition #0
  x03 red                # color field value
  x08 corvette           # model field value

x60                      # object def #0 (short form)
  x05 green              # color field value
  x05 civic              # model field value
```

```text
enum Color {
  RED,
  GREEN,
  BLUE,
}

out.writeObject(Color.RED);
out.writeObject(Color.GREEN);
out.writeObject(Color.BLUE);
out.writeObject(Color.GREEN);

---

C                         # class definition #0
  x0b example.Color       # type is example.Color
  x91                     # one field
  x04 name                # enumeration field is "name"

x60                       # object #0 (class def #0)
  x03 RED                 # RED value

x60                       # object #1 (class def #0)
  x90                     # object definition ref #0
  x05 GREEN               # GREEN value

x60                       # object #2 (class def #0)
  x04 BLUE                # BLUE value

x51 x91                   # object ref #1, i.e. Color.GREEN
```

### 4.11. ref

Ref Grammar

```text
ref ::= x51 int
```

An integer referring to a previous list, map, or object instance. As each list, map or object is read from the input stream, it is assigned the integer position in the stream, i.e. the first list or map is `0`, the next is `1`, etc. A later ref can then use the previous object. Writers MAY generate refs. Parsers MUST be able to recognize them.

ref can refer to incompletely-read items. For example, a circular linked-list will refer to the first link before the entire list has been read.

A possible implementation would add each map, list, and object to an array as it is read. The ref will return the corresponding value from the array. To support circular structures, the implementation would store the map, list or object immediately, before filling in the contents.

Each map or list is stored into an array as it is parsed. ref selects one of the stored objects. The first object is numbered `0`.

#### 4.11.1. Ref examples

Circular list

```text
list = new LinkedList();
list.data = 1;
list.tail = list;

---
C
  x0a LinkedList
  x92
  x04 head
  x04 tail

o x90      # object stores ref #0
  x91      # data = 1
  x51 x90  # next field refers to itself, i.e. ref #0
```

ref only refers to list, map and objects elements. Strings and binary data, in particular, will only share references if they're wrapped in a list or map.

### 4.12. string

String Grammar

```text
string ::= x52 b1 b0 <utf8-data> string
       ::= S b1 b0 <utf8-data>
       ::= [x00-x1f] <utf8-data>
       ::= [x30-x33] b0 <utf8-data>
```

A 16-bit unicode character string encoded in UTF-8. Strings are encoded in chunks. x53 ('S') represents the final chunk and x52 ('R') represents any non-final chunk. Each chunk has a 16-bit unsigned integer length value.

The length is the number of 16-bit characters, which may be different than the number of bytes.

String chunks may not split surrogate pairs.

#### 4.12.1. Compact: short strings

Strings with length less than 32 may be encoded with a single octet length [x00-x1f].

value = code

#### 4.12.2. String Examples

```text
x00                 # "", empty string
x05 hello           # "hello"
x01 xc3 x83         # "\u00c3"

S x00 x05 hello     # "hello" in long form

x52 x00 x07 hello,  # "hello, world" split into two chunks
  x05 world
```

### 4.13. type

Type Grammar

```text
type ::= string
     ::= int
```

A [map](#48-map) or [list](#46-list) includes a type attribute indicating the type name of the map or list for object-oriented languages.

Each type is added to the [type map](#53-type-reference) for future reference.

### 4.14. Compact: type references

Repeated type strings MAY use the [type map](#53-type-reference) to refer to a previously used type. The type reference is zero-based over all the types encountered during parsing.

## Reference Maps

Hessian 2.0 has 3 internal reference maps:

   - An map/object/list reference map.
   - An class definition map.
   - A type (class name) map.

The value reference map lets Hessian support arbitrary graphs, and recursive and circular data structures.

The class and type maps improve Hessian efficiency by avoiding repetition of common string data.

### 5.1. Value Reference

Hessian supports arbitrary graphs by adding [list](#46-list), [object](#410-object), and [map](#48-map) as it encounters them in the bytecode stream.

Parsers MUST store each list, object and map in the reference map as they are encountered.

The stored objects can be used with a [ref](#411-ref) bytecode.

### 5.2. Class Reference

Each [object definition](#410-object) is automatically added to the class-map. Parsers MUST add a class definition to the class map as each is encountered. Following object instances will refer to the defined class.

### 5.3. Type Reference

The [type](#413-type) strings for [map](#48-map) and [list](#46-list) values are stored in a type map for reference.

Parsers MUST add a type string to the type map as each is encountered.

## Bytecode map

Hessian is organized as a bytecode protocol. A Hessian reader is essentially a switch statement on the initial octet.

Bytecode Encoding

```text
x00 - x1f    # utf-8 string length 0-32
x20 - x2f    # binary data length 0-16
x30 - x33    # utf-8 string length 0-1023
x34 - x37    # binary data length 0-1023
x38 - x3f    # three-octet compact long (-x40000 to x3ffff)
x40          # reserved (expansion/escape)
x41          # 8-bit binary data non-final chunk ('A')
x42          # 8-bit binary data final chunk ('B')
x43          # object type definition ('C')
x44          # 64-bit IEEE encoded double ('D')
x45          # reserved
x46          # boolean false ('F')
x47          # reserved
x48          # untyped map ('H')
x49          # 32-bit signed integer ('I')
x4a          # 64-bit UTC millisecond date
x4b          # 32-bit UTC minute date
x4c          # 64-bit signed long integer ('L')
x4d          # map with type ('M')
x4e          # null ('N')
x4f          # object instance ('O')
x50          # reserved
x51          # reference to map/list/object - integer ('Q')
x52          # utf-8 string non-final chunk ('R')
x53          # utf-8 string final chunk ('S')
x54          # boolean true ('T')
x55          # variable-length list/vector ('U')
x56          # fixed-length list/vector ('V')
x57          # variable-length untyped list/vector ('W')
x58          # fixed-length untyped list/vector ('X')
x59          # long encoded as 32-bit int ('Y')
x5a          # list/map terminator ('Z')
x5b          # double 0.0
x5c          # double 1.0
x5d          # double represented as byte (-128.0 to 127.0)
x5e          # double represented as short (-32768.0 to 327676.0)
x5f          # double represented as float
x60 - x6f    # object with direct type
x70 - x77    # fixed list with direct length
x78 - x7f    # fixed untyped list with direct length
x80 - xbf    # one-octet compact int (-x10 to x3f, x90 is 0)
xc0 - xcf    # two-octet compact int (-x800 to x7ff)
xd0 - xd7    # three-octet compact int (-x40000 to x3ffff)
xd8 - xef    # one-octet compact long (-x8 to xf, xe0 is 0)
xf0 - xff    # two-octet compact long (-x800 to x7ff, xf8 is 0)
```
[TOC](#table-of-contents)
