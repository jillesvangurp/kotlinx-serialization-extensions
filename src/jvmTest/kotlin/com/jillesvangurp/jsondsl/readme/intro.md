[![Process Pull Request](https://github.com/jillesvangurp/kotlinx-serialization-extensions/actions/workflows/pr_master.yaml/badge.svg)](https://github.com/jillesvangurp/kotlinx-serialization-extensions/actions/workflows/pr_master.yaml)

This is a small library that fixes the default settings for `Json()` in `kotlinx.serialization` that should
benefit anyone that uses that for e.g. use in servers or API clients.

I got tired of copy pasting these between essentially every project where I use `kotlinx.serialization`, so I created this library. This allows me to pull this in everywhere I need this and gives me a central place to manage these defaults.

Features

- DEFAULT_JSON and DEFAULT_PRETTY_JSON Json configurations with some sane defaults. The actual defaults in kotlinx.serialization are wrong for anyone looking to implement forward compatible APIs. 
- Some extension functions on various things that make working with `JsonElement` in kotlinx a bit less painful. 

See the documentation below for more details.

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