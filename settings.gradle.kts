@file:Suppress("DEPRECATION", "UNCHECKED_CAST")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// Fix for duplicate ANDROID_PREFS_ROOT and ANDROID_USER_HOME env vars causing AndroidLocationsException
try {
    val unsafeField = sun.misc.Unsafe::class.java.getDeclaredField("theUnsafe")
    unsafeField.isAccessible = true
    val unsafe = unsafeField.get(null) as sun.misc.Unsafe

    val peClass = Class.forName("java.lang.ProcessEnvironment")

    for (fieldName in listOf("theEnvironment", "theUnmodifiableEnvironment", "theCaseInsensitiveEnvironment")) {
        try {
            val field = peClass.getDeclaredField(fieldName)
            val offset = unsafe.staticFieldOffset(field)
            val map = unsafe.getObject(peClass, offset) as? MutableMap<Any, Any>
            map?.keys?.removeIf { it.toString().equals("ANDROID_PREFS_ROOT", ignoreCase = true) }
        } catch (_: Throwable) {}
    }
} catch (_: Throwable) {}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "EventSphere Scanner"
include(":app")
