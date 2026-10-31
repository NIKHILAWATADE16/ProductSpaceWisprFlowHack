package com.financecoach.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "categorization_rules", indexes = {
    @Index(name = "idx_cat_rules_user_keyword", columnList = "user_id, keyword")
})
public class CategorizationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // null userId indicates a system/generic rule; non-null indicates user override
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotBlank(message = "Keyword is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String keyword;

    @NotBlank(message = "Category is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "is_user_override", nullable = false)
    private boolean isUserOverride = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public CategorizationRule() {
    }

    public CategorizationRule(User user, String keyword, String category, boolean isUserOverride) {
        this.user = user;
        this.keyword = keyword != null ? keyword.toLowerCase().trim() : "";
        this.category = category;
        this.isUserOverride = isUserOverride;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.keyword != null) {
            this.keyword = this.keyword.toLowerCase().trim();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword != null ? keyword.toLowerCase().trim() : "";
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

