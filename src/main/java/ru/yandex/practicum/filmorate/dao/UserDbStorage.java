package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ElementNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

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

        try {

            return jdbcTemplate.query("SELECT id, email, login, name, birthday FROM users", new UserRowMapper());

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

        String sqlQuery = "insert into users (email, login, name, birthday) values (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"ID"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);
        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

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
        try {
            return jdbcTemplate.queryForObject("SELECT u.id, email, login, name, birthday FROM users u JOIN friends f on f.user_id = u.id where u.id = ? and f.friend_id = ?", new UserRowMapper(), id, otherId);
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Override
    public List<User> getUserFriends(Long id) {

        Long idFr = jdbcTemplate.queryForObject("select id from users where id = ?", Long.class, id);

        if (idFr == null) {
            throw new ElementNotFoundException("id = " + id + " не найден");
        }

        List<User> list = jdbcTemplate.query("select u.id, u.email, u.login, name, u.birthday from USERS u, FRIENDS where u.ID = FRIENDS.FRIEND_ID AND FRIENDS.USER_ID = ?", new UserRowMapper(), id);

        return list;
    }
}
