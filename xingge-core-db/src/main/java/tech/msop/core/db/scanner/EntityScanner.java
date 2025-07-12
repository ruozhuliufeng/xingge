/**
 * 实体类扫描器
 * 
 * @author xingge-core-db
 * @since 1.0.0
 */
package tech.msop.core.db.scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import tech.msop.core.db.annotation.Table;
import tech.msop.core.db.config.TableMaintenanceConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 实体类扫描器
 * 用于扫描指定包下的实体类并解析其注解信息
 */
public class EntityScanner {
    
    private static final Logger logger = LoggerFactory.getLogger(EntityScanner.class);
    
    /**
     * 资源模式解析器
     */
    private final ResourcePatternResolver resourcePatternResolver;
    
    /**
     * 元数据读取器工厂
     */
    private final MetadataReaderFactory metadataReaderFactory;
    
    /**
     * 表维护配置
     */
    private final TableMaintenanceConfig config;
    
    /**
     * 构造函数
     * 
     * @param config 表维护配置
     */
    public EntityScanner(TableMaintenanceConfig config) {
        this.config = config;
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
        this.metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);
    }
    
    /**
     * 扫描所有实体类
     * 
     * @return 实体类列表
     * @throws IOException 扫描过程中发生IO异常
     */
    public List<Class<?>> scanEntities() throws IOException {
        List<Class<?>> entities = new ArrayList<>();
        
        // 如果配置了包含的实体类，则只扫描这些实体类
        if (!config.getIncludeEntities().isEmpty()) {
            for (String entityClassName : config.getIncludeEntities()) {
                try {
                    Class<?> entityClass = Class.forName(entityClassName);
                    if (isValidEntity(entityClass)) {
                        entities.add(entityClass);
                        logger.debug("包含实体类: {}", entityClassName);
                    }
                } catch (ClassNotFoundException e) {
                    logger.warn("无法找到指定的实体类: {}", entityClassName, e);
                }
            }
            return entities;
        }
        
        // 扫描指定包下的实体类
        Set<String> scannedClasses = new HashSet<>();
        for (String packageName : config.getEntityPackages()) {
            List<Class<?>> packageEntities = scanPackage(packageName, scannedClasses);
            entities.addAll(packageEntities);
        }
        
        // 过滤排除的实体类
        entities.removeIf(entityClass -> config.getExcludeEntities().contains(entityClass.getName()));
        
        logger.info("扫描到 {} 个实体类", entities.size());
        return entities;
    }
    
    /**
     * 扫描指定包下的实体类
     * 
     * @param packageName 包名
     * @param scannedClasses 已扫描的类名集合（用于去重）
     * @return 实体类列表
     * @throws IOException 扫描过程中发生IO异常
     */
    private List<Class<?>> scanPackage(String packageName, Set<String> scannedClasses) throws IOException {
        List<Class<?>> entities = new ArrayList<>();
        
        if (packageName == null || packageName.trim().isEmpty()) {
            logger.warn("包名为空，跳过扫描");
            return entities;
        }
        
        logger.debug("开始扫描包: {}", packageName);
        
        // 构建资源路径模式
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                ClassUtils.convertClassNameToResourcePath(packageName) + "/**/*.class";
        
        try {
            Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
            
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    try {
                        MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                        String className = metadataReader.getClassMetadata().getClassName();
                        
                        // 避免重复扫描
                        if (scannedClasses.contains(className)) {
                            continue;
                        }
                        scannedClasses.add(className);
                        
                        // 检查是否为实体类
                        if (isEntityClass(metadataReader)) {
                            try {
                                Class<?> entityClass = Class.forName(className);
                                if (isValidEntity(entityClass)) {
                                    entities.add(entityClass);
                                    logger.debug("发现实体类: {}", className);
                                }
                            } catch (ClassNotFoundException e) {
                                logger.warn("无法加载类: {}", className, e);
                            }
                        }
                    } catch (IOException e) {
                        logger.warn("读取资源失败: {}", resource.getDescription(), e);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("扫描包 {} 时发生异常", packageName, e);
            throw e;
        }
        
        logger.debug("包 {} 扫描完成，发现 {} 个实体类", packageName, entities.size());
        return entities;
    }
    
    /**
     * 检查类是否为实体类（通过元数据）
     * 
     * @param metadataReader 元数据读取器
     * @return 是否为实体类
     */
    private boolean isEntityClass(MetadataReader metadataReader) {
        // 检查是否有@Table注解
        return metadataReader.getAnnotationMetadata().hasAnnotation(Table.class.getName());
    }
    
    /**
     * 验证实体类是否有效
     * 
     * @param entityClass 实体类
     * @return 是否有效
     */
    private boolean isValidEntity(Class<?> entityClass) {
        if (entityClass == null) {
            return false;
        }
        
        // 检查是否有@Table注解
        if (!entityClass.isAnnotationPresent(Table.class)) {
            logger.debug("类 {} 没有@Table注解，跳过", entityClass.getName());
            return false;
        }
        
        // 检查是否为接口或抽象类
        if (entityClass.isInterface() || java.lang.reflect.Modifier.isAbstract(entityClass.getModifiers())) {
            logger.debug("类 {} 是接口或抽象类，跳过", entityClass.getName());
            return false;
        }
        
        // 检查是否在排除列表中
        if (config.getExcludeEntities().contains(entityClass.getName())) {
            logger.debug("类 {} 在排除列表中，跳过", entityClass.getName());
            return false;
        }
        
        // 检查@Table注解的autoMaintain属性
        Table tableAnnotation = entityClass.getAnnotation(Table.class);
        if (!tableAnnotation.autoMaintain()) {
            logger.debug("类 {} 的@Table注解设置了autoMaintain=false，跳过", entityClass.getName());
            return false;
        }
        
        return true;
    }
    
    /**
     * 扫描单个实体类
     * 
     * @param entityClass 实体类
     * @return 是否为有效的实体类
     */
    public boolean scanEntity(Class<?> entityClass) {
        return isValidEntity(entityClass);
    }
    
    /**
     * 获取实体类的表名
     * 
     * @param entityClass 实体类
     * @return 表名
     */
    public String getTableName(Class<?> entityClass) {
        if (!entityClass.isAnnotationPresent(Table.class)) {
            return null;
        }
        
        Table tableAnnotation = entityClass.getAnnotation(Table.class);
        String tableName = tableAnnotation.name();
        
        // 如果没有指定表名，则使用类名转换
        if (tableName.isEmpty()) {
            tableName = entityClass.getSimpleName();
            if (config.isCamelCaseToUnderscore()) {
                tableName = camelCaseToUnderscore(tableName);
            }
        }
        
        // 添加前缀和后缀
        if (config.getTablePrefix() != null && !config.getTablePrefix().isEmpty()) {
            tableName = config.getTablePrefix() + tableName;
        }
        if (config.getTableSuffix() != null && !config.getTableSuffix().isEmpty()) {
            tableName = tableName + config.getTableSuffix();
        }
        
        return tableName;
    }
    
    /**
     * 驼峰命名转下划线命名
     * 
     * @param camelCase 驼峰命名字符串
     * @return 下划线命名字符串
     */
    private String camelCaseToUnderscore(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}