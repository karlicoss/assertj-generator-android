package com.github.karlicoss.assertjgenerator

import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.dsl.ProductFlavor
import org.gradle.api.GradleException
import org.gradle.api.internal.AbstractTask
import org.gradle.api.internal.plugins.PluginApplicationException
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.assertj.core.api.Assertions.assertThatThrownBy

public class AssertjGeneratorPluginTest {
    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    private ProjectInternal project

    @Before
    public void beforeEachTest() throws IOException {
        project = ProjectBuilder.builder().build() as ProjectInternal
        File alala =  testProjectDir.newFile("Alala.java")
        alala.write("package com.assertjgenerator.test; public class Alala { }")
    }

    @Test
    public void requiresAndroidOrLibraryPlugin() {
        assertThatThrownBy {
            project.apply plugin: AssertjGeneratorPlugin
        }
        .isInstanceOf(PluginApplicationException.class)
        .hasCauseInstanceOf(GradleException.class) // TODO assert that cause contains message?
    }

    @Test
    public void succeedsWithEmptyTargetsList() {
        project.apply plugin: 'com.android.application'
//        project.android {
//            compileSdkVersion 23
//            buildToolsVersion '23.0.3'
//
//            defaultConfig {
//                applicationId "com.assertjgenerator.test"
//                minSdkVersion 21
//                targetSdkVersion 22
//                versionCode 1
//                versionName "1.0"
//            }
//
//            sourceSets {
//                main.java.srcDir testProjectDir.root
//            }
//        }


        def AppExtension app = project.extensions.getByType(AppExtension.class)
        app.setCompileSdkVersion(23)
        app.setBuildToolsVersion('23.0.3')

        app.defaultConfig { ProductFlavor flavor ->
            flavor.setApplicationId('com.assertjgenerator.test')
            flavor.setMinSdkVersion(21)
            flavor.setTargetSdkVersion(22)
            flavor.setVersionCode(1)
            flavor.setVersionName('1.0')
        }

        println app.sourceSets.getNames()

        app.sourceSets {
            main {
                java {
                    srcDir testProjectDir.root
                }
            }
        }

        println "MAIN: " + (app.sourceSets.getByName("main").java.sourceDirectoryTrees as List)

//        app.sourceSets.getByName('debug')
//                .java.srcDirs.add(testProjectDir.root)


        project.repositories {
            mavenCentral()
        }

        project.apply plugin: AssertjGeneratorPlugin

        project.dependencies {
            assertjGenerator 'org.assertj:assertj-core:2.4.1'
            assertjGenerator 'org.assertj:assertj-assertions-generator:2.0.0'
        }

        def AssertjGeneratorExtension assertjGenerator = project.extensions.getByType(AssertjGeneratorExtension.class)
        assertjGenerator.setUseInUnitTests(true)
        assertjGenerator.setClassesAndPackages(['com.assertjgenerator.test.Alala'])
        assertjGenerator.setForceRun(true) // TODO

        // TODO extract
        project.evaluate()

        def AbstractTask debugGenerator = project.tasks.getByName('assertjGeneratorDebug') as AbstractTask // should not throw
//        def AbstractTask releaseGenerator = project.tasks.getByName('assertjGeneratorRelease') as AbstractTask // should not throw
        println "GENERATOR INPUTSL " + (debugGenerator.inputs.files as List)
        debugGenerator.execute()


        def task = project.tasks.getByName('compileDebugJavaWithJavac') as JavaCompile
        println task.inputs.files.asPath
//        task.ec
        task.execute()
        println "OUTPUTS!: " + (task.outputs.files as List)
    }
}