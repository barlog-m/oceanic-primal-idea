import org.jetbrains.changelog.date
import org.jetbrains.changelog.markdownToHTML

plugins {
    java
    id("org.jetbrains.intellij") version "1.1.4"
    id("org.jetbrains.changelog") version "1.2.1"

    // ./gradlew dependencyUpdates -Drevision=release
    id("com.github.ben-manes.versions") version "0.39.0"
}

val appName = "oceanic-primal"
val appVersion = "0.0.8"

group = "li.barlog"
version = appVersion

repositories {
    mavenCentral()
}

val javaVer = JavaVersion.VERSION_11

dependencies {
}

java {
    sourceCompatibility = javaVer
    targetCompatibility = javaVer
}

intellij {
    pluginName.set("Oceanic Primal Theme")
    version.set("IC-2021.2")
    type.set("IC")
}

changelog {
    version.set(appVersion)
    path.set("${project.projectDir}/CHANGELOG.md")
    header.set(provider { "[${version.get()}] - ${date()}" })
    itemPrefix.set("-")
    keepUnreleasedSection.set(true)
    unreleasedTerm.set("[Unreleased]")
    groups.set(
        listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security")
    )
}

tasks {
    patchPluginXml {
        version.set(appVersion)
        sinceBuild.set("193")
        untilBuild.set("212.*")

        changeNotes.set(provider { changelog.getLatest().toHTML() })

        pluginDescription.set(
            provider {
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
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("TOKEN"))
    }

    wrapper {
        gradleVersion = "7.1.1"
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
