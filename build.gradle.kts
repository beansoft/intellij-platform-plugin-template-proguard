fun properties(key: String) = project.findProperty(key).toString()

buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.0.1")
    }
}

plugins {
    java
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "1.0"
}

fun getIDEAPath(): String {
    return properties( "localIdeaPath" );
}

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))
    downloadSources.set(properties("platformDownloadSources").toBoolean())
    updateSinceUntilBuild.set(true)
    localPath.set(getIDEAPath())

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.12")
}

tasks.register<proguard.gradle.ProGuardTask>("proguard") {
    verbose()
//    keepdirectories()// By default, directory entries are removed.
    ignorewarnings()
    target("11")

    // Alternatively put your config in a separate file
    // configuration("config.pro")

    // Use the jar task output as a input jar. This will automatically add the necessary task dependency.
    injars(tasks.named("jar"))

    outjars("build/${rootProject.name}-obfuscated.jar")

    val javaHome = System.getProperty("java.home")
    // Automatically handle the Java version of this build, don't support JBR
        // As of Java 9, the runtime classes are packaged in modular jmod files.
//        libraryjars(
//            // filters must be specified first, as a map
//            mapOf("jarfilter" to "!**.jar",
//                  "filter"    to "!module-info.class"),
//            "$javaHome/jmods/java.base.jmod"
//        )

    // Add all JDK deps
    if( ! properties("skipProguard").toBoolean()) {
        File("$javaHome/jmods/").listFiles()!!.forEach { libraryjars(it.absolutePath) }
    }

//    libraryjars(configurations.runtimeClasspath.get().files)
    val ideaPath = getIDEAPath()

    // Add all java plugins to classpath
//    File("$ideaPath/plugins/java/lib").listFiles()!!.forEach { libraryjars(it.absolutePath) }
    // Add all IDEA libs to classpath
//    File("$ideaPath/lib").listFiles()!!.forEach { libraryjars(it.absolutePath) }

    libraryjars(configurations.compileClasspath.get())

    dontshrink()
    dontoptimize()

//    allowaccessmodification() //you probably shouldn't use this option when processing code that is to be used as a library, since classes and class members that weren't designed to be public in the API may become public

    adaptclassstrings("**.xml")
    adaptresourcefilecontents("**.xml")// or   adaptresourcefilecontents()

    // Allow methods with the same signature, except for the return type,
    // to get the same obfuscation name.
    overloadaggressively()
    // Put all obfuscated classes into the nameless root package.
//    repackageclasses("")

    printmapping("build/proguard-mapping.txt")

    adaptresourcefilenames()
    optimizationpasses(9)
    allowaccessmodification()
    mergeinterfacesaggressively()
    renamesourcefileattribute("SourceFile")
    keepattributes("Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod")

    keep("""class org.jetbrains.plugins.template.MyBundle
    """.trimIndent())

    keep("""class beansoft.mykeep.**
    """.trimIndent())
    keep("class beansoft.mykeep.**{*;}")
}


tasks {
    prepareSandbox {
        if( ! properties("skipProguard").toBoolean()) {
            dependsOn("proguard")
            doFirst {
                val original = File("build/libs/${rootProject.name}.jar")
                println(original.absolutePath)
                val obfuscated =  File("build/${rootProject.name}-obfuscated.jar")
                println(obfuscated.absolutePath)
                if (original.exists() && obfuscated.exists()) {
                    original.delete()
                    obfuscated.renameTo(original)
                    println("plugin file obfuscated")
                } else {
                    println("error: some file does not exist, plugin file not obfuscated")
                }
            }
        }

    }
}

