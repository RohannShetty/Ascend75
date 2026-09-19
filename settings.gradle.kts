pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Ascend75"
include(":app")

// Core Modules
include(":core:common")
include(":core:designsystem")
include(":core:database")
include(":core:datastore")
include(":core:crypto")
include(":core:notifications")

// Feature Modules
include(":feature:onboarding")
include(":feature:dashboard")
include(":feature:workout")
include(":feature:water")
include(":feature:reading")
include(":feature:photos")
include(":feature:learn")
include(":feature:settings")
