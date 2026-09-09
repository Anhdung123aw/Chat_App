package com.example.chatcore.controller;

import com.example.chatcore.dto.AttachmentDTO.*;
import com.example.chatcore.service.AttachmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    /**
     * POST /api/v1/attachments/upload-request
     * Client request upload URL (Section 15)
     */
    @PostMapping("/upload-request")
    @ResponseStatus(HttpStatus.OK)
    public UploadRequestResponse requestUpload(@Valid @RequestBody UploadRequestRequest request) {
        return attachmentService.requestUpload(request);
    }

    /**
     * GET /api/v1/attachments/{id}
     * Get attachment metadata with download URL
     */
    @GetMapping("/{id}")
    public AttachmentResponse getAttachment(@PathVariable String id) {

        return attachmentService.getAttachment(id);
    }

    /**
     * GET /api/v1/attachments?messageId=xxx
     * List attachments by message
     */
    @GetMapping
    public List<AttachmentResponse> listByMessage(@RequestParam String messageId) {
        return attachmentService.listByMessage(messageId);
    }
}
