package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    List<Film> findAll();

    Film findById(Long id);

    Film create(Film film);

    Film update(Film newFilm);

    void delete(Integer id);
}
