package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemDbInterface extends JpaRepository<Item, Long> {
    @Query
    List<Item> findAllByOwnerId(Long ownerId);

    @Query("select i " +
            "from Item as i " +
            "join fetch i.owner " +
            "where i.id = ?1")
    Optional<Item> findByIdWithOwner(Long id);

    void deleteAllByOwnerId(Long ownerId);
}