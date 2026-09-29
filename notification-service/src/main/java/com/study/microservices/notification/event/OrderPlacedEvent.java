package com.study.microservices.notification.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderPlacedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String orderNumber;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal totalAmount;
    private String customerEmail;
    private LocalDateTime placedAt;

    public OrderPlacedEvent() {
    }

    public OrderPlacedEvent(Long orderId, String orderNumber, Long productId, String productName,
                            Integer quantity, BigDecimal totalAmount, String customerEmail, LocalDateTime placedAt) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.customerEmail = customerEmail;
        this.placedAt = placedAt;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public LocalDateTime getPlacedAt() {
        return placedAt;
    }

    public void setPlacedAt(LocalDateTime placedAt) {
        this.placedAt = placedAt;
    }
}
