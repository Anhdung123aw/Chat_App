package com.example.chatcore.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "CHAT_CONTEXT")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatContextEntity {

    @Id
    @Column(name = "CONVERSATION_ID", length = 36)
    private String conversationId;

    @Column(name = "SCREEN_NAME", length = 128)
    private String screenName;

    @Column(name = "FEATURE", length = 128)
    private String feature;

    @Column(name = "LAST_ACTION", length = 128)
    private String lastAction;

    @Column(name = "APP_VERSION", length = 32)
    private String appVersion;

    @Column(name = "OS", length = 32)
    private String os;

    @Column(name = "DEVICE", length = 128)
    private String device;

    @Column(name = "CONTEXT_JSON", columnDefinition = "CLOB")
    private String contextJson;

    public static ChatContextEntity create(String conversationId, String screenName, String feature,
                                     String lastAction, String appVersion, String os, String device,
                                     String contextJson) {
        ChatContextEntity ctx = new ChatContextEntity();
        ctx.conversationId = conversationId;
        ctx.screenName = screenName;
        ctx.feature = feature;
        ctx.lastAction = lastAction;
        ctx.appVersion = appVersion;
        ctx.os = os;
        ctx.device = device;
        ctx.contextJson = contextJson;
        return ctx;
    }
}
