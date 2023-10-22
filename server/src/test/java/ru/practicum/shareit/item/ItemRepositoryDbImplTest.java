package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;

@SpringBootTest
class ItemRepositoryDbImplTest extends ItemRepositoryTest {
    @Autowired
    ItemRepositoryDbImplTest(
            @Qualifier("itemRepositoryDbImpl") ItemRepository itemRepository,
            @Qualifier("userRepositoryDbImpl") UserRepository userRepository,
            ItemRequestRepository itemRequestRepository) {
        setItemRepository(itemRepository);
        setUserRepository(userRepository);
        setItemRequestRepository(itemRequestRepository);
    }

    @Test
    @Override
    void getAll() {
        super.getAll();
    }

    @Test
    @Override
    void getById() {
        super.getById();
    }

    @Test
    @Override
    void getAllByOwnerId() {
        super.getAllByOwnerId();
    }

    @Test
    @Override
    void getAllByOwnerIdPageable() {
        super.getAllByOwnerIdPageable();
    }

    @Test
    @Override
    void create() {
        super.create();
    }

    @Test
    @Override
    void update() {
        super.update();
    }

    @Test
    @Override
    void deleteById() {
        super.deleteById();
    }

    @Test
    @Override
    void deleteAll() {
        super.deleteAll();
    }

    @Test
    @Override
    void deleteAllByOwnerId() {
        super.deleteAllByOwnerId();
    }

    @Test
    @Override
    void searchItems() {
        super.searchItems();
    }

    @Test
    @Override
    void searchItemsPageable() {
        super.searchItemsPageable();
    }

    @Test
    @Override
    void checkForPresenceById() {
        super.checkForPresenceById();
    }

    @Test
    @Override
    void getAllItemsByRequestId() {
        super.getAllItemsByRequestId();
    }

    @Test
    @Override
    void getAllItemsByRequestIds() {
        super.getAllItemsByRequestIds();
    }
}