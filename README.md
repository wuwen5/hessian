## Hessian2 Serialization

[![Java CI](https://github.com/wuwen5/hessian/actions/workflows/ci.yml/badge.svg)](https://github.com/wuwen5/hessian/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/wuwen5/hessian/branch/main/graph/badge.svg)](https://codecov.io/gh/wuwen5/hessian)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.wuwen5.hessian/hessian/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.wuwen5.hessian/hessian/)
[![GitHub release](https://img.shields.io/github/release/wuwen5/hessian.svg)](https://github.com/wuwen5/hessian/releases)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

English | [简体中文](./README_zh.md) 

## Introduction

This project is a refactored and modularized version of the original [Hessian](http://hessian.caucho.com/) repository, with all RPC-related logic removed. It focuses solely on maintaining and enhancing the **Hessian serialization protocol**.

> The new commits are copied from the source code of `hessian-4.0.xx-sources.jar` in [https://repo1.maven.org/maven2/com/caucho/hessian/](https://repo1.maven.org/maven2/com/caucho/hessian/)

While the **Hessian RPC framework** is considered outdated today, its **binary serialization protocol** remains valuable due to the following key advantages:

* **High Performance**: Fast (de)serialization
* **Compact Size**: Efficient binary encoding
* **Cross-language Compatibility**: Useful in polyglot systems
* **Ease of use**: simple to use, no predefined data structure required

### Overview of Hessian2 Protocol

Hessian2 is an enhanced version of the original Hessian protocol, with the following features:

* Binary serialization of Java primitives, collections, and custom classes
* **Object reference support** to prevent redundant serialization
* **Class definition caching** to reduce payload size
* Designed for **high-efficiency data transmission**
* Not compatible with Hessian1 protocol (this project drops Hessian1 support)

📄 **Protocol Documentation**: [Hessian2 Protocol Specification (English)](./docs/hessian-serialization.md)

### Key Features

* Strip the RPC-related code from the original Hessian project
* Modularized architecture for better extensibility and maintenance
* Supports **Hessian 2 protocol only**
* Ideal for use as a standalone, high-efficiency serialization library

### Use Cases

* Pluggable serialization layer in custom RPC or messaging systems
* Compact data transfer in bandwidth-constrained environments
* Lightweight and efficient data encoding for IoT, mobile, or embedded systems

Community contributions are welcome. We are committed to keeping a clean, modular, and efficient implementation of the Hessian serialization protocol in Java.

## Maven dependency

```xml
<dependency>
    <groupId>io.github.wuwen5.hessian</groupId>
    <artifactId>hessian2-codec</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
