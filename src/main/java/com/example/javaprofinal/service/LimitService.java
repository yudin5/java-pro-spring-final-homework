package com.example.javaprofinal.service;

import com.example.javaprofinal.dto.LimitDto;
import com.example.javaprofinal.dto.UpdateLimitRequest;
import com.example.javaprofinal.entity.Limit;
import com.example.javaprofinal.repository.LimitRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LimitService {

    @Value("${limits.daily}")
    private Integer maxDailyLimit;

    private final LimitRepository limitRepository;

    public LimitDto getLimit(Long clientId) {
        return limitRepository.findById(clientId)
                .map(limit -> new LimitDto(
                        clientId, limit.getDailyLimit()) // Возвращаем через примитивный маппинг
                )
                .orElseThrow(EntityNotFoundException::new);
    }

    @Transactional
    public LimitDto updateLimit(UpdateLimitRequest request) {
        LimitDto response;
        switch (request.getOperationType()) {
            case DECREASE -> response = decreaseLimit(request);
            case INCREASE -> response = increaseLimit(request);
            case RESET -> response = resetLimit(request);
            default -> throw new IllegalArgumentException("Некорректный тип запроса обновления лимита");
        }
        return response;
    }

    public LimitDto decreaseLimit(UpdateLimitRequest request) {
        Integer amount = request.getAmount();

        if (amount == null || amount < 0 || amount > maxDailyLimit) {
            throw new IllegalArgumentException("Проверьте значение, на которое нужно уменьшить лимит");
        }

        Long clientId = request.getClientId();
        Limit limit = limitRepository.findById(clientId)
                .orElseThrow(EntityNotFoundException::new);
        Integer remainingLimit = limit.getDailyLimit();
        int updatedLimit = remainingLimit - amount;
        if (updatedLimit < 0) {
            throw new IllegalArgumentException("Значение, на которое нужно уменьшить лимит, больше остатка дневного лимита = " + remainingLimit);
        }
        limit.setDailyLimit(updatedLimit);
        limitRepository.saveAndFlush(limit);
        return new LimitDto(clientId, updatedLimit);
    }

    public LimitDto increaseLimit(UpdateLimitRequest request) {
        return null;
    }

    public LimitDto resetLimit(UpdateLimitRequest request) {
        Integer amount = request.getAmount();
        if (amount == null || amount < 0 || amount > maxDailyLimit) {
            throw new IllegalArgumentException("Проверьте значение передаваемого лимита");
        }
        Long clientId = request.getClientId();
        Limit limit = limitRepository.findById(clientId)
                .orElse(new Limit());
        limit.setClientId(clientId);
        limit.setDailyLimit(amount);
        limitRepository.saveAndFlush(limit);
        return new LimitDto(limit.getClientId(), limit.getDailyLimit());
    }

    @Scheduled(cron = "${limits.reset-cron-schedule}")
    public void resetLimits() {
        log.info("Запущен сброс лимитов. Максимальный дневной лимит = {}", maxDailyLimit);
        limitRepository.resetLimits(maxDailyLimit);
    }
}
