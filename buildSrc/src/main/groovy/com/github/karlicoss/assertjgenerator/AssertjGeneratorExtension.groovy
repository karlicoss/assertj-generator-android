package com.github.karlicoss.assertjgenerator

import groovy.transform.TypeChecked

@TypeChecked
public class AssertjGeneratorExtension {
    boolean useInUnitTests = true
    boolean useInAndroidTests = true
    Iterable<String> classesAndPackages = []
}