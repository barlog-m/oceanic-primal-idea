import org.jetbrains.changelog.closure
import org.jetbrains.changelog.markdownToHTML

plugins {
    java
    id("org.jetbrains.intellij") version "0.7.2"
    id("org.jetbrains.changelog") version "1.1.2"

    // ./gradlew dependencyUpdates -Drevision=release
    id("com.github.ben-manes.versions") version "0.38.0"
}

val appName = "oceanic-primal"

group = "li.barlog"
version = "0.0.7"

repositories {
    mavenCentral()
}

val javaVer = JavaVersion.VERSION_1_8

dependencies {
}

java {
    sourceCompatibility = javaVer
    targetCompatibility = javaVer
}

intellij {
    pluginName = "Oceanic Primal Theme"
    version = "IC-2021.1"
    type = "IC"
}

tasks {
    patchPluginXml {
        version(version)
        sinceBuild("193")
        untilBuild("211.*")

        pluginDescription(
            closure {
                File("./README.md").readText().lines().run {
                    val start = "<!-- Plugin description -->"
                    val end = "<!-- Plugin description end -->"

                    if (!containsAll(listOf(start, end))) {
                        throw GradleException("Plugin description section not found in README.md file:\n$start ... $end")
                    }
                    subList(indexOf(start) + 1, indexOf(end))
                }.joinToString("\n").run { markdownToHTML(this) }
            }
        )

        changeNotes(
            closure {
                changelog.getLatest().toHTML()
            }
        )
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token(System.getenv("TOKEN"))
    }

    wrapper {
        gradleVersion = "7.0"
        distributionType = Wrapper.DistributionType.ALL
    }

    jar {
        manifest {
            attributes(
                "Implementation-Title" to rootProject.name,
                "Implementation-Version" to archiveVersion,
                "Multi-Release" to true
            )
        }
    }

    register<Delete>("cleanOut") {
        delete("out")
        delete("target")
    }

    clean {
        dependsOn("cleanOut")
    }
}
