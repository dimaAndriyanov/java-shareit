package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("select b " +
            "from Booking as b " +
            "join fetch b.booker " +
            "join fetch b.item " +
            "where b.id = ?1 " +
            "and (b.booker.id = ?2 or b.item.owner.id = ?2)")
    Optional<Booking> findByIdAndOwnerOrBookerId(Long id, Long userId);

    @Query("select b " +
            "from Booking as b " +
            "join fetch b.booker " +
            "join fetch b.item " +
            "where b.booker.id = ?1 " +
            "order by b.start desc")
    List<Booking> findAllByBookerIdOrderByStart(Long bookerId);

    @Query("select b " +
            "from Booking as b " +
            "join fetch b.booker " +
            "join fetch b.item " +
            "where b.item.owner.id = ?1 " +
            "order by b.start desc")
    List<Booking> findAllByItemOwnerIdOrderByStart(Long ownerId);

    @Query("select b " +
            "from Booking as b " +
            "join fetch b.booker " +
            "join fetch b.item " +
            "where b.item.id = ?1 " +
            "order by b.start")
    List<Booking> findAllByItemIdOrderByStart(Long itemId);

    @Query("select b " +
            "from Booking as b " +
            "join fetch b.booker " +
            "join fetch b.item " +
            "where b.item.id in ?1 " +
            "order by b.start")
    List<Booking> findAllByItemIdOrderByStart(Iterable<Long> ids);

    List<Booking> findAllByItemIdAndBookerId(Long itemId, Long bookerId);
}