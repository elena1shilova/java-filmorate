package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genres;
import ru.yandex.practicum.filmorate.service.GenresService;

import java.util.List;

@RestController
@RequestMapping("/genres")
@Slf4j
@RequiredArgsConstructor
public class GenresController {

    private final GenresService genresService;

    @GetMapping()
    public List<Genres> getFilmGenres() {
        return genresService.findAll();
    }

    @GetMapping("/{id}")
    public Genres getFilmGenresId(@PathVariable Long id) {
        return genresService.getFilmMpaById(id);
    }
}
