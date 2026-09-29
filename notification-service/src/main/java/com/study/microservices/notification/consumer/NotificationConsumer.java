package com.study.microservices.notification.consumer;

import com.study.microservices.notification.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    @KafkaListener(topics = "order-placed-topic", groupId = "notification-group")
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("==========================================================================");
        log.info("--> [KAFKA CONSUMED] Nhận sự kiện OrderPlacedEvent từ topic 'order-placed-topic'");
        log.info("📧 Giả lập gửi Email cho khách hàng: {}", event.getCustomerEmail());
        log.info("   - Mã đơn hàng: {}", event.getOrderNumber());
        log.info("   - Sản phẩm: {} (Mã SP: {})", event.getProductName(), event.getProductId());
        log.info("   - Số lượng: {}", event.getQuantity());
        log.info("   - Tổng thanh toán: ${}", event.getTotalAmount());
        log.info("   - Thời gian đặt: {}", event.getPlacedAt());
        log.info("--> Đã gửi email xác nhận đơn hàng thành công!");
        log.info("==========================================================================");
    }
}
