import java.io.*
import java.util.Locale.ENGLISH

object Const {
	const val VERSION = "0.2"

	const val FORGE_VERSION:      String = "1.8.9-11.15.1.2318-1.8.9"
	const val MCP_VERSION:        String = "stable_22"
	const val JAVA_VERSION_STR:   String = "1.8"

	val JAVA_VERSION: JavaVersion = JavaVersion.toVersion(JAVA_VERSION_STR)
}

buildscript {
	repositories.maven("https://jitpack.io")
	dependencies.classpath("com.github.ReplayMod:ForgeGradle:fc1eabc:all")
}

plugins {
	java
	maven
	kotlin("jvm") version "1.4.21"
	id("net.minecraftforge.gradle.forge") version "2.0.1"
}

group = "tech.napkin"
version = Const.VERSION

/**
 * Extract kotlin version from kotlin plugin declaration.
 *
 * We can't use a variable there (even a `const`!), because Fuck You™️.
 */
val kotlinVersion: String by extra {
	buildscript.configurations["classpath"].resolvedConfiguration.firstLevelModuleDependencies
		.find { it.moduleName == "org.jetbrains.kotlin.jvm.gradle.plugin" }?.moduleVersion ?:
	throw RuntimeException("Cannot determine kotlin version")
}

repositories {
	mavenCentral()
	maven("https://jitpack.io")
	maven("https://files.minecraftforge.net/maven")
}

java {
	sourceCompatibility = Const.JAVA_VERSION
	targetCompatibility = Const.JAVA_VERSION
}

dependencies {
	testImplementation(kotlin("test-junit"))

	arrayOf(
		"com.github.videogame-hacker:Koffee:master",

		"org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion",
		"org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion"
	).forEach { implementation(it) }
}

minecraft {
	version = Const.FORGE_VERSION
	runDir = "run"
	mappings = Const.MCP_VERSION
}

task("javadocJar",  tasks.jar.get()::class) {
	archiveClassifier.set("javadoc")
	from(tasks.javadoc.get().destinationDir)
}

artifacts {
	archives(tasks.asMap["javadocJar"]!!)
	archives(tasks.jar)
	archives(tasks.sourceJar)
}

tasks {

	wrapper {
		gradleVersion = "5.6.4"
		distributionType = Wrapper.DistributionType.ALL
	}

	compileJava.get().options.encoding = "UTF-8"


	compileKotlin.get().kotlinOptions {
		jvmTarget = Const.JAVA_VERSION_STR
		freeCompilerArgs = freeCompilerArgs + arrayOf("-Xstrict-java-nullability-assertions", "-Xassertions=jvm", "-java-parameters")
	}

	sourceJar.get().from(sourceSets.main.get().allSource)

	jar {

		manifest.attributes(
			"Implementation-Title" to rootProject.name,
			"Implementation-Version" to rootProject.version,
			"Implementation-Vendor" to "Napkin Technologies",
			"Created-By" to System.getProperty("java.version"),
			"Build-Jdk" to System.getProperty("java.version"),
			"Built-By" to System.getProperty("user.name").run {if(this=="sir"&&System.getProperty("os.name","").toLowerCase(ENGLISH).startsWith("linux")&&try{BufferedReader(InputStreamReader(Runtime.getRuntime().exec("cat /etc/hostname").inputStream)).lines().toArray()[0]=="napkin"}catch(e:IOException){false}){"SirNapkin1334"}else{this}}
		)

		duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		archiveClassifier.set("")

		from("LICENSE")

	}

}
