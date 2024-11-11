package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(@Valid @RequestBody Film film) {
        return filmStorage.create(film);
    }

    public Film update(@Valid @RequestBody Film newFilm) {
        return filmStorage.update(newFilm);
    }

    public void delete(@Valid @RequestParam Integer id) {
        filmStorage.delete(id);
    }

    public List<Film> getFilmPopular(Integer count) {

        return filmStorage.findAll().stream()
                .sorted(Collections.reverseOrder(Comparator.comparingLong(film -> film.getIdLike().size())))
                .limit(count).toList();
    }

    public void deleteLike(Long id, Long userId) {

        if (id == null || userId == null) {
            throw new RuntimeException("Id фильма/лайка должен быть указан");
        }
        Film filmDelete = filmStorage.findById(id);
        User user = userStorage.findById(userId);
        filmDelete.getIdLike().remove(user.getId());
    }

    public Film updateLike(Long id, Long userId) {
        if (id == null || userId == null) {
            throw new RuntimeException("Id фильма/лайка должен быть указан");
        }
        Film filmUpdate = filmStorage.findById(id);
        User user = userStorage.findById(userId);
        filmUpdate.getIdLike().add(user.getId());
        return filmUpdate;
    }
}
