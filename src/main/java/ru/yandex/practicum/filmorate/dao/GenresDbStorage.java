package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ElementNotFoundException;
import ru.yandex.practicum.filmorate.model.Genres;
import ru.yandex.practicum.filmorate.storage.genres.GenresStorage;

import java.util.List;

@Component
@Slf4j
public class GenresDbStorage implements GenresStorage {

    private final JdbcTemplate jdbcTemplate;

    public GenresDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genres> findAll() {
        return jdbcTemplate.query("SELECT id, genre FROM genre_info", new GenresInfoRowMapper());

    }

    @Override
    public Genres getFilmMpaById(Long id) {
        try {
            return jdbcTemplate.queryForObject("SELECT id, genre FROM genre_info WHERE id = ?", new GenresInfoRowMapper(), id);

        } catch (RuntimeException e) {
            throw new ElementNotFoundException("Ошибка поиска по ид " + id);
        }

    }
}
