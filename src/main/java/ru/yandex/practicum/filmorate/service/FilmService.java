package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.filmorate.exception.ElementNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {

    @Qualifier(value = "filmDbStorage")
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(@Valid @RequestBody Film film) {

        if (film.getName() == null) {
            throw new RuntimeException("Наименование должно быть указано");
        }
        validFilm(film);
        return filmStorage.create(film);
    }

    public Film update(@Valid @RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            throw new RuntimeException("Id должен быть указан");
        }
        validFilm(newFilm);
        return filmStorage.update(newFilm);
    }

    public void delete(@Valid @RequestParam Long id) {
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
        if (filmDelete == null || user == null) {
            throw new ElementNotFoundException("Фильм/пользователь не найден");
        }
        filmStorage.deleteLike(id, userId);
    }

    public Film updateLike(Long id, Long userId) {
        if (id == null || userId == null) {
            throw new RuntimeException("Id фильма/лайка должен быть указан");
        }
        Film filmUpdate = filmStorage.findById(id);
        User user = userStorage.findById(userId);

        if (filmUpdate == null || user == null) {
            throw new ElementNotFoundException("Фильм/пользователь не найден");
        }
        filmUpdate.getIdLike().addAll(filmStorage.addLike(id, userId));
        return filmUpdate;
    }

    public Film getById(Long id) {
        try {
            return filmStorage.findById(id);

        } catch (RuntimeException e) {
            throw new ElementNotFoundException("Фильм не найден");
        }
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
