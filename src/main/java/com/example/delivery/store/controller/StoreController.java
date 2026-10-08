package com.example.delivery.store.controller;

import com.example.delivery.security.AuthMember;
import com.example.delivery.store.dto.StoreRequest;
import com.example.delivery.store.dto.StoreResponse;
import com.example.delivery.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    public ResponseEntity<StoreResponse> createStore(@AuthenticationPrincipal AuthMember authMember,
                                                     @Valid @RequestBody StoreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(storeService.createStore(authMember.id(), request));
    }

    @GetMapping("/mine")
    public ResponseEntity<StoreResponse> getMyStore(@AuthenticationPrincipal AuthMember authMember) {
        return ResponseEntity.ok(storeService.getMyStore(authMember.id()));
    }
}
