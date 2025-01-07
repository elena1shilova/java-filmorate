package ru.yandex.practicum.filmorate.dao;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Mpa {
    private Long id;
    private String name;
    private String description;
}
