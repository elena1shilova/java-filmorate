package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ElementNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private Long filmId = 1L;


    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film findById(Long id) {

        Film film = films.get(id);
        if (film == null) {
            throw new ElementNotFoundException("id = " + id + " не найден");
        }
        return film;
    }

    @Override
    public Film create(Film film) {

        if (film.getName() == null) {
            throw new RuntimeException("Наименование должно быть указано");
        }

        validFilm(film);
        film.setId(filmId);
        filmId++;
        films.put(film.getId(), film);
        log.debug("Фильм успешно создан");
        return film;
    }

    @Override
    public Film update(Film newFilm) {

        if (newFilm.getId() == null) {
            throw new RuntimeException("Id должен быть указан");
        }
        if (!films.containsKey(newFilm.getId())) {
            throw new ElementNotFoundException("id = " + newFilm.getId() + " не найден");
        }
        validFilm(newFilm);
        films.put(newFilm.getId(), newFilm);
        return newFilm;
    }

    @Override
    public void delete(Integer id) {

        if (id == null) {
            throw new RuntimeException("Id должен быть указан");
        }
        if (!films.containsKey(id)) {
            throw new ElementNotFoundException("id = " + id + " не найден");
        }
        films.remove(id);
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
