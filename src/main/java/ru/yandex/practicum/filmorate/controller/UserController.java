package ru.yandex.practicum.filmorate.controller;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final LocalDate MIN_BIRTHDAY = LocalDate.now();
    private final short MIN_MAIL_LENGTH = 6;
    private final short MIN_MAIL_POSITION = 0;

    public int maxId = 0;

    @Getter
    @Setter
    public Map<Integer, User> users = new HashMap<>(); //информация о фильмах

    //добавление пользователя
    @PostMapping
    private User addUser(@Valid @RequestBody User user) {
        //если валидация прошла успешно, то генерируем id для пользователя и добавляем в список
        String name = user.getName();
        String login = user.getLogin();
        if (name == null || name.trim() == "") {
            user.setName(login);
        }
        final int id = generateId();
        user.setId(id);
        this.users.put(id, user);
        //возвращаем информацию о добавленном пользователе
        log.info("Добавлен пользователь с id={}, name={}, email={}, login={}, birthday={}"
                , id, user.getName(), user.getEmail(), user.getLogin(), user.getBirthday());
        return user;
    }

    //обновление пользователя
    @PutMapping
    private User updateUser(@Valid @RequestBody User user) {
        //валидация добавляемого фильма, если не проходит, то возвращаем на сервер сообщение о некорректных данных
            if (this.users.containsKey(user.getId())) {
                String name = user.getName();
                String login = user.getLogin();
                if (name == null || name.trim() == "") {
                    user.setName(login);
                }
                this.users.put(user.getId(), user);
                log.info("Обновлены данные пользователя с id={}, name={}, email={}, login={}, birthday={}"
                        , user.getId(), user.getName(), user.getEmail(), user.getLogin(), user.getBirthday());
                return user;
            } else {
                log.warn("Данные пользователя не обновлены, неверный формат (данные): с id={}" +
                                ", name={}, email={}, login={}, birthday={}"
                        , user.getId(), user.getName(), user.getEmail(), user.getLogin(), user.getBirthday());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Пользователя с таким id нет.");
            }
    }

    //возвращает информацию обо всех фильмах
    //в формате json
    @GetMapping
    private List<User> getUsers() {
        final List<User> users = new ArrayList<>(this.users.values());
        return users;
    }

    //генерация очередного id фильма
    private int generateId() {
        return ++maxId;
    }
}
