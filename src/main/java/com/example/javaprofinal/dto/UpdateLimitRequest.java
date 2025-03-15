package com.example.javaprofinal.dto;

import lombok.Getter;

@Getter
public record UpdateLimitRequest(Long clientId, Integer amount, LimitOperationType operationType) {

    public enum LimitOperationType {
        INCREASE,  // Увеличить лимит
        DECREASE,  // Уменьшить лимит
        RESET      // Сбросить лимит
    }

}
