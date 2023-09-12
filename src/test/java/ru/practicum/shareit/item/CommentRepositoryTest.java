package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepositoryDbInterface;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CommentRepositoryTest {
    private final CommentRepository commentRepository;

    private final ItemRepositoryDbInterface itemRepository;

    private final UserRepositoryDbInterface userRepository;

    private final LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

    User saveUser(String name, String email) {
        return userRepository.save(new User(name, email));
    }

    Item saveItem(String name, String description, User owner) {
        return itemRepository.save(new Item(name, description, true, owner, null));
    }

    Comment saveComment(Item item, String text, String authorName, LocalDateTime created) {
        return commentRepository.save(new Comment(item, text, authorName, created));
    }

    @Test
    void shouldBeManagedByEntityManagerWhenSave() {
        User owner = saveUser("ownerName", "ownerEmail");
        User commentAuthor = saveUser("authorName", "authorEmail");
        Item item = saveItem("itemName", "itemDescription", owner);

        Comment comment = new Comment(item, "text", commentAuthor.getName(), now);

        assertThat(comment.getId(), nullValue());

        Comment savedComment = commentRepository.save(comment);

        assertThat(comment.getId(), not(nullValue()));
        assertThat(savedComment, is(comment));
    }

    @Test
    void shouldReturnListOfCommentsWhenFindAllByItemIdIn() {
        User owner = saveUser("ownerName", "ownerEmail");
        User commentAuthor = saveUser("authorName", "authorEmail");
        Item itemWithTwoComments = saveItem("firstItemName", "firstItemDescription", owner);
        Item itemWithOneComment = saveItem("secondItemName", "secondItemDescription", owner);
        Item itemWithoutComments = saveItem("thirdItemName", "thirdItemDescription", owner);

        Comment firstComment = saveComment(itemWithTwoComments, "firstCommentText", commentAuthor.getName(), now);
        Comment secondComment = saveComment(itemWithTwoComments, "secondCommentText", commentAuthor.getName(), now);
        Comment thirdComment = saveComment(itemWithOneComment, "thirdCommentText", commentAuthor.getName(), now);

        List<Comment> foundComments = commentRepository.findAllByItemIdIn(
                List.of(
                        itemWithTwoComments.getId(), itemWithOneComment.getId(), itemWithoutComments.getId()
                )
        );
        assertThat(foundComments, hasSize(3));
        assertThat(foundComments, contains(firstComment, secondComment, thirdComment));

        foundComments = commentRepository.findAllByItemIdIn(
                List.of(
                        itemWithTwoComments.getId(), itemWithOneComment.getId()
                )
        );
        assertThat(foundComments, hasSize(3));
        assertThat(foundComments, contains(firstComment, secondComment, thirdComment));

        foundComments = commentRepository.findAllByItemIdIn(
                List.of(
                        itemWithTwoComments.getId()
                )
        );
        assertThat(foundComments, hasSize(2));
        assertThat(foundComments, contains(firstComment, secondComment));

        foundComments = commentRepository.findAllByItemIdIn(
                List.of(
                        itemWithOneComment.getId()
                )
        );
        assertThat(foundComments, hasSize(1));
        assertThat(foundComments, contains(thirdComment));

        foundComments = commentRepository.findAllByItemIdIn(
                List.of(
                        itemWithoutComments.getId()
                )
        );
        assertThat(foundComments, is(empty()));

        foundComments = commentRepository.findAllByItemIdIn(List.of());
        assertThat(foundComments, is(empty()));
    }
}