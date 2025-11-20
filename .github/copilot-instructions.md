```markdown
**Purpose**: Quick, actionable guidance for AI coding agents contributing to this repo.

**Big Picture**: Starlight is a modular Java RPC framework. Key module groups:
- `starlight/` (multi-module Maven reactor): `starlight-api`, `starlight-protocol`, `starlight-transport`, `starlight-core`, `starlight-serialization`, `starlight-all`.
- `starlight/starlight-protocol/` contains protocol adapters: `starlight-protocol-brpc`, `starlight-protocol-http`, `starlight-protocol-stargate`.
- `spring-cloud-starter-baidu-starlight/`: Spring Cloud starter that pulls `starlight-all` in and targets Java 17.

The design separates API, transport, serialization, and protocol adapters so new protocols or transports can be added without changing API contracts.

**Build & Test (reproducible commands)**
- Build the core reactor (JDK 8):
  - `mvn -B verify --file starlight/pom.xml --no-transfer-progress`
- Build the Spring starter (JDK 17):
  - `mvn -B verify --file spring-cloud-starter-baidu-starlight/pom.xml --no-transfer-progress`
- Run tests for a single module: `cd <module-dir> && mvn test` (example: `cd starlight/starlight-core && mvn test`).
- CI uses a matrix for Java 8 and Java 17 in `.github/workflows/unit-test.yml` — mirror that locally when validating cross-JDK behavior.

**Multi-JDK note**: Different modules target different Java versions. Use the appropriate JDK when building modules (e.g. JDK8 for the `starlight` reactor, JDK17 for the Spring starter). Example:
```
JAVA_HOME=/path/to/jdk17 mvn -f spring-cloud-starter-baidu-starlight/pom.xml clean package
```

**Formatting & Licensing**
- Project uses Spotless with Baidu formatting rules. Formatter file: `resources/baidu-formatter.xml`. License header: `resources/license-header`.
- Spotless is hooked into the compile phase in the poms; run `mvn spotless:apply` or just `mvn compile` to let it run.

**Protobuf / Codegen**
- Protobuf sources (if present) are under `src/main/proto` and codegen is performed by `protoc-jar-maven-plugin` (configured in `starlight/pom.xml`).
- Known caveat: plugin uses `protocVersion=2.5.0`. On Apple M1 you may need `-Dos.arch=x86_64` or to adjust Maven `settings.xml` to avoid missing prebuilt binaries.

**Conventions & Patterns (project-specific)**
- Package root: `com.baidu.cloud`.
- SPI and service providers: look for `META-INF/services/` and `META-INF/spring/` resources in module `resources` directories. Many integrations use Java SPI and Spring factories.
- Module responsibilities:
  - `starlight-api`: interfaces and annotations used by users and providers.
  - `starlight-core`: protocol-agnostic runtime (routing, connection management, lifecycle).
  - `starlight-protocol-*`: protocol-specific encoders/decoders and adapters.
  - `starlight-serialization`: serializer implementations.
  - `starlight-transport`: low-level NIO transport and multiplexing.

**Where to look first when debugging or making a change**
- For RPC behavior trace: `starlight/starlight-core/src/main/java/com/...` (core runtime)
- For protocol bugs: `starlight/starlight-protocol/*/src/main/java` (brpc/http/stargate)
- For transport issues: `starlight/starlight-transport/src/main/java`.
- For Spring integration: `spring-cloud-starter-baidu-starlight/src/main/java` and its `src/main/resources/META-INF/spring.factories`.

**CI / Release notes**
- CI workflows are in `.github/workflows/` (unit tests, CodeQL, release flows). Mimic these steps locally when possible.
- Spotless, jacoco, cobertura/jacoco and git-commit-id plugin are integrated in poms; follow existing plugin configs rather than adding new global checks.

**Examples to include in PRs from agents**
- When changing serialization or protocol code, add a small unit test under the relevant module's `src/test/java` that exercises serialize/deserialize or encode/decode.
- When adding new module entries, update parent `starlight/pom.xml` `modules` list and ensure `spotless` config and license header are applied.

**Strict rules for AI agents**
- Do not change Java target versions across modules without explicit owner approval — cross-module JDK changes affect CI.
- Preserve `resources/license-header` and formatting settings; run `spotless:apply` before proposing formatting changes.
- For any change touching codegen or protobuf, document how to reproduce generated sources locally (include `mvn -DskipTests=false generate-sources` example and any platform workarounds).

If anything here is unclear or you want more examples (tests, typical debugging traces, or specific class pointers), tell me which area to expand. 
```
