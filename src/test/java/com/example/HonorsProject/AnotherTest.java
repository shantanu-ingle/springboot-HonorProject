package com.example.HonorsProject;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class AnotherTest {
    @Autowired
    private HelloController controller;

    @Test
    public void contextLoads() {
        assertThat(controller).isNotNull();
    }
}