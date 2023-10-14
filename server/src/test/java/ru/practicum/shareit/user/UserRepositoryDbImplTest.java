package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserRepositoryDbImplTest extends UserRepositoryTest {
    @Autowired
    UserRepositoryDbImplTest(@Qualifier("userRepositoryDbImpl") UserRepository userRepository) {
        setUserRepository(userRepository);
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
    void checkForPresence() {
        super.checkForPresence();
    }
}