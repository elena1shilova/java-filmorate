package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.exception.ElementNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class})
public class FilmControllerTest {

    private final FilmDbStorage filmStorage;

    @Test
    public void testReturnsAllF() {

        Film film1 = new Film();
        film1.setName("filmName1");
        film1.setDescription("Descr1");
        film1.setReleaseDate(LocalDate.of(2023, 12, 15));
        film1.setDuration(168);

        Film film2 = new Film();
        film2.setName("filmName2");
        film2.setDescription("Descr2");
        film2.setReleaseDate(LocalDate.of(2023, 12, 15));
        film2.setDuration(168);

        filmStorage.create(film1);
        filmStorage.create(film2);

        List<Film> films = filmStorage.findAll();

        films.forEach(f -> {
            assertTrue(f.getName().equals(film1.getName()) || f.getName().equals(film2.getName()));
        });
    }

    @Test
    void testCreateFilnsValid() {
        Film film = new Film();
        film.setName("filmName1");
        film.setDescription("Descr1");
        film.setReleaseDate(LocalDate.of(2023, 12, 15));
        film.setDuration(168);

        Film savedFilm = filmStorage.create(film);

        assertNotNull(savedFilm.getId());
        assertEquals("filmName1", savedFilm.getName());
        assertEquals("Descr1", savedFilm.getDescription());
        assertEquals(LocalDate.of(2023, 12, 15), savedFilm.getReleaseDate());
        assertEquals(168, savedFilm.getDuration());

        film.setName(null);
        assertThrows(RuntimeException.class, () -> filmStorage.create(film));

        film.setName("filmName1");
        film.setDescription("1111111111111111111111111111111111111111111111111111111111111111111" +
                "111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
                "111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111");
        assertThrows(ValidationException.class, () -> filmStorage.create(film));

        film.setDescription("Descr1");
        film.setReleaseDate(LocalDate.of(1894, 12, 15));
        assertThrows(ValidationException.class, () -> filmStorage.create(film));

        film.setReleaseDate(LocalDate.of(2023, 12, 15));
        film.setDuration(-1);
        assertThrows(ValidationException.class, () -> filmStorage.create(film));
    }

    @Test
    void testUpdateFilmValid() {
        Film film = new Film();
        film.setName("filmName1");
        film.setDescription("Descr1");
        film.setReleaseDate(LocalDate.of(2023, 12, 15));
        film.setDuration(168);

        filmStorage.create(film);

        Film filmUpdate = new Film();
        filmUpdate.setId(film.getId() + 5);
        assertThrows(ElementNotFoundException.class, () -> filmStorage.update(filmUpdate));
    }
}
