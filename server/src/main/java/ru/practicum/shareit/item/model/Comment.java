package ru.practicum.shareit.item.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@RequiredArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    @ToString.Exclude
    private final Item item;

    @Column(nullable = false)
    private final String text;

    @Column(name = "author_name", nullable = false)
    private final String authorName;

    @Column(nullable = false)
    private final LocalDateTime created;

    Comment() {
        item = null;
        text = null;
        authorName = null;
        created = null;
    }
}