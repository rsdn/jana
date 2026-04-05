buildscript {
    dependencies {
        // Flyway + SQLite JDBC must be on the buildscript classpath so the
        // flywayMigrate task can reference Flyway classes at execution time.
        classpath("org.flywaydb:flyway-core:11.8.0")
        classpath("org.xerial:sqlite-jdbc:3.49.1.0")
    }
}

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jooq.codegen)
}

kotlin {
    jvm()

    sourceSets {
        jvmMain.dependencies {
            implementation(project(":core"))
            implementation(libs.bundles.jooq)
            implementation(libs.flyway.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.slf4j.api)
        }
        jvmTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.logback.classic)
        }
    }
}

// ---------------------------------------------------------------------------
// jOOQ code generation
// Flyway migrates a temp SQLite DB → jOOQ introspects it → generates sources
// ---------------------------------------------------------------------------
val generatedSrcDir = layout.buildDirectory.dir("generated/jooq")

// Make generated sources visible to Kotlin compilation
kotlin.sourceSets.named("jvmMain") {
    kotlin.srcDir(generatedSrcDir)
}

// Temp DB for codegen
val codegenDbFile = layout.buildDirectory.file("tmp/jana-codegen.db")

// 1. Run Flyway migrations against the temp DB before jOOQ introspects it
val flywayMigrate by tasks.registering {
    description = "Migrate temp SQLite DB for jOOQ codegen"
    group = "jooq"

    inputs.files(fileTree("src/jvmMain/resources/db/migration"))
    outputs.file(codegenDbFile)

    doFirst {
        codegenDbFile.get().asFile.parentFile.mkdirs()
    }
    doLast {
        val url = "jdbc:sqlite:${codegenDbFile.get().asFile.absolutePath}"
        val flyway = org.flywaydb.core.Flyway.configure()
            .dataSource(url, null, null)
            .locations("filesystem:${projectDir}/src/jvmMain/resources/db/migration")
            .load()
        flyway.migrate()
    }
}

// 2. jOOQ codegen depends on Flyway having run first
tasks.named("jooqCodegen") {
    dependsOn(flywayMigrate)
}

jooq {
    configuration {
        jdbc {
            driver = "org.sqlite.JDBC"
            url = "jdbc:sqlite:${codegenDbFile.get().asFile.absolutePath}"
        }
        generator {
            name = "org.jooq.codegen.KotlinGenerator"
            database {
                name = "org.jooq.meta.sqlite.SQLiteDatabase"
                inputSchema = "main"
                excludes = "flyway_schema_history"
            }
            generate {
                isKotlinSetterJvmNameAnnotationsOnIsPrefix = true
                isFluentSetters = true
                isPojosAsKotlinDataClasses = true
                isImmutablePojos = true
            }
            target {
                packageName = "org.rsdn.jana.data.db.generated"
                directory = generatedSrcDir.get().asFile.absolutePath
            }
        }
    }
}

// Ensure codegen runs before compilation
tasks.named("compileKotlinJvm") {
    dependsOn(tasks.named("jooqCodegen"))
}

dependencies {
    // Required by jOOQ codegen task at build time
    jooqCodegen(libs.sqlite.jdbc)
    jooqCodegen(libs.jooq.codegen)
    jooqCodegen(libs.jooq.meta)
    jooqCodegen(libs.flyway.core)
}