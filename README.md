## Hessian2 Serialization

[![Java CI](https://github.com/wuwen5/hessian/actions/workflows/ci.yml/badge.svg)](https://github.com/wuwen5/hessian/actions/workflows/ci.yml)
<a href="https://openjdk.java.net/"><img src="https://img.shields.io/badge/Java-11+-339933?logo=openjdk&logoColor=white" alt="JDK support"></a>
[![codecov](https://codecov.io/gh/wuwen5/hessian/branch/main/graph/badge.svg)](https://codecov.io/gh/wuwen5/hessian)
[![Coverage Status](https://coveralls.io/repos/github/wuwen5/hessian/badge.svg?branch=main)](https://coveralls.io/github/wuwen5/hessian?branch=main)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=wuwen5_hessian&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=wuwen5_hessian)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=wuwen5_hessian&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=wuwen5_hessian)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=wuwen5_hessian&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=wuwen5_hessian)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=wuwen5_hessian&metric=bugs)](https://sonarcloud.io/summary/new_code?id=wuwen5_hessian)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.wuwen5.hessian/hessian/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.wuwen5.hessian/hessian/)
[![Last SNAPSHOT](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fio%2Fgithub%2Fwuwen5%2Fhessian%2Fhessian%2Fmaven-metadata.xml&label=latest%20snapshot)](https://central.sonatype.com/repository/maven-snapshots/io/github/wuwen5/hessian/hessian/maven-metadata.xml)
[![GitHub release](https://img.shields.io/github/release/wuwen5/hessian.svg)](https://github.com/wuwen5/hessian/releases)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![zread](https://img.shields.io/badge/Ask_Zread-_.svg?style=flat&color=00b0aa&labelColor=000000&logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZpZXdCb3g9IjAgMCAxNiAxNiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTQuOTYxNTYgMS42MDAxSDIuMjQxNTZDMS44ODgxIDEuNjAwMSAxLjYwMTU2IDEuODg2NjQgMS42MDE1NiAyLjI0MDFWNC45NjAxQzEuNjAxNTYgNS4zMTM1NiAxLjg4ODEgNS42MDAxIDIuMjQxNTYgNS42MDAxSDQuOTYxNTZDNS4zMTUwMiA1LjYwMDEgNS42MDE1NiA1LjMxMzU2IDUuNjAxNTYgNC45NjAxVjIuMjQwMUM1LjYwMTU2IDEuODg2NjQgNS4zMTUwMiAxLjYwMDEgNC45NjE1NiAxLjYwMDFaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00Ljk2MTU2IDEwLjM5OTlIMi4yNDE1NkMxLjg4ODEgMTAuMzk5OSAxLjYwMTU2IDEwLjY4NjQgMS42MDE1NiAxMS4wMzk5VjEzLjc1OTlDMS42MDE1NiAxNC4xMTM0IDEuODg4MSAxNC4zOTk5IDIuMjQxNTYgMTQuMzk5OUg0Ljk2MTU2QzUuMzE1MDIgMTQuMzk5OSA1LjYwMTU2IDE0LjExMzQgNS42MDE1NiAxMy43NTk5VjExLjAzOTlDNS42MDE1NiAxMC42ODY0IDUuMzE1MDIgMTAuMzk5OSA0Ljk2MTU2IDEwLjM5OTlaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik0xMy43NTg0IDEuNjAwMUgxMS4wMzg0QzEwLjY4NSAxLjYwMDEgMTAuMzk4NCAxLjg4NjY0IDEwLjM5ODQgMi4yNDAxVjQuOTYwMUMxMC4zOTg0IDUuMzEzNTYgMTAuNjg1IDUuNjAwMSAxMS4wMzg0IDUuNjAwMUgxMy43NTg0QzE0LjExMTkgNS42MDAxIDE0LjM5ODQgNS4zMTM1NiAxNC4zOTg0IDQuOTYwMVYyLjI0MDFDMTQuMzk4NCAxLjg4NjY0IDE0LjExMTkgMS42MDAxIDEzLjc1ODQgMS42MDAxWiIgZmlsbD0iI2ZmZiIvPgo8cGF0aCBkPSJNNCAxMkwxMiA0TDQgMTJaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00IDEyTDEyIDQiIHN0cm9rZT0iI2ZmZiIgc3Ryb2tlLXdpZHRoPSIxLjUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIvPgo8L3N2Zz4K&logoColor=ffffff)](https://zread.ai/wuwen5/hessian)

[English](./README_en.md) | 简体中文 

## 简介

本项目基于原始 [Hessian](http://hessian.caucho.com/) 仓库进行改造，剥离了所有与 RPC 调用相关的功能，专注于 Hessian 序列化协议的持续维护与模块化改造。

> 本项目的初始提交来源于 [https://repo1.maven.org/maven2/com/caucho/hessian/](https://repo1.maven.org/maven2/com/caucho/hessian/) 中的 `hessian-4.0.xx-sources.jar` 源码。

Hessian 其序列化协议因以下优点，仍具有广泛的实际应用：

* ⚡ **高性能**：序列化与反序列化速度快
* 📦 **体积小**：编码紧凑，适合网络传输
* 🌐 **跨语言**：可用于多语言系统之间的数据交换
* 🛠️ **易用性**：使用简单，无需预定义数据结构

### ✅ 对象图复用：Hessian2 的独特优势

Hessian2 原生支持**对象引用复用**与**循环引用结构**，能自动识别和复用重复对象实例，并正确还原循环引用，适用于复杂对象图结构。

相比之下：

| 序列化协议        | 支持引用复用 | 支持循环引用 | 备注                 |
| ------------ | ------ | ------ | ------------------ |
| **Hessian2** | ✅ 是    | ✅ 是    | 原生支持，无需额外配置        |
| **JSON (标准实现)**     | ❌ 否    | ❌ 否    | 默认仅支持值拷贝，循环引用会抛异常；部分 JSON 库可通过注解或自定义策略实现引用支持   |
| **Protobuf** | ❌ 否    | ❌ 否    | 树状结构，无法表达共享引用或循环引用，需要在应用层使用 ID 或映射处理共享对象 |

### 🌍 Hessian2 的不可替代性

基于对象引用复用能力，Hessian2 在以下场景中依然具有不可替代的价值：

* 对象缓存、持久化等需要共享或循环引用还原的业务场景
* 深度克隆、运行时状态快照
* Java-to-Java 微服务之间上下文传递
* 高度还原对象图的 RPC 框架数据传输

即使在现代系统中，**Hessian2 仍以其高效性、紧凑性和表达力在某些特定领域不可替代。**

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

我们欢迎社区贡献，并持续维护 Hessian 序列化协议的 Java 实现。

## 📦 Maven 依赖

- 基础功能依赖
  
如果只需要 Hessian 序列化/反序列化功能，可以仅引入 hessian2-codec：
```xml
<dependency>
    <groupId>io.github.wuwen5.hessian</groupId>
    <artifactId>hessian2-codec</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

- 在 Dubbo 中使用

在 Dubbo 场景下，需要同时引入 hessian-dubbo-adapter 模块，并排除 Dubbo 自带的 hessian-lite：

```xml
<dependencies>
  <dependency>
    <groupId>io.github.wuwen5.hessian</groupId>
    <artifactId>hessian2-codec</artifactId>
    <version>0.1.0-SNAPSHOT</version>
  </dependency>
  <dependency>
    <groupId>io.github.wuwen5.hessian</groupId>
    <artifactId>hessian-dubbo-adapter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
  </dependency>
  <dependency>
    <groupId>org.apache.dubbo</groupId>
    <artifactId>dubbo</artifactId>
    <version>${dubbo.version}</version>
    <exclusions>
      <exclusion>
        <groupId>com.alibaba</groupId>
        <artifactId>hessian-lite</artifactId>
      </exclusion>
    </exclusions>
  </dependency>
</dependencies>
```
