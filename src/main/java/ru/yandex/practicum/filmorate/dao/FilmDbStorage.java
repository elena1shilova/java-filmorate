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
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.mapper.GenresRowMapper;
import ru.yandex.practicum.filmorate.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genres;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

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

        try {
            List<Film> listFilm = jdbcTemplate.query("SELECT id, name, description, releasedate, duration FROM film", new FilmRowMapper());
            listFilm.forEach(film -> {
                film.setGenres(jdbcTemplate.query("SELECT g2.id, g2.genre as name FROM genre g1 JOIN genre_info g2 on g2.id = g1.genre_id WHERE g1.film_id = ?", new GenresRowMapper(), film.getId()));
                film.setIdLike(new HashSet<>(jdbcTemplate.queryForList("SELECT user_id FROM likes WHERE film_id = ?", Long.class, film.getId())));
                try {
                    film.setMpa(jdbcTemplate.queryForObject("SELECT m1.mpa_id id, m2.rating as name FROM film m1 JOIN mpa_info m2 on m2.id = m1.mpa_id WHERE m1.id = ?", new MpaRowMapper(), film.getId()));
                } catch (EmptyResultDataAccessException e) {
                    film.setMpa(null);
                }
            });
            return listFilm;
        } catch (EmptyResultDataAccessException e) {
            throw new ElementNotFoundException("фильмы не найдены");
        }

    }

    @Override
    public Film findById(Long id) {
        try {
            Film film = jdbcTemplate.queryForObject("SELECT id, name, description, releasedate, duration FROM film where id = ?", new FilmRowMapper(), id);

            film.setGenres(jdbcTemplate.query("SELECT g2.id, g2.genre as name FROM genre g1 JOIN genre_info g2 on g2.id = g1.genre_id WHERE g1.film_id = ?", new GenresRowMapper(), film.getId()));
            try {
                film.setMpa(jdbcTemplate.queryForObject("SELECT m1.mpa_id id, m2.rating as name FROM film m1 JOIN mpa_info m2 on m2.id = m1.mpa_id WHERE m1.id = ?", new MpaRowMapper(), film.getId()));
            } catch (EmptyResultDataAccessException e) {
                film.setMpa(null);
            }
            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new ElementNotFoundException("id = " + id + " не найден");
        }
    }

    @Override
    public Film create(Film film) {

        try {
            String sqlQuery = "INSERT INTO film (name, description, releaseDate, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"ID"});
                stmt.setString(1, film.getName());
                stmt.setString(2, film.getDescription());
                stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
                stmt.setLong(4, film.getDuration());
                stmt.setLong(5, film.getMpa().getId());
                return stmt;
            }, keyHolder);
            film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        } catch (RuntimeException e) {
            throw new ValidationException("ошибка сохранения по ид mpa");
        }

        saveGenres(film);
        log.debug("Фильм успешно создан");
        return film;
    }

    @Override
    public Film update(Film newFilm) {

        Film film = findById(newFilm.getId());
        if (film == null) {
            throw new ElementNotFoundException("id = " + newFilm.getId() + " не найден");
        }

        jdbcTemplate.update(
                "UPDATE film SET name = ?, description = ?, releaseDate = ?, duration = ?",
                newFilm.getName(), newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration());

        saveGenres(film);

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

        jdbcTemplate.update(
                "merge into LIKES (film_id, user_id) values (?, ?)",
                idFilm, userId);

        return jdbcTemplate.queryForList("SELECT user_id FROM likes WHERE film_id = ?", Long.class, idFilm);
    }

    @Override
    public void deleteLike(Long idFilm, Long userId) {
        jdbcTemplate.update(
                "DELETE FROM likes WHERE film_id = ? AND user_id = ?",
                idFilm, userId);
    }

    private void saveGenres(Film film) {
        final Long filmId = film.getId();
        jdbcTemplate.update("delete from GENRE where FILM_ID = ?", filmId);
        final List<Genres> genres = film.getGenres();
        if (genres == null || genres.isEmpty()) {
            return;
        }
        film.getGenres().forEach(g -> {
                    try {
                        jdbcTemplate.update(
                                "merge into genre (film_id, genre_id) values (?, ?)",
                                film.getId(), g.getId());
                    } catch (RuntimeException e) {
                        throw new ValidationException("Ошибка сохранения с ид жанра " + g.getId());
                    }
                }
        );
    }

}
