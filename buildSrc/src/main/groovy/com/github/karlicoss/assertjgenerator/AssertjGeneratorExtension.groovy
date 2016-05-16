package com.github.karlicoss.assertjgenerator

import groovy.transform.TypeChecked

@TypeChecked
public class AssertjGeneratorExtension {
    /**
     * Set to false if you don't need generated assertions in unit tests (might give a minor build speedup).
     */
    boolean useInUnitTests = true

    /**
     * Set to false if you don't need generated assertions in instrumented tests (might give a minor build speedup).
     */
    boolean useInAndroidTests = true

    /**
     * Classes and packages to run assertions generator against. These values are passed intact to the generator.
     *
     * Examples of valid values: "org.assertj.examples.data", "org.assertj.examples.data.MyAssert".
     *
     * See https://joel-costigliola.github.io/assertj/assertj-assertions-generator.html for more information.
     */
    Iterable<String> classesAndPackages = []

    /**
     * Set this to true to force generator run on every code change.
     *
     * You probably don't need that unless you are passing inner classes in {@link #classesAndPackages}.
     */
    boolean forceRun = false
}