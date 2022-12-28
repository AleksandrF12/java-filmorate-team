package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    public int maxId = 0;

    public Map<Integer, Film> films = new HashMap<>(); //информация о фильмах

    //добавление фильма
    //валидация добавляемого фильма
    //возвращаем информацию о добавленном фильме в формате json
    @PostMapping
    protected Film addFilm(@Valid @RequestBody Film film){
                //если валидация прошла успешно, то генерируем id для фильма и добавляем в фильмотеку
                final int id = generateId();
                film.setId(id);
                this.films.put(id, film);
                //возвращаем информацию о добавленном фильме
                log.info("Добавлен фильм с id={}, name={}, description={}, releaseDate={}, duration={}"
                        , id, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration());
                return film;
    }

    //обновление фильма
    //валидация обновляемого фильма
    //возвращаем информацию об обновлённом фильме в формате json
    @PutMapping
    protected Film updateFilm(@Valid @RequestBody Film film) {
//        try {
            //валидация добавляемого фильма, если не проходит, то возвращаем на сервер сообщение о некорректных данных
            if (this.films.containsKey(film.getId())) {
                this.films.put(film.getId(), film);
                log.info("Обновлены данные фильма с id={}, name={}, description={}, releaseDate={}, duration={}"
                        , film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration());
                return film;
            } else {
                log.warn("Фильм не обновлён, неверный формат: с id={}, name={}, description={}, releaseDate={}, duration={}"
                        , film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Фильма с таким id нет.");
            }
    }

    //возвращает информацию обо всех фильмах
    @GetMapping
    protected List<Film> getFilms() {
        final List<Film> films = new ArrayList<>(this.films.values());
        return films;
    }

    //генерация очередного id фильма
    private int generateId() {
        return ++maxId;
    }
}
