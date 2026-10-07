package com.example.delivery.menu.service;

import com.example.delivery.common.exception.ForbiddenException;
import com.example.delivery.common.exception.NotFoundException;
import com.example.delivery.member.entity.Member;
import com.example.delivery.member.repository.MemberRepository;
import com.example.delivery.menu.dto.MenuRequest;
import com.example.delivery.menu.dto.MenuResponse;
import com.example.delivery.menu.entity.Menu;
import com.example.delivery.menu.repository.MenuRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public MenuResponse createMenu(Long memberId, MenuRequest request) {
        Member owner = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다."));

        Menu menu = new Menu(owner, request.name(), request.price(), request.description());
        return MenuResponse.from(menuRepository.save(menu));
    }

    public List<MenuResponse> getMenus() {
        return menuRepository.findAllByDeletedFalseOrderByIdAsc().stream()
                .map(MenuResponse::from)
                .toList();
    }

    public MenuResponse getMenu(Long menuId) {
        return MenuResponse.from(findActiveMenu(menuId));
    }

    @Transactional
    public MenuResponse updateMenu(Long memberId, Long menuId, MenuRequest request) {
        Menu menu = findActiveMenu(menuId);
        validateOwner(menu, memberId);

        menu.update(request.name(), request.price(), request.description());
        // 변경 감지는 원래 커밋 시점에 UPDATE를 실행한다.
        // 응답에 갱신된 updatedAt을 담기 위해 DTO 변환 전에 flush해 @LastModifiedDate를 먼저 반영한다.
        menuRepository.flush();
        return MenuResponse.from(menu);
    }

    @Transactional
    public void deleteMenu(Long memberId, Long menuId) {
        Menu menu = findActiveMenu(menuId);
        validateOwner(menu, memberId);

        menu.delete();
    }

    private Menu findActiveMenu(Long menuId) {
        return menuRepository.findByIdAndDeletedFalse(menuId)
                .orElseThrow(() -> new NotFoundException("메뉴를 찾을 수 없습니다."));
    }

    private void validateOwner(Menu menu, Long memberId) {
        if (!menu.getOwner().getId().equals(memberId)) {
            throw new ForbiddenException("본인 메뉴만 수정·삭제할 수 있습니다.");
        }
    }
}
