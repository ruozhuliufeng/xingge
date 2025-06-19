package tech.msop.xingge.interceptor.config;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import tech.msop.xingge.interceptor.interceptor.HttpClientInterceptor;
import tech.msop.xingge.interceptor.interceptor.HttpTemplateInterceptor;
import tech.msop.xingge.interceptor.interceptor.OkHttpInterceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * HTTP客户端配置
 * 配置RestTemplate、OkHttpClient并注册相应的拦截器
 * 
 * @author 若竹流风
 * @since 1.0.0
 */
@Configuration
public class HttpClientConfig {

    @Autowired
    private HttpClientInterceptor httpClientInterceptor;
    
    @Autowired
    private HttpTemplateInterceptor httpTemplateInterceptor;
    
    @Autowired
    private OkHttpInterceptor okHttpInterceptor;

    /**
     * 配置RestTemplate并添加拦截器
     */
    @Bean
    @ConditionalOnClass(RestTemplate.class)
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // 添加拦截器
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(httpClientInterceptor);
        interceptors.add(httpTemplateInterceptor);
        restTemplate.setInterceptors(interceptors);
        
        return restTemplate;
    }
    
    /**
     * 配置OkHttpClient并添加拦截器
     */
    @Bean
    @ConditionalOnClass(OkHttpClient.class)
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(okHttpInterceptor)
                .build();
    }
}