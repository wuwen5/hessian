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

English | [简体中文](./README.md) 

## Introduction

This project is a refactored and modularized version of the original [Hessian](http://hessian.caucho.com/) repository, with all RPC-related logic removed. It focuses solely on maintaining and enhancing the **Hessian serialization protocol**.

> The new commits are copied from the source code of `hessian-4.0.xx-sources.jar` in [https://repo1.maven.org/maven2/com/caucho/hessian/](https://repo1.maven.org/maven2/com/caucho/hessian/)

The Hessian serialization protocol remains widely used in practice due to the following advantages:

* ⚡ **High Performance**: Fast (de)serialization
* 📦 **Compact Size**: Efficient binary encoding
* 🌐 **Cross-language Compatibility**: Useful in polyglot systems
* 🛠️ **Ease of use**: simple to use, no predefined data structure required

### ✅ Object Reference Reuse: Hessian2’s Unique Strength

Hessian2 **supports shared references and cyclic object graphs** out-of-the-box. It detects duplicated object instances and reuses them during serialization, and can correctly handle circular references without stack overflow or infinite loops.

In contrast:

| Format       | Shared References | Cyclic References | Notes                                                                  |
| ------------ | ----------------- | ----------------- | ---------------------------------------------------------------------- |
| **Hessian2** | ✅ Yes             | ✅ Yes             | Native support, no special handling                                    |
| **JSON**     | ❌ No              | ❌ No              | Value copy only; circular references cause errors                      |
| **Protobuf** | ❌ No              | ❌ No              | Tree-based, cannot represent object graphs with cycles or shared state |

### 🌍 When Hessian2 Still Shines

These features make Hessian2 ideal for scenarios such as:

* Java object persistence or caching with shared/circular references
* Deep cloning or snapshotting of runtime state
* Java-to-Java microservice communication with contextual state
* RPC frameworks that demand full fidelity object reconstruction

Even in modern systems, **Hessian2 remains irreplaceable** in certain specialized fields due to its fidelity, compactness, and ease of integration.

### Overview of Hessian2 Protocol

Hessian2 is an enhanced version of the original Hessian protocol, with the following features:

* Binary serialization of Java primitives, collections, and custom classes
* **Object reference support** to prevent redundant serialization
* **Class definition caching** to reduce payload size
* Designed for **high-efficiency data transmission**

📄 **Protocol Documentation**: [Hessian2 Protocol Specification (English)](./docs/hessian-serialization.md)

### What’s Special About This Project

* Strip the RPC-related code from the original Hessian project
* Modularized architecture for better extensibility and maintenance
* Supports **Hessian 2 protocol only**
* Ideal for use as a standalone, high-efficiency serialization library
* Supports Java 11 and above versions

Community contributions are welcome. We are committed to keeping a clean, modular, and efficient implementation of the Hessian serialization protocol in Java.

## Maven dependency

```xml
<dependency>
    <groupId>io.github.wuwen5.hessian</groupId>
    <artifactId>hessian2-codec</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```
