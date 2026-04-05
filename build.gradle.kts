plugins {
    kotlin("jvm") version "2.1.20"
    id("org.jetbrains.compose") version "1.9.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
}

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    // Compose
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)   // <-- добавить
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.ui)
    implementation(compose.materialIconsExtended)
    implementation(compose.components.resources)
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