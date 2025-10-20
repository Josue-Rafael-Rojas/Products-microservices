package com.products.products.dto.abstracts;

import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

public class MetaData {
    LocalDateTime createdDate;
    LocalDateTime lastModifiedDate;


    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
