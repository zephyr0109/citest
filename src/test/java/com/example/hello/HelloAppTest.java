package com.example.hello;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HelloAppTest {

    @Test
    void testGreet() {
        HelloApp app = new HelloApp();
        assertEquals("Hello CI/CD!", app.greet());
    }
}
