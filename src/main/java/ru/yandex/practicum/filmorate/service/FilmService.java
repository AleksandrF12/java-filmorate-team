package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.exceptions.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

//отвечает за операции с фильмами, — добавление и удаление лайка, вывод 10 наиболее популярных фильмов
// по количеству лайков. Пусть пока каждый пользователь может поставить лайк фильму только один раз.

@Service
@Slf4j
public class FilmService {

    InMemoryUserStorage inMemoryUserStorage;
    InMemoryFilmStorage inMemoryFilmStorage;

    public FilmService(InMemoryUserStorage inMemoryUserStorage, InMemoryFilmStorage inMemoryFilmStorage) {
        this.inMemoryUserStorage = inMemoryUserStorage;
        this.inMemoryFilmStorage = inMemoryFilmStorage;
    }

    //добавляем фильм
    public Film addFilm(Film film) {
        return inMemoryFilmStorage.addFilm(film);
    }

    //обновляем фильм
    public Film updateFilm(Film film) {
        return inMemoryFilmStorage.updateFilm(film);
    }

    //удаление фильма по id
    public void deleteFilm(long filmId) {
        inMemoryFilmStorage.deleteFilm(filmId);
    }

    //получение фильма по id
    public Film getFilm(long filmId) {
        log.info("GET Запрос на поиск фильма с id={}", filmId);
        return inMemoryFilmStorage.getFilm(filmId);
    }

    //возвращает информацию обо всех фильмах
    public List<Film> getFilms() {
        return inMemoryFilmStorage.getFilms();
    }

    //пользователь ставит лайк фильму.
    public void addLike(long filmId, long userId) {
        //проверка корректности значений filmId,userId
        //проверка существования фильма: наличие в списке фильмов и пользователей
        log.debug("Запрос на добавление фильму с id={} лайка от пользователя с userId={}", filmId, userId);
        if (isValidFilmId(filmId) && isValidUserId(userId)) {
            Optional<Film> filmOpt = Optional.ofNullable(inMemoryFilmStorage.getFilm(filmId));
            log.debug("Получен фильм: " + filmOpt);
            if (filmOpt.isPresent()) {
                Film film = filmOpt.get();
                Set<Long> likeNew = film.getLike();
                likeNew.add(userId);
                film.setLike(likeNew);
                log.info("Фильму с filmId={} добавлен лайк от пользователя с id={}.", filmId, userId);
            } else {
                throw new UserNotFoundException("Пользователь с id=" + userId + " не найден.");
            }
        }
    }

    //пользователь удаляет лайк.
    public void deleteLike(long filmId, long userId) {
        //проверка корректности значений filmId,userId : null, меньше 0
        //проверка существования фильма: наличие в списке фильмов
        //проверка существования пользователя: наличие в списке пользователей
        //проверка существования фильма с filmId и пользователя с userId
        log.debug("Запрос на удаление лайка фильму с id={} лайка от пользователя с userId={}", filmId, userId);
        if (isValidFilmId(filmId) && isValidUserId(userId)) {
            Optional<Film> filmOpt = Optional.ofNullable(inMemoryFilmStorage.getFilm(filmId));
            log.debug("Получен фильм: " + filmOpt);
            if (filmOpt.isPresent()) {
                //получаем текущее количество лайков у фильма
                Film film = filmOpt.get();
                Set<Long> filmLike = film.getLike();
                if (!filmLike.isEmpty() && filmLike.contains(userId)) {
                    filmLike.remove(userId);
                    film.setLike(filmLike);
                    log.debug("Лайк фильма с filmId={} для пользователя userId={} удалён.", filmId, userId);
                }
            } else {
                throw new UserNotFoundException("Пользователь с id=" + userId + " не найден.");
            }
        }
    }

    //вывод популярных фильмов,если параметр не задан, то выводим 10 фильмов
    public List<Film> getPopularFilms(long count) {
        //проверка корректности значения count : null, меньше 0
        if (count <= 0) {
            throw new ValidationException("Запрошено отрицательное количество популярных фильмов.");
        }
        log.debug("Запрос на получение {} популярных фильмов...", count);
        return inMemoryFilmStorage.getFilms().stream()
                .sorted((e1, e2) -> (int) (e2.getId() - e1.getId()))
                .sorted((e1, e2) -> e2.getLike().size() - e1.getLike().size())
                .limit(count)
                .collect(Collectors.toList());
    }

    //проверка корректности значений filmId
    private boolean isValidFilmId(long filmId) {
        if (filmId <= 0) {
            throw new FilmNotFoundException("Некорректный id фильма.");
        }
        //проверка существования фильма userId : наличие в списке фильмов
        Optional<Film> film = Optional.ofNullable(inMemoryFilmStorage.getFilm(filmId));
        if (!film.isPresent()) {
            throw new FilmNotFoundException("Фильм с id=" + filmId + " не найден.");
        }
        return true;
    }

    //проверка корректности значений filmId
    private boolean isValidUserId(long userId) {
        if (userId <= 0) {
            throw new UserNotFoundException("Некорректный id пользователя.");
        }
        Optional<User> user = Optional.ofNullable(inMemoryUserStorage.getUser(userId));
        if (!user.isPresent()) {
            throw new UserNotFoundException("Пользователь с id=" + userId + " не найден.");
        }
        return true;
    }
}
