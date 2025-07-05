/*
 * Copyright (c) 2024 行歌(xingge)
 * 请求拦截器测试应用
 * 
 * 功能说明：
 * - Spring Boot测试应用启动类
 * - 配置RestTemplate Bean
 * - 提供测试环境
 */
package tech.msop.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * 请求拦截器测试应用
 * 
 * <p>该类是用于测试请求拦截器功能的Spring Boot应用启动类，包含：</p>
 * <ul>
 *   <li>Spring Boot应用配置</li>
 *   <li>RestTemplate Bean配置</li>
 *   <li>测试环境初始化</li>
 * </ul>
 * 
 * <p>启动方法：</p>
 * <ol>
 *   <li>运行main方法启动应用</li>
 *   <li>访问 http://localhost:8080/test/health 检查服务状态</li>
 *   <li>访问其他测试接口验证拦截器功能</li>
 * </ol>
 * 
 * <p>测试接口列表：</p>
 * <ul>
 *   <li>GET /test/simple - 简单请求测试</li>
 *   <li>GET /test/params?name=张三&age=25 - 带参数请求测试</li>
 *   <li>POST /test/post - POST请求测试</li>
 *   <li>GET /test/rest-template - RestTemplate请求测试</li>
 *   <li>GET /test/error - 错误请求测试</li>
 *   <li>GET /test/large-data - 大数据量请求测试</li>
 *   <li>GET /test/health - 健康检查</li>
 * </ul>
 * 
 * @author 若竹流风
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication(scanBasePackages = {"tech.msop"})
public class RequestInterceptorTestApplication {
    
    /**
     * 应用程序入口点
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        String separator = createRepeatedString("=", 80);
        System.out.println(separator);
        System.out.println("🚀 启动请求拦截器测试应用");
        System.out.println("📝 测试接口地址: http://localhost:8080/test/");
        System.out.println("🔍 健康检查: http://localhost:8080/test/health");
        System.out.println(separator);
        
        SpringApplication.run(RequestInterceptorTestApplication.class, args);
        
        String separator2 = createRepeatedString("=", 80);
        System.out.println("\n" + separator2);
        System.out.println("✅ 请求拦截器测试应用启动成功！");
        System.out.println("📋 可用的测试接口:");
        System.out.println("   • GET  /test/simple           - 简单请求测试");
        System.out.println("   • GET  /test/params           - 带参数请求测试");
        System.out.println("   • POST /test/post             - POST请求测试");
        System.out.println("   • GET  /test/rest-template    - RestTemplate请求测试");
        System.out.println("   • GET  /test/error            - 错误请求测试");
        System.out.println("   • GET  /test/large-data       - 大数据量请求测试");
        System.out.println("   • GET  /test/health           - 健康检查");
        System.out.println(separator2);
    }
    
    /**
     * 配置RestTemplate Bean
     * 
     * <p>该Bean将被请求拦截器自动拦截，用于测试HTTP客户端拦截功能。</p>
     * 
     * @return RestTemplate实例
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    /**
     * 创建重复字符串（Java 8兼容）
     * 
     * @param str 要重复的字符串
     * @param count 重复次数
     * @return 重复后的字符串
     */
    private static String createRepeatedString(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}