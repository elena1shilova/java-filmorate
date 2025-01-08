package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.List;

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
        film1.setMpa(new Mpa(1L, null, null));

        Film film2 = new Film();
        film2.setName("filmName2");
        film2.setDescription("Descr2");
        film2.setReleaseDate(LocalDate.of(2023, 12, 15));
        film2.setDuration(168);
        film2.setMpa(new Mpa(1L, null, null));

        filmStorage.create(film1);
        filmStorage.create(film2);

        List<Film> films = filmStorage.findAll();

        films.forEach(f -> {
            assertTrue(f.getName().equals(film1.getName()) || f.getName().equals(film2.getName()));
        });
    }
}
