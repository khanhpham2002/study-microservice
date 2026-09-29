package com.study.microservices.order.service;

import com.study.microservices.order.client.ProductClient;
import com.study.microservices.order.dto.OrderRequest;
import com.study.microservices.order.dto.ProductDto;
import com.study.microservices.order.event.OrderPlacedEvent;
import com.study.microservices.order.model.Order;
import com.study.microservices.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final String TOPIC_ORDER_PLACED = "order-placed-topic";

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final DistributedLockService lockService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderService(OrderRepository orderRepository,
                        ProductClient productClient,
                        DistributedLockService lockService,
                        KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.lockService = lockService;
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order placeOrder(OrderRequest request) {
        String lockKey = "lock:product:" + request.getProductId();
        String lockValue = UUID.randomUUID().toString();

        // BƯỚC 1: Thu thập Redis Distributed Lock để chống Race Condition mua trùng tồn kho
        boolean locked = lockService.acquireLock(lockKey, lockValue, 10);
        if (!locked) {
            throw new RuntimeException("Sản phẩm đang được người khác xử lý thanh toán, vui lòng thử lại sau giây lát!");
        }

        try {
            // BƯỚC 2: Gọi đồng bộ (OpenFeign) sang Product-Service để kiểm tra sản phẩm & tồn kho
            log.info("Gọi sang Product-Service để lấy thông tin sản phẩm id={}", request.getProductId());
            ProductDto product = productClient.getProductById(request.getProductId());
            if (product == null) {
                throw new RuntimeException("Không tìm thấy sản phẩm với id: " + request.getProductId());
            }

            if (product.getStockQuantity() < request.getQuantity()) {
                throw new RuntimeException("Sản phẩm '" + product.getName() + "' không đủ tồn kho (Còn lại: "
                        + product.getStockQuantity() + ", Yêu cầu: " + request.getQuantity() + ")");
            }

            // BƯỚC 3: Trừ tồn kho qua Product-Service
            productClient.reduceStock(request.getProductId(), request.getQuantity());

            // BƯỚC 4: Tạo và lưu đơn hàng vào DB nội bộ của Order-Service
            BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
            String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            Order order = new Order(
                    null,
                    orderNumber,
                    product.getId(),
                    request.getQuantity(),
                    totalAmount,
                    request.getCustomerEmail(),
                    "CONFIRMED",
                    LocalDateTime.now()
            );
            Order savedOrder = orderRepository.save(order);
            log.info("Tạo thành công đơn hàng số #{}, Tổng tiền: {}", orderNumber, totalAmount);

            // BƯỚC 5: Bắn Event vào Kafka bất đồng bộ để Notification-Service tự xử lý ngầm
            OrderPlacedEvent event = new OrderPlacedEvent(
                    savedOrder.getId(),
                    savedOrder.getOrderNumber(),
                    product.getId(),
                    product.getName(),
                    savedOrder.getQuantity(),
                    savedOrder.getTotalAmount(),
                    savedOrder.getCustomerEmail(),
                    savedOrder.getCreatedAt()
            );

            log.info("--> [KAFKA PUBLISH] Bắn sự kiện OrderPlacedEvent vào topic '{}'...", TOPIC_ORDER_PLACED);
            kafkaTemplate.send(TOPIC_ORDER_PLACED, orderNumber, event);

            return savedOrder;
        } finally {
            // BƯỚC 6: Giải phóng Distributed Lock để các request khác có thể mua tiếp
            lockService.releaseLock(lockKey, lockValue);
        }
    }
}
