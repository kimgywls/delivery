package com.example.delivery.menu.service;

import com.example.delivery.common.exception.ForbiddenException;
import com.example.delivery.common.exception.NotFoundException;
import com.example.delivery.common.exception.StoreRequiredException;
import com.example.delivery.menu.dto.MenuPageResponse;
import com.example.delivery.menu.dto.MenuRequest;
import com.example.delivery.menu.dto.MenuResponse;
import com.example.delivery.menu.entity.Menu;
import com.example.delivery.menu.repository.MenuRepository;
import com.example.delivery.store.entity.Store;
import com.example.delivery.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    // 등록 시각 내림차순, 등록 시각이 같으면 id 내림차순
    private static final Sort LATEST_FIRST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public MenuResponse createMenu(Long memberId, MenuRequest request) {
        // 메뉴는 인증된 사장님의 가게에 등록한다. 가게가 없으면 409.
        Store store = storeRepository.findByOwner_Id(memberId)
                .orElseThrow(StoreRequiredException::new);

        Menu menu = new Menu(store, request.name(), request.price(), request.description());
        return MenuResponse.from(menuRepository.save(menu));
    }

    // 삭제되지 않은 메뉴를 최신 등록순으로 DB에서 페이징 조회한다.
    public MenuPageResponse getMenus(int page, int size) {
        Page<Menu> menus = menuRepository.findAllByDeletedFalse(PageRequest.of(page, size, LATEST_FIRST));
        return MenuPageResponse.from(menus.map(MenuResponse::from));
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
        if (!menu.getStore().getOwner().getId().equals(memberId)) {
            throw new ForbiddenException("본인 메뉴만 수정·삭제할 수 있습니다.");
        }
    }
}
