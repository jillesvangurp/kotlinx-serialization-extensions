This is an opinionated template for creating kotlin multi platform library projects.

This works for me and might help you bootstrap your kotlin projects.

## Batteries included

My goal with this is to waste less time setting up new projects. Kotlin multiplatform can be a bit fiddly to get going with and there are a lot of things that I want to add to projects. This gets me there with minimal fiddling.

- Gradle wrapper with recent version of gradle & kts dialect
- [ktfmt](https://github.com/facebook/ktfmt) - Code style is enforced with ktfmt. A matching .editorconfig is included. The style is Kotlin's [official style](kotlinlang.org/docs/coding-conventions.html). Tip, configure your IDE to format on save.
- [Refresh versions plugin](https://splitties.github.io/refreshVersions/) - Great way to manage dependencies and stay on top of updates.
- [kotlin4example](https://github.com/jillesvangurp/kotlin4example) integrated to generate the readme and any other documentation you are going to write. This is all driven via the tests.
- Some dependencies for testing (junit, kotest-assertions, etc.) and test setup for junit
- generic publish script that tags and publishes
- Github action that builds your stuff generated using [github-workflows-kt](https://github.com/typesafegithub/github-workflows-kt). Setup to cache gradle and konan related files to speed up your builds.
- LICENSE file (MIT)

## Usage & project create checklist

- [ ] Go to Github and push the "Use this template" button. This will create a new project based on this template
- [ ] Fix your project name by changing `rootProject.name = "my-new-kmp-project"` in settings.gradle.kts. Override the group name in gradle.properties
- [ ] Review default maven repo for releases and other things in build.gradle.kts
- [ ] Update copyright file
- [ ] Start writingb your own README.md by modifying the code that generates it

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
    implementation("com.jillesvangurp:my-new-kmp-project:1.x.y")
```
