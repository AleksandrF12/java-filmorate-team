package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.exceptions.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.dao.FilmLikeDao;
import ru.yandex.practicum.filmorate.storage.film.dao.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.dao.MpaDao;
import ru.yandex.practicum.filmorate.storage.user.dao.UserStorage;

import java.util.List;

//отвечает за операции с фильмами, — добавление и удаление лайка, вывод 10 наиболее популярных фильмов
// по количеству лайков. Пусть пока каждый пользователь может поставить лайк фильму только один раз.

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaDao mpaDao;
    private final FilmLikeDao filmLikeDao;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage, MpaDao mpaDao, FilmLikeDao filmLikeDao) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaDao = mpaDao;
        this.filmLikeDao = filmLikeDao;
    }

    //добавляем фильм
    @Qualifier("filmDbStorage")
    public Film addFilm(Film film) {
        log.info("Запрос на добавление фильма: {} направлен в хранилище...",film.getName());
        return filmStorage.addFilm(film);
    }

    //обновляем фильм
    public Film updateFilm(Film film) {
        isValidFilmId(film.getId());
        return filmStorage.updateFilm(film);
    }

    //удаление фильма по id
    public void deleteFilm(long filmId) {
        isValidFilmId(filmId);
        filmStorage.deleteFilm(filmId);
    }

    //получение фильма по id
    public Film getFilm(long filmId) {
        log.info("GET Запрос на поиск фильма с id={}", filmId);
        isValidFilmId(filmId);
        return filmStorage.getFilm(filmId);
    }

    //возвращает информацию обо всех фильмах
    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    //пользователь ставит лайк фильму.
    public void addLike(long filmId, long userId) {
        log.debug("Запрос на добавление фильму с id={} лайка от пользователя с userId={}", filmId, userId);
        //проверка существования фильма с id
        isValidFilmId(filmId);
        isValidUserId(userId);
        Film film=filmStorage.getFilm(filmId);
        if(film==null) {
            throw new FilmNotFoundException("Фильм с id="+filmId+" не найден.");
        }
        //проверка существования пользователя с id
        User user=userStorage.getUser(userId);
        if(user==null) {
            throw new UserNotFoundException("Пользователь с id=" + userId + " не найден.");
        }
        filmLikeDao.addLike(filmId,userId);
    }

    //пользователь удаляет лайк.
    public void deleteLike(long filmId, long userId) {
        log.debug("Запрос на удаление лайка фильму с id={} лайка от пользователя с userId={}", filmId, userId);
        isValidFilmId(filmId);
        isValidUserId(userId);
        filmLikeDao.deleteLike(filmId,userId);
    }

    //вывод популярных фильмов,если параметр не задан, то выводим 10 фильмов
    public List<Film> getPopularFilms(long count) {
        //проверка корректности значения count : null, меньше 0
        if (count <= 0) {
            throw new ValidationException("Запрошено отрицательное количество популярных фильмов.");
        }
        log.debug("Запрос на получение {} популярных фильмов...", count);
        return filmStorage.getPopularFilms(count);
    }

    //проверка корректности значений filmId
    private boolean isValidFilmId(long filmId) {
        if (filmId <= 0) {
            throw new FilmNotFoundException("Некорректный id фильма.");
        }
        return true;
    }

    //проверка корректности значений filmId
    private boolean isValidUserId(long userId) {
        if (userId <= 0) {
            throw new UserNotFoundException("Некорректный id пользователя.");
        }
        return true;
    }
}
