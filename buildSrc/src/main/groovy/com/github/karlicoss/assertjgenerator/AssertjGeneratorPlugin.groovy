package com.github.karlicoss.assertjgenerator

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.TestedVariant
import com.android.build.gradle.internal.tasks.MockableAndroidJarTask
import com.android.builder.model.AndroidProject
import com.google.common.base.Joiner
import groovy.transform.TypeChecked
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.compile.JavaCompile

@TypeChecked
public class AssertjGeneratorPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def Iterable<? extends BaseVariant> variants
        if (project.plugins.hasPlugin(AppPlugin)) {
            def app = project.extensions.getByType(AppExtension)
            variants = app.getApplicationVariants()
        } else if (project.plugins.hasPlugin(LibraryPlugin)) {
            def lib = project.extensions.getByType(LibraryExtension)
            variants = lib.getLibraryVariants()
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

    private static configureVariant(Project project, Configuration configuration, BaseVariant variant) {
        def AssertjGeneratorExtension assertjGenerator = project.extensions.getByType(AssertjGeneratorExtension)

        def TestedVariant testedVariant = variant as TestedVariant // ugh no intersection types :(
        def JavaCompile javaCompileTask = variant.javaCompile

        def File baseDestinationDir = project.file(Joiner.on(File.separatorChar).join(project.buildDir, AndroidProject.FD_GENERATED, "source", "assertj"))
        def File destinationDir = new File(baseDestinationDir, variant.dirName)

        def MockableAndroidJarTask mockableAndroidJarTask = project.tasks.getByName('mockableAndroidJar') as MockableAndroidJarTask


        def Task assertjGeneratorTask = project.task(
                "assertjGenerator${variant.name.capitalize()}",
                type: JavaExec,
                dependsOn: [javaCompileTask, mockableAndroidJarTask]
        ).doFirst { JavaExec self ->
            destinationDir.mkdirs()

            /*
                I have to add android.jar to classpath so assertj generator could classload and discover assertion targets
                is this actually the proper way of providing this dependency?
             */
            self.setClasspath(project.files(configuration, javaCompileTask.outputs, mockableAndroidJarTask.outputs))
            self.setWorkingDir(destinationDir)
            self.setMain('org.assertj.assertions.generator.cli.AssertionGeneratorLauncher')
            self.setArgs(assertjGenerator.classesAndPackages) // TODO check for empty classes/package names
        }
        assertjGeneratorTask.setGroup("AssertJ generator")
        assertjGeneratorTask.setDescription("Generate assertions for ${variant.name} classes")
        assertjGeneratorTask.inputs.files(getGeneratorInputs(javaCompileTask, assertjGenerator))
        assertjGeneratorTask.outputs.dir(destinationDir)


        def configureTestVariant = { BaseVariant bv ->
            /*
                I wish I could just use bv.registerJavaGeneratingTask(assertjGeneratorTask, destinationDir)
                However, it results in NullPointerException. For some reason, bv.variantData.sourceGenTask is null.
             */
            bv.addJavaSourceFoldersToModel(destinationDir)
            bv.javaCompile.source(destinationDir)
            bv.javaCompile.dependsOn(assertjGeneratorTask)
        }

        if (assertjGenerator.useInUnitTests && testedVariant.unitTestVariant != null) {
            configureTestVariant(testedVariant.unitTestVariant)
        }
        if (assertjGenerator.useInAndroidTests && testedVariant.testVariant != null) {
            configureTestVariant(testedVariant.testVariant)
        }
    }

    /*
        In current implementation, we just map dots in class/package names to filename separators, and check the subpathes we get against file names
        in source code tree.

        However, this doesn't work for
          1) inner classes (e.g. com.example.SomeClass.InnerStatic), since its Java file name would actually be com/exampl/SomeClass.java
          2) multiple classes defined in one file

        A more appropriate way of doing this is probably:
          1) Checking file name against the pattern. If it passes the check, include the file.
          2) If if didn't pass the check, parse it, extract all the classes defined in it and check against the patterns

        However, I'm not sure if it is a big issue anyway, and I couldn't find any simple Java parser, so if you have any ideas on the proper way of
        fixing this, please tell!
    */
    private static FileCollection getGeneratorInputs(JavaCompile javaCompile, AssertjGeneratorExtension extension) {
        def trackedPathes = extension.classesAndPackages.collect { String name -> name.replace('.' as char, File.separatorChar)}
        return javaCompile.inputs.files.filter { File file ->  trackedPathes.any {tracked -> file.path.contains(tracked)}}
    }
}
