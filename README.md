Stitcher
========

Stitcher is a lightweight and fast class transformation library for Java developed using the
[ASM](https://asm.ow2.io/) library.

:warning: Stitcher is not yet considered stable. There may be lurking issues which impact stability or correctness.
Please report any issues you find on the [issue tracker](https://github.com/jellysquid3/stitcher/issues).

## Features
- Support for code injection, method redirection and overwriting, local variable capture, field and method shadowing
- Supports obfuscated environments with plugin re-obfuscation as a Gradle build task
- Detects early class-loading errors which prevent transforming targets
- Compiles to a single JAR for the Forge platform which weighs in just under 120KB with dependencies included

### Upcoming
- [ ] Control flow modification (break from branches and loops, return early from methods, etc)
- [ ] Dump transformed class bytecode on errors
- [ ] Debugging information for injected bytecode in IDEs
- [ ] Extended injection bytecode validation

### Stretch goals
- [ ] Annotation processor for compile time analysis
- [ ] Hot-swap/Live-reload functionality for injections

## Building

Clone the repository and use the provided Gradle wrapper to perform a build with the following command:

```
./gradlew build
```

## License

Stitcher is free and open-source software licensed under the
[GNU GPL v3.0 license](https://www.gnu.org/licenses/gpl-3.0.en.html).