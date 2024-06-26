[![Process Pull Request](https://github.com/jillesvangurp/kotlinx-serialization-extensions/actions/workflows/pr_master.yaml/badge.svg)](https://github.com/jillesvangurp/kotlinx-serialization-extensions/actions/workflows/pr_master.yaml)

I got tired of copy pasting these between projects, so I created a library that I can include quickly. This project has the following features:


- DEFAULT_JSON and DEFAULT_PRETTY_JSON Json configurations with some sane defaults. The actual defaults in kotlinx.serialization are wrong for anyone looking to implement forward compatible APIs
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
