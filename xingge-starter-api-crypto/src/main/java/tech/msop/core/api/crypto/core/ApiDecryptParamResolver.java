package tech.msop.core.api.crypto.core;

import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import tech.msop.core.api.crypto.annotation.decrypt.ApiDecrypt;
import tech.msop.core.api.crypto.bean.CryptoInfoBean;
import tech.msop.core.api.crypto.config.ApiCryptoProperties;
import tech.msop.core.api.crypto.util.ApiCryptoUtil;
import tech.msop.core.tool.jackson.JsonUtil;
import tech.msop.core.tool.utils.Charsets;
import tech.msop.core.tool.utils.StringUtil;

import java.lang.reflect.Parameter;

/**
 * param 参数解析
 *
 * @author ruozhuliufeng
 */
@RequiredArgsConstructor
public class ApiDecryptParamResolver implements HandlerMethodArgumentResolver {
    private final ApiCryptoProperties apiCryptoProperties;
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AnnotatedElementUtils.hasAnnotation(parameter.getParameter(), ApiDecrypt.class);
    }

    @Nullable
    @Override
    public Object resolveArgument(MethodParameter methodParameter,
                                  ModelAndViewContainer modelAndViewContainer,
                                  NativeWebRequest nativeWebRequest,
                                  WebDataBinderFactory webDataBinderFactory) throws Exception {
        Parameter parameter = methodParameter.getParameter();
        ApiDecrypt apiDecrypt = AnnotatedElementUtils.getMergedAnnotation(parameter, ApiDecrypt.class);
        String text = nativeWebRequest.getParameter(apiCryptoProperties.getParamName());
        if (StringUtil.isBlank(text)) {
            return null;
        }
        CryptoInfoBean infoBean = new CryptoInfoBean(apiDecrypt.value(), apiDecrypt.secretKey());
        byte[] textBytes = text.getBytes(Charsets.UTF_8);
        byte[] decryptData = ApiCryptoUtil.decryptData(apiCryptoProperties, textBytes, infoBean);
        return JsonUtil.readValue(decryptData, parameter.getType());
    }
}
