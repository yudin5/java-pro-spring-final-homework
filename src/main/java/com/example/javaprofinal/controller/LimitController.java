package com.example.javaprofinal.controller;

import com.example.javaprofinal.dto.LimitDto;
import com.example.javaprofinal.dto.UpdateLimitRequest;
import com.example.javaprofinal.service.LimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/limits")
public class LimitController {

    private final LimitService limitService;

    @GetMapping("/get/{clientId}")
    public LimitDto getLimit(@PathVariable("clientId") Long clientId) {
        return limitService.getLimit(clientId);
    }

    @PutMapping("/update")
    public LimitDto updateLimit(@RequestBody UpdateLimitRequest request) {
        return limitService.updateLimit(request);
    }


}
