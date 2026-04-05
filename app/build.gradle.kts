import org.gradle.internal.impldep.org.bouncycastle.pqc.crypto.lms.Composer.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import kotlin.text.Typography.copyright

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm()

    sourceSets {
        jvmMain.dependencies {
            implementation(project(":core"))
            implementation(project(":data"))
            implementation(project(":network"))
            implementation(project(":sync"))

            // compose.desktop.currentOs is the only accessor that must remain —
            // it resolves to a platform-specific artifact at build time.
            implementation(compose.desktop.currentOs)

            // Direct coordinates as recommended by the CMP 1.10 deprecation notices.
            // Versions are intentionally omitted: the CMP Gradle plugin manages
            // compatible versions via Gradle Module Metadata.
            implementation(libs.compose.components.resources)

            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.bundles.koin)
            implementation(libs.logback.classic)
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.rsdn.jana.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Jana"
            packageVersion = "1.0.0"
            description = "Offline client for RSDN forums"
            vendor = "RSDN"
            copyright = "© 2024 RSDN"

            linux {
                iconFile.set(project.file("src/jvmMain/resources/icons/jana.png"))
            }
            windows {
                iconFile.set(project.file("src/jvmMain/resources/icons/jana.ico"))
                menuGroup = "Jana"
                upgradeUuid = "A1B2C3D4-E5F6-7890-ABCD-EF1234567890"
            }
            macOS {
                iconFile.set(project.file("src/jvmMain/resources/icons/jana.icns"))
                bundleID = "org.rsdn.jana"
            }
        }
    }
}