group = "app.morphe.patches.fromm"

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(11)
}

// ── Version constant generation ───────────────────────────────────────────

val generatedSrcDir = layout.buildDirectory.dir("generated/source/version")

val generateVersionKt by tasks.registering {
    inputs.property("version", project.version)
    val outFile = generatedSrcDir.map { it.file("app/morphe/patches/fromm/PatchVersion.kt") }
    outputs.file(outFile)
    doLast {
        outFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText(
                "package app.morphe.patches.fromm\n\n" +
                "internal const val PATCH_VERSION = \"${project.version}\"\n"
            )
        }
    }
}

sourceSets.main {
    kotlin.srcDir(generatedSrcDir)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn(generateVersionKt)
}

// ── Copy to live folder after build ───────────────────────────────────────

tasks.named("buildAndroid") {
    doLast {
        val mpp = layout.buildDirectory.file("libs/patches-${project.version}.mpp").get().asFile
        val dest = file("C:/Users/Administrator/Downloads/live/patches-latest.mpp")
        mpp.copyTo(dest, overwrite = true)
        println("Copied ${mpp.name} → ${dest.absolutePath}")
    }
}

patches {
    about {
        name = "fromm Patches"
        description = "Patches for the fromm fan app"
        author = ""
        contact = ""
        website = ""
        source = ""
        license = "GNU General Public License v3.0"
    }
}
