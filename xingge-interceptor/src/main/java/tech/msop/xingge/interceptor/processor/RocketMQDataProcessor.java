package tech.msop.xingge.interceptor.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.msop.xingge.interceptor.DataProcessor;
import tech.msop.xingge.interceptor.InterceptData;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RocketMQ消息处理器
 * 将拦截数据发送到RocketMQ消息队列
 * 
 * @author 若竹流风
 * @since 1.0.0
 */
@Slf4j
@Component
public class RocketMQDataProcessor implements DataProcessor {

    private static final String PROCESSOR_TYPE = "rocketmq";
    
    // 配置键
    private static final String CONFIG_TOPIC = "topic";
    private static final String CONFIG_TAG = "tag";
    private static final String CONFIG_KEYS = "keys";
    private static final String CONFIG_PRODUCER_GROUP = "producerGroup";
    private static final String CONFIG_NAME_SERVER = "nameServer";
    private static final String CONFIG_SEND_TIMEOUT = "sendTimeout";
    private static final String CONFIG_RETRY_TIMES = "retryTimes";
    private static final String CONFIG_MESSAGE_FORMAT = "messageFormat";
    private static final String CONFIG_CUSTOM_PROPERTIES = "customProperties";
    
    @Autowired(required = false)
    private DefaultMQProducer defaultMQProducer;
    
    @Autowired(required = false)
    private ObjectMapper objectMapper;
    
    // 缓存不同配置的生产者
    private final Map<String, DefaultMQProducer> producerCache = new ConcurrentHashMap<>();

    @Override
    public String getType() {
        return PROCESSOR_TYPE;
    }

    @Override
    public void process(InterceptData data, Map<String, Object> config) {
        try {
            String topic = (String) config.get(CONFIG_TOPIC);
            if (topic == null || topic.trim().isEmpty()) {
                log.error("RocketMQ处理器缺少topic配置");
                return;
            }
            
            // 获取或创建生产者
            DefaultMQProducer producer = getOrCreateProducer(config);
            if (producer == null) {
                log.error("无法获取RocketMQ生产者");
                return;
            }
            
            // 构建消息
            Message message = buildMessage(data, config);
            
            // 发送消息
            sendMessage(producer, message, config);
            
        } catch (Exception e) {
            log.error("RocketMQ处理器处理数据时发生异常", e);
        }
    }

    @Override
    public int getPriority() {
        return 70;
    }

    @Override
    public boolean validateConfig(Map<String, Object> config) {
        String topic = (String) config.get(CONFIG_TOPIC);
        if (topic == null || topic.trim().isEmpty()) {
            log.error("RocketMQ处理器配置验证失败：缺少topic");
            return false;
        }
        
        // 检查是否有可用的生产者或配置
        if (defaultMQProducer == null) {
            String nameServer = (String) config.get(CONFIG_NAME_SERVER);
            String producerGroup = (String) config.get(CONFIG_PRODUCER_GROUP);
            if (nameServer == null || producerGroup == null) {
                log.error("RocketMQ处理器配置验证失败：未找到默认生产者且缺少nameServer或producerGroup配置");
                return false;
            }
        }
        
        return true;
    }

    @Override
    public void initialize(Map<String, Object> config) {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
    }

    @Override
    public void destroy() {
        // 关闭所有缓存的生产者
        for (DefaultMQProducer producer : producerCache.values()) {
            try {
                producer.shutdown();
            } catch (Exception e) {
                log.warn("关闭RocketMQ生产者时发生异常", e);
            }
        }
        producerCache.clear();
    }

    /**
     * 获取或创建生产者
     */
    private DefaultMQProducer getOrCreateProducer(Map<String, Object> config) {
        // 优先使用默认注入的生产者
        if (defaultMQProducer != null) {
            return defaultMQProducer;
        }
        
        // 根据配置创建生产者
        String nameServer = (String) config.get(CONFIG_NAME_SERVER);
        String producerGroup = (String) config.get(CONFIG_PRODUCER_GROUP);
        
        if (nameServer == null || producerGroup == null) {
            return null;
        }
        
        String cacheKey = nameServer + "#" + producerGroup;
        return producerCache.computeIfAbsent(cacheKey, key -> {
            try {
                DefaultMQProducer producer = new DefaultMQProducer(producerGroup);
                producer.setNamesrvAddr(nameServer);
                
                Integer sendTimeout = (Integer) config.get(CONFIG_SEND_TIMEOUT);
                if (sendTimeout != null) {
                    producer.setSendMsgTimeout(sendTimeout);
                }
                
                Integer retryTimes = (Integer) config.get(CONFIG_RETRY_TIMES);
                if (retryTimes != null) {
                    producer.setRetryTimesWhenSendFailed(retryTimes);
                }
                
                producer.start();
                log.info("成功创建并启动RocketMQ生产者: {}", producerGroup);
                return producer;
                
            } catch (Exception e) {
                log.error("创建RocketMQ生产者失败", e);
                return null;
            }
        });
    }

    /**
     * 构建消息
     */
    private Message buildMessage(InterceptData data, Map<String, Object> config) throws Exception {
        String topic = (String) config.get(CONFIG_TOPIC);
        String tag = (String) config.getOrDefault(CONFIG_TAG, "");
        String keys = buildMessageKeys(data, config);
        
        // 构建消息体
        String messageBody = buildMessageBody(data, config);
        
        Message message = new Message(topic, tag, keys, messageBody.getBytes(StandardCharsets.UTF_8));
        
        // 设置自定义属性
        setMessageProperties(message, data, config);
        
        return message;
    }

    /**
     * 构建消息键
     */
    private String buildMessageKeys(InterceptData data, Map<String, Object> config) {
        String keysConfig = (String) config.get(CONFIG_KEYS);
        if (keysConfig != null && !keysConfig.trim().isEmpty()) {
            return evaluateExpression(data, keysConfig).toString();
        }
        
        // 默认使用ID作为键
        return data.getId();
    }

    /**
     * 构建消息体
     */
    private String buildMessageBody(InterceptData data, Map<String, Object> config) throws Exception {
        String messageFormat = (String) config.getOrDefault(CONFIG_MESSAGE_FORMAT, "json");
        
        switch (messageFormat.toLowerCase()) {
            case "json":
                return buildJsonMessage(data, config);
            case "simple":
                return buildSimpleMessage(data, config);
            case "full":
                return objectMapper.writeValueAsString(data);
            default:
                return buildJsonMessage(data, config);
        }
    }

    /**
     * 构建JSON格式消息
     */
    private String buildJsonMessage(InterceptData data, Map<String, Object> config) throws Exception {
        Map<String, Object> messageData = new HashMap<>();
        
        // 基础信息
        messageData.put("id", data.getId());
        messageData.put("interceptType", data.getInterceptType());
        messageData.put("interceptScope", data.getInterceptScope());
        messageData.put("method", data.getMethod());
        messageData.put("url", data.getUrl());
        messageData.put("path", data.getPath());
        messageData.put("responseStatus", data.getResponseStatus());
        messageData.put("duration", data.getDuration());
        messageData.put("clientIp", data.getClientIp());
        messageData.put("userId", data.getUserId());
        messageData.put("sessionId", data.getSessionId());
        messageData.put("tenantId", data.getTenantId());
        messageData.put("applicationName", data.getApplicationName());
        messageData.put("timestamp", data.getTimestamp());
        
        // 可选信息（根据配置决定是否包含）
        if (data.getQueryParams() != null && !data.getQueryParams().isEmpty()) {
            messageData.put("queryParams", data.getQueryParams());
        }
        if (data.getHeaders() != null && !data.getHeaders().isEmpty()) {
            messageData.put("headers", data.getHeaders());
        }
        if (data.getRequestBody() != null) {
            messageData.put("requestBody", data.getRequestBody());
        }
        if (data.getResponseBody() != null) {
            messageData.put("responseBody", data.getResponseBody());
        }
        if (data.getException() != null) {
            messageData.put("exception", data.getException());
        }
        
        return objectMapper.writeValueAsString(messageData);
    }

    /**
     * 构建简单格式消息
     */
    private String buildSimpleMessage(InterceptData data, Map<String, Object> config) {
        StringBuilder sb = new StringBuilder();
        sb.append("拦截数据: ");
        sb.append("ID=").append(data.getId());
        sb.append(", 类型=").append(data.getInterceptType());
        sb.append(", 方法=").append(data.getMethod());
        sb.append(", URL=").append(data.getUrl());
        if (data.getResponseStatus() != null) {
            sb.append(", 状态=").append(data.getResponseStatus());
        }
        if (data.getDuration() != null) {
            sb.append(", 耗时=").append(data.getDuration()).append("ms");
        }
        sb.append(", 时间=").append(data.getTimestamp());
        
        return sb.toString();
    }

    /**
     * 设置消息属性
     */
    private void setMessageProperties(Message message, InterceptData data, Map<String, Object> config) {
        // 基础属性
        message.putUserProperty("interceptType", data.getInterceptType());
        message.putUserProperty("interceptScope", data.getInterceptScope());
        message.putUserProperty("method", data.getMethod());
        message.putUserProperty("path", data.getPath());
        
        if (data.getResponseStatus() != null) {
            message.putUserProperty("responseStatus", String.valueOf(data.getResponseStatus()));
        }
        if (data.getClientIp() != null) {
            message.putUserProperty("clientIp", data.getClientIp());
        }
        if (data.getUserId() != null) {
            message.putUserProperty("userId", data.getUserId());
        }
        if (data.getTenantId() != null) {
            message.putUserProperty("tenantId", data.getTenantId());
        }
        if (data.getApplicationName() != null) {
            message.putUserProperty("applicationName", data.getApplicationName());
        }
        
        message.putUserProperty("timestamp", String.valueOf(data.getTimestamp().getTime()));
        
        // 自定义属性
        @SuppressWarnings("unchecked")
        Map<String, String> customProperties = (Map<String, String>) config.get(CONFIG_CUSTOM_PROPERTIES);
        if (customProperties != null && !customProperties.isEmpty()) {
            for (Map.Entry<String, String> entry : customProperties.entrySet()) {
                String propertyName = entry.getKey();
                String expression = entry.getValue();
                Object value = evaluateExpression(data, expression);
                if (value != null) {
                    message.putUserProperty(propertyName, value.toString());
                }
            }
        }
    }

    /**
     * 发送消息
     */
    private void sendMessage(DefaultMQProducer producer, Message message, Map<String, Object> config) {
        try {
            SendResult sendResult = producer.send(message);
            log.debug("成功发送拦截数据到RocketMQ: topic={}, msgId={}, status={}", 
                    message.getTopic(), sendResult.getMsgId(), sendResult.getSendStatus());
        } catch (Exception e) {
            log.error("发送消息到RocketMQ失败: topic={}, keys={}", message.getTopic(), message.getKeys(), e);
            throw new RuntimeException("发送RocketMQ消息失败", e);
        }
    }

    /**
     * 评估表达式（简单实现）
     */
    private Object evaluateExpression(InterceptData data, String expression) {
        // 这里可以实现更复杂的表达式解析
        // 目前只支持简单的字段引用
        switch (expression) {
            case "${id}":
                return data.getId();
            case "${interceptType}":
                return data.getInterceptType();
            case "${method}":
                return data.getMethod();
            case "${url}":
                return data.getUrl();
            case "${path}":
                return data.getPath();
            case "${clientIp}":
                return data.getClientIp();
            case "${userId}":
                return data.getUserId();
            case "${sessionId}":
                return data.getSessionId();
            case "${tenantId}":
                return data.getTenantId();
            case "${timestamp}":
                return data.getTimestamp().getTime();
            case "${date}":
                return data.getTimestamp();
            default:
                return expression;
        }
    }
}