import nu.studer.gradle.jooq.JooqEdition

plugins {
    kotlin("jvm") version "2.1.20"
    id("org.jetbrains.compose") version "1.9.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
    id("nu.studer.jooq") version "9.0"
}

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.ui)
    implementation(compose.materialIconsExtended)
    implementation(compose.components.resources)

    // Kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")

    // SQLite + Flyway
    implementation("org.xerial:sqlite-jdbc:3.49.0.0")
    val flywayVersion = "12.3.0"
    implementation("org.flywaydb:flyway-core:$flywayVersion")

    // Logs
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("io.github.oshai:kotlin-logging:7.0.0")

    jooqGenerator("org.xerial:sqlite-jdbc:3.46.1.3")
}

sourceSets {
    main {
        resources {
            srcDirs("src/main/resources")
            srcDirs("src/main/composeResources")
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.rsdn.jana.MainKt"
    }
}

compose.resources {
    generateResClass = always
    publicResClass = true
    packageOfResClass = "org.rsdn.jana.resources"
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

jooq {
    edition.set(JooqEdition.OSS)
    version.set("3.19.10")

    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(true)

            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.sqlite.JDBC"
                    url = "jdbc:sqlite:jana.db"
                }

                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"

                    database.apply {
                        name = "org.jooq.meta.sqlite.SQLiteDatabase"
                        includes = ".*"
                        excludes = "flyway_schema_history"
                    }

                    target.apply {
                        packageName = "org.rsdn.jana.data.jooq"
                        directory = "build/generated/source/jooq/main"
                    }
                }
            }
        }
    }
}