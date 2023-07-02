package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.DataAccessException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ItemServiceImplTest {
    final ItemService itemService;
    final UserService userService;

    @Autowired
    ItemServiceImplTest(ItemService itemService, UserService userService) {
        this.itemService = itemService;
        this.userService = userService;
    }

    @BeforeEach
    void clearAllRepositories() {
        userService.deleteAllUsers();
    }

    User createOneUser(String name, String email) {
        return userService.createUser(new User(name, email));
    }

    ItemDto createOneItem(String name, String description, Boolean available, User user) {
        return itemService.createItem(new ItemDto(name, description, available), user.getId());
    }

    @Test
    void searchItems() {
        User user = createOneUser("userName", "userEmail");
        ItemDto availableScrewdriver = createOneItem("Screwdriver", "Works on batteries", true, user);
        ItemDto availableDrill = createOneItem("Battery drill", "Works on batteries", true, user);
        ItemDto availableToyCar = createOneItem("RC toy car", "Batteries NOT INCLUDED", true, user);
        List<ItemDto> availableItems = List.of(availableScrewdriver, availableDrill, availableToyCar);

        ItemDto notAvailableScrewdriver = createOneItem("Screwdriver", "Works on batteries", false, user);
        ItemDto notAvailableDrill = createOneItem("Battery drill", "Works on batteries", false, user);
        ItemDto notAvailableToyCar = createOneItem("RC toy car", "Batteries NOT INCLUDED", false, user);
        List<ItemDto> notAvailableItems = List.of(notAvailableScrewdriver, notAvailableDrill, notAvailableToyCar);

        List<ItemDto> searchForScrewdriver = itemService.searchItems("screw");
        assertNotNull(searchForScrewdriver);
        assertEquals(1, searchForScrewdriver.size());
        assertTrue(searchForScrewdriver.contains(availableScrewdriver));
        assertFalse(searchForScrewdriver.contains(notAvailableScrewdriver));

        List<ItemDto> searchForIncluded = itemService.searchItems("included");
        assertNotNull(searchForIncluded);
        assertEquals(1, searchForIncluded.size());
        assertTrue(searchForIncluded.contains(availableToyCar));
        assertFalse(searchForIncluded.contains(notAvailableToyCar));

        List<ItemDto> searchForBattery = itemService.searchItems("batt");
        assertNotNull(searchForBattery);
        assertEquals(3, searchForBattery.size());
        availableItems.forEach(item -> assertTrue(searchForBattery.contains(item)));
        notAvailableItems.forEach(item -> assertFalse(searchForBattery.contains(item)));

        List<ItemDto> searchForSnowboard = itemService.searchItems("snowboard");
        assertNotNull(searchForSnowboard);
        assertTrue(searchForSnowboard.isEmpty());

        List<ItemDto> searchForNothing = itemService.searchItems("");
        assertNotNull(searchForNothing);
        assertTrue(searchForNothing.isEmpty());

        searchForNothing = itemService.searchItems("   ");
        assertNotNull(searchForNothing);
        assertTrue(searchForNothing.isEmpty());
    }

    @Test
    void createItem() {
        User user = createOneUser("userName", "userEmail");
        List<ItemDto> searchForScrewdriver = itemService.searchItems("screw");
        List<ItemDto> searchForDrill = itemService.searchItems("drill");
        List<ItemDto> searchForBattery = itemService.searchItems("batt");

        assertTrue(searchForScrewdriver.isEmpty());
        assertTrue(searchForDrill.isEmpty());
        assertTrue(searchForBattery.isEmpty());

        ItemDto availableScrewdriver = createOneItem("Screwdriver", "Works on batteries", true, user);
        ItemDto notAvailableDrill = createOneItem("Battery drill", "Works on batteries", false, user);
        searchForScrewdriver = itemService.searchItems("screw");
        searchForDrill = itemService.searchItems("drill");
        searchForBattery = itemService.searchItems("batt");

        assertEquals(1, searchForScrewdriver.size());
        assertTrue(searchForScrewdriver.contains(availableScrewdriver));
        assertTrue(searchForDrill.isEmpty());
        assertEquals(1, searchForBattery.size());
        assertTrue(searchForBattery.contains(availableScrewdriver));
        assertFalse(searchForBattery.contains(notAvailableDrill));
    }

    @Test
    void updateItem() {
        User user = createOneUser("userName", "userEmail");
        ItemDto availableScrewdriver = createOneItem("Screwdriver", "New", true, user);
        ItemDto notAvailableDrill = createOneItem("Battery drill", "New", false, user);
        List<ItemDto> searchForScrewdriver = itemService.searchItems("screw");
        List<ItemDto> searchForDrill = itemService.searchItems("drill");
        List<ItemDto> searchForBattery = itemService.searchItems("batt");

        assertEquals(1, searchForScrewdriver.size());
        assertTrue(searchForScrewdriver.contains(availableScrewdriver));
        assertTrue(searchForDrill.isEmpty());
        assertTrue(searchForBattery.isEmpty());

        User otherUser = createOneUser("otherUserName", "otherUserEmail");
        DataAccessException dataAccessException = assertThrows(DataAccessException.class,
                () -> itemService.updateItem(
                        new ItemDto(null, null, true),
                        notAvailableDrill.getId(),
                        otherUser.getId()
                ));
        assertEquals("Can not update someone else's item", dataAccessException.getMessage());

        ItemDto stillAvailableScrewdriver = itemService.updateItem(
                new ItemDto(null, "Works on batteries", null),
                availableScrewdriver.getId(),
                user.getId()
        );
        ItemDto stillNotAvailableDrill = itemService.updateItem(
                new ItemDto(null, "Works on batteries", null),
                notAvailableDrill.getId(),
                user.getId()
        );
        searchForScrewdriver = itemService.searchItems("screw");
        searchForDrill = itemService.searchItems("drill");
        searchForBattery = itemService.searchItems("batt");

        assertEquals(1, searchForScrewdriver.size());
        assertTrue(searchForScrewdriver.contains(stillAvailableScrewdriver));
        assertTrue(searchForDrill.isEmpty());
        assertEquals(1, searchForBattery.size());
        assertTrue(searchForBattery.contains(stillAvailableScrewdriver));
        assertFalse(searchForBattery.contains(stillNotAvailableDrill));

        ItemDto nowNotAvailableScrewdriver = itemService.updateItem(
                new ItemDto(null, null, false),
                availableScrewdriver.getId(),
                user.getId()
        );
        ItemDto nowAvailableDrill = itemService.updateItem(
                new ItemDto(null, null, true),
                notAvailableDrill.getId(),
                user.getId()
        );
        searchForScrewdriver = itemService.searchItems("screw");
        searchForDrill = itemService.searchItems("drill");
        searchForBattery = itemService.searchItems("batt");

        assertTrue(searchForScrewdriver.isEmpty());
        assertEquals(1, searchForDrill.size());
        assertTrue(searchForDrill.contains(nowAvailableDrill));
        assertEquals(1, searchForBattery.size());
        assertFalse(searchForBattery.contains(nowNotAvailableScrewdriver));
        assertTrue(searchForBattery.contains(nowAvailableDrill));
    }

    @Test
    void deleteItemById() {
        User user = createOneUser("userName", "userEmail");
        ItemDto availableScrewdriver = createOneItem("Screwdriver", "Works on batteries", true, user);
        ItemDto notAvailableDrill = createOneItem("Battery drill", "New", false, user);
        List<ItemDto> searchForScrewdriver = itemService.searchItems("screw");
        List<ItemDto> searchForDrill = itemService.searchItems("drill");
        List<ItemDto> searchForBattery = itemService.searchItems("batt");

        assertEquals(1, searchForScrewdriver.size());
        assertTrue(searchForScrewdriver.contains(availableScrewdriver));
        assertTrue(searchForDrill.isEmpty());
        assertEquals(1, searchForBattery.size());
        assertTrue(searchForBattery.contains(availableScrewdriver));
        assertFalse(searchForBattery.contains(notAvailableDrill));

        User otherUser = createOneUser("otherUserName", "otherUserEmail");
        DataAccessException dataAccessException = assertThrows(DataAccessException.class,
                () -> itemService.deleteItemById(
                        availableScrewdriver.getId(),
                        otherUser.getId()
                ));
        assertEquals("Can not delete someone else's item", dataAccessException.getMessage());

        itemService.deleteItemById(availableScrewdriver.getId(), user.getId());
        itemService.deleteItemById(notAvailableDrill.getId(), user.getId());
        searchForScrewdriver = itemService.searchItems("screw");
        searchForDrill = itemService.searchItems("drill");
        searchForBattery = itemService.searchItems("batt");

        assertTrue(searchForScrewdriver.isEmpty());
        assertTrue(searchForDrill.isEmpty());
        assertTrue(searchForBattery.isEmpty());
    }

    @Test
    void deleteAllItems() {
        User user = createOneUser("userName", "userEmail");
        ItemDto availableScrewdriver = createOneItem("Screwdriver", "Works on batteries", true, user);
        ItemDto availableDrill = createOneItem("Battery drill", "Works on batteries", true, user);
        ItemDto availableToyCar = createOneItem("RC toy car", "Batteries NOT INCLUDED", true, user);
        List<ItemDto> availableItems = List.of(availableScrewdriver, availableDrill, availableToyCar);

        ItemDto notAvailableScrewdriver = createOneItem("Screwdriver", "Works on batteries", false, user);
        ItemDto notAvailableDrill = createOneItem("Battery drill", "Works on batteries", false, user);
        ItemDto notAvailableToyCar = createOneItem("RC toy car", "Batteries NOT INCLUDED", false, user);
        List<ItemDto> notAvailableItems = List.of(notAvailableScrewdriver, notAvailableDrill, notAvailableToyCar);

        List<ItemDto> searchForBattery = itemService.searchItems("batt");
        assertEquals(3, searchForBattery.size());
        availableItems.forEach(item -> assertTrue(searchForBattery.contains(item)));
        notAvailableItems.forEach(item -> assertFalse(searchForBattery.contains(item)));

        itemService.deleteAllItems();
        List<ItemDto> searchForBatteryAfterDeletingAllItems = itemService.searchItems("batt");
        assertTrue(searchForBatteryAfterDeletingAllItems.isEmpty());
    }

    @Test
    void deleteAllByOwnerId() {
        User user = createOneUser("userName", "userEmail");
        ItemDto availableUsersScrewdriver = createOneItem("Screwdriver", "Works on batteries", true, user);
        ItemDto notAvailableUsersDrill = createOneItem("Battery drill", "Works on batteries", false, user);

        User otherUser = createOneUser("otherUserName", "otherUserEmail");
        ItemDto availableOtherUsersToyCar = createOneItem("RC toy car", "Batteries NOT INCLUDED", true, otherUser);
        ItemDto notAvailableOtherUsersToyHelicopter = createOneItem(
                "RC toy helicopter", "Batteries NOT INCLUDED!!!", false, otherUser
        );

        List<ItemDto> availableItems = List.of(availableUsersScrewdriver, availableOtherUsersToyCar);
        List<ItemDto> notAvailableItems = List.of(notAvailableUsersDrill, notAvailableOtherUsersToyHelicopter);
        List<ItemDto> searchForBatteries = itemService.searchItems("batt");

        assertEquals(2, searchForBatteries.size());
        availableItems.forEach(item -> assertTrue(searchForBatteries.contains(item)));
        notAvailableItems.forEach(item -> assertFalse(searchForBatteries.contains(item)));

        itemService.deleteAllByOwnerId(otherUser.getId());
        List<ItemDto> newSearchForBatteries = itemService.searchItems("batt");
        assertEquals(1, newSearchForBatteries.size());
        assertTrue(newSearchForBatteries.contains(availableUsersScrewdriver));
        assertFalse(newSearchForBatteries.contains(availableOtherUsersToyCar));
        notAvailableItems.forEach(item -> assertFalse(newSearchForBatteries.contains(item)));
    }
}