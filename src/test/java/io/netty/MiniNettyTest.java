package io.netty;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 项目初始化测试 - 验证测试框架配置正确
 */
class MiniNettyTest {

    @Test
    void projectInitialized() {
        // 验证项目结构正确初始化
        assertThat(true).isTrue();
    }

    @Test
    void junitAndAssertjWorking() {
        // 验证 JUnit 5 和 AssertJ 正常工作
        String message = "Hello, Mini-Netty!";
        assertThat(message)
                .isNotNull()
                .startsWith("Hello")
                .contains("Mini-Netty");
    }
}
