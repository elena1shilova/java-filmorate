package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User create(@RequestBody User user) {
        return userStorage.create(user);
    }

    public User update(@RequestBody User newUser) {
        return userStorage.update(newUser);
    }

    public void delete(@RequestParam Long id) {
        userStorage.delete(id);
    }

    public List<User> getUserFriends(Long id) {

        if (id == null) {
            throw new RuntimeException("Id пользователя должен быть указан");
        }

        return userStorage.findById(id).getIdFriends().stream()
                .map(userStorage::findById)
                .toList();
    }

    public List<User> updateFriends(Long id, Long otherId) {

        if (id == null || otherId == null) {
            throw new RuntimeException("Id пользователя/друга должен быть указан");
        }

        User user = userStorage.findById(id);
        User userFriends = userStorage.findById(otherId);
        user.getIdFriends().add(userFriends.getId());
        userFriends.getIdFriends().add(user.getId());
        return Arrays.asList(user, userFriends);
    }

    public void deleteFriends(Long id, Long otherId) {

        if (id == null || otherId == null) {
            throw new RuntimeException("Id пользователя/друга должен быть указан");
        }

        User user = userStorage.findById(id);
        User userFriends = userStorage.findById(otherId);
        user.getIdFriends().remove(userFriends.getId());
        userFriends.getIdFriends().remove(user.getId());
    }

    public List<User> getUserFriendsCommon(Long id, Long otherId) {
        if (id == null || otherId == null) {
            throw new RuntimeException("Id пользователя/другого пользователя должен быть указан");
        }
        return userStorage.findById(id).getIdFriends().stream()
                .filter(userStorage.findById(otherId).getIdFriends()::contains)
                .map(userStorage::findById)
                .toList();
    }
}
