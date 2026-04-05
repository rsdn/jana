plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.jooq.codegen) apply false
}

// Force patched dependency versions across all subprojects.
//
// Jackson: CVE-2025-52999 (DoS, CVSS 8.7) and CVE-2025-49128 (memory disclosure)
//   fixed in 2.15+. Using 2.21.2 (latest LTS).
//   Note: jackson-annotations uses a different scheme — it releases as "2.21"
//   without a patch number, so it must be pinned separately.
//
// Logback: CVE-2025-11226 / CVE-2026-1225 / GHSA-qqpg-mvqg-649v (ACE in config
//   processing) fixed in 1.5.25.
subprojects {
    configurations.all {
        resolutionStrategy.eachDependency {
            when (requested.group) {
                "com.fasterxml.jackson.core" -> when (requested.name) {
                    "jackson-annotations" -> {
                        useVersion("2.21")
                        because("CVE-2025-52999 / CVE-2025-49128 — annotations releases without patch number")
                    }
                    else -> {
                        useVersion("2.21.2")
                        because("CVE-2025-52999 / CVE-2025-49128 — force safe Jackson LTS")
                    }
                }
                "com.fasterxml.jackson.dataformat",
                "com.fasterxml.jackson.datatype",
                "com.fasterxml.jackson.module" -> {
                    useVersion("2.21.2")
                    because("CVE-2025-52999 / CVE-2025-49128 — align all Jackson modules")
                }
                "ch.qos.logback" -> {
                    useVersion("1.5.25")
                    because("CVE-2025-11226 / CVE-2026-1225 / GHSA-qqpg-mvqg-649v")
                }
            }
        }
    }
}