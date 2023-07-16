package ru.practicum.shareit.item.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;

@Entity
@Table(name = "items")
@Data
@RequiredArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @Column(name = "item_name", nullable = false)
    private final String name;

    @Column(nullable = false)
    private final String description;

    @Column(nullable = false)
    private final Boolean available;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private final User owner;

    Item() {
        this.name = null;
        this.description = null;
        this.available = null;
        this.owner = null;
    }

    public void setNullId() {
        id = null;
    }
}