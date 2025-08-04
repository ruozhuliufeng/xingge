/*
 * Copyright (c) 2024 行歌(xingge)
 * 测试控制器
 * 
 * 功能说明：
 * - 提供测试接口用于验证请求拦截功能
 * - 演示不同HTTP客户端的使用
 * - 展示请求拦截器的效果
 */
package tech.msop.core.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 * 
 * <p>该控制器提供了一些测试接口，用于验证请求拦截器的功能：</p>
 * <ul>
 *   <li>模拟HTTP请求</li>
 *   <li>测试不同的请求方法</li>
 *   <li>验证请求拦截和日志记录</li>
 * </ul>
 * 
 * <p>使用方法：</p>
 * <ol>
 *   <li>启动应用</li>
 *   <li>访问测试接口</li>
 *   <li>查看日志输出或数据库记录</li>
 * </ol>
 * 
 * @author 若竹流风
 * @version 0.0.2
 * @since 2025-07-11
 */
@RestController
@RequestMapping("/test")
public class TestController {
    
    @Autowired(required = false)
    private RestTemplate restTemplate;
    
    /**
     * 简单的GET请求测试
     * 
     * @return 测试响应
     */
    @GetMapping("/simple")
    public Map<String, Object> simpleTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "这是一个简单的GET请求测试");
        response.put("timestamp", System.currentTimeMillis());
        response.put("success", true);
        return response;
    }
    
    /**
     * 带参数的GET请求测试
     * 
     * @param name 姓名参数
     * @param age 年龄参数
     * @return 测试响应
     */
    @GetMapping("/params")
    public Map<String, Object> paramsTest(@RequestParam String name, 
                                         @RequestParam(defaultValue = "0") int age) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "带参数的GET请求测试");
        response.put("name", name);
        response.put("age", age);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    /**
     * POST请求测试
     * 
     * @param requestData 请求数据
     * @return 测试响应
     */
    @PostMapping("/post")
    public Map<String, Object> postTest(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "这是一个POST请求测试");
        response.put("receivedData", requestData);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    /**
     * 使用RestTemplate发起HTTP请求的测试
     * 
     * @return 测试响应
     */
    @GetMapping("/rest-template")
    public Map<String, Object> restTemplateTest() {
        Map<String, Object> response = new HashMap<>();
        
        if (restTemplate != null) {
            try {
                // 发起一个HTTP请求，这个请求会被拦截器捕获
                String result = restTemplate.getForObject("http://httpbin.org/get", String.class);
                response.put("message", "RestTemplate请求测试成功");
                response.put("result", result);
                response.put("success", true);
            } catch (Exception e) {
                response.put("message", "RestTemplate请求测试失败");
                response.put("error", e.getMessage());
                response.put("success", false);
            }
        } else {
            response.put("message", "RestTemplate未配置");
            response.put("success", false);
        }
        
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    /**
     * 模拟错误请求的测试
     * 
     * @return 错误响应
     */
    @GetMapping("/error")
    public Map<String, Object> errorTest() {
        // 模拟一个业务异常
        throw new RuntimeException("这是一个模拟的错误，用于测试错误处理");
    }
    
    /**
     * 大数据量请求测试
     * 
     * @return 大数据响应
     */
    @GetMapping("/large-data")
    public Map<String, Object> largeDataTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "大数据量请求测试");
        
        // 生成一些大数据
        StringBuilder largeData = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeData.append("这是第").append(i + 1).append("行数据，用于测试大数据量的请求和响应处理。");
        }
        
        response.put("largeData", largeData.toString());
        response.put("dataSize", largeData.length());
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    /**
     * 健康检查接口
     * 
     * @return 健康状态
     */
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "请求拦截器测试服务运行正常");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}