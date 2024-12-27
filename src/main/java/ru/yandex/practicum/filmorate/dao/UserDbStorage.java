package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ElementNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@Component("userDbStorage")
@Primary
@Slf4j
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;


    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> findAll() {
        List<User> listUser = jdbcTemplate.query("SELECT id, email, login, name, birthday FROM users", new UserRowMapper());
        listUser.forEach(user -> {
            user.setIdFriends(
                    new HashSet<>(jdbcTemplate.queryForList("SELECT friend_id FROM friends WHERE user_id = ?", Long.class, user.getId()))
            );
        });

        return listUser;
    }

    @Override
    public User findById(Long id) {
        User user = jdbcTemplate.queryForObject("SELECT id, email, login, name, birthday FROM users" + " where id = ?", new UserRowMapper(), id);
        if (user == null) {
            throw new ElementNotFoundException("id = " + id + " не найден");
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

        validUser(user);

        jdbcTemplate.update(
                "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                user.getEmail(), user.getLogin(),
                user.getName(), user.getBirthday());

        user.setId(
                jdbcTemplate.queryForObject(
                        "SELECT MAX(id) FROM users",
                        Long.class
                )
        );
        return user;
    }

    @Override
    public User update(User newUser) {
        if (newUser.getId() == null) {
            throw new RuntimeException("Id должен быть указан");
        }

        User oldUser = findById(newUser.getId());

        if (oldUser != null) {

            validUser(newUser);

            //User oldUser = user.;
            List<String> emailList = jdbcTemplate.queryForList("select email from users", String.class);

            if (!oldUser.getEmail().equals(newUser.getEmail()) && emailList.contains(newUser.getEmail())) {
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

            jdbcTemplate.update(
                    "UPDATE users SET email = ?, login = ?, name = ?, birthday = ?",
                    oldUser.getEmail(), oldUser.getLogin(),
                    oldUser.getName(), oldUser.getBirthday());

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

        User user = findById(id);
        if (user != null) {
            throw new ElementNotFoundException("id = " + id + " не найден");
        }
        jdbcTemplate.update(
                "DELETE FROM users WHERE ID = ?",
                id);
    }

    public List<Long> updateFriends(Long idUser, Long idFriend) {
        boolean friendship = false;
        List<Long> idFr = jdbcTemplate.queryForList("SELECT user_id FROM friends WHERE friend_id = ?", Long.class, idUser);

        if (idFr.contains(idFriend)) {
            friendship = true;
        }
        jdbcTemplate.update(
                "INSERT INTO users (user_id, film_id,) VALUES (?, ?, ?, ?)",
                idUser, idFriend, friendship);

        List<Long> idFr2 = jdbcTemplate.queryForList("SELECT friend_id FROM friends WHERE user_id = ?", Long.class, idUser);
        idFr2.add(idFriend);
        return idFr2;
    }

    public void deleteFriends(Long idUser, Long idFriend) {
        jdbcTemplate.update(
                "DELETE FROM users WHERE user_id = ? AND friend_id = ?",
                idUser, idFriend, false);
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
