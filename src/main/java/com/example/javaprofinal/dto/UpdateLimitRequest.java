package com.example.javaprofinal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateLimitRequest {

    private Long clientId;
    private Integer amount;
    private LimitOperationType operationType;

    public enum LimitOperationType {
        INCREASE,  // Увеличить лимит
        DECREASE  // Уменьшить лимит
    }

}
