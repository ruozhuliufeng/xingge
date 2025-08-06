package tech.msop.core.redis.ratelimiter;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.EvaluationContext;
import org.springframework.util.Assert;
import tech.msop.core.tool.constant.CharConstant;
import tech.msop.core.tool.spel.MsExpressionEvaluator;
import tech.msop.core.tool.utils.StringUtil;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Redis 限流注解切面
 *
 * @author ruozhuliufeng
 */
@Aspect
@RequiredArgsConstructor
public class RedisRateLimiterAspect implements ApplicationContextAware {

    /**
     * Spring EL 表达式处理
     */
    private final MsExpressionEvaluator EVALUATOR = new MsExpressionEvaluator();

    /**
     * Redis 限流服务
     */
    private final RedisRateLimiterClient rateLimiterClient;
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * AOP 环切 注解 @RateLimiter
     */
    @Around("@annotation(limiter)")
    public Object aroundRateLimiter(ProceedingJoinPoint point, RateLimiter limiter) throws Throwable {
        String limieKey = limiter.value();
        Assert.hasText(limieKey, "@RateLimiter 值不能为空");
        // el 表达式
        String limitPram = limiter.param();
        // 表达式不为空
        String rateKey;
        if (StringUtil.isNotBlank(limitPram)) {
            String evalAsText = evalLimitParam(point, limitPram);
            rateKey = limieKey + CharConstant.COLON + evalAsText;
        } else {
            rateKey = limieKey;
        }
        long max = limiter.max();
        long ttl = limiter.ttl();
        TimeUnit timeUnit = limiter.timeUnit();
        return rateLimiterClient.allow(rateKey, max, ttl, timeUnit, point::proceed);

    }

    /**
     * 计算参数表达式
     *
     * @param point      切点
     * @param limitParam 参数
     * @return 结果
     */
    private String evalLimitParam(ProceedingJoinPoint point, String limitParam) {
        MethodSignature ms = (MethodSignature) point.getSignature();
        Method method = ms.getMethod();
        Object[] args = point.getArgs();
        Object target = point.getTarget();
        Class<?> targetClass = target.getClass();
        EvaluationContext context = EVALUATOR.createContext(method, args, target, targetClass, applicationContext);
        AnnotatedElementKey elementKey = new AnnotatedElementKey(method, targetClass);
        return EVALUATOR.evalAsText(limitParam, elementKey, context);
    }
}
