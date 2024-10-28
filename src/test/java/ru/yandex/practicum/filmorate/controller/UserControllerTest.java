package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class UserControllerTest {

    @Autowired
    private UserController userController;

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

        userController.create(user);
        userController.create(user2);

        Collection<User> users = userController.findAll();

        assertTrue(users.contains(user));
        assertTrue(users.contains(user2));
    }

    @Test
    void testCreateUserValid() {
        User user = new User();
        user.setEmail("user1@example.com");
        user.setLogin("user1");
        user.setName(null);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User savedUser1 = userController.create(user);

        assertNotNull(savedUser1.getId());
        assertEquals("user1@example.com", savedUser1.getEmail());
        assertEquals("user1", savedUser1.getLogin());
        assertEquals("user1", savedUser1.getName());
        assertEquals(LocalDate.of(1990, 1, 1), savedUser1.getBirthday());

        user.setEmail(null);
        assertThrows(ValidationException.class, () -> userController.create(user));

        user.setEmail("user3.com");
        assertThrows(ValidationException.class, () -> userController.create(user));

        user.setEmail("user1@example.com");
        user.setLogin("");
        assertThrows(ValidationException.class, () -> userController.create(user));

        user.setLogin(null);
        assertThrows(ValidationException.class, () -> userController.create(user));

        user.setLogin("user 1");
        assertThrows(ValidationException.class, () -> userController.create(user));

        user.setBirthday(LocalDate.now().plusMonths(5));
        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void testUpdateUserValid() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName(null);
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        userController.create(user1);

        User userUpdate = new User();
        userUpdate.setId(8L);
        assertThrows(ValidationException.class, () -> userController.update(userUpdate));
    }
}
