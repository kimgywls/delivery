package com.example.delivery.menu.controller;

import com.example.delivery.menu.dto.MenuPageResponse;
import com.example.delivery.menu.dto.MenuRequest;
import com.example.delivery.menu.dto.MenuResponse;
import com.example.delivery.menu.service.MenuService;
import com.example.delivery.security.AuthMember;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @PostMapping
    public ResponseEntity<MenuResponse> createMenu(@AuthenticationPrincipal AuthMember authMember,
                                                   @Valid @RequestBody MenuRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.createMenu(authMember.id(), request));
    }

    // 파라미터에 직접 붙인 제약은 Spring MVC의 메서드 검증으로 검사되고, 실패하면 HandlerMethodValidationException(400)이 발생한다.
    @GetMapping
    public ResponseEntity<MenuPageResponse> getMenus(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "page는 0 이상이어야 합니다.")
            Integer page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "size는 1 이상 100 이하여야 합니다.")
            @Max(value = 100, message = "size는 1 이상 100 이하여야 합니다.")
            Integer size) {
        return ResponseEntity.ok(menuService.getMenus(page, size));
    }

    @GetMapping("/{menuId}")
    public ResponseEntity<MenuResponse> getMenu(@PathVariable Long menuId) {
        return ResponseEntity.ok(menuService.getMenu(menuId));
    }

    @PutMapping("/{menuId}")
    public ResponseEntity<MenuResponse> updateMenu(@AuthenticationPrincipal AuthMember authMember,
                                                   @PathVariable Long menuId,
                                                   @Valid @RequestBody MenuRequest request) {
        return ResponseEntity.ok(menuService.updateMenu(authMember.id(), menuId, request));
    }

    @DeleteMapping("/{menuId}")
    public ResponseEntity<Void> deleteMenu(@AuthenticationPrincipal AuthMember authMember,
                                           @PathVariable Long menuId) {
        menuService.deleteMenu(authMember.id(), menuId);
        return ResponseEntity.noContent().build();
    }
}
