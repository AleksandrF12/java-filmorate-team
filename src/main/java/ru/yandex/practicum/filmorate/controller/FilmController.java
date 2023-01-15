package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    InMemoryFilmStorage inMemoryFilmStorage;
    FilmService filmService;

    public FilmController(InMemoryFilmStorage inMemoryFilmStorage, FilmService filmService) {
        this.inMemoryFilmStorage = inMemoryFilmStorage;
        this.filmService = filmService;
    }

    //добавление фильма
    @PostMapping
    protected Film addFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    //обновление фильма
    @PutMapping
    protected Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    //удаление фильма по id
    @DeleteMapping("/{id}")
    protected void deleteFilm(@PathVariable("id") long filmId) {
        filmService.deleteFilm(filmId);
    }

    //получение фильма по id
    @GetMapping("/{id}")
    protected Film getFilm(@PathVariable("id") long filmId) {
        return filmService.getFilm(filmId);
    }

    //возвращает информацию обо всех фильмах
    @GetMapping
    protected List<Film> getFilms() {
        return filmService.getFilms();
    }

    //пользователь ставит лайк фильму
    @PutMapping("/{id}/like/{userId}")
    protected void addLike(@PathVariable("id") long filmId, @PathVariable("userId") long userId) {
        filmService.addLike(filmId, userId);
    }

    //пользователь удаляет лайк у фильма
    @DeleteMapping("/{id}/like/{userId}")
    protected void deleteLike(@PathVariable("id") long filmId, @PathVariable("userId") long userId) {
        filmService.deleteLike(filmId, userId);
    }

    //вернуть самые популярные фильмы
    @GetMapping("/popular")
    protected List<Film> getPopularFilms(@RequestParam(defaultValue = "10", required = false) Long count) {
        log.info("1.Запрос на получение {} популярных фильмов...", count);
        return filmService.getPopularFilms(count);
    }
}
