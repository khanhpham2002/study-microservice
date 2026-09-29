package com.study.microservices.product.service;

import com.study.microservices.product.model.Product;
import com.study.microservices.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        log.info("Fetching all products from DATABASE...");
        return productRepository.findAll();
    }

    /**
     * Cache-Aside Pattern:
     * Lần 1: Gọi vào DB, log 'Fetching product from DATABASE...', sau đó lưu kết quả vào Redis với key 'product::1'.
     * Lần 2+: Spring Cache tìm thấy trong Redis, trả về ngay lập tức KHÔNG vào method này, KHÔNG query DB!
     */
    @Cacheable(value = "products", key = "#id")
    public Product getProductById(Long id) {
        log.info("--> [CACHE MISS] Fetching product id={} from DATABASE...", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    /**
     * Lưu vào DB đồng thời cập nhật cache mới nhất trong Redis
     */
    @CachePut(value = "products", key = "#result.id")
    public Product createProduct(Product product) {
        log.info("Saving product '{}' to DATABASE and updating REDIS cache...", product.getName());
        return productRepository.save(product);
    }

    /**
     * Xóa trong DB và xóa luôn cache trong Redis (Cache Invalidation)
     */
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        log.info("Deleting product id={} from DATABASE and evicting from REDIS...", id);
        productRepository.deleteById(id);
    }

    /**
     * Giảm tồn kho và xóa cache cũ để lần sau query lấy tồn kho mới nhất
     */
    @CacheEvict(value = "products", key = "#id")
    public Product reduceStock(Long id, int quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Out of stock! Current stock: " + product.getStockQuantity() + ", requested: " + quantity);
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        log.info("Reduced stock for product id={} by {}. Remaining: {}", id, quantity, product.getStockQuantity());
        return productRepository.save(product);
    }
}
