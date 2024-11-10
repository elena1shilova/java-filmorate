package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.filmorate.exception.ElementNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Arrays;
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

    public List<User> getUserFriends(Long id) {

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

    public List<User> updateFriends(Long id, Long otherId) {

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
        return Arrays.asList(user.get(), userFriends.get());
    }

    public void deleteFriends(Long id, Long otherId) {

        if (id == null || otherId == null) {
            throw new RuntimeException("Id пользователя/друга должен быть указан");
        }

        List<User> userList = findAll();
        Optional<User> user = userList.stream().filter(a -> Objects.equals(a.getId(), id)).findFirst();
        Optional<User> userFriends = userList.stream().filter(a -> Objects.equals(a.getId(), otherId)).findFirst();
        if (user.isEmpty()) {
            throw new ElementNotFoundException("Id пользователя не найден");
        }
        if (userFriends.isPresent()) {
            user.get().getIdFriends().remove(userFriends.get().getId());
            userFriends.get().getIdFriends().remove(id);
        } else {
            throw new ElementNotFoundException("Id друга не найден");
        }

    }

    public List<User> getUserFriendsCommon(Long id, Long otherId) {
        if (id == null || otherId == null) {
            throw new RuntimeException("Id пользователя/другого пользователя должен быть указан");
        }
        List<User> userList = findAll();
        Optional<User> user = userList.stream().filter(a -> Objects.equals(a.getId(), id)).findFirst();//.get().getIdFriends();
        Optional<User> userOther = userList.stream().filter(a -> Objects.equals(a.getId(), otherId)).findFirst();//.get().getIdFriends();

        if (user.isEmpty() || userOther.isEmpty()) {
            throw new ElementNotFoundException("Id пользователя/друга не найден");
        }

        Set<Long> userConcurrence = user.get().getIdFriends().stream().filter(userOther.get().getIdFriends()::contains).collect(Collectors.toSet());
        return userList.stream().filter(a -> userConcurrence.contains(a.getId())).toList();
    }
}
