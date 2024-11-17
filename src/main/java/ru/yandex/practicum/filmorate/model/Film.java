package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Film {

    private Long id;
    @NotBlank
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Long duration;
    private Set<Long> idLike = new HashSet<>();
    private List<String> genre;
    private MotionPictureAssociation mpa;
}
