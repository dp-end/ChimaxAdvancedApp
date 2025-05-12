package com.chimax.chimax_backend.dto; // Paket adınızı kendi projenize göre güncelleyin

import java.math.BigDecimal;

// SellerInfoResponseDto'nun aynı pakette olduğunu veya doğru şekilde import edildiğini varsayıyoruz.
// Eğer farklı bir paketteyse: import com.chimax.chimax_backend.dto.SellerInfoResponseDto;

public class ProductResponseDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl; 
    private String category; // Kategori adı (string)
    private String type;
    private Boolean active; 
    private int stockQuantity; // EKLENDİ: Stok miktarı
    private SellerInfoResponseDto seller;

    // Constructors
    public ProductResponseDto() {
    }

    // İsteğe bağlı: Tüm alanları içeren bir constructor
    public ProductResponseDto(Long id, String name, String description, BigDecimal price, String imageUrl, String category, String type, Boolean active, int stockQuantity, SellerInfoResponseDto seller) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
        this.type = type;
        this.active = active;
        this.stockQuantity = stockQuantity;
        this.seller = seller;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public int getStockQuantity() { // EKLENDİ
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) { // EKLENDİ
        this.stockQuantity = stockQuantity;
    }

    public SellerInfoResponseDto getSeller() {
        return seller;
    }

    public void setSeller(SellerInfoResponseDto seller) {
        this.seller = seller;
    }
}
