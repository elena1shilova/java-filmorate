package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserStorage userStorage;

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
        User user = userStorage.findById(id);
        List<User> userList = findAll();

        return userList.stream().filter(a -> user.getIdFriends().contains(a.getId())).toList();
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
        User user = userStorage.findById(id);
        User userFriends = userStorage.findById(otherId);
        List<User> userList = findAll();

        Set<Long> userConcurrence = user.getIdFriends().stream().filter(userFriends.getIdFriends()::contains).collect(Collectors.toSet());
        return userList.stream().filter(a -> userConcurrence.contains(a.getId())).toList();
    }
}
