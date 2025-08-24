# Hessian2 Serialization Library

**Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.**

Hessian is a Java serialization library focused on the Hessian2 binary protocol. This is a multi-module Maven project that provides high-performance, compact serialization with support for object references and circular dependencies.

## Working Effectively

### Bootstrap and Build Commands
- **PREREQUISITE**: Java 11 or later is required. Use `java --version` to verify.
- **Initial setup**: `./mvnw clean compile` -- takes ~30 seconds. Set timeout to 60+ seconds.
- **Run tests**: `./mvnw test` -- takes ~17 seconds. Set timeout to 45+ seconds. 
- **Full build and validation**: `./mvnw clean verify javadoc:javadoc` -- takes ~38 seconds. Set timeout to 90+ seconds.
- **NEVER CANCEL** builds or tests. All commands complete within 60 seconds under normal conditions.

### Code Quality and Formatting
- **ALWAYS run before commits**: `./mvnw spotless:check` -- validates code formatting (~1 second)
- **Auto-format code**: `./mvnw spotless:apply` -- fixes formatting issues
- **Coverage reports**: `./mvnw jacoco:report -pl hessian2-codec` -- generates code coverage

### Testing Specific Modules
- **Test core serialization**: `./mvnw test -pl hessian2-codec`
- **Test specific class**: `./mvnw test -Dtest=HessianIOTest -pl hessian2-codec`
- **Test with coverage**: `./mvnw verify -pl hessian2-codec`

## Validation and Testing Changes

**ALWAYS manually validate changes by running the validation scenario:**

```bash
cd hessian2-codec
java -cp "$(../mvnw dependency:build-classpath -B -q -Dmdep.outputFile=/dev/stdout):target/classes:target/test-classes" io.github.wuwen5.hessian.io.ValidationTest
```

This test validates:
- Basic serialization/deserialization functionality
- Object reference reuse (Hessian2's key strength)
- Circular reference handling
- Compact binary encoding

**Expected output should show:**
- Successful serialization to ~132 bytes
- Objects equal: true
- Circular reference test: true
- "All validation tests passed!" message

## Project Structure and Navigation

### Key Modules
- **`hessian2-codec/`** -- CORE MODULE: Contains the main Hessian2 serialization implementation
- **`hessian-rpc/`** -- RPC client/server functionality (legacy support)
- **`burlap-rpc/`** -- Burlap XML-RPC implementation (legacy support)  
- **`hessian-services/`** -- Service interface definitions
- **`docs/`** -- Protocol specification and documentation

### Important Directories
- **`hessian2-codec/src/main/java/io/github/wuwen5/hessian/io/`** -- Main serialization classes
- **`hessian2-codec/src/test/java/io/github/wuwen5/hessian/io/`** -- Test cases and examples
- **`.github/workflows/`** -- CI/CD configuration (GitHub Actions)

### Key Files to Know
- **`pom.xml`** -- Root Maven configuration (Java 11+, multi-module setup)
- **`hessian2-codec/src/main/java/io/github/wuwen5/hessian/io/HessianEncoder.java`** -- Main encoder
- **`hessian2-codec/src/main/java/io/github/wuwen5/hessian/io/HessianDecoder.java`** -- Main decoder
- **`hessian2-codec/src/test/java/io/github/wuwen5/hessian/io/SerializeTestBase.java`** -- Test utility base class

## Common Development Tasks

### Making Changes to Serialization Logic
1. **Always start with tests**: Look in `hessian2-codec/src/test/java/io/github/wuwen5/hessian/io/` for similar tests
2. **Build incrementally**: `./mvnw compile -pl hessian2-codec` after each change
3. **Run targeted tests**: `./mvnw test -pl hessian2-codec -Dtest=YourTestClass`
4. **Validate with scenario**: Run the ValidationTest to ensure core functionality works
5. **Format code**: `./mvnw spotless:apply` before committing

### Adding New Features
1. **Follow existing patterns**: Study `HessianIOTest.java` and `SerializeTestBase.java`
2. **Test both directions**: Always test serialization AND deserialization
3. **Test edge cases**: Include null values, empty collections, circular references
4. **Update documentation**: If changing protocol behavior, update `docs/hessian-serialization.md`

### Debugging Serialization Issues
1. **Enable debug logging**: Tests have methods `enableLog(HessianDecoder.class)` 
2. **Use small test cases**: Create minimal reproduction in ValidationTest format
3. **Check binary output**: Hessian2 uses compact binary format, debug output shows structure
4. **Test reference handling**: Hessian2's strength is object reference reuse

## Build Requirements and Environment

### Java Version Support
- **Minimum**: Java 11 (enforced by Maven Enforcer Plugin)
- **Tested on**: Java 11, 17, 21 (see `.github/workflows/ci.yml`)
- **Current CI**: Uses Eclipse Temurin distribution

### Maven Configuration
- **Maven version**: 3.9.9+ (via Maven wrapper)
- **Compiler target**: Java 11
- **Encoding**: UTF-8 throughout

### Dependencies
- **Core**: No external runtime dependencies (only SLF4J for logging)
- **Test**: JUnit 5, Mockito, Hamcrest, Vavr (functional helpers)
- **Build**: Spotless (formatting), JaCoCo (coverage), Enforcer (compliance)

## CI/CD Integration

### GitHub Actions Workflows
- **`ci.yml`**: Runs on Java 11/17/21, Ubuntu/macOS, full build with verification
- **`coverage.yml`**: Daily coverage reporting to Codecov/Coveralls
- **`release.yml`**: Maven Central publishing workflow

### Pre-commit Checks
**ALWAYS run these before committing:**
1. `./mvnw spotless:check` -- code formatting (REQUIRED - CI will fail if not formatted)
2. `./mvnw test` -- all tests pass
3. Run ValidationTest manually -- core functionality works
4. `./mvnw verify -pl hessian2-codec` -- full module validation

## Troubleshooting

### Common Build Issues
- **"Java version not supported"**: Ensure Java 11+ with `java --version`
- **Formatting failures**: Run `./mvnw spotless:apply` to auto-fix
- **Test failures**: Check if changes broke serialization compatibility

### Performance Considerations
- **Builds are fast**: Full build ~38 seconds, no need for parallel builds
- **Tests are comprehensive**: 183 classes, extensive coverage in hessian2-codec
- **Memory usage**: Normal Maven defaults are sufficient

### Getting Help
- **Protocol questions**: Check `docs/hessian-serialization.md` for specification
- **Examples**: Look at existing tests in `hessian2-codec/src/test/java/`
- **CI failures**: Check GitHub Actions logs, usually formatting or test issues

## Repository Statistics (for reference)
```
Root: pom.xml (multi-module setup)
├── hessian2-codec/ (182 classes, primary module)
├── hessian-rpc/ (63 classes, legacy RPC)
├── burlap-rpc/ (25 classes, XML-RPC)
├── hessian-services/ (10 classes, interfaces)
└── docs/ (protocol specification)

Languages: Java 11+
Build: Maven with wrapper (./mvnw)
Tests: JUnit 5 (282 test files total)
CI: GitHub Actions on multiple Java versions/OS
```

Fixes #33.