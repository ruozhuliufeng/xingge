package tech.msop.core.mybatis.encrypt.config;


import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import tech.msop.core.mybatis.encrypt.interceptor.CryptoInterceptor;
import tech.msop.core.mybatis.encrypt.interceptor.DesensitizeInterceptor;
import tech.msop.core.mybatis.encrypt.properties.CryptoProperties;

@AutoConfiguration
@EnableConfigurationProperties(CryptoProperties.class)
@AllArgsConstructor
public class CryptoConfiguration {

    private final CryptoProperties cryptoProperties;

    @Bean
    public CryptoInterceptor cryptoInterceptor(){
        return new CryptoInterceptor(cryptoProperties);
    }

    @Bean
    public DesensitizeInterceptor desensitizeInterceptor(){
        return new DesensitizeInterceptor();
    }
}
