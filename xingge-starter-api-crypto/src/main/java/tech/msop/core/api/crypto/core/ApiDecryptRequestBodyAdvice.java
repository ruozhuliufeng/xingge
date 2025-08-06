package tech.msop.core.api.crypto.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import tech.msop.core.api.crypto.annotation.decrypt.ApiDecrypt;
import tech.msop.core.api.crypto.bean.CryptoInfoBean;
import tech.msop.core.api.crypto.bean.DecryptHttpInputMessage;
import tech.msop.core.api.crypto.config.ApiCryptoProperties;
import tech.msop.core.api.crypto.exception.DecryptBodyFailException;
import tech.msop.core.api.crypto.util.ApiCryptoUtil;
import tech.msop.core.tool.utils.ClassUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

/**
 * 请求数据的加密信息解密处理<br>
 * 本类只对控制器参数中含有<strong>{@link org.springframework.web.bind.annotation.RequestBody}</strong>
 * 以及package为<strong><code>tech.msop.core.api.signature.annotation.decrypt</code></strong>下的注解有效
 *
 * @author ruozhuliufeng
 * @see RequestBodyAdvice
 */
@Slf4j
@Order(1)
@AutoConfiguration
@ControllerAdvice
@RequiredArgsConstructor
@ConditionalOnProperty(value = ApiCryptoProperties.PREFIX  + ".enabled", havingValue = "true",matchIfMissing = true)
public class ApiDecryptRequestBodyAdvice implements RequestBodyAdvice {
    private final ApiCryptoProperties properties;
    @Override
    public boolean supports(MethodParameter methodParameter,
                            @NonNull Type type,
                            @NonNull Class<? extends HttpMessageConverter<?>> aClass) {
        return ClassUtil.isAnnotated(methodParameter.getMethod(), ApiDecrypt.class );
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage,
                                           @NonNull MethodParameter parameter,
                                           @NonNull Type type,
                                           @NonNull Class<? extends HttpMessageConverter<?>> aClass) throws IOException {
// 判断 body 是否为空
        InputStream messageBody = inputMessage.getBody();
        if (messageBody.available() <= 0) {
            return inputMessage;
        }
        byte[] decryptedBody = null;
        CryptoInfoBean cryptoInfoBean = ApiCryptoUtil.getDecryptInfo(parameter);
        if (cryptoInfoBean != null) {
            // base64 byte array
            byte[] bodyByteArray = StreamUtils.copyToByteArray(messageBody);
            decryptedBody = ApiCryptoUtil.decryptData(properties, bodyByteArray, cryptoInfoBean);
        }
        if (decryptedBody == null) {
            throw new DecryptBodyFailException("Decryption error, " +
                    "please check if the selected source data is encrypted correctly." +
                    " (解密错误，请检查选择的源数据的加密方式是否正确。)");
        }
        InputStream inputStream = new ByteArrayInputStream(decryptedBody);
        return new DecryptHttpInputMessage(inputStream, inputMessage.getHeaders());
    }

    @Override
    public Object afterBodyRead(@NonNull Object body,
                                @NonNull HttpInputMessage httpInputMessage,
                                @NonNull MethodParameter methodParameter,
                                @NonNull Type type,
                                @NonNull Class<? extends HttpMessageConverter<?>> aClass) {
        return body;
    }

    @Override
    public Object handleEmptyBody(@NonNull Object body,
                                  @NonNull HttpInputMessage httpInputMessage,
                                  @NonNull MethodParameter methodParameter,
                                  @NonNull Type type,
                                  @NonNull Class<? extends HttpMessageConverter<?>> aClass) {
        return body;
    }
}
