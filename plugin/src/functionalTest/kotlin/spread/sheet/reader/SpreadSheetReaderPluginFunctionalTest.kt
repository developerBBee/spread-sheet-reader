/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package spread.sheet.reader

import java.io.File
import kotlin.test.assertTrue
import kotlin.test.Test
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir

/**
 * A simple functional test for the 'spread.sheet.reader.output' plugin.
 */
class SpreadSheetReaderPluginFunctionalTest {

    @field:TempDir
    lateinit var projectDir: File

    private val buildFile by lazy { projectDir.resolve("build.gradle") }
    private val settingsFile by lazy { projectDir.resolve("settings.gradle") }

    @Test fun `can run task`() {
        // Set up the test build
        settingsFile.writeText("")
        buildFile.writeText("""
            plugins {
                id('spread.sheet.reader.output')
            }
        """.trimIndent())

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("output")
        runner.withProjectDir(projectDir)
        val result = runner.build()

        // Verify the result
        //assertTrue(result.output.contains("Hello from plugin 'spread.sheet.reader.output'"))
    }
}
