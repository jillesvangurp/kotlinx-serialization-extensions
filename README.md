# JsonDsl

Some simple extensions for kotlinx serialization.

- DEFAULT_JSON and DEFAULT_PRETTY_JSON Json configurations with some sane defaults. The actual defaults in kotlinx.serialization ae wrong for anyone looking to implement forward compatible APIs
- Some extension functions on various things.


## Gradle

This library is published to our own maven repository.

```kotlin
repositories {
    mavenCentral()
    maven("https://maven.tryformation.com/releases") {
        // optional but it speeds up the gradle dependency resolution
        content {
            includeGroup("com.jillesvangurp")
            includeGroup("com.tryformation")
        }
    }
}
```

And then you can add the dependency:

```kotlin
    // check the latest release tag for the latest version
    implementation("com.jillesvangurp:kotlinx-serialization-extensions:1.x.y")
```

## Example usage

The main feature of [kotlin4example](https://github.com/jillesvangurp/kotlin4example) is of course integrating code samples into your documentation.   

### Json

```kotlin
println( DEFAULT_JSON.encodeToString(Foo("foo")))
println( DEFAULT_PRETTY_JSON.encodeToString(Foo("foo")))
```

```text
{"bar":"foo"}
{
  "bar": "foo"
}
```

## Multi platform

This is a Kotlin multi platform library that should work on most  kotlin platforms (jvm, js, ios, android, wasm, etc). 

