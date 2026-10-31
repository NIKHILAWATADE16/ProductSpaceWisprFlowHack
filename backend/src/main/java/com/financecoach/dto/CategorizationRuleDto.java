package com.financecoach.dto;

import com.financecoach.entity.CategorizationRule;
import java.time.LocalDateTime;

public class CategorizationRuleDto {
    private Long id;
    private String keyword;
    private String category;
    private boolean isUserOverride;
    private LocalDateTime createdAt;

    public CategorizationRuleDto() {
    }

    public CategorizationRuleDto(Long id, String keyword, String category, boolean isUserOverride, LocalDateTime createdAt) {
        this.id = id;
        this.keyword = keyword;
        this.category = category;
        this.isUserOverride = isUserOverride;
        this.createdAt = createdAt;
    }

    public static CategorizationRuleDto fromEntity(CategorizationRule entity) {
        return new CategorizationRuleDto(
            entity.getId(),
            entity.getKeyword(),
            entity.getCategory(),
            entity.isUserOverride(),
            entity.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isUserOverride() {
        return isUserOverride;
    }

    public void setUserOverride(boolean userOverride) {
        isUserOverride = userOverride;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

