package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Genres;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GenresRowMapper implements RowMapper<Genres> {
    @Override
    public Genres mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Genres genres = new Genres();
        genres.setId(resultSet.getLong("id"));
        genres.setName(resultSet.getString("name"));
        return genres;
    }
}
