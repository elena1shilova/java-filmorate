package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ElementNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
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

        try {
            List<Film> listFilm = jdbcTemplate.query("SELECT id, name, description, releasedate, duration FROM film", new FilmRowMapper());
            listFilm.forEach(film -> {
                film.setGenres(jdbcTemplate.query("SELECT g2.id, g2.genre as name FROM genre g1 JOIN genre_info g2 on g2.id = g1.genre_id WHERE g1.film_id = ?", new GenresRowMapper(), film.getId()));
                film.setIdLike(new HashSet<>(jdbcTemplate.queryForList("SELECT user_id FROM likes WHERE film_id = ?", Long.class, film.getId())));
                try {
                    film.setMpa(jdbcTemplate.queryForObject("SELECT m2.id, rating as name FROM mpa m1 JOIN mpa_info m2 on m2.id = m1.mpa_id WHERE m1.film_id = ?", new MpaRowMapper(), film.getId()));
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
            film.setIdLike(new HashSet<>(jdbcTemplate.queryForList("SELECT user_id FROM likes WHERE film_id = ?", Long.class, film.getId())));
            try {
                film.setMpa(jdbcTemplate.queryForObject("SELECT m2.id, rating as name FROM mpa m1 JOIN mpa_info m2 on m2.id = m1.mpa_id WHERE m1.film_id = ?", new MpaRowMapper(), film.getId()));
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

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {

            film.getGenres().forEach(g -> {
                        try {
                            List<Long> idGenre = jdbcTemplate.queryForList("SELECT genre_id FROM genre WHERE film_id = ?", Long.class, film.getId());

                            if(idGenre.size() == 0 || !idGenre.contains(g.getId())) {
                                jdbcTemplate.update(
                                        "INSERT INTO genre (film_id, genre_id) VALUES (?, ?)",
                                        film.getId(), g.getId());
                            }
                        } catch (RuntimeException e) {
                            throw new ValidationException("Ошибка сохранения с ид жанра " + g.getId());
                        }
                    }
            );


        }
        if (film.getMpa() != null) {
            try {
                jdbcTemplate.update(
                        "INSERT INTO mpa (film_id, mpa_id) VALUES (?, ?)",
                        film.getId(), film.getMpa().getId());
            } catch (RuntimeException e) {
                throw new ValidationException("Ошибка сохранения по ид рейтинга " + film.getMpa().getId());
            }
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
                    "INSERT INTO likes (film_id, user_id) VALUES (?, ?)",
                    idFilm, userId);
            idLike.add(userId);
        }
        return idLike;
    }

    @Override
    public void deleteLike(Long idFilm, Long userId) {
        jdbcTemplate.update(
                "DELETE FROM likes WHERE film_id = ? AND user_id = ?",
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
