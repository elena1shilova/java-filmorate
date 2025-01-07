package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.exception.ElementNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class})
public class UserControllerTest {

    private final UserDbStorage userDbStorage;

    @Test
    public void testReturnsAllUsers() {
        User user = new User();
        user.setEmail("user1@example.com");
        user.setLogin("user1");
        user.setName("User One");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1992, 2, 2));

        userDbStorage.create(user);
        userDbStorage.create(user2);

        Collection<User> users = userDbStorage.findAll();

        users.forEach(u -> {
            assertTrue(u.getLogin().equals(user.getLogin()) || u.getLogin().equals(user2.getLogin()));
        });
    }

    @Test
    void testCreateUserValid() {
        User user = new User();
        user.setEmail("user1@example.com");
        user.setLogin("user1");
        user.setName(null);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User savedUser1 = userDbStorage.create(user);

        assertNotNull(savedUser1.getId());
        assertEquals("user1@example.com", savedUser1.getEmail());
        assertEquals("user1", savedUser1.getLogin());
        assertEquals("user1", savedUser1.getName());
        assertEquals(LocalDate.of(1990, 1, 1), savedUser1.getBirthday());

        user.setEmail(null);
        assertThrows(RuntimeException.class, () -> userDbStorage.create(user));

        user.setEmail("user3.com");
        assertThrows(ValidationException.class, () -> userDbStorage.create(user));

        user.setEmail("user1@example.com");
        user.setLogin("");
        assertThrows(RuntimeException.class, () -> userDbStorage.create(user));

        user.setLogin(null);
        assertThrows(RuntimeException.class, () -> userDbStorage.create(user));

        user.setLogin("user 1");
        assertThrows(ValidationException.class, () -> userDbStorage.create(user));

        user.setBirthday(LocalDate.now().plusMonths(5));
        assertThrows(ValidationException.class, () -> userDbStorage.create(user));
    }

    @Test
    void testUpdateUserValid() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName(null);
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        userDbStorage.create(user1);

        User userUpdate = new User();
        userUpdate.setId(user1.getId() + 5);
        assertThrows(ElementNotFoundException.class, () -> userDbStorage.update(userUpdate));
    }
}
