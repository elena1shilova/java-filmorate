package ru.yandex.practicum.filmorate.exception;

public class ElementNotFoundException extends RuntimeException {

    public ElementNotFoundException(String message) {
        super(message);
    }
}
