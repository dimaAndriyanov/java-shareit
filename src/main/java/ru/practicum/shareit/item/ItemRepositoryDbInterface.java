package ru.practicum.shareit.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepositoryDbInterface extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(Long ownerId);

    Page<Item> findAllByOwnerId(Long ownerId, Pageable page);

    Page<Item> findAllByIdIn(Iterable<Long> ids, Pageable page);

    @Query("select i " +
            "from Item as i " +
            "join fetch i.owner " +
            "where i.id = ?1")
    Optional<Item> findByIdWithOwner(Long id);

    void deleteAllByOwnerId(Long ownerId);

    List<Item> findAllByItemRequestId(Long requestId);

    List<Item> findAllByItemRequestIdIn(List<Long> requestIds);
}