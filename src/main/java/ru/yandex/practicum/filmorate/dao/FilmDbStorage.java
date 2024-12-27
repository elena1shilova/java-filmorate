package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ElementNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@Component("filmDbStorage")
@Primary
@Slf4j
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> findAll() {
        List<Film> listFilm = jdbcTemplate.query("SELECT id, name, description, releasedate, duration FROM film", new FilmRowMapper());
        listFilm.forEach(film -> {
            film.setGenre(jdbcTemplate.queryForList("SELECT genre FROM genre WHERE film_id = ?", String.class, film.getId()));
            film.setIdLike(new HashSet<>(jdbcTemplate.queryForList("SELECT user_id FROM likes WHERE film_id = ?", Long.class, film.getId())));
            try {
                film.setMpa(jdbcTemplate.queryForObject("SELECT rating FROM mpa WHERE film_id = ?", String.class, film.getId()));
            } catch (EmptyResultDataAccessException e) {
                film.setMpa(null);
            }
        });
        return listFilm;
    }

    @Override
    public Film findById(Long id) {
        Film film = jdbcTemplate.queryForObject("SELECT id, name, description, releasedate, duration FROM film where id = ?", new FilmRowMapper(), id);
        if (film == null) {
            throw new ElementNotFoundException("id = " + id + " не найден");
        }
        return film;
    }

    @Override
    public Film create(Film film) {
        if (film.getName() == null) {
            throw new RuntimeException("Наименование должно быть указано");
        }

        validFilm(film);

        jdbcTemplate.update(
                "INSERT INTO film (name, description, releaseDate, duration) VALUES (?, ?, ?, ?)",
                film.getName(), film.getDescription(),
                film.getReleaseDate(),
                film.getDuration());

        film.setId(
                jdbcTemplate.queryForObject(
                        "SELECT MAX(id) FROM film",
                        Long.class
                )
        );

        if (film.getGenre() != null && !film.getGenre().isEmpty()) {
            film.getGenre().forEach(g ->
                    jdbcTemplate.update(
                            "INSERT INTO genre (film_id, genre) VALUES (?, ?)",
                            film.getId(), g)
            );

        }
        if (film.getMpa() != null) {
            jdbcTemplate.update(
                    "INSERT INTO mpa (film_id, rating, description) VALUES (?, ?, ?)",
                    film.getId(), film.getMpa(),
                    MotionPictureAssociation.valueOf(film.getMpa()).getTitle());
        }

        log.debug("Фильм успешно создан");
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            throw new RuntimeException("Id должен быть указан");
        }
        Film film = findById(newFilm.getId());
        if (film == null) {
            throw new ElementNotFoundException("id = " + newFilm.getId() + " не найден");
        }
        validFilm(newFilm);

        jdbcTemplate.update(
                "UPDATE film SET name = ?, description = ?, releaseDate = ?, duration = ?",
                newFilm.getName(), newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration());

        return newFilm;
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new RuntimeException("Id должен быть указан");
        }
        Film film = findById(id);
        if (film == null) {
            throw new ElementNotFoundException("id = " + id + " не найден");
        }
        jdbcTemplate.update(
                "DELETE FROM film WHERE ID = ?",
                id);
    }

    @Override
    public List<Long> addLike(Long idFilm, Long userId) {

        List<Long> idLike = jdbcTemplate.queryForList("SELECT user_id FROM likes WHERE film_id = ?", Long.class, idFilm);
        if (!idLike.contains(userId)) {
            jdbcTemplate.update(
                    "UPDATE likes SET film_id = ?, user_id = ?",
                    idFilm, userId);
            idLike.add(userId);
        }
        return idLike;
    }

    @Override
    public void deleteLike(Long idFilm, Long userId) {
        jdbcTemplate.update(
                "DELETE FROM likes WHERE film_id = AND user_id = ?",
                idFilm, userId);
    }

    private void validFilm(Film film) {
        if (film.getName() == null) {
            throw new ValidationException("Наименование должно быть указано");
        }
        if (film.getDescription().length() >= 200) {
            throw new ValidationException("Превышена максимальная длина описания в 200 символов");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() < 1) {
            throw new ValidationException("\"Продолжительность фильма не может быть меньше нуля");
        }
    }
}
