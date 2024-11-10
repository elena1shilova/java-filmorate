package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.filmorate.exception.ElementNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class FilmService {

    @Autowired
    private FilmStorage filmStorage;

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
        List<Film> films = filmStorage.findAll();
        films.sort(Comparator.comparingLong(film -> film.getIdLike().size()));

        if (count == null) {
            return films.stream().limit(10).toList();
        } else {
            return films.stream().limit(count).toList();
        }
    }

    public void deleteLike(Integer id, Integer userId) {

        if (id == null || userId == null) {
            throw new RuntimeException("Id фильма/лайка должен быть указан");
        }
        List<Film> films = filmStorage.findAll();
        Optional<Film> filmDelete = films.stream().filter(a -> Objects.equals(a.getId(), id)).findFirst();
        if (filmDelete.isEmpty()) {
            throw new ElementNotFoundException("id = " + id + " не найден");
        }
        filmDelete.get().getIdLike().remove(userId);
    }

    public Film updateLike(Integer id, Long userId) {
        if (id == null || userId == null) {
            throw new RuntimeException("Id фильма/лайка должен быть указан");
        }
        List<Film> films = filmStorage.findAll();
        Optional<Film> filmUpdate = films.stream().filter(a -> Objects.equals(a.getId(), id)).findFirst();
        if (filmUpdate.isEmpty()) {
            throw new ElementNotFoundException("id = " + id + " не найден");
        }
        filmUpdate.get().getIdLike().add(userId);
        return filmUpdate.get();
    }
}
