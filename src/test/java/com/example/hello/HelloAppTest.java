package com.example.hello;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelloAppTest {

    @Test
    void testGreet() {
        HelloApp app = new HelloApp();
        assertEquals("Hello CI/CD! - latest", app.greet());
        System.out.println("test done");

    }
}
