package com.github.karlicoss.assertjgenerator

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.api.TestedVariant
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.compile.JavaCompile
import groovy.transform.TypeChecked

@TypeChecked
public class AssertjGeneratorPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def BaseExtension android
        def Iterable<? extends BaseVariant> variants
        if (project.plugins.hasPlugin(AppPlugin)) {
            android = project.extensions.getByType(AppExtension)
            variants = android.getApplicationVariants()
        } else if (project.plugins.hasPlugin(LibraryPlugin)) {
            android = project.extensions.getByType(LibraryExtension)
            variants = android.getLibraryVariants()
        } else {
            throw new GradleException('You must apply Android application plugin or Android library plugin first!')
        }

        def String extensionName = 'assertjGenerator'
        project.extensions.create(extensionName, AssertjGeneratorExtension)

        def String configurationName = 'assertjGenerator'
        def Configuration conf = project.configurations.create(configurationName).extendsFrom(
                project.configurations.getByName('compile'),
                project.configurations.getByName('provided')
        )

        project.afterEvaluate {
            variants.all { BaseVariant variant ->
                configureVariant(project, conf, variant)
            }
        }
    }

    /*
        TODO how to handle inputs and outputs properly?
        In order to do that I have to declare package/class files inputs somehow, it that possible with Gradle Android plugin?
    */

    private
    static configureVariant(Project project, Configuration configuration, BaseVariant variant) {
        def AssertjGeneratorExtension assertjGenerator = project.extensions.getByType(AssertjGeneratorExtension)

        def TestedVariant testedVariant = variant as TestedVariant // ugh no intersection types :(
        def JavaCompile javaCompileTask = variant.javaCompile

        // TODO is there a way not to hardcore 'generated' directory?
        def File baseDestinationDir = project.file(new File(project.buildDir, "generated/source/assertj"))
        def File destinationDir = new File(baseDestinationDir, variant.dirName)

        def Task assertjCleanTask = project.task(
                "assertjCleanr${variant.name.capitalize()}",
                type: Delete
        ).doFirst { Delete task ->
            task.delete(destinationDir)
        }

        def Task assertjGeneratorTask = project.task(
                "assertjGenerator${variant.name.capitalize()}",
                type: JavaExec,
                dependsOn: [javaCompileTask, assertjCleanTask]
        ).doFirst { JavaExec self ->
            destinationDir.mkdirs()

            // TODO is this the proper way to provide source files for this task?
            // TODO Is there a way not to hardcode android.jar dependency???
            self.setClasspath(project.files(configuration, javaCompileTask.destinationDir, new File(project.rootDir, 'build/generated/mockable-android-23.jar')))
            self.setWorkingDir(destinationDir)
            self.setMain('org.assertj.assertions.generator.cli.AssertionGeneratorLauncher')
            self.setArgs(assertjGenerator.classesAndPackages) // TODO check for empty classes/package names
        }

        def configureTestVariant = { BaseVariant bv ->
            bv.addJavaSourceFoldersToModel(destinationDir)
            bv.javaCompile.source(destinationDir)// TODO WTF? Isn't adding to model enough???
            bv.javaCompile.dependsOn(assertjGeneratorTask)
        }

        if (assertjGenerator.useInUnitTests && testedVariant.unitTestVariant != null) {
            configureTestVariant(testedVariant.unitTestVariant)
        }
        if (assertjGenerator.useInAndroidTests && testedVariant.testVariant != null) {
            configureTestVariant(testedVariant.testVariant)
        }
    }
}
