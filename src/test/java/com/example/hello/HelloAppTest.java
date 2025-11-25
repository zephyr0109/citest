package com.example.hello;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HelloAppTest {

    @Test
    void testGreet() {
        HelloApp app = new HelloApp();
        System.out.println("test done");
        assertEquals("Hello CI/CD!", app.greet());
    }
}
