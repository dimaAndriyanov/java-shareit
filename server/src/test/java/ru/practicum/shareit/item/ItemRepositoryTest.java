package ru.practicum.shareit.item;

import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Setter
abstract class ItemRepositoryTest {
    private ItemRepository itemRepository;
    private UserRepository userRepository;
    private ItemRequestRepository itemRequestRepository;

    @BeforeEach
    void clearRepositories() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
        itemRequestRepository.deleteAll();
    }

    User createOneUser(String name, String email) {
        return userRepository.create(new User(name, email));
    }

    Item createOneItem(String name, String description, Boolean available, User owner) {
        return itemRepository.create(new Item(name, description, available, owner, null));
    }

    Item createOneItemWithRequest(String name, String description, User owner, ItemRequest request) {
        return itemRepository.create(new Item(name, description, true, owner, request));
    }

    List<Item> createThreeItems(User owner) {
        return new ArrayList<>(List.of(
                itemRepository.create(new Item("Car", "Very fast!", true, owner, null)),
                itemRepository.create(new Item("Hammer", "Very strong!", false, owner, null)),
                itemRepository.create(new Item("Skis", "New!", true, owner, null))));
    }

    List<ItemRequest> createThreeRequests(User requestsAuthor) {
        return List.of(
                itemRequestRepository.save(new ItemRequest("description1", LocalDateTime.now(), requestsAuthor)),
                itemRequestRepository.save(new ItemRequest("description2", LocalDateTime.now(), requestsAuthor)),
                itemRequestRepository.save(new ItemRequest("description3", LocalDateTime.now(), requestsAuthor))
        );
    }

    @Transactional
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

    @Transactional
    void getById() {
        NullPointerException nullPointerException = assertThrows(NullPointerException.class,
                () -> itemRepository.getById(null));
        assertEquals("Id must not be null", nullPointerException.getMessage());

        List<Item> addedItems = createThreeItems(createOneUser("userName", "userEmail"));

        assertEquals(addedItems.get(1), itemRepository.getById(addedItems.get(1).getId()));
    }

    @Transactional
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

    @Transactional
    void getAllByOwnerIdPageable() {
        NullPointerException nullPointerException = assertThrows(NullPointerException.class,
                () -> itemRepository.getAllByOwnerId(null, 0, 10));
        assertEquals("Owner id must not be null", nullPointerException.getMessage());

        User user = createOneUser("userName", "userEmail");
        createThreeItems(user);
        List<Item> allSavedUsersItems = itemRepository.getAllByOwnerId(user.getId());

        List<Item> pagedSavedUsersItems = itemRepository.getAllByOwnerId(user.getId(), 0, 1);
        assertEquals(1, pagedSavedUsersItems.size());
        assertTrue(allSavedUsersItems.contains(pagedSavedUsersItems.get(0)));

        pagedSavedUsersItems = itemRepository.getAllByOwnerId(user.getId(), 1, 2);
        assertEquals(2, pagedSavedUsersItems.size());
        assertTrue(allSavedUsersItems.containsAll(pagedSavedUsersItems));

        pagedSavedUsersItems = itemRepository.getAllByOwnerId(user.getId(), 2, 3);
        System.out.println(pagedSavedUsersItems);
        assertEquals(3, pagedSavedUsersItems.size());
        assertTrue(allSavedUsersItems.containsAll(pagedSavedUsersItems));

        pagedSavedUsersItems = itemRepository.getAllByOwnerId(user.getId(), 3, 3);
        assertTrue(pagedSavedUsersItems.isEmpty());
    }

    @Transactional
    void create() {
        NullPointerException nullPointerException = assertThrows(NullPointerException.class,
                () -> itemRepository.create(null));
        assertEquals("Can not create null item", nullPointerException.getMessage());

        User user = createOneUser("userName", "userEmail");
        Item newItem = createOneItem(
                "Snowboard",
                "Almost new",
                true,
                user
        );

        assertNotNull(itemRepository.getById(newItem.getId()));
        assertEquals(newItem, itemRepository.getById(newItem.getId()));

        // Tests on updating item catalogue
        clearRepositories();

        List<Item> searchForScrewdriver = itemRepository.searchItems("screw", 0, 10);
        List<Item> searchForDrill = itemRepository.searchItems("drill", 0, 10);
        List<Item> searchForBattery = itemRepository.searchItems("batt", 0, 10);

        assertTrue(searchForScrewdriver.isEmpty());
        assertTrue(searchForDrill.isEmpty());
        assertTrue(searchForBattery.isEmpty());

        User newUser = createOneUser("newUserName", "newUserEmail");
        Item availableScrewdriver = createOneItem("Screwdriver", "Works on batteries", true, newUser);
        Item notAvailableDrill = createOneItem("Battery drill", "Works on batteries", false, newUser);
        searchForScrewdriver = itemRepository.searchItems("screw", 0, 10);
        searchForDrill = itemRepository.searchItems("drill", 0, 10);
        searchForBattery = itemRepository.searchItems("batt", 0, 10);

        assertEquals(1, searchForScrewdriver.size());
        assertTrue(searchForScrewdriver.contains(availableScrewdriver));
        assertTrue(searchForDrill.isEmpty());
        assertEquals(1, searchForBattery.size());
        assertTrue(searchForBattery.contains(availableScrewdriver));
        assertFalse(searchForBattery.contains(notAvailableDrill));
    }

    @Transactional
    void update() {
        NullPointerException nullPointerException = assertThrows(NullPointerException.class,
                () -> itemRepository.update(null, 1L));
        assertEquals("Can not update null item", nullPointerException.getMessage());

        nullPointerException = assertThrows(NullPointerException.class,
                () -> itemRepository.update(
                        new Item("SnowBoard", "Slightly used", true, new User("name", "email"), null),
                        null
                ));
        assertEquals("Id must not be null", nullPointerException.getMessage());

        User user = createOneUser("userName", "userEmail");
        List<Item> addedItems = createThreeItems(user);
        addedItems.add(createOneItem("Snowboard", "Three years old", false, user));

        Item carWithUpdatedName = itemRepository.update(
                new Item("Electric car", null, null, user, null),
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
                new Item(null, "Steel, very heavy", null, user, null),
                addedItems.get(1).getId()
        );

        assertEquals(hammerWithUpdatedDescription.getName(),
                itemRepository.getById(addedItems.get(1).getId()).getName());
        assertEquals("Steel, very heavy",
                itemRepository.getById(addedItems.get(1).getId()).getDescription());
        assertEquals(hammerWithUpdatedDescription.getAvailable(),
                itemRepository.getById(addedItems.get(1).getId()).getAvailable());

        Item notAvailableSkis = itemRepository.update(
                new Item(null, null, false, user, null),
                addedItems.get(2).getId()
        );

        assertEquals(notAvailableSkis.getName(),
                itemRepository.getById(addedItems.get(2).getId()).getName());
        assertEquals(notAvailableSkis.getDescription(),
                itemRepository.getById(addedItems.get(2).getId()).getDescription());
        assertEquals(false,
                itemRepository.getById(addedItems.get(2).getId()).getAvailable());

        itemRepository.update(
                new Item("Burton Blossom Camber Snowboard", "Park snowboard", true, user, null),
                addedItems.get(3).getId()
        );

        assertEquals("Burton Blossom Camber Snowboard",
                itemRepository.getById(addedItems.get(3).getId()).getName());
        assertEquals("Park snowboard",
                itemRepository.getById(addedItems.get(3).getId()).getDescription());
        assertEquals(true,
                itemRepository.getById(addedItems.get(3).getId()).getAvailable());

        // Tests on updating item catalogue
        clearRepositories();

        User newUser = createOneUser("newUserName", "newUserEmail");
        Item availableScrewdriver = createOneItem("Screwdriver", "New", true, newUser);
        Item notAvailableDrill = createOneItem("Battery drill", "New", false, newUser);
        List<Item> searchForScrewdriver = itemRepository.searchItems("screw", 0, 10);
        List<Item> searchForDrill = itemRepository.searchItems("drill", 0, 10);
        List<Item> searchForBattery = itemRepository.searchItems("batt", 0, 10);

        assertEquals(1, searchForScrewdriver.size());
        assertTrue(searchForScrewdriver.contains(availableScrewdriver));
        assertTrue(searchForDrill.isEmpty());
        assertTrue(searchForBattery.isEmpty());

        Item stillAvailableScrewdriver = itemRepository.update(
                new Item(null, "Works on batteries", null, newUser, null),
                availableScrewdriver.getId()
        );
        Item stillNotAvailableDrill = itemRepository.update(
                new Item(null, "Works on batteries", null, newUser, null),
                notAvailableDrill.getId()
        );
        searchForScrewdriver = itemRepository.searchItems("screw", 0, 10);
        searchForDrill = itemRepository.searchItems("drill", 0, 10);
        searchForBattery = itemRepository.searchItems("batt", 0, 10);

        assertEquals(1, searchForScrewdriver.size());
        assertTrue(searchForScrewdriver.contains(stillAvailableScrewdriver));
        assertTrue(searchForDrill.isEmpty());
        assertEquals(1, searchForBattery.size());
        assertTrue(searchForBattery.contains(stillAvailableScrewdriver));
        assertFalse(searchForBattery.contains(stillNotAvailableDrill));

        Item nowNotAvailableScrewdriver = itemRepository.update(
                new Item(null, null, false, newUser, null),
                availableScrewdriver.getId()
        );
        Item nowAvailableDrill = itemRepository.update(
                new Item(null, null, true, newUser, null),
                notAvailableDrill.getId()
        );
        searchForScrewdriver = itemRepository.searchItems("screw", 0, 10);
        searchForDrill = itemRepository.searchItems("drill", 0, 10);
        searchForBattery = itemRepository.searchItems("batt", 0, 10);

        assertTrue(searchForScrewdriver.isEmpty());
        assertEquals(1, searchForDrill.size());
        assertTrue(searchForDrill.contains(nowAvailableDrill));
        assertEquals(1, searchForBattery.size());
        assertFalse(searchForBattery.contains(nowNotAvailableScrewdriver));
        assertTrue(searchForBattery.contains(nowAvailableDrill));
    }

    @Transactional
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

        // Tests on updating item catalogue
        clearRepositories();

        User newUser = createOneUser("newUserName", "newUserEmail");
        Item availableScrewdriver = createOneItem("Screwdriver", "Works on batteries", true, newUser);
        Item notAvailableDrill = createOneItem("Battery drill", "New", false, newUser);
        List<Item> searchForScrewdriver = itemRepository.searchItems("screw", 0, 10);
        List<Item> searchForDrill = itemRepository.searchItems("drill", 0, 10);
        List<Item> searchForBattery = itemRepository.searchItems("batt", 0, 10);

        assertEquals(1, searchForScrewdriver.size());
        assertTrue(searchForScrewdriver.contains(availableScrewdriver));
        assertTrue(searchForDrill.isEmpty());
        assertEquals(1, searchForBattery.size());
        assertTrue(searchForBattery.contains(availableScrewdriver));
        assertFalse(searchForBattery.contains(notAvailableDrill));

        itemRepository.deleteById(availableScrewdriver.getId());
        itemRepository.deleteById(notAvailableDrill.getId());
        searchForScrewdriver = itemRepository.searchItems("screw", 0, 10);
        searchForDrill = itemRepository.searchItems("drill", 0, 10);
        searchForBattery = itemRepository.searchItems("batt", 0, 10);

        assertTrue(searchForScrewdriver.isEmpty());
        assertTrue(searchForDrill.isEmpty());
        assertTrue(searchForBattery.isEmpty());
    }

    @Transactional
    void deleteAll() {
        User user = createOneUser("userName", "userEmail");
        List<Item> addedItems = createThreeItems(user);

        assertEquals(3, itemRepository.getAll().size());
        assertDoesNotThrow(() -> itemRepository.checkForPresenceById(addedItems.get(1).getId()));

        itemRepository.deleteAll();

        assertTrue(itemRepository.getAll().isEmpty());
        assertThrows(ObjectNotFoundException.class,
                () -> itemRepository.checkForPresenceById(addedItems.get(1).getId()));

        // Tests on updating item catalogue
        clearRepositories();

        User newUser = createOneUser("newUserName", "newUserEmail");
        Item availableScrewdriver = createOneItem("Screwdriver", "Works on batteries", true, newUser);
        Item availableDrill = createOneItem("Battery drill", "Works on batteries", true, newUser);
        Item availableToyCar = createOneItem("RC toy car", "Batteries NOT INCLUDED", true, newUser);
        List<Item> availableItems = List.of(availableScrewdriver, availableDrill, availableToyCar);

        Item notAvailableScrewdriver = createOneItem("Screwdriver", "Works on batteries", false, newUser);
        Item notAvailableDrill = createOneItem("Battery drill", "Works on batteries", false, newUser);
        Item notAvailableToyCar = createOneItem("RC toy car", "Batteries NOT INCLUDED", false, newUser);
        List<Item> notAvailableItems = List.of(notAvailableScrewdriver, notAvailableDrill, notAvailableToyCar);

        List<Item> searchForBattery = itemRepository.searchItems("batt", 0, 10);
        assertEquals(3, searchForBattery.size());
        availableItems.forEach(item -> assertTrue(searchForBattery.contains(item)));
        notAvailableItems.forEach(item -> assertFalse(searchForBattery.contains(item)));

        itemRepository.deleteAll();
        List<Item> searchForBatteryAfterDeletingAllItems = itemRepository.searchItems("batt", 0, 10);
        assertTrue(searchForBatteryAfterDeletingAllItems.isEmpty());
    }

    @Transactional
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

        // Tests on updating item catalogue
        clearRepositories();

        User newUser = createOneUser("newUserName", "newUserEmail");
        Item availableUsersScrewdriver = createOneItem("Screwdriver", "Works on batteries", true, newUser);
        Item notAvailableUsersDrill = createOneItem("Battery drill", "Works on batteries", false, newUser);

        User newOtherUser = createOneUser("newOtherUserName", "newOtherUserEmail");
        Item availableOtherUsersToyCar = createOneItem("RC toy car", "Batteries NOT INCLUDED", true, newOtherUser);
        Item notAvailableOtherUsersToyHelicopter = createOneItem(
                "RC toy helicopter", "Batteries NOT INCLUDED!!!", false, newOtherUser
        );

        List<Item> availableItems = List.of(availableUsersScrewdriver, availableOtherUsersToyCar);
        List<Item> notAvailableItems = List.of(notAvailableUsersDrill, notAvailableOtherUsersToyHelicopter);
        List<Item> searchForBatteries = itemRepository.searchItems("batt", 0, 10);

        assertEquals(2, searchForBatteries.size());
        availableItems.forEach(item -> assertTrue(searchForBatteries.contains(item)));
        notAvailableItems.forEach(item -> assertFalse(searchForBatteries.contains(item)));

        itemRepository.deleteAllByOwnerId(newOtherUser.getId());
        List<Item> newSearchForBatteries = itemRepository.searchItems("batt", 0, 10);
        assertEquals(1, newSearchForBatteries.size());
        assertTrue(newSearchForBatteries.contains(availableUsersScrewdriver));
        assertFalse(newSearchForBatteries.contains(availableOtherUsersToyCar));
        notAvailableItems.forEach(item -> assertFalse(newSearchForBatteries.contains(item)));
    }

    @Transactional
    void searchItems() {
        User user = createOneUser("userName", "userEmail");
        Item availableScrewdriver = createOneItem("Screwdriver", "Works on batteries", true, user);
        Item availableDrill = createOneItem("Battery drill", "Works on batteries", true, user);
        Item availableToyCar = createOneItem("RC toy car", "Batteries NOT INCLUDED", true, user);
        List<Item> availableItems = List.of(availableScrewdriver, availableDrill, availableToyCar);

        Item notAvailableScrewdriver = createOneItem("Screwdriver", "Works on batteries", false, user);
        Item notAvailableDrill = createOneItem("Battery drill", "Works on batteries", false, user);
        Item notAvailableToyCar = createOneItem("RC toy car", "Batteries NOT INCLUDED", false, user);
        List<Item> notAvailableItems = List.of(notAvailableScrewdriver, notAvailableDrill, notAvailableToyCar);

        List<Item> searchForScrewdriver = itemRepository.searchItems("screw", 0, 10);
        assertNotNull(searchForScrewdriver);
        assertEquals(1, searchForScrewdriver.size());
        assertTrue(searchForScrewdriver.contains(availableScrewdriver));
        assertFalse(searchForScrewdriver.contains(notAvailableScrewdriver));

        List<Item> searchForIncluded = itemRepository.searchItems("included", 0, 10);
        assertNotNull(searchForIncluded);
        assertEquals(1, searchForIncluded.size());
        assertTrue(searchForIncluded.contains(availableToyCar));
        assertFalse(searchForIncluded.contains(notAvailableToyCar));

        List<Item> searchForBattery = itemRepository.searchItems("batt", 0, 10);
        assertNotNull(searchForBattery);
        assertEquals(3, searchForBattery.size());
        availableItems.forEach(item -> assertTrue(searchForBattery.contains(item)));
        notAvailableItems.forEach(item -> assertFalse(searchForBattery.contains(item)));

        List<Item> searchForSnowboard = itemRepository.searchItems("snowboard", 0, 10);
        assertNotNull(searchForSnowboard);
        assertTrue(searchForSnowboard.isEmpty());

        List<Item> searchForNothing = itemRepository.searchItems("", 0, 10);
        assertNotNull(searchForNothing);
        assertTrue(searchForNothing.isEmpty());

        searchForNothing = itemRepository.searchItems("   ", 0, 10);
        assertNotNull(searchForNothing);
        assertTrue(searchForNothing.isEmpty());
    }

    @Transactional
    void searchItemsPageable() {
        User user = createOneUser("userName", "userEmail");
        Item availableScrewdriver = createOneItem("Screwdriver", "Works on batteries", true, user);
        Item availableDrill = createOneItem("Battery drill", "Works on batteries", true, user);
        Item availableToyCar = createOneItem("RC toy car", "Batteries NOT INCLUDED", true, user);
        List<Item> availableItems = List.of(availableScrewdriver, availableDrill, availableToyCar);

        List<Item> searchForBatteryPageable = itemRepository.searchItems("batt", 0, 1);
        assertEquals(1, searchForBatteryPageable.size());
        assertTrue(availableItems.contains(searchForBatteryPageable.get(0)));

        searchForBatteryPageable = itemRepository.searchItems("batt", 1, 2);
        assertEquals(2, searchForBatteryPageable.size());
        assertTrue(availableItems.containsAll(searchForBatteryPageable));

        searchForBatteryPageable = itemRepository.searchItems("batt", 2, 3);
        assertEquals(3, searchForBatteryPageable.size());
        assertTrue(availableItems.containsAll(searchForBatteryPageable));

        searchForBatteryPageable = itemRepository.searchItems("batt", 3, 3);
        assertTrue(searchForBatteryPageable.isEmpty());
    }

    @Transactional
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

    @Transactional
    void getAllItemsByRequestId() {
        NullPointerException nullPointerException = assertThrows(NullPointerException.class,
                () -> itemRepository.getAllItemsByRequestId(null));
        assertEquals("Id must not be null", nullPointerException.getMessage());

        User requestsAuthor = createOneUser("requester", "requester@mail.com");
        List<ItemRequest> requests = createThreeRequests(requestsAuthor);
        User itemOwner = createOneUser("owner", "owner@mail.com");
        Item itemWithNoRequest = createOneItem("item1", "desc1", true, itemOwner);
        Item itemWithFirstRequest = createOneItemWithRequest("item2", "desc2", itemOwner, requests.get(0));
        Item itemWithSecondRequest = createOneItemWithRequest("item3", "desc3", itemOwner, requests.get(1));
        Item anotherItemWithFirstRequest = createOneItemWithRequest("item4", "desc4", itemOwner, requests.get(0));

        List<Item> itemsByFirstRequest = itemRepository.getAllItemsByRequestId(requests.get(0).getId());
        assertEquals(2, itemsByFirstRequest.size());
        assertTrue(itemsByFirstRequest.contains(itemWithFirstRequest));
        assertTrue(itemsByFirstRequest.contains(anotherItemWithFirstRequest));

        List<Item> itemsBySecondRequest = itemRepository.getAllItemsByRequestId(requests.get(1).getId());
        assertEquals(1, itemsBySecondRequest.size());
        assertTrue(itemsBySecondRequest.contains(itemWithSecondRequest));

        List<Item> itemsByThirdRequest = itemRepository.getAllItemsByRequestId(requests.get(2).getId());
        assertTrue(itemsByThirdRequest.isEmpty());
    }

    @Transactional
    void getAllItemsByRequestIds() {
        NullPointerException nullPointerException = assertThrows(NullPointerException.class,
                () -> itemRepository.getAllItemsByRequestIds(null));
        assertEquals("Id list must not be null", nullPointerException.getMessage());

        User requestsAuthor = createOneUser("requester", "requester@mail.com");
        List<ItemRequest> requests = createThreeRequests(requestsAuthor);
        User itemOwner = createOneUser("owner", "owner@mail.com");
        Item itemWithNoRequest = createOneItem("item1", "desc1", true, itemOwner);
        Item itemWithFirstRequest = createOneItemWithRequest("item2", "desc2", itemOwner, requests.get(0));
        Item itemWithSecondRequest = createOneItemWithRequest("item3", "desc3", itemOwner, requests.get(1));
        Item anotherItemWithFirstRequest = createOneItemWithRequest("item4", "desc4", itemOwner, requests.get(0));

        List<Item> itemsByEmptyRequestIdsList = itemRepository.getAllItemsByRequestIds(List.of());
        assertTrue(itemsByEmptyRequestIdsList.isEmpty());

        List<Item> itemsByFirstSecondAndThirdRequest = itemRepository.getAllItemsByRequestIds(
                List.of(requests.get(0).getId(), requests.get(1).getId(), requests.get(2).getId())
        );
        assertEquals(3, itemsByFirstSecondAndThirdRequest.size());
        assertTrue(itemsByFirstSecondAndThirdRequest.contains(itemWithFirstRequest));
        assertTrue(itemsByFirstSecondAndThirdRequest.contains(itemWithSecondRequest));
        assertTrue(itemsByFirstSecondAndThirdRequest.contains(anotherItemWithFirstRequest));

        List<Item> itemsByFirstAndSecondRequest = itemRepository.getAllItemsByRequestIds(
                List.of(requests.get(0).getId(), requests.get(1).getId())
        );
        assertEquals(3, itemsByFirstAndSecondRequest.size());
        assertTrue(itemsByFirstAndSecondRequest.contains(itemWithFirstRequest));
        assertTrue(itemsByFirstAndSecondRequest.contains(itemWithSecondRequest));
        assertTrue(itemsByFirstAndSecondRequest.contains(anotherItemWithFirstRequest));

        List<Item> itemsByFirstRequest = itemRepository.getAllItemsByRequestIds(
                List.of(requests.get(0).getId())
        );
        assertEquals(2, itemsByFirstRequest.size());
        assertTrue(itemsByFirstRequest.contains(itemWithFirstRequest));
        assertTrue(itemsByFirstRequest.contains(anotherItemWithFirstRequest));

        List<Item> itemsByThirdRequest = itemRepository.getAllItemsByRequestIds(
                List.of(requests.get(2).getId())
        );
        assertTrue(itemsByThirdRequest.isEmpty());
    }
}