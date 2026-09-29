package com.study.microservices.order.dto;

public class OrderRequest {
    private Long productId;
    private Integer quantity;
    private String customerEmail;

    public OrderRequest() {
    }

    public OrderRequest(Long productId, Integer quantity, String customerEmail) {
        this.productId = productId;
        this.quantity = quantity;
        this.customerEmail = customerEmail;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
}
