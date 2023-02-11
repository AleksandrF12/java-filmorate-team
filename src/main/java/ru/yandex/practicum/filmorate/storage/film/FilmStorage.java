package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

//методы добавления, удаления и модификации объектов.

public interface FilmStorage {

    //добавление фильма
    Film addFilm(Film film);

    //обновление данных о фильме
    Film updateFilm(Film film);

    //удаление фильма
    void deleteFilm(long filmId);

    Film getFilm(long filmId);
    List<Film> getFilms();

}