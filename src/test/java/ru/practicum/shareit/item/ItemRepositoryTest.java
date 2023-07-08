package ru.practicum.shareit.item;

import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Setter
abstract class ItemRepositoryTest {
    ItemRepository itemRepository;
    UserRepository userRepository;

    @BeforeEach
    void clearRepositories() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    User createOneUser(String name, String email) {
        return userRepository.create(new User(name, email));
    }

    Item createOneItem(String name, String description, Boolean available, User owner) {
        return itemRepository.create(new Item(name, description, available, owner));
    }

    List<Item> createThreeItems(User owner) {
        return new ArrayList<>(List.of(
                itemRepository.create(new Item("Car", "Very fast!", true, owner)),
                itemRepository.create(new Item("Hammer", "Very strong!", false, owner)),
                itemRepository.create(new Item("Skis", "New!", true, owner))));
    }

    void getAll() {
        List<Item> savedItems = itemRepository.getAll();

        assertNotNull(savedItems);
        assertTrue(savedItems.isEmpty());

        List<Item> addedItems = createThreeItems(createOneUser("userName", "userEmail"));
        savedItems = itemRepository.getAll();

        assertEquals(3, savedItems.size());
        assertEquals(new HashSet<>(addedItems), new HashSet<>(savedItems));

        addedItems.add(createOneItem("Snowboard", "Three years old", true, createOneUser("otherUser", "otherEmail")));
        savedItems = itemRepository.getAll();

        assertEquals(4, savedItems.size());
        assertEquals(new HashSet<>(addedItems), new HashSet<>(savedItems));
    }

    void getAllByOwnerId() {
        NullPointerException nullPointerException = assertThrows(NullPointerException.class,
                () -> itemRepository.getAllByOwnerId(null));
        assertEquals("Owner id must not be null", nullPointerException.getMessage());

        User user = createOneUser("userName", "userEmail");
        List<Item> savedUsersItems = itemRepository.getAllByOwnerId(user.getId());

        assertNotNull(savedUsersItems);
        assertTrue(savedUsersItems.isEmpty());

        List<Item> addedUsersItems = createThreeItems(user);
        savedUsersItems = itemRepository.getAllByOwnerId(user.getId());

        assertEquals(3, savedUsersItems.size());
        assertEquals(new HashSet<>(addedUsersItems), new HashSet<>(savedUsersItems));

        User otherUser = createOneUser("otherUser", "otherEmail");
        createOneItem("Snowboard", "Three years old", true, otherUser);
        savedUsersItems = itemRepository.getAllByOwnerId(user.getId());

        assertEquals(3, savedUsersItems.size());
        assertEquals(new HashSet<>(addedUsersItems), new HashSet<>(savedUsersItems));

        addedUsersItems.add(createOneItem("Tent", "Waterproof", false, user));
        savedUsersItems = itemRepository.getAllByOwnerId(user.getId());

        assertEquals(4, savedUsersItems.size());
        assertEquals(new HashSet<>(addedUsersItems), new HashSet<>(savedUsersItems));
    }

