pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven { url = uri("https://maven.mozilla.org/maven2") }
    }
}

rootProject.name = "WebToApp"
include(":app")
include(":shell")
include(":clone-host")

val fdroidRaw = providers.gradleProperty("fdroid").orNull?.trim()?.lowercase()
val isFdroidBuild = fdroidRaw != null && fdroidRaw != "false" && fdroidRaw != "0" && fdroidRaw != "no"
if (!isFdroidBuild) {
    include(":feature-stacks:admob")
}
