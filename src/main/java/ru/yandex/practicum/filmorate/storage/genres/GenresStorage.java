package ru.yandex.practicum.filmorate.storage.genres;

import ru.yandex.practicum.filmorate.model.Genres;

import java.util.List;

public interface GenresStorage {
    List<Genres> findAll();

    Genres getFilmMpaById(Long id);
}
