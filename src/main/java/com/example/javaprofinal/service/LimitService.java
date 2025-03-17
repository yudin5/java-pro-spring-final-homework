package com.example.javaprofinal.service;

import com.example.javaprofinal.dto.LimitDto;
import com.example.javaprofinal.dto.UpdateLimitRequest;
import com.example.javaprofinal.entity.Limit;
import com.example.javaprofinal.repository.LimitRepository;
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

    @Scheduled(cron = "${limits.reset-cron-schedule}")
    public void resetLimits() {
        log.info("Запущен сброс/обновление лимитов. Максимальный дневной лимит = {}", maxDailyLimit);
        limitRepository.resetLimits(maxDailyLimit);
    }

    public LimitDto getLimit(Long clientId) {
        Limit limit = getByClientIdOrCreate(clientId);
        return new LimitDto(limit.getClientId(), limit.getDailyLimit()); // Возвращаем через простой маппинг
    }

    /**
     * Метод апдейта лимитов.
     * При операции платежа клиент вызывает метод с типом операции DECREASE.
     * Если платёж по какой-либо причине не прошёл, то клиент вызывает метод с типом операции INCREASE.
     * Таким образом клиент (вызывающая сторона, некий client-api, находящийся в DMZ) сам управляет процессом и сервис лимитов не становится лишней точкой отказа, что случилось бы,
     * если бы через него шёл синхронный вызов платёжного сервиса.
     * К тому же в случае отказа сервиса лимитов, который может быть второстепенным по отношению к операционной деятельности, мы не останавливаем
     * основной бизнес процесс (операционную деятельность), что также является преимуществом. Это можно настроить на клиентской стороне.
     *
     * @param request запрос на апдейт лимитов
     * @return информация об остатке лимита
     */
    @Transactional
    public LimitDto updateLimit(UpdateLimitRequest request) {
        validateRequest(request);
        Limit limit = getByClientIdOrCreate(request.getClientId());

        int updatedLimit;
        switch (request.getOperationType()) {
            case DECREASE -> {
                Integer remainingLimit = limit.getDailyLimit();
                updatedLimit = remainingLimit - request.getAmount();
                if (updatedLimit < 0) {
                    throw new IllegalArgumentException("Значение, на которое нужно уменьшить лимит, больше остатка дневного лимита = " + remainingLimit);
                }
            }
            case INCREASE -> {
                Integer remainingLimit = limit.getDailyLimit();
                updatedLimit = remainingLimit + request.getAmount();
                if (updatedLimit > maxDailyLimit) {
                    throw new IllegalArgumentException("Невозможно увеличить дневной лимит, иначе он станет больше, чем максимальный = " + maxDailyLimit);
                }
            }
            default -> throw new IllegalArgumentException("Некорректный тип запроса обновления лимита");
        }
        limit.setDailyLimit(updatedLimit);
        limitRepository.saveAndFlush(limit);
        return new LimitDto(request.getClientId(), updatedLimit);
    }

    private void validateRequest(UpdateLimitRequest request) {
        Integer amount = request.getAmount();
        if (amount == null || amount < 0 || amount > maxDailyLimit) {
            throw new IllegalArgumentException(String.format("Проверьте значение, на которое нужно уменьшить/увеличить лимит. " +
                    "Оно не может быть отрицательным, а также превышать максимальный дневной лимит = %s", maxDailyLimit));
        }
    }

    private Limit getByClientIdOrCreate(Long clientId) {
        return limitRepository.findById(clientId)
                .orElseGet(() -> {
                            Limit limit = new Limit();
                            limit.setClientId(clientId);
                            limit.setDailyLimit(maxDailyLimit);
                            return limit;
                        }
                );
    }

}
