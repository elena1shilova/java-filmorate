package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ElementNotFoundException;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Component
@Slf4j
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Mpa> findAll() {
        return jdbcTemplate.query("SELECT id, rating, description FROM mpa_info", new MpaInfoRowMapper());
    }

    @Override
    public Mpa getFilmMpaById(Long id) {
        try {
            return jdbcTemplate.queryForObject("SELECT id, rating, description FROM mpa_info WHERE id = ?", new MpaInfoRowMapper(), id);
        } catch (RuntimeException e) {
            throw new ElementNotFoundException("Ошибка поиска по ид " + id);
        }
    }
}