    void getByIdList() {
        NullPointerException nullPointerException = assertThrows(NullPointerException.class,
                () -> itemRepository.getByIdList(null));
        assertEquals("Id list must not be null", nullPointerException.getMessage());

        List<Item> addedItems = createThreeItems(createOneUser("userName", "userEmail"));

        List<Long> idList = List.of();
        List<Item> itemsByIdList = itemRepository.getByIdList(idList);
        assertNotNull(itemsByIdList);
        assertTrue(itemsByIdList.isEmpty());

        idList = List.of(addedItems.get(1).getId());
        itemsByIdList = itemRepository.getByIdList(idList);
        assertEquals(1, itemsByIdList.size());
        assertEquals(new HashSet<>(itemsByIdList), new HashSet<>(List.of(addedItems.get(1))));

        idList = List.of(addedItems.get(0).getId(), addedItems.get(2).getId());
        itemsByIdList = itemRepository.getByIdList(idList);
        assertEquals(2, itemsByIdList.size());
        assertEquals(new HashSet<>(itemsByIdList), new HashSet<>(List.of(addedItems.get(0), addedItems.get(2))));

        idList = addedItems.stream().map(Item::getId).collect(Collectors.toList());
        itemsByIdList = itemRepository.getByIdList(idList);
        assertEquals(3, itemsByIdList.size());
        assertEquals(new HashSet<>(itemsByIdList), new HashSet<>(addedItems));

        List<Long> idListWithNotExistingId = List.of(addedItems.get(0).getId(), addedItems.get(2).getId(), 9999L);
        ObjectNotFoundException objectNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> itemRepository.getByIdList(idListWithNotExistingId));
        assertEquals("Item with id = 9999 not found", objectNotFoundException.getMessage());
    }

    void getById() {
        NullPointerException nullPointerException = assertThrows(NullPointerException.class,
                () -> itemRepository.getById(null));
        assertEquals("Id must not be null", nullPointerException.getMessage());

        List<Item> addedItems = createThreeItems(createOneUser("userName", "userEmail"));

        assertEquals(addedItems.get(1), itemRepository.getById(addedItems.get(1).getId()));
    }

    void create() {
        NullPointerException nullPointerException = assertThrows(NullPointerException.class,
                () -> itemRepository.create(null));
        assertEquals("Can not create null item", nullPointerException.getMessage());

        Item newItem = createOneItem(
                "Snowboard",
                "Almost new",
                true,
                createOneUser("userName", "userEmail")
        );

        assertNotNull(itemRepository.getById(newItem.getId()));
        assertEquals(newItem, itemRepository.getById(newItem.getId()));
    }

    void update() {
        NullPointerException nullPointerException = assertThrows(NullPointerException.class,
                () -> itemRepository.update(null, 1L));
        assertEquals("Can not update null item", nullPointerException.getMessage());

        nullPointerException = assertThrows(NullPointerException.class,
                () -> itemRepository.update(
                        new Item("SnowBoard", "Slightly used", true, new User("name", "email")),
                        null
                ));
        assertEquals("Id must not be null", nullPointerException.getMessage());

        User user = createOneUser("userName", "userEmail");
        List<Item> addedItems = createThreeItems(user);
        addedItems.add(createOneItem("Snowboard", "Three years old", false, user));

        Item carWithUpdatedName = itemRepository.update(
                new Item("Electric car", null, null, user),
                addedItems.get(0).getId()
        );

        assertEquals(carWithUpdatedName, itemRepository.getById(addedItems.get(0).getId()));
        assertEquals("Electric car",
                itemRepository.getById(addedItems.get(0).getId()).getName());
        assertEquals(carWithUpdatedName.getDescription(),
                itemRepository.getById(addedItems.get(0).getId()).getDescription());
        assertEquals(carWithUpdatedName.getAvailable(),
                itemRepository.getById(addedItems.get(0).getId()).getAvailable());

        Item hammerWithUpdatedDescription = itemRepository.update(
                new Item(null, "Steel, very heavy", null, user),
                addedItems.get(1).getId()
        );

        assertEquals(hammerWithUpdatedDescription.getName(),
                itemRepository.getById(addedItems.get(1).getId()).getName());
        assertEquals("Steel, very heavy",
                itemRepository.getById(addedItems.get(1).getId()).getDescription());
        assertEquals(hammerWithUpdatedDescription.getAvailable(),
                itemRepository.getById(addedItems.get(1).getId()).getAvailable());

        Item notAvailableSkis = itemRepository.update(
                new Item(null, null, false, user),
                addedItems.get(2).getId()
        );

        assertEquals(notAvailableSkis.getName(),
                itemRepository.getById(addedItems.get(2).getId()).getName());
        assertEquals(notAvailableSkis.getDescription(),
                itemRepository.getById(addedItems.get(2).getId()).getDescription());
        assertEquals(false,
                itemRepository.getById(addedItems.get(2).getId()).getAvailable());

        itemRepository.update(
                new Item("Burton Blossom Camber Snowboard", "Park snowboard", true, user),
                addedItems.get(3).getId()
        );

        assertEquals("Burton Blossom Camber Snowboard",
                itemRepository.getById(addedItems.get(3).getId()).getName());
        assertEquals("Park snowboard",
                itemRepository.getById(addedItems.get(3).getId()).getDescription());
        assertEquals(true,
                itemRepository.getById(addedItems.get(3).getId()).getAvailable());
    }

    void deleteById() {
        NullPointerException nullPointerException = assertThrows(NullPointerException.class,
                () -> itemRepository.deleteById(null));
        assertEquals("Id must not be null", nullPointerException.getMessage());

        User user = createOneUser("userName", "userEmail");
        List<Item> addedItems = createThreeItems(user);

        assertEquals(3, itemRepository.getAll().size());

        Item deletedItem = itemRepository.deleteById(addedItems.get(1).getId());

        assertEquals(2, itemRepository.getAll().size());
        assertEquals(deletedItem, addedItems.get(1));
        assertThrows(ObjectNotFoundException.class,
                () -> itemRepository.checkForPresenceById(addedItems.get(1).getId()));
    }

    void deleteByIdList() {
        NullPointerException nullPointerException = assertThrows(NullPointerException.class,
                () -> itemRepository.deleteByIdList(null));
        assertEquals("Id list must not be null", nullPointerException.getMessage());

        User user = createOneUser("userName", "userEmail");
        List<Item> addedItems = createThreeItems(user);

        assertEquals(3, itemRepository.getAll().size());
        assertEquals(new HashSet<>(addedItems), new HashSet<>(itemRepository.getAll()));

        itemRepository.deleteByIdList(List.of());

        assertEquals(3, itemRepository.getAll().size());
        assertEquals(new HashSet<>(addedItems), new HashSet<>(itemRepository.getAll()));

        itemRepository.deleteByIdList(List.of(addedItems.get(1).getId()));
        assertEquals(2, itemRepository.getAll().size());
        assertEquals(new HashSet<>(List.of(addedItems.get(0), addedItems.get(2))),
                new HashSet<>(itemRepository.getAll()));

        itemRepository.deleteByIdList(List.of(addedItems.get(0).getId(), addedItems.get(2).getId()));
        assertTrue(itemRepository.getAll().isEmpty());

        List<Item> newlyAddedItems = createThreeItems(user);
        itemRepository.deleteByIdList(List.of(newlyAddedItems.get(1).getId(), newlyAddedItems.get(2).getId()));
        assertEquals(1, itemRepository.getAll().size());
        assertEquals(new HashSet<>(List.of(newlyAddedItems.get(0))),
                new HashSet<>(itemRepository.getAll()));

        assertDoesNotThrow(() -> itemRepository.deleteByIdList(List.of(newlyAddedItems.get(0).getId(), 9999L)));
        assertTrue(itemRepository.getAll().isEmpty());
    }

    void deleteAll() {
        List<Item> addedItems = createThreeItems(createOneUser("userName", "userEmail"));

        assertEquals(3, itemRepository.getAll().size());
        assertDoesNotThrow(() -> itemRepository.checkForPresenceById(addedItems.get(1).getId()));

        itemRepository.deleteAll();

        assertTrue(itemRepository.getAll().isEmpty());
        assertThrows(ObjectNotFoundException.class,
                () -> itemRepository.checkForPresenceById(addedItems.get(1).getId()));
    }

    void checkForPresenceById() {
        NullPointerException nullPointerException = assertThrows(NullPointerException.class,
                () -> itemRepository.checkForPresenceById(null));
        assertEquals("Id must not be null", nullPointerException.getMessage());

        ObjectNotFoundException objectNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> itemRepository.checkForPresenceById(9999L));
        assertEquals("Item with id = 9999 not found", objectNotFoundException.getMessage());

        List<Item> addedItems = createThreeItems(createOneUser("userName", "userEmail"));
        addedItems.forEach(item -> assertDoesNotThrow(() -> itemRepository.checkForPresenceById(item.getId())));
    }

    void deleteAllByOwnerId() {
        User user = createOneUser("userName", "userEmail");
        List<Item> usersItems = createThreeItems(user);
        User otherUser = createOneUser("otherUserName", "otherUserEmail");
        Item otherUsersItem = createOneItem("Snowboard", "Three years old", false, otherUser);
        User userWithNoItems = createOneUser("userWithNoItemsName", "userWithNoItemsEmail");

        assertEquals(4, itemRepository.getAll().size());

        assertDoesNotThrow(() -> itemRepository.deleteAllByOwnerId(userWithNoItems.getId()));
        assertEquals(4, itemRepository.getAll().size());
        usersItems.forEach(item -> assertDoesNotThrow(() -> itemRepository.checkForPresenceById(item.getId())));
        assertDoesNotThrow(() -> itemRepository.checkForPresenceById(otherUsersItem.getId()));

        assertDoesNotThrow(() -> itemRepository.deleteAllByOwnerId(user.getId()));
        assertEquals(1, itemRepository.getAll().size());
        usersItems.forEach(item -> assertThrows(ObjectNotFoundException.class,
                () -> itemRepository.checkForPresenceById(item.getId())));
        assertDoesNotThrow(() -> itemRepository.checkForPresenceById(otherUsersItem.getId()));

        assertDoesNotThrow(() -> itemRepository.deleteAllByOwnerId(otherUser.getId()));
        assertTrue(itemRepository.getAll().isEmpty());
    }
}