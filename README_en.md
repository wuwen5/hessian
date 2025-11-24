## Hessian2 Serialization

[![Java CI](https://github.com/wuwen5/hessian/actions/workflows/ci.yml/badge.svg)](https://github.com/wuwen5/hessian/actions/workflows/ci.yml)
<a href="https://openjdk.java.net/"><img src="https://img.shields.io/badge/Java-11+-339933?logo=openjdk&logoColor=white" alt="JDK support"></a>
[![codecov](https://codecov.io/gh/wuwen5/hessian/branch/main/graph/badge.svg)](https://codecov.io/gh/wuwen5/hessian)
[![Coverage Status](https://coveralls.io/repos/github/wuwen5/hessian/badge.svg?branch=main)](https://coveralls.io/github/wuwen5/hessian?branch=main)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=wuwen5_hessian&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=wuwen5_hessian)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=wuwen5_hessian&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=wuwen5_hessian)
[![Maven Central](https://maven-badges.herokuapp.com/sonatype-central/io.github.wuwen5.hessian/hessian/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.wuwen5.hessian/hessian/)
[![Last SNAPSHOT](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fio%2Fgithub%2Fwuwen5%2Fhessian%2Fhessian%2Fmaven-metadata.xml&label=latest%20snapshot)](https://central.sonatype.com/repository/maven-snapshots/io/github/wuwen5/hessian/hessian/maven-metadata.xml)
[![GitHub release](https://img.shields.io/github/release/wuwen5/hessian.svg)](https://github.com/wuwen5/hessian/releases)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![zread](https://img.shields.io/badge/Ask_Zread-_.svg?style=flat&color=00b0aa&labelColor=000000&logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZpZXdCb3g9IjAgMCAxNiAxNiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTQuOTYxNTYgMS42MDAxSDIuMjQxNTZDMS44ODgxIDEuNjAwMSAxLjYwMTU2IDEuODg2NjQgMS42MDE1NiAyLjI0MDFWNC45NjAxQzEuNjAxNTYgNS4zMTM1NiAxLjg4ODEgNS42MDAxIDIuMjQxNTYgNS42MDAxSDQuOTYxNTZDNS4zMTUwMiA1LjYwMDEgNS42MDE1NiA1LjMxMzU2IDUuNjAxNTYgNC45NjAxVjIuMjQwMUM1LjYwMTU2IDEuODg2NjQgNS4zMTUwMiAxLjYwMDEgNC45NjE1NiAxLjYwMDFaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00Ljk2MTU2IDEwLjM5OTlIMi4yNDE1NkMxLjg4ODEgMTAuMzk5OSAxLjYwMTU2IDEwLjY4NjQgMS42MDE1NiAxMS4wMzk5VjEzLjc1OTlDMS42MDE1NiAxNC4xMTM0IDEuODg4MSAxNC4zOTk5IDIuMjQxNTYgMTQuMzk5OUg0Ljk2MTU2QzUuMzE1MDIgMTQuMzk5OSA1LjYwMTU2IDE0LjExMzQgNS42MDE1NiAxMy43NTk5VjExLjAzOTlDNS42MDE1NiAxMC42ODY0IDUuMzE1MDIgMTAuMzk5OSA0Ljk2MTU2IDEwLjM5OTlaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik0xMy43NTg0IDEuNjAwMUgxMS4wMzg0QzEwLjY4NSAxLjYwMDEgMTAuMzk4NCAxLjg4NjY0IDEwLjM5ODQgMi4yNDAxVjQuOTYwMUMxMC4zOTg0IDUuMzEzNTYgMTAuNjg1IDUuNjAwMSAxMS4wMzg0IDUuNjAwMUgxMy43NTg0QzE0LjExMTkgNS42MDAxIDE0LjM5ODQgNS4zMTM1NiAxNC4zOTg0IDQuOTYwMVYyLjI0MDFDMTQuMzk4NCAxLjg4NjY0IDE0LjExMTkgMS42MDAxIDEzLjc1ODQgMS42MDAxWiIgZmlsbD0iI2ZmZiIvPgo8cGF0aCBkPSJNNCAxMkwxMiA0TDQgMTJaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00IDEyTDEyIDQiIHN0cm9rZT0iI2ZmZiIgc3Ryb2tlLXdpZHRoPSIxLjUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIvPgo8L3N2Zz4K&logoColor=ffffff)](https://zread.ai/wuwen5/hessian)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/wuwen5/hessian.svg)](http://isitmaintained.com/project/wuwen5/hessian "Average time to resolve an issue")

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
| **JSON (standard)**     | ❌ No              | ❌ No              | By default, only supports value copies; circular references will cause an error. Some JSON libraries provide annotations or custom strategies to support references.                      |
| **Protobuf** | ❌ No              | ❌ No              | Tree-structured; cannot express shared or circular references. Shared objects must be handled at the application level using IDs or mappings. |

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

## 📦 Maven dependency

- Basic usage

If you only need Hessian serialization/deserialization, simply include the `hessian2-codec` dependency:
```xml
<dependency>
    <groupId>io.github.wuwen5.hessian</groupId>
    <artifactId>hessian2-codec</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

- Using with Dubbo

When integrating with Dubbo, you need to add the `hessian-dubbo-adapter` module and exclude Dubbo’s built-in `hessian-lite` dependency:
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


