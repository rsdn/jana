rootProject.name = "jana"

include(
    ":app",
    ":core",
    ":data",
    ":network",
    ":sync",
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}
