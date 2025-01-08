package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.filmorate.exception.ElementNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
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

        if (user.getEmail() == null) {
            throw new RuntimeException("Имейл должен быть указан");
        }
        if (user.getLogin() == null || user.getLogin().isEmpty()) {
            throw new RuntimeException("Логин должен быть указан");
        }

        validUser(user);

        return userStorage.create(user);
    }

    public User update(@RequestBody User newUser) {

        if (newUser.getId() == null) {
            throw new RuntimeException("Id должен быть указан");
        }
        validUser(newUser);
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

        if (user == null || userFriends == null) {
            throw new ElementNotFoundException("Id пользователя/друга не найдено");
        }

        user.getIdFriends().addAll(userStorage.updateFriends(id, otherId));
        return Arrays.asList(user, userFriends);
    }

    public void deleteFriends(Long id, Long otherId) {

        if (id == null || otherId == null) {
            throw new RuntimeException("Id пользователя/друга должен быть указан");
        }

        User user = userStorage.getFriends(id, otherId);
        if (user == null) {
            throw new ElementNotFoundException("Id пользователя/друга не найдено");
        }
        userStorage.deleteFriends(id, otherId);
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

    private void validUser(User user) {
        if (user.getEmail() != null && !user.getEmail().contains("@")) {
            throw new ValidationException("Имейл должен содержать @");
        }
        if (user.getLogin() != null && user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не должен содержать пробелов");
        }
        if (user.getName() == null && user.getLogin() != null) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть больше текущей");
        }
    }
}
