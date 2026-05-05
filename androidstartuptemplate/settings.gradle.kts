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
    }
}
plugins {

}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "android-startup-template"

include(":apps:app-main")
project(":apps:app-main").projectDir = file("apps/app-main")


include(":core:ui")
project(":core:ui").projectDir = file("core/ui")


include(":core:data")
project(":core:data").projectDir = file("core/data")

include(":core:domain")
project(":core:domain").projectDir = file("core/domain")

include(":core:common")
project(":core:common").projectDir = file("core/common")


include(":features:auth")
project(":features:auth").projectDir = file("features/auth")

include(":features:onboarding")
project(":features:onboarding").projectDir = file("features/onboarding")

include(":features:settings")
project(":features:settings").projectDir = file("features/settings")



// Brand Kit , All data stays here hehe
include(":brand-kit")
project(":brand-kit").projectDir=file("brand-kit")
