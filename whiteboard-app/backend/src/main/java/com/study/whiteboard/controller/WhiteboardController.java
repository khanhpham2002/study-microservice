package com.study.whiteboard.controller;

import com.study.whiteboard.model.DrawMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class WhiteboardController {

    private static final Logger log = LoggerFactory.getLogger(WhiteboardController.class);

    /**
     * Nhận tọa độ vẽ từ một người dùng tại '/app/draw'
     * và lập tức phát thanh (Broadcast) đến tất cả những ai đang subscribe '/topic/draw'
     */
    @MessageMapping("/draw")
    @SendTo("/topic/draw")
    public DrawMessage broadcastDrawing(DrawMessage message) {
        return message;
    }

    /**
     * Nhận yêu cầu xóa trắng bảng vẽ tại '/app/clear'
     * và phát thanh đến '/topic/clear' để tất cả các màn hình tự xóa sạch
     */
    @MessageMapping("/clear")
    @SendTo("/topic/clear")
    public Map<String, String> broadcastClear(Map<String, String> payload) {
        log.info("Nhận yêu cầu xóa trắng bảng từ user: {}", payload.get("senderId"));
        return payload;
    }
}
