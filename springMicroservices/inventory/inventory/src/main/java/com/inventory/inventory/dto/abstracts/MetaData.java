package com.inventory.inventory.dto.abstracts;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MetaData {
    LocalDateTime createdDate;
    LocalDateTime lastModifiedDate;
}
