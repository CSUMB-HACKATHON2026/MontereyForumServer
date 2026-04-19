package com.MCF.backend.dto.response;

import com.MCF.backend.model.Category;

public class CategoryResponse {
    private Long categoryId;
    private String name;

    public CategoryResponse() {
    }

    public CategoryResponse(Category category) {
        this.categoryId = category.getCategoryId();
        this.name = category.getName();
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }
}
