package com.example.delivery.menu.repository;

import com.example.delivery.menu.entity.Menu;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    Page<Menu> findAllByDeletedFalse(Pageable pageable);

    Optional<Menu> findByIdAndDeletedFalse(Long id);

    List<Menu> findAllByIdInAndDeletedFalse(Collection<Long> ids);
}
