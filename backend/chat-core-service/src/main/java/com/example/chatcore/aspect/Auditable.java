package com.example.chatcore.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation để đánh dấu method cần audit
 * Section 16: Audit - ghi actor, action, before/after, traceId, timestamp
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    
    /**
     * Event type cho audit log (e.g., "CONVERSATION_ASSIGNED", "CONVERSATION_CLOSED")
     */
    String eventType();
    
    /**
     * Có log old value không
     */
    boolean logOldValue() default false;
}
