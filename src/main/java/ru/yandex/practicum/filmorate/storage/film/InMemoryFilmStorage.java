package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.*;

//реализация методов добавления, удаления и модификации объектов.
@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private int maxId = 0;

    private Map<Long, Film> films = new HashMap<>(); //информация о фильмах
    InMemoryUserStorage inMemoryUserStorage;

    public InMemoryFilmStorage(InMemoryUserStorage inMemoryUserStorage) {
        this.inMemoryUserStorage = inMemoryUserStorage;
    }

    @Override
    public Film addFilm(Film film) {
        //если валидация прошла успешно, то генерируем id для фильма и добавляем в фильмотеку
        final long id = generateId();
        film.setId(id);
        film.setLike(new HashSet<>());
        log.debug("Получен запрос на добавление фильма : {}", film);
        this.films.put(id, film);
        log.info("Фильм добавлен : {}", this.films.get(id));
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        //валидация добавляемого фильма, если не проходит, то возвращаем на сервер сообщение о некорректных данных
        long filmId=film.getId();
        if (this.films.containsKey(filmId)) {
            Film filmNew = films.get(filmId);
            if (filmNew.getLike().isEmpty()) {
                film.setLike(new HashSet<>());
            }
            this.films.put(film.getId(), film);
            log.info("Фильм обновлён : {}", this.films.get(filmId));
            return film;
        }
        throw new FilmNotFoundException("Фильм с id="+filmId+" не найден.");
    }

    //удаление фильма
    @Override
    public void deleteFilm(long filmId) {
        if (isValidIdFilm(filmId)) {
            films.remove(filmId);
            log.info("Фильм с id={} удалён.", filmId);
        }
    }

    //возвращает список всех фильмов
    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(this.films.values());
    }

    //получение фильма по id
    @Override
    public Film getFilm(long filmId) {
        //проверка корректности значений filmId,userId : null, меньше 0
        //проверка существования фильма: наличие в списке фильмов
        //проверка существования пользователя: наличие в списке пользователей
        log.debug("Запрошен фильм с id={}", filmId);
        if (isValidIdFilm(filmId)) {
            return this.films.get(filmId);
        }
        throw new FilmNotFoundException("Фильм с id="+filmId+" не найден.");
    }

    //генерация очередного id фильма
    private int generateId() {
        return ++maxId;
    }

    //проверка корректности значений filmId
    private boolean isValidIdFilm(long filmId) {
        if (filmId <= 0) {
            throw new FilmNotFoundException("Некорректный id фильма.");
        }
        //проверка существования фильма userId : наличие в списке фильмов
        if (!films.containsKey(filmId)) {
            throw new FilmNotFoundException("Фильм с id=" + filmId + " не найден.");
        }
        return true;
    }

    //метод для тестирования
    //удаление всей фильмотеки
    public void clear() {
        this.films.clear();
        this.maxId = 0;
    }
}
