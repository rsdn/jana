import nu.studer.gradle.jooq.JooqEdition
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

group = "org.rsdn.jana"
version = "1.0.0-alpha"

plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    id("org.jetbrains.compose") version "1.9.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0"
    id("nu.studer.jooq") version "9.0"
    // Добавлен плагин для миграции схемы перед генерацией кода jOOQ
    id("org.flywaydb.flyway") version "10.0.0"
}

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

// Переменные для автономной генерации
val genDbFile = layout.buildDirectory.file("jooq-gen.db").get().asFile
val genDbUrl = "jdbc:sqlite:${genDbFile.absolutePath}"

compose.desktop {
    application {
        mainClass = "org.example.MainKt" // Ваш главный класс
        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Msi)
            packageName = "Jana"
            packageVersion = "1.0.0"
            modules("java.sql", "jdk.crypto.mscapi")
            windows {
//                iconFile.set(project.file("icon.ico"))
                jvmArgs(
                    "-Djavax.net.ssl.trustStoreType=Windows-ROOT",
                    "-Djavax.net.ssl.trustStore=NONE"
                )
            }
        }
        buildTypes.release.proguard {
            isEnabled.set(false)
        }
    }
}

// Настройка Flyway Gradle плагина (читает твои миграции из ресурсов)
flyway {
    url = genDbUrl
    locations = arrayOf("filesystem:src/main/resources/db/migration")
}

dependencies {
    val composeVersion = "1.9.0"

    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.material3:material3-desktop:$composeVersion")
    implementation("org.jetbrains.compose.runtime:runtime-desktop:$composeVersion")
    implementation("org.jetbrains.compose.foundation:foundation-desktop:$composeVersion")
    implementation("org.jetbrains.compose.ui:ui-desktop:$composeVersion")
    implementation("org.jetbrains.compose.components:components-resources-desktop:$composeVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")

    implementation("org.xerial:sqlite-jdbc:3.49.0.0")
    val flywayVersion = "12.3.0"
    implementation("org.flywaydb:flyway-core:$flywayVersion")

    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("io.github.oshai:kotlin-logging:7.0.0")

    jooqGenerator("org.xerial:sqlite-jdbc:3.49.0.0") // Обновил до актуальной версии

    val ktorVersion = "3.4.2"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("io.ktor:ktor-server-core:${ktorVersion}")
    implementation("io.ktor:ktor-server-netty:${ktorVersion}")
    implementation("io.ktor:ktor-server-host-common:${ktorVersion}")

    implementation("io.coil-kt.coil3:coil-compose:3.4.0")
    implementation("io.coil-kt.coil3:coil-network-ktor3:3.4.0")
}

sourceSets {
    main {
        resources {
            srcDirs("src/main/resources")
            srcDirs("src/main/composeResources")
        }
        // Добавляем сгенерированные jOOQ классы в SourceSet
        kotlin.srcDir("build/generated/source/jooq/main")
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
                    url = genDbUrl // Теперь генератор смотрит в актуальную временную базу
                }

                generator.apply {
                    // Используем KotlinGenerator для нормальных Kotlin-классов
                    name = "org.jooq.codegen.KotlinGenerator"

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

// 1. Связываем миграцию Flyway с генерацией jOOQ
tasks.named("generateJooq") {
    dependsOn("flywayMigrate")
}

// 2. Связываем компиляцию Kotlin с генерацией кода
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    // Не привязываем служебные задачи (тесты, капт и т.д.), только основную компиляцию
    if (name == "compileKotlin") {
        dependsOn("generateJooq")
    }
}

// 3. Убеждаемся, что исходники видны IDE и компилятору
sourceSets {
    main {
        kotlin {
            srcDir("build/generated/source/jooq/main")
        }
    }
}

tasks.processResources {
    doLast {
        val resourcesDir = destinationDir
        val versionFile = File(resourcesDir, "version.properties")
        versionFile.writeText("version=${project.version}")
    }
}