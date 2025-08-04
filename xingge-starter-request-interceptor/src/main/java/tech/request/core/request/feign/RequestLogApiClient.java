/*
 * Copyright (c) 2024 行歌(xingge)
 * 请求日志API客户端
 *
 * 功能说明：
 * - 定义请求日志API调用接口
 * - 支持单个和批量日志保存
 * - 基于OpenFeign实现
 */
package tech.request.core.request.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import tech.msop.core.tool.common.Result;
import tech.request.core.request.model.RequestLogInfo;

import java.util.List;

/**
 * 请求日志API客户端
 *
 * <p>该接口定义了与外部API交互的方法，用于保存请求日志信息。
 * 通过OpenFeign实现HTTP客户端调用。</p>
 *
 * <p>API接口规范：</p>
 * <ul>
 *   <li>单个保存：POST /api/request/interceptor/log/save</li>
 *   <li>批量保存：POST /api/request/interceptor/log/batch-save</li>
 * </ul>
 *
 * @author 若竹流风
 * @version 0.0.3
 * @since 2025-07-11
 */
@FeignClient(
        name = "request-log-api",
        url = "${xg.request.api.url:http://localhost:8080}",
        configuration = RequestLogFeignConfiguration.class
)
public interface RequestLogApiClient {

    /**
     * 保存单个请求日志
     *
     * @param requestLogInfo 请求日志信息
     * @return API响应结果
     */
    @PostMapping("/api/request/interceptor/log/save")
    Result<Void> saveRequestLog(@RequestBody RequestLogInfo requestLogInfo);

    /**
     * 批量保存请求日志
     *
     * @param requestLogInfoList 请求日志信息列表
     * @return API响应结果
     */
    @PostMapping("/api/request/interceptor/log/batch-save")
    Result<Void> batchSaveRequestLog(@RequestBody List<RequestLogInfo> requestLogInfoList);
}