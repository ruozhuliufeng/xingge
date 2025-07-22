package tech.msop.core.log;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 测试应用启动类
 * 用于审计日志功能测试的Spring Boot应用
 *
 * @author 星歌
 * @since 1.0.0
 */
@SpringBootApplication
public class TestApplication {

    /**
     * 应用程序入口点
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}