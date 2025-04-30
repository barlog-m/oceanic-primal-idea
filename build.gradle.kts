import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.date
import org.jetbrains.changelog.markdownToHTML

fun props(key: String) = project.findProperty(key).toString()

plugins {
    java
    id("org.jetbrains.intellij.platform") version "2.5.0"
    id("org.jetbrains.changelog") version "2.2.1"

    // ./gradlew dependencyUpdates -Drevision=release
    id("com.github.ben-manes.versions") version "0.52.0"
}

group = props("pluginGroup")
version = props("pluginVersion")

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

val javaVer = JavaVersion.VERSION_21

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2025.1")
    }
}

java {
    sourceCompatibility = javaVer
    targetCompatibility = javaVer
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
        pluginVersion.set(props("pluginVersion"))
        sinceBuild.set(props("pluginSinceBuild"))
        untilBuild.set("${props("pluginSinceBuild")}.*")

        changeNotes.set(provider { changelog.renderItem(changelog.getLatest(), Changelog.OutputType.HTML) })

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
        gradleVersion = "8.14"
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
