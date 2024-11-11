package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ElementNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emailSet = new HashSet<>();
    private Long userId = 1L;

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User findById(Long id) {

        User user = users.get(id);
        if (user == null) {
            throw new ElementNotFoundException("id = " + userId + " не найден");
        }
        return user;
    }

    @Override
    public User create(User user) {
        if (user.getEmail() == null) {
            throw new RuntimeException("Имейл должен быть указан");
        }
        if (user.getLogin() == null || user.getLogin().isEmpty()) {
            throw new RuntimeException("Логин должен быть указан");
        }

        validFilm(user);
        user.setId(userId);
        userId++;
        users.put(user.getId(), user);
        emailSet.add(user.getEmail());
        log.debug("Пользователь успешно создан");
        return user;
    }

    @Override
    public User update(User newUser) {

        if (newUser.getId() == null) {
            throw new RuntimeException("Id должен быть указан");
        }

        if (users.containsKey(newUser.getId())) {

            validFilm(newUser);
            User oldUser = users.get(newUser.getId());

            if (!oldUser.getEmail().equals(newUser.getEmail()) && emailSet.contains(newUser.getEmail())) {
                throw new ValidationException("Этот имейл уже используется");
            }
            if (newUser.getEmail() != null) {
                oldUser.setEmail(newUser.getEmail());
            }
            if (newUser.getLogin() != null) {
                oldUser.setLogin(newUser.getLogin());
            }
            if (newUser.getName() != null) {
                oldUser.setName(newUser.getName());
            }
            if (newUser.getBirthday() != null) {
                oldUser.setBirthday(newUser.getBirthday());
            }
            log.debug("Пользователь успешно обновлен");
            return oldUser;
        }
        throw new ElementNotFoundException("id = " + newUser.getId() + " не найден");
    }

    @Override
    public void delete(Long id) {

        if (id == null) {
            throw new RuntimeException("Id должен быть указан");
        }
        if (!users.containsKey(id)) {
            throw new ElementNotFoundException("id = " + id + " не найден");
        }
        users.remove(id);
    }

    private void validFilm(User user) {
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
