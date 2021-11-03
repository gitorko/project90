package com.demo.project90.repo;

import com.demo.project90.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

    long countByCartOfAndType(String username, String type);

    Item findFirstByCartOfIsNull();

    Item findByIdAndCartOfIsNull(Long itemId);

    Iterable<Item> findAllByCartOf(String username);

    long countAllByCartOfIsNull();

}
