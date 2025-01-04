package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.dao.Mpa;

import java.util.List;

public interface MpaStorage {

    List<Mpa> findAll();

    Mpa getFilmMpaById(Long id);
}
