package com.example.delivery.menu.repository;

import com.example.delivery.menu.entity.Menu;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findAllByDeletedFalseOrderByIdAsc();

    Optional<Menu> findByIdAndDeletedFalse(Long id);
}
