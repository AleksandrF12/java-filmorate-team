package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    InMemoryUserStorage inMemoryUserStorage;

    public UserService(InMemoryUserStorage inMemoryUserStorage) {
        this.inMemoryUserStorage = inMemoryUserStorage;
    }

    //возвращает информацию обо всех пользователях
    public List<User> getUsers() {
        return inMemoryUserStorage.getUsers();
    }

    //получение данных о пользователе
    public User getUser(long userId) {
        log.info("Получен запрос на получение данных пользователя с id={}", userId);
        return inMemoryUserStorage.getUser(userId);
    }

    //добавление в друзья
    public void addFriend(long userId, long friendId) {
        if (isValidIdUser(userId) && isValidIdUser(friendId)) {
            log.debug("Получен запрос на добавление для пользователя с id={} друга с id={}", userId, friendId);
            //взаимно добавляем друзей в списки
            addFriendsInUser(userId, friendId);
            addFriendsInUser(friendId, userId);
            log.info("Для пользователя с id = {} добавлен друг с id={}", userId, friendId);
        }
    }

    //обновляет список друзей
    private void addFriendsInUser(long userId, long friendId) {
        Optional<Set<Long>> friendsUser = Optional.ofNullable(inMemoryUserStorage.getUser(userId).getFriends());
        log.debug("Друзья пользователя с id={} : {}", userId, friendsUser);
        if (!friendsUser.isPresent()) {
            Set<Long> friend = new HashSet<>();
            friend.add(friendId);
            inMemoryUserStorage.getUser(userId).setFriends(friend);
        } else {
            Set<Long> friendsSet = friendsUser.get();
            friendsSet.add(friendId);
        }
        log.info("Друзья пользователя с id={} после обновления: {}", userId, inMemoryUserStorage.getUser(userId).getFriends());
    }

    //удаление из друзей
    public void deleteFriend(long userId, long friendId) {
        log.debug("Получен запрос на удаление для пользователя с id={} друга с id={}", userId, friendId);
        if (isValidIdUser(userId) && isValidIdUser(friendId) && !isEqualIdUser(userId, friendId)) {
            log.debug("Запрос на удаление для пользователя с id={} друга с id={} одобрен.", userId, friendId);
            Optional<Set<Long>> friendsOp = Optional.ofNullable(inMemoryUserStorage.getUser(userId).getFriends());
            if (friendsOp.isPresent() && friendsOp.get().contains(friendId)) {
                Set<Long> friends = friendsOp.get();
                friends.remove(friendId);
                log.info("У пользователя с id = {} удалён друг с id={}", userId, friendId);
            }
        }
    }

    //возвращение списка друзей пользователя
    public List<User> getFriends(long userId) {
        log.debug("Получен запрос на получение для пользователя с id={} списка друзей", userId);
        if (isValidIdUser(userId)) {
            return inMemoryUserStorage.getUser(userId).getFriends().stream()
                    .map(inMemoryUserStorage::getUser)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }


    //список друзей, общих с другим пользователем.
    public List<User> getOtherFriends(long userId, long otherId) {
        log.debug("Получен запрос на поиск общих друзей для пользователей с userId={} и otherId={}.", userId, otherId);
        if (isValidIdUser(userId) && isValidIdUser(otherId) && !isEqualIdUser(userId, otherId)) {
            log.debug("Запрос на поиск общих друзей для пользователей с userId={} и otherId={} одобрен.", userId, otherId);
            Optional<Set<Long>> userFriendsOp = Optional.ofNullable(inMemoryUserStorage.getUser(userId).getFriends());
            Optional<Set<Long>> otherFriendsOp = Optional.ofNullable(inMemoryUserStorage.getUser(otherId).getFriends());
            if (userFriendsOp.isPresent() && otherFriendsOp.isPresent()) {
                Set<Long> userFriends = userFriendsOp.get();
                Set<Long> otherFriends = otherFriendsOp.get();
                return userFriends.stream().filter(otherFriends::contains).map(inMemoryUserStorage::getUser)
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    private boolean isValidIdUser(long userId) {
        if (userId <= 0) {
            throw new UserNotFoundException("Некорректный id=" + userId + " пользователя.");
        }
        Optional<User> user = Optional.ofNullable(inMemoryUserStorage.getUser(userId));
        if (!user.isPresent()) {
            throw new UserNotFoundException("Пользователь с id=" + userId + " не найден.");
        }
        return true;
    }

    //проверяет не равныли id пользователя и друга
    private boolean isEqualIdUser(long userId, long friendId) {
        if (userId == friendId) {
            throw new UserNotFoundException("Пользователь с id=" + userId + " не может добавить сам себя в друзья.");
        }
        return false;
    }
}
