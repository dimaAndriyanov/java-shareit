package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    @Qualifier("userRepositoryInMemoryImpl")
    private final UserRepository userRepository;

    @Override
    public List<User> getAllUsers() {
        return userRepository.getAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.getById(id);
    }

    @Override
    public User createUser(User user) {
        return userRepository.create(user);
    }

    @Override
    public User updateUser(User user, Long id) {
        return userRepository.update(user, id);
    }

    @Override
    public User deleteUserById(Long id) {
        return userRepository.deleteById(id);
    }

    @Override
    public void deleteAllUsers() {
        userRepository.deleteAll();
    }
}