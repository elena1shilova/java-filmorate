package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genres;
import ru.yandex.practicum.filmorate.storage.genres.GenresStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenresService {

    private final GenresStorage genresStorage;

    public List<Genres> findAll() {
        return genresStorage.findAll();
    }

    public Genres getFilmMpaById(Long id) {
        return genresStorage.getFilmMpaById(id);
    }
}
