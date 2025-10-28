package tech.msop.core.file.config;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import tech.msop.core.file.FileService;
import tech.msop.core.file.properties.FileProperties;

/**
 * 文件服务自动配置
 *
 * @author ruozhuliufeng
 */
@AutoConfiguration
@AllArgsConstructor
@EnableConfigurationProperties(FileProperties.class)
@ConditionalOnProperty(prefix = "xingge.file", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FileConfiguration {

    private final FileProperties fileProperties;

    @Bean
    public FileService fileService() {
        return new FileService(fileProperties);
    }
}
