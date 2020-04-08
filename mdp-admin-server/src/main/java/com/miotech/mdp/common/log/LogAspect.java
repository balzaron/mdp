package com.miotech.mdp.common.log;

import com.miotech.mdp.common.model.bo.LogInfo;
import com.miotech.mdp.common.model.bo.UserInfo;
import com.miotech.mdp.common.model.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Aspect
@Component
@Slf4j
public class LogAspect {

    @Autowired
    LogService logService;

    @Pointcut("within(@com.miotech.mdp.common.log.EnableAuditLog *)")
    public void pointcut() {

    }

    @Around("pointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {

        Object object = joinPoint.proceed();

        if (!(object instanceof Result)) {
            log.warn("The returned object is not instance of Result. Audit process is terminated");
            return object;
        }
        Result result = (Result) object;
        if (result.getCode() != 0) {
            return object;
        }

        Signature signature = joinPoint.getSignature();
        MethodSignature methodSign = (MethodSignature) signature;
        Method method = methodSign.getMethod();

        AuditLog auditLog = AnnotationUtils.getAnnotation(method, AuditLog.class);
        if (auditLog != null) {
            if (auditLog.ignore()) {
                return object;
            }
            LogInfo logInfo = new LogInfo();
            logInfo.setTopic(auditLog.topic());

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                String userId = ((UserInfo) authentication.getDetails()).getId();
                logInfo.setUserId(userId);
            }
            logInfo.setModel(auditLog.model());
            String parameters = handleMethodParameters(joinPoint, method);
            logInfo.setDetails(parameters);
            String modelId = extractModelId(parameters, auditLog.model(), auditLog.modelKey());
            logInfo.setModelId(modelId);
            logService.saveAuditLog(logInfo);
        }

        return object;
    }

    private String handleMethodParameters(JoinPoint joinPoint, Method method) {
        Object[] parameters = joinPoint.getArgs();
        ParameterNameDiscoverer pnd = new DefaultParameterNameDiscoverer();
        String[] parameterNames = pnd.getParameterNames(method);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < parameters.length; i++) {
            stringBuilder.append(parameterNames[i]);
            stringBuilder.append("=");
            stringBuilder.append(parameters[i].toString());
            stringBuilder.append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    private String extractModelId(String originalString,
                                  String... modelKeys) {
        StringBuilder patternStringBuilder = new StringBuilder();
        patternStringBuilder.append("(").append("id");
        for (String modelKey : modelKeys) {
            if (StringUtils.isEmpty(modelKey)) {
                continue;
            }
            patternStringBuilder.append("|");
            patternStringBuilder.append(modelKey);
            patternStringBuilder.append("Id");
        }
        patternStringBuilder.append(")").append("=").append("(\\d+)");
        Pattern pattern = Pattern.compile(patternStringBuilder.toString());
        Matcher matcher = pattern.matcher(originalString);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return "";
    }
}
