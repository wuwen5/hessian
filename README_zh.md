## Hessian2 Serialization

[![Java CI](https://github.com/wuwen5/hessian/actions/workflows/ci.yml/badge.svg)](https://github.com/wuwen5/hessian/actions/workflows/ci.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.wuwen5.hessian/hessian/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.wuwen5.hessian/hessian/)
[![GitHub release](https://img.shields.io/github/release/wuwen5/hessian.svg)](https://github.com/wuwen5/hessian/releases)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

[English](./README.md) | 简体中文 

## 简介

本项目基于原始 [Hessian](http://hessian.caucho.com/) 仓库进行改造，剥离了所有与 RPC 调用相关的功能，专注于 Hessian 序列化协议的持续维护与模块化改造。

> 本项目的初始提交来源于 [https://repo1.maven.org/maven2/com/caucho/hessian/](https://repo1.maven.org/maven2/com/caucho/hessian/) 中的 `hessian-4.0.xx-sources.jar` 源码。

虽然 Hessian 作为 RPC 协议已逐渐过时，但其序列化协议因以下优点，仍具有广泛的实际应用价值：

* **高性能**：序列化与反序列化速度快
* **体积小**：编码紧凑，适合网络传输
* **跨语言**：可用于多语言系统之间的数据交换
* **易用性**：使用简单，无需预定义数据结构

### Hessian2 协议简介

Hessian2 是 Hessian 协议的升级版本，主要特性包括：

* 使用 **二进制紧凑格式** 表示 Java 对象、数组、Map、List 等结构
* 支持 **对象引用**（避免重复序列化相同实例）
* 支持 **类定义缓存**，提高传输效率
* 可用于 Java 与其他语言之间的数据交互（如 Python、Go 社区实现）

📄 **协议文档请参见**：[Hessian2 序列化协议文档（简体中文）](./docs/hessian-serialization_zh.md)

### 本项目的特点

* 剥离原始 Hessian 项目的 RPC 框架代码
* 采用模块化结构，更易于集成与扩展
* 仅保留 **Hessian 2 协议** （本项目不再支持 Hessian1.0）
* 适合用作轻量级、高效的数据序列化方案
* 支持 Java 11 及以上版本

### 使用场景

* 自定义 RPC 或消息中间件的序列化层
* 数据压缩传输
* 嵌入式、移动端、IoT 等资源受限场景

我们欢迎社区贡献，并持续维护 Hessian 序列化协议的 Java 实现。

## Maven dependency

```xml
<dependency>
    <groupId>io.github.wuwen5.hessian</groupId>
    <artifactId>hessian2-codec</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
