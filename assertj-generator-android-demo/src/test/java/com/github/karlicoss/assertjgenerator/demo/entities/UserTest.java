package com.github.karlicoss.assertjgenerator.demo.entities;

import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;

public class UserTest {

    private User user;

    @Before
    public void beforeEachTest() {
        user = new User("Dmitrii", true, asList("read", "write"));
    }

    @Test
    public void assertionsGeneratorDemo() {
        UserAssert.assertThat(user).hasName("Dmitrii");
        UserAssert.assertThat(user).isPremium();
        UserAssert.assertThat(user).hasPermissions("read");
    }
}