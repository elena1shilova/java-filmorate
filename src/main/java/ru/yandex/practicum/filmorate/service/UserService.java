package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.filmorate.exception.ElementNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    public List<User> getUserFriends(Integer id) {

        if (id == null) {
            throw new RuntimeException("Id пользователя должен быть указан");
        }
        List<User> userList = findAll();
        Optional<User> user = userList.stream().filter(a -> Objects.equals(a.getId(), id)).findFirst();

        if (user.isEmpty()) {
            throw new ElementNotFoundException("Id пользователя не найден");
        }
        return userList.stream().filter(a -> user.get().getIdFriends().contains(a.getId())).toList();
    }

    public User updateFriends(Long id, Long otherId) {

        if (id == null || otherId == null) {
            throw new RuntimeException("Id пользователя/друга должен быть указан");
        }

        List<User> userList = findAll();
        Optional<User> user = userList.stream().filter(a -> Objects.equals(a.getId(), id)).findFirst();
        Optional<User> userFriends = userList.stream().filter(a -> Objects.equals(a.getId(), otherId)).findFirst();
        if (user.isEmpty() || userFriends.isEmpty()) {
            throw new ElementNotFoundException("Id пользователя/друга не найден");
        }
        user.get().getIdFriends().add(otherId);
        userFriends.get().getIdFriends().add(id);
        return user.get();
    }

    public void deleteFriends(Integer id, Integer otherId) {

        if (id == null || otherId == null) {
            throw new RuntimeException("Id пользователя/друга должен быть указан");
        }

        List<User> userList = findAll();
        Optional<User> user = userList.stream().filter(a -> Objects.equals(a.getId(), id)).findFirst();
        Optional<User> userFriends = userList.stream().filter(a -> Objects.equals(a.getId(), otherId)).findFirst();
        if (!user.isEmpty() && !userFriends.isEmpty()) {
            user.get().getIdFriends().remove(userFriends);
            userFriends.get().getIdFriends().remove(id);
        }

    }

    public List<User> getUserFriendsCommon(Integer id, Integer otherId) {
        if (id == null || otherId == null) {
            throw new RuntimeException("Id пользователя/другого пользователя должен быть указан");
        }
        List<User> userList = findAll();
        Set<Long> user = userList.stream().filter(a -> Objects.equals(a.getId(), id)).findFirst().get().getIdFriends();
        Set<Long> userOther = userList.stream().filter(a -> Objects.equals(a.getId(), otherId)).findFirst().get().getIdFriends();

        if (user.isEmpty() || userOther.isEmpty()) {
            throw new ElementNotFoundException("Id пользователя/друга не найден");
        }

        Set<Long> userConcurrence = user.stream().filter(userOther::contains).collect(Collectors.toSet());
        return userList.stream().filter(a -> userConcurrence.contains(a.getId())).toList();
    }
}
