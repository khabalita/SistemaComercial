package com.khabalita.sistemacomercial.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;

    @Around("@annotation(com.khabalita.sistemacomercial.audit.Audited)")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        Audited audited = ((MethodSignature) joinPoint.getSignature()).getMethod()
                .getAnnotation(Audited.class);
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable exception) {
            try {
                auditService.record(audited.value(), joinPoint.getTarget().getClass().getSimpleName(),
                        entityId(joinPoint, null), exception.getClass().getSimpleName() + ": " + exception.getMessage(), "FAILURE");
            } catch (Exception auditException) {
                log.error("No se pudo registrar el fallo de auditoria para {}", audited.value(), auditException);
            }
            throw exception;
        }

        try {
            String outcome = resultOutcome(result);
            auditService.record(audited.value(), joinPoint.getTarget().getClass().getSimpleName(),
                    entityId(joinPoint, result), "Metodo: " + joinPoint.getSignature().getName(), outcome);
        } catch (Exception auditException) {
            log.error("No se pudo registrar auditoria para {}", audited.value(), auditException);
        }
        return result;
    }

    private String entityId(ProceedingJoinPoint joinPoint, Object result) {
        if (result != null) {
            try {
                Object id = result.getClass().getMethod("id").invoke(result);
                if (id != null) return id.toString();
            } catch (ReflectiveOperationException ignored) {
                // The returned value does not expose an id.
            }
        }
        for (Object argument : joinPoint.getArgs()) {
            if (argument instanceof Long id) return id.toString();
        }
        return null;
    }

    private String resultOutcome(Object result) {
        if (result == null) return "SUCCESS";
        try {
            Object valid = result.getClass().getMethod("valid").invoke(result);
            return Boolean.FALSE.equals(valid) ? "FAILURE" : "SUCCESS";
        } catch (ReflectiveOperationException ignored) {
            return "SUCCESS";
        }
    }
}
