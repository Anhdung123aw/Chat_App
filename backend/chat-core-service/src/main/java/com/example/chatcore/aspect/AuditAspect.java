package com.example.chatcore.aspect;

import com.example.chatcore.entity.ChatAuditLogEntity;
import com.example.chatcore.entity.ChatConversationEntity;
import com.example.chatcore.repository.ChatAuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * AOP Aspect để tự động audit các thao tác quan trọng
 * Section 16: Audit mọi thao tác agent/supervisor/admin
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final ChatAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String conversationId = extractConversationId(joinPoint);
        String oldValue = null;

        // Get old value if needed (before execution)
        if (auditable.logOldValue() && conversationId != null) {
            Object result = joinPoint.getTarget();
            oldValue = captureOldValue(result, conversationId);
        }

        // Execute the actual method
        Object result = joinPoint.proceed();

        // Log audit after successful execution
        try {
            String newValue = serializeValue(result);
            String actorId = getCurrentActorId();
            String actorType = getCurrentActorType();

            ChatAuditLogEntity auditLog = ChatAuditLogEntity.create(
                    UUID.randomUUID().toString(),
                    conversationId != null ? conversationId : extractConversationIdFromResult(result),
                    auditable.eventType(),
                    actorId,
                    actorType,
                    oldValue,
                    newValue
            );

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: eventType={}, conversationId={}, actorId={}",
                    auditable.eventType(), auditLog.getConversationId(), actorId);

        } catch (Exception e) {
            log.error("Failed to create audit log for event: {}", auditable.eventType(), e);
            // Don't fail the original operation if audit logging fails
        }

        return result;
    }

    private String extractConversationId(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof String) {
            return (String) args[0];
        }
        return null;
    }

    private String extractConversationIdFromResult(Object result) {
        if (result instanceof ChatConversationEntity) {
            return ((ChatConversationEntity) result).getConversationId();
        }
        return "UNKNOWN";
    }

    private String captureOldValue(Object target, String conversationId) {
        // This is a simplified version - in production you might query the repository
        return null;
    }

    private String serializeValue(Object value) {
        try {
            if (value instanceof ChatConversationEntity) {
                ChatConversationEntity conv = (ChatConversationEntity) value;
                return String.format("status=%s,agent=%s", conv.getStatus(), conv.getAssignedAgent());
            }
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            return "ERROR_SERIALIZING";
        }
    }

    private String getCurrentActorId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) auth.getPrincipal();
            String userId = jwt.getClaimAsString("sub");
            return userId != null ? userId : "ANONYMOUS";
        }
        return "SYSTEM";
    }

    private String getCurrentActorType() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null) {
            boolean isAgent = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().contains("agent") || a.getAuthority().contains("supervisor"));
            return isAgent ? "AGENT" : "USER";
        }
        return "SYSTEM";
    }
}
