[![Build Status](https://travis-ci.org/karlicoss/assertj-generator-android.svg?branch=master)](https://travis-ci.org/karlicoss/assertj-generator-android)

This plugin uses [AssertJ generator](http://joel-costigliola.github.io/assertj/assertj-assertions-generator.html) to generate assertions specific to
your classes. No need to run it manually and commit assertions boilerplate in your repository!

For examples of generated assertions boilerplate, see the [generator page](http://joel-costigliola.github.io/assertj/assertj-assertions-generator.html).

# Configuring

```groovy
apply plugin: com.github.karlicoss.assertj-generator

assertjGenerator {
    // Put the class names and package names for assertJ generator here
    classesAndPackages = ['com.github.karlicoss.assertjgenerator.demo.entities']
    
    useInUnitTests = true
    
    // if you only need to generate assertions in unit tests, you can set this flag  
    useInAndroidTests = false
}


dependencies {
    assertjGenerator libraries.assertJ
    // Update generator version if necessary
    assertjGenerator 'org.assertj:assertj-assertions-generator:2.0.0'
}

```
