package com.example.servicelock.info;


import com.example.servicelock.core.SpringUtil;
import com.example.servicelock.parser.ExtParameterNameDiscoverer;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * 锁信息抽象
 **/
@Slf4j
public abstract class AbstractLockInfoHandle implements LockInfoHandle {

    /**
     * 分布式锁ID名称前缀常量
     */
    private static final String LOCK_DISTRIBUTE_ID_NAME_PREFIX = "LOCK_DISTRIBUTE_ID";
    /**
     * 分隔符常量
     */
    public static final String SEPARATOR = ":";

    /**
     * 参数名发现器，用于获取方法参数名称
     */
    private final ParameterNameDiscoverer nameDiscoverer = new ExtParameterNameDiscoverer();

    /**
     * Spring表达式解析器，用于解析SpEL表达式
     */
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 锁信息前缀
     *
     * @return 具体前缀
     */
    protected abstract String getLockPrefixName();

    @Override
    public String getLockName(JoinPoint joinPoint, String name, String[] keys) {
        return SpringUtil.getPrefixDistinctionName() + "-" + getLockPrefixName() + SEPARATOR + name + getRelKey(joinPoint, keys);
    }

    @Override
    public String simpleGetLockName(String name, String[] keys) {
        List<String> definitionKeyList = new ArrayList<>();
        for (String key : keys) {
            if (StringUtil.isNotEmpty(key)) {
                definitionKeyList.add(key);
            }
        }
        return SpringUtil.getPrefixDistinctionName() + "-" +
                LOCK_DISTRIBUTE_ID_NAME_PREFIX + SEPARATOR + name + SEPARATOR + String.join(SEPARATOR, definitionKeyList);
    }

    /**
     * 获取自定义键
     */
    private String getRelKey(JoinPoint joinPoint, String[] keys) {
        Method method = getMethod(joinPoint);
        List<String> definitionKeys = getSpElKey(keys, method, joinPoint.getArgs());
        return SEPARATOR + String.join(SEPARATOR, definitionKeys);
    }

    private Method getMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.getDeclaringClass().isInterface()) {
            try {
                method = joinPoint.getTarget().getClass().getDeclaredMethod(signature.getName(),
                        method.getParameterTypes());
            } catch (Exception e) {
                log.error("get method error ", e);
            }
        }
        return method;
    }

    private List<String> getSpElKey(String[] definitionKeys, Method method, Object[] parameterValues) {
        List<String> definitionKeyList = new ArrayList<>();
        for (String definitionKey : definitionKeys) {
            if (!ObjectUtils.isEmpty(definitionKey)) {
                EvaluationContext context = new MethodBasedEvaluationContext(null, method, parameterValues, nameDiscoverer);
                Object objKey = parser.parseExpression(definitionKey).getValue(context);
                definitionKeyList.add(ObjectUtils.nullSafeToString(objKey));
            }
        }
        return definitionKeyList;
    }

}
