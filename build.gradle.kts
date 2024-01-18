import org.jetbrains.changelog.date
import org.jetbrains.changelog.markdownToHTML

fun props(key: String) = project.findProperty(key).toString()

plugins {
    java
    id("org.jetbrains.intellij") version "1.17.0"
    id("org.jetbrains.changelog") version "2.2.0"

    // ./gradlew dependencyUpdates -Drevision=release
    id("com.github.ben-manes.versions") version "0.50.0"
}

group = props("pluginGroup")
version = props("pluginVersion")

repositories {
    mavenCentral()
    maven { url = uri("https://cache-redirector.jetbrains.com/intellij-dependencies") }
}

val javaVer = JavaVersion.VERSION_11

dependencies {
}

java {
    sourceCompatibility = javaVer
    targetCompatibility = javaVer
}

intellij {
    pluginName.set(props("pluginName"))
    version.set("IC-2023.3")
    type.set("IC")
}

changelog {
    version.set(props("pluginVersion"))
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
        version.set(props("pluginVersion"))
        sinceBuild.set(props("pluginSinceBuild"))
        untilBuild.set("")

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
        gradleVersion = "8.5"
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
