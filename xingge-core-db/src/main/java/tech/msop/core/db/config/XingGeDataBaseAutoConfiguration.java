/**
 * 表结构自动维护自动配置类
 *
 * @author xingge-core-db
 * @since 1.0.0
 */
package tech.msop.core.db.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import tech.msop.core.db.dialect.Dialect;
import tech.msop.core.db.dialect.DialectFactory;
import tech.msop.core.db.parser.EntityMetadataParser;
import tech.msop.core.db.scanner.EntityScanner;
import tech.msop.core.db.service.TableMaintenanceService;
import tech.msop.core.db.service.AsyncTableMaintenanceService;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import tech.msop.core.tool.async.AsyncProcessor;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * 表结构自动维护自动配置类
 * 用于自动配置表结构维护相关的Bean和功能
 */
@Configuration
@ConditionalOnClass({DataSource.class})
@ConditionalOnProperty(prefix = "xg.db.table-maintenance", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(XingGeDataBaseCconfig.class)
@EnableAsync
public class XingGeDataBaseAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(XingGeDataBaseAutoConfiguration.class);
    @Autowired
    private AsyncProcessor asyncProcessor;

    /**
     * 创建实体元数据解析器Bean
     *
     * @return 实体元数据解析器
     */
    @Bean
    @ConditionalOnMissingBean
    public EntityMetadataParser entityMetadataParser() {
        logger.info("创建实体元数据解析器");
        return new EntityMetadataParser();
    }

    /**
     * 创建数据库方言Bean
     *
     * @param dataSource 数据源
     * @return 数据库方言
     */
    @Bean
    @ConditionalOnMissingBean
    public Dialect dialect(DataSource dataSource) {
        try {
            logger.info("创建数据库方言");
            Dialect dialect = DialectFactory.getDialect(dataSource);
            logger.info("使用数据库方言: {}", dialect.getClass().getSimpleName());
            return dialect;
        } catch (Exception e) {
            logger.error("创建数据库方言失败", e);
            throw new RuntimeException("创建数据库方言失败", e);
        }
    }

    /**
     * 创建实体扫描器Bean
     *
     * @param config 表维护配置
     * @return 实体扫描器
     */
    @Bean
    @ConditionalOnMissingBean
    public EntityScanner entityScanner(XingGeDataBaseCconfig config) {
        logger.info("创建实体扫描器");
        return new EntityScanner(config);
    }

    /**
     * 创建表维护服务Bean
     *
     * @param dataSource           数据源
     * @param dialect              数据库方言
     * @param entityMetadataParser 实体元数据解析器
     * @return 表维护服务
     */
    @Bean
    @ConditionalOnMissingBean
    public TableMaintenanceService tableMaintenanceService(
            DataSource dataSource,
            Dialect dialect,
            EntityMetadataParser entityMetadataParser) {
        logger.info("创建表维护服务");
        return new TableMaintenanceService(dataSource, dialect, entityMetadataParser);
    }

    /**
     * 创建异步表维护服务Bean
     *
     * @param dataSource              数据源
     * @param dialect                 数据库方言
     * @param entityMetadataParser    实体元数据解析器
     * @param tableMaintenanceService 表维护服务
     * @param config                  配置
     * @return 异步表维护服务
     */
    @Bean
    @ConditionalOnMissingBean
    public AsyncTableMaintenanceService asyncTableMaintenanceService(
            DataSource dataSource,
            Dialect dialect,
            EntityMetadataParser entityMetadataParser,
            TableMaintenanceService tableMaintenanceService,
            XingGeDataBaseCconfig config) {
        logger.info("创建异步表维护服务");
        return new AsyncTableMaintenanceService(dataSource, dialect, entityMetadataParser, tableMaintenanceService, config, asyncProcessor);
    }

    /**
     * 表结构维护专用线程池
     * 用于异步执行表结构维护任务
     *
     * @param config 配置
     * @return 线程池执行器
     */
    @Bean(name = "tableMaintenanceExecutor")
    @ConditionalOnMissingBean(name = "tableMaintenanceExecutor")
    public Executor tableMaintenanceExecutor(XingGeDataBaseCconfig config) {
        logger.info("创建表结构维护专用线程池");
        XingGeDataBaseCconfig.TableMaintenanceConfig maintenanceConfig = config.getTableMaintenance();

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(maintenanceConfig.getAsyncCorePoolSize());
        executor.setMaxPoolSize(maintenanceConfig.getAsyncMaxPoolSize());
        executor.setQueueCapacity(maintenanceConfig.getAsyncQueueCapacity());
        executor.setThreadNamePrefix(maintenanceConfig.getAsyncThreadNamePrefix());

        // 设置拒绝策略：调用者运行策略，确保任务不会丢失
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        // 设置线程池关闭时等待任务完成
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }

    /**
     * 创建表维护启动监听器Bean
     *
     * @param tableMaintenanceService 表维护服务
     * @param entityScanner           实体扫描器
     * @param config                  表维护配置
     * @param environment             Spring环境
     * @return 表维护启动监听器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "xingge.db.table-maintenance", name = "auto-execute-on-startup", havingValue = "true", matchIfMissing = true)
    public TableMaintenanceStartupListener tableMaintenanceStartupListener(
            TableMaintenanceService tableMaintenanceService,
            EntityScanner entityScanner,
            XingGeDataBaseCconfig config,
            Environment environment) {
        logger.info("创建表维护启动监听器");
        return new TableMaintenanceStartupListener(tableMaintenanceService, entityScanner, config, environment);
    }

    /**
     * 表维护启动监听器
     * 在应用启动完成后自动执行表结构维护
     */
    public static class TableMaintenanceStartupListener implements ApplicationListener<ApplicationReadyEvent> {

        private static final Logger logger = LoggerFactory.getLogger(TableMaintenanceStartupListener.class);

        private final TableMaintenanceService tableMaintenanceService;
        private final EntityScanner entityScanner;
        private final XingGeDataBaseCconfig config;
        private final Environment environment;

        /**
         * 构造函数
         *
         * @param tableMaintenanceService 表维护服务
         * @param entityScanner           实体扫描器
         * @param config                  表维护配置
         * @param environment             Spring环境
         */
        public TableMaintenanceStartupListener(
                TableMaintenanceService tableMaintenanceService,
                EntityScanner entityScanner,
                XingGeDataBaseCconfig config,
                Environment environment) {
            this.tableMaintenanceService = tableMaintenanceService;
            this.entityScanner = entityScanner;
            this.config = config;
            this.environment = environment;
        }

        /**
         * 处理应用启动完成事件
         *
         * @param event 应用启动完成事件
         */
        @Override
        public void onApplicationEvent(ApplicationReadyEvent event) {
            try {
                logger.info("开始执行表结构自动维护...");

                // 扫描实体类
                List<Class<?>> entities = entityScanner.scanEntities();
                if (entities.isEmpty()) {
                    logger.warn("未发现任何实体类，跳过表结构维护");
                    return;
                }

                logger.info("发现 {} 个实体类，开始维护表结构", entities.size());

                // 执行表结构维护
                long startTime = System.currentTimeMillis();
                tableMaintenanceService.maintainTables(entities);
                long endTime = System.currentTimeMillis();

                logger.info("表结构自动维护完成，耗时: {} ms", endTime - startTime);

            } catch (Exception e) {
                logger.error("表结构自动维护失败", e);

                // 根据配置决定是否抛出异常
                if (!config.getTableMaintenance().isContinueOnError()) {
                    throw new RuntimeException("表结构自动维护失败", e);
                }
            }
        }

        /**
         * 检查是否为测试环境
         *
         * @return 是否为测试环境
         */
        private boolean isTestEnvironment() {
            String[] activeProfiles = environment.getActiveProfiles();
            for (String profile : activeProfiles) {
                if ("test".equalsIgnoreCase(profile) ||
                        "testing".equalsIgnoreCase(profile) ||
                        "junit".equalsIgnoreCase(profile)) {
                    return true;
                }
            }

            // 检查是否在单元测试环境中运行
            try {
                Class.forName("org.junit.Test");
                return true;
            } catch (ClassNotFoundException e) {
                // 忽略异常
            }

            try {
                Class.forName("org.junit.jupiter.api.Test");
                return true;
            } catch (ClassNotFoundException e) {
                // 忽略异常
            }

            return false;
        }
    }
}