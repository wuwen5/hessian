# Hessian 2.0 序列化协议

**作者:**
- Scott Ferguson (Caucho Technology Inc.) - ferg@caucho.com
- Emil Ong (Caucho Technology Inc.) - emil@caucho.com  
  **日期:** August 2007

## 目录

1. [介绍](#介绍)
2. [设计目标](#设计目标)
3. [Hessian 语法](#Hessian-语法)
4. [序列化](#序列化)
    - [4.1. binary data](#41-binary-data)
        - [4.1.1. 紧凑格式：短二进制](#411-compact-short-binary)
        - [4.1.2. 二进制示例](#412-binary-examples)
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

## 介绍

Hessian是一种动态类型的二进制序列化和Web服务协议，专为面向对象的传输而设计。

## 设计目标

Hessian具有动态类型、紧凑且可跨语言移植的特性。  
Hessian协议的设计目标如下：
- 必须自描述序列化类型，即不需要外部模式或接口定义
- 必须与语言无关，包括支持脚本语言
- 必须支持单次读写操作
- 必须尽可能紧凑
- 必须简单，以便有效测试和实现
- 必须尽可能快速
- 必须支持`Unicode`字符串
- 必须支持8位二进制数据而不需要转义或使用附件
- 必须支持加密、压缩、签名和事务上下文信封

## Hessian 语法
### 序列化语法
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

## 序列化

Hessian的对象序列化包含8种基本类型：

- [raw binary data](#41-binary-data)
- [boolean](#42-boolean)
- [64-bit millisecond date](#43-date)
- [64-bit double](#44-double)
- [32-bit int](#45-int)
- [64-bit long](#47-long)
- [null](#49-null)
- [UTF8-encoded string](#412-string)

包含3种递归类型：

- [list for lists and arrays](#46-list)
- [map for maps and dictionaries](#48-map)
- [object for objects](#410-object)

最后，包含一种特殊结构：

- [ref for shared and circular object references.](#411-ref)

Hessian 2.0包含3个内部引用映射：

- [An object/list reference map.](#51-value-reference)
- [A class definition reference map.](#52-class-reference)
- [A type (class name) reference map.](#53-type-reference)

### 4.1. binary data

Binary 语法
```text
binary ::= x41 b1 b0 &lt;binary-data> binary
       ::= B b1 b0 &lt;binary-data>
       ::= [x20-x2f] &lt;binary-data>
       ::= [x34-x37] b0 &lt;binary-data>
```
二进制数据以块形式编码。8位字节`x42`(`B`)编码最终块，`x41`(`A`)表示任何非最终块。每个块都有一个16位无符号长度值。

len = 256 * b1 + b0

#### 4.1.1. Compact: short binary

长度小于15的二进制数据可以通过单个8位字节长度`[x20-x2f]`编码。

len = code - 0x20

#### 4.1.2. Binary Examples

```text
x20               # zero-length binary data

x23 x01 x02 x03   # 3 octet data

B x10 x00 ....    # 4k final chunk of data

A x04 x00 ....    # 1k non-final chunk of data
```

### 4.2. boolean

Boolean 语法

```text
boolean ::= T
        ::= F
```

8位字节`F`表示假，`T`表示真。

#### 4.2.1. Boolean Examples

```text
T   # true
F   # false
```

### 4.3. date

Date 语法

```text
date ::= x4a b7 b6 b5 b4 b3 b2 b1 b0
     ::= x4b b4 b3 b2 b1 b0
```

日期表示为自 1970年1月1日 00:00H UTC 起的64位长整型毫秒数。

#### 4.3.1. Compact: date in minutes

第二种形式包含自 1970年1月1日 00:00H UTC 起的32位整型分钟数。

#### 4.3.2. Date Examples

```text
x4a x00 x00 x00 xd0 x4b x92 x84 xb8   # 09:51:31 May 8, 1998 UTC
```

```text
x4b x4b x92 x0b xa0                 # 09:51:00 May 8, 1998 UTC
```

### 4.4. double

Double 语法

```text
double ::= D b7 b6 b5 b4 b3 b2 b1 b0
       ::= x5b
       ::= x5c
       ::= x5d b0
       ::= x5e b1 b0
       ::= x5f b3 b2 b1 b0
```

64位IEEE浮点数。

#### 4.4.1. Compact: double zero

double`0.0`可以用8位字节`x5b`表示

#### 4.4.2. Compact: double one

double`1.0`可以用8位字节`x5c`表示

#### 4.4.3. Compact: double octet

`-128.0`到`127.0`之间无小数部分的`double`可以用两个8位字节表示，通过将 `byte value` 转换为`double`。

value = (double) b0

#### 4.4.4. Compact: double short

`-32768.0`到`32767.0`之间无小数部分的 `double` 可以用3个8位字节表示，通过将 `short value` 转换为 `double`。

value = (double) (256 * b1 + b0)

#### 4.4.5. Compact: double float
等同于 32-bit float 表示的 double 可以表示为 4-octet float，然后转换为double。

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

Integer 语法

```text
int ::= 'I' b3 b2 b1 b0
    ::= [x80-xbf]
    ::= [xc0-xcf] b0
    ::= [xd0-xd7] b1 b0
```

32位有符号整型。整型由8位字节`x49`(`I`)后跟大端序的4字节整型表示。

value = (b3 << 24) + (b2 << 16) + (b1 << 8) + b0;

#### 4.5.1. Compact: single octet integers

`-16`到`47`之间的整型可以通过范围`x80`到`xbf`内的单个8位字节编码。

value = code - 0x90

#### 4.5.2. Compact: two octet integers

`-2048`到`2047`之间的整型可以通过两个8位字节编码，前导字节在范围`xc0`到`xcf`内。

value = ((code - 0xc8) << 8) + b0;

#### 4.5.3. Compact: three octet integers

`-262144`到`262143`之间的整型可以通过3个字节编码，前导字节在范围`xd0`到`xd7`内。

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

List 语法

```text
list ::= x55 type value* 'Z'   # variable-length list
     ::= 'V' type int value*   # fixed-length list
     ::= x57 value* 'Z'        # variable-length untyped list
     ::= x58 int value*        # fixed-length untyped list
     ::= [x70-77] type value*  # fixed-length typed list
     ::= [x78-7f] value*       # fixed-length untyped list
```

有序列表，类似于数组。两种列表产生式是固定长度列表和可变长度列表。两种列表都有类型。类型字符串可以是服务理解的任意UTF-8字符串。

每个列表项都会添加到引用列表中以处理共享和循环元素。参见`ref`元素。

任何期望列表的解析器也必须接受空值或共享引用。

类型有效值未在本文档中指定，可能取决于特定应用。例如，使用静态类型语言实现并公开Hessian接口的服务器可以使用类型信息实例化特定数组类型。另一方面，使用动态类型语言编写的服务器可能完全忽略类型内容并创建通用数组。

#### 4.6.1. Compact: fixed length list

Hessian 2.0 允许对已知长度的相同类型的连续列表使用紧凑形式。类型和长度通过整型编码，其中类型是对先前指定类型的引用。

#### 4.6.2. List examples

整型数组序列化: int[] = {0, 1}

```text
V                    # fixed length, typed list
  x04 [int           # encoding of int[] type
  x92                # length = 2
  x90                # integer 0
  x91                # integer 1
```

无类型可变长度 list = {0, 1}

```text
x57                  # variable-length, untyped
  x90                # integer 0
  x91                # integer 1
  Z
```

固定长度类型

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

Long 语法

```text
long ::= L b7 b6 b5 b4 b3 b2 b1 b0
     ::= [xd8-xef]
     ::= [xf0-xff] b0
     ::= [x38-x3f] b1 b0
     ::= x4c b3 b2 b1 b0
```
64位有符号整型。长整型由8位字节`x4c`(`L`)后跟大端序的8字节整型表示。

#### 4.7.1. Compact: single octet longs

`-8`到`15`之间的长整型可以通过范围`xd8`到`xef`内的单个8位字节表示。

value = (code - 0xe0)

#### 4.7.2. Compact: two octet longs

`-2048`到`2047`之间的长整型可以通过两个8位字节编码，前导字节在范围`xf0`到`xff`内。

value = ((code - 0xf8) << 8) + b0

#### 4.7.3. Compact: three octet longs

`-262144`到`262143`之间的长整型可以通过3个8位字节编码，前导字节在范围`x38`到`x3f`内。

value = ((code - 0x3c) << 16) + (b1 << 8) + b0

#### 4.7.4. Compact: four octet longs

适合`32`位的长整型可以通过5个8位字节编码，前导字节为`x4c`。

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

Map 语法

```text
map        ::= M type (value value)* Z
```

表示序列化的 `Map`，可以表示对象。type元素描述 `Map` 的类型。

类型可以为空，即零长度。如果未指定类型，解析器负责选择类型。对于对象，无法识别的 `key` 将被忽略。

每个 `Map` 都会添加到引用列表。任何时候解析器期望 `Map` 时，也必须支持 `null` 或引用。

类型由服务选择。

#### 4.8.1. Map examples

稀疏数组

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

Java对象的 `Map` 表示

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

Null 语法

```text
null ::= N
```
空值 `null` 表示空指针。

8位字节 `N` 表示 `null`。

### 4.10. object

Object 语法

```text
class-def  ::= 'C' string int string*

object     ::= 'O' int value*
           ::= [x60-x6f] value*
```

#### 4.10.1.  Compact: class definition

Hessian 2.0 具有紧凑对象形式，其中字段名仅序列化一次。后续对象只需序列化其值。

对象定义包含必需的类型字符串、字段数量和字段名称。对象定义存储在对象定义映射中，对象实例将通过整型引用引用它。

#### 4.10.2.  Compact: object instantiation

Hessian 2.0 具有紧凑对象形式，其中字段名仅序列化一次。后续对象只需序列化其值。

对象实例化基于先前的定义创建新对象。整型值引用对象定义。

#### 4.10.3.  Object examples

Object 序列化

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

Ref 语法

```text
ref ::= x51 int
```

引用先前 `list`、`map` 或 `object` 实例的整型。当每个 `list`、`map` 或 `object` 从输入流中读取时，会被分配流中的整型位置，即第一个列表或映射为 `0`，下一个为 `1`，依此类推。后续引用可以使用先前的对象。写入器可以生成引用。解析器必须能够识别它们。

引用可以引用未完全读取的项。例如，循环链表将在整个列表被读取之前引用第一个链接。

可能的实现是将每个 `map`、`list` 和 `object`在读取时添加到数组中。引用将返回数组中对应的值。为了支持循环结构，实现会在填充内容之前立即存储`map`、`list` 或 `object`。

每个 `map` 或 `list` 在解析时存储到数组中。`ref`选择其中一个存储的对象。第一个对象编号为`0`。

#### 4.11.1. Ref examples

循环 list

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

`ref` 仅引用`list`、`map` 和 `object` 元素。特别是字符串和二进制数据只有在包装在 `list` 或 `map` 中时才会共享引用。

### 4.12. string

String 语法

```text
string ::= x52 b1 b0 <utf8-data> string
       ::= S b1 b0 <utf8-data>
       ::= [x00-x1f] <utf8-data>
       ::= [x30-x33] b0 <utf8-data>
```

`UTF-8`编码的`16-bit`Unicode字符串。字符串以块形式编码。`x53`(`S`)表示最终块，`x52`(`R`)表示任何非最终块。每个块都有一个 `16-bit` 无符号整型长度值。

长度是`16-bit`字符的数量，可能与字节数不同。

字符串块不能分割代理对。处理UTF-16字符串时，必须确保每个代理对（由高低两个代理项组成的四字节字符）在分割操作中保持完整，否则会导致乱码。

#### 4.12.1. Compact: short strings

长度小于32的字符串可以通过单个8位字节长度`[x00-x1f]`编码。

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

Type 语法

```text
type ::= string
     ::= int
```

[map](#48-map) 或 [list](#46-list) 包含类型属性，指示面向对象语言的`map`或`list`的类型名称。

每个类型都会添加到[type map](#53-type-reference)以供将来引用。

### 4.14. Compact: type references

重复的类型字符串可以使用 [type map](#53-type-reference) 引用先前使用的类型。类型引用基于解析过程中遇到的所有类型从零开始编号。

## Reference Maps

Hessian 2.0 具有3个内部引用Map:

   - map/object/list 引用Map.
   - 类定义Map.
   - 类型（类名）Map.

值引用映射允许Hessian支持任意图、递归和循环数据结构。

类和类型映射通过避免重复常见字符串数据来提高Hessian效率。

### 5.1. Value Reference

Hessian 通过在字节码流中遇到 [list](#46-list)、[object](#410-object) 和 [map](#48-map) 时添加它们来支持任意图。

解析器必须在遇到每个 `list`、`object` 和 `map` 时将其存储在引用映射中。

存储的对象可以与 [ref](#411-ref) 字节码一起使用。

### 5.2. Class Reference

每个 [object definition](#410-object) 都会自动添加到类映射中。解析器必须在遇到每个类定义时将其添加到类映射中。后续对象实例将引用已定义的类。

### 5.3. Type Reference

[map](#48-map) 和 [list](#46-list) 值的 [type](#413-type) 字符串存储在类型映射中以供引用。

解析器必须在遇到每个类型字符串时将其添加到类型映射中。

## Bytecode map

Hessian 被组织为字节码协议。Hessian 读取器本质上是基于初始8位字节的`switch`语句。

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
[TOC](#目录)
