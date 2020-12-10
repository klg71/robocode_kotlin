
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.3.72"
    id("com.github.johnrengelman.shadow") version "6.0.0"
}


repositories {
    mavenCentral()
}
val robocodeDir = project.findProperty("robocodeDir") as String? ?: error(
    "Please create robocodeDir property in gradle.properties")

dependencies {
    implementation(kotlin("stdlib"))
    implementation(fileTree("${robocodeDir}/libs"){include("robocode.jar")})
}

tasks {

    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("fat")
        manifest {
            attributes["robots"] = "de.doom.robocop.RoboCop"
        }
        from(sourceSets.main.get().output)
        exclude("robocode.jar")
    }

    register("install") {
        dependsOn("shadowJar")
        doLast {
            val fromPath = "${rootDir}/build/libs/robocop-fat.jar"
            val intoPath = "${robocodeDir}/robots/"

            copy {
                from(fromPath)
                into(intoPath)
            }
        }
    }

    register("start") {
        group = "robocode"
        dependsOn("install")

        doLast {
            exec {
                commandLine("java",
                    "-Xmx512M",
                    "-Ddebug=true",
                    "-DWORKINGDIRECTORY=${robocodeDir}",
                    "-cp", "${robocodeDir}/libs/robocode.jar", "robocode.Robocode",
                    "-battle", "${rootDir}/battles/nomovementshoot.battle")
            }
        }
    }
}
