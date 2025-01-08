package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ElementNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Component("userDbStorage")
@Primary
@Slf4j
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    final String sqlQuery = "select * from USERS u, FRIENDS f, FRIENDS o " +
            "where u.USER_ID = f.FRIEND_ID AND u.USER_ID = o.FRIEND_ID AND f.USER_ID = ? AND o.USER_ID = ?";


    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> findAll() {

        try {
            List<User> listUser = jdbcTemplate.query("SELECT id, email, login, name, birthday FROM users", new UserRowMapper());

            return listUser;

        } catch (EmptyResultDataAccessException e) {
            throw new ElementNotFoundException("Пользователи не найдены");
        }
    }

    @Override
    public User findById(Long id) {
        try {
            User user = jdbcTemplate.queryForObject("SELECT id, email, login, name, birthday FROM users  where id = ?", new UserRowMapper(), id);

            user.setIdFriends(
                    new HashSet<>(jdbcTemplate.queryForList("SELECT friend_id FROM friends WHERE user_id = ?", Long.class, user.getId()))
            );
            return user;
        } catch (EmptyResultDataAccessException e) {
            throw new ElementNotFoundException("id = " + id + " не найден");
        }
    }

    @Override
    public User create(User user) {

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

        User oldUser = findById(newUser.getId());

        if (oldUser != null) {

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

        jdbcTemplate.update(
                "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)",
                idUser, idFriend);

        List<Long> idFr2 = new ArrayList<>();
        try {
            idFr2.addAll(jdbcTemplate.queryForList("SELECT friend_id FROM friends WHERE user_id = ?", Long.class, idUser));
        } catch (EmptyResultDataAccessException e) {
            log.debug("Нет друзей");
        }
        idFr2.add(idFriend);
        return idFr2;
    }

    public void deleteFriends(Long idUser, Long idFriend) {
        jdbcTemplate.update(
                "DELETE FROM friends WHERE user_id = ? AND friend_id = ?",
                idUser, idFriend);
    }

    @Override
    public User getFriends(Long id, Long otherId) {
        return jdbcTemplate.queryForObject(sqlQuery, new UserRowMapper(), id, otherId);
    }
}
