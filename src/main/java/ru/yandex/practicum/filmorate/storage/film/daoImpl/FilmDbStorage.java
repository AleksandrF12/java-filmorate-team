package ru.yandex.practicum.filmorate.storage.film.daoImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.exceptions.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.film.dao.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.dao.GenreDao;
import ru.yandex.practicum.filmorate.storage.film.dao.MpaDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Qualifier("filmDbStorage")
@Primary
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaDao mpaDao;
    private final GenreDao genreDao;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, MpaDao mpaDao, GenreDao genreDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaDao = mpaDao;
        this.genreDao = genreDao;
    }

    @Override
    public Film addFilm(Film film) {
        log.info("Запрос на добавление фильма: {} получен хранилищем БД", film.getName());

        //проверка наличия рейтинга в таблице ratings (MPA)
        if (!isRatingsMpa(film.getMpa().getId())) {
            throw new ValidationException("Не найден рейтинг фильма с id=" + film.getMpa().getId());
        }

        //проверка наличия жанра в таблице genres
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            if (!isGenres(film.getGenres())) {
                throw new ValidationException("Для обновляемого фильма не найдены все жанры.");
            }
        }

        //добавить информацию о фильме в таблицу films
        String addFilmSql = "INSERT INTO films(name,description,release_date,duration,rate,rating_id) VALUES(?,?,?,?,?,?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                new PreparedStatementCreator() {
                    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                        PreparedStatement ps =
                                connection.prepareStatement(addFilmSql, new String[]{"film_id"});
                        ps.setString(1, film.getName());
                        ps.setString(2, film.getDescription());
                        ps.setString(3, film.getReleaseDate().toString());
                        ps.setInt(4, film.getDuration());
                        ps.setInt(5, film.getRate());
                        ps.setInt(6, film.getMpa().getId());
                        return ps;
                    }
                },
                keyHolder);
        long filmId = keyHolder.getKey().intValue();
        film.setId(filmId);
        log.debug("Добавлен новый фильм с id={}", filmId);

        //если все жанры найдены в БД, то добавляем записи о жанрах в таблицу films_genre
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> genres=film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            for (int gr : genres) {
                genreDao.addFilmGenre(film.getId(), gr);
            }
        }
        return getFilm(film.getId());
    }

    @Override
    //обновляем поля таблицы films: name, releaseDate, description, duration, rate, rating_id
    //поле rating_id сначала ищем в таблице ratings_mpa и если найден, то обновляем
    //обновляем поля таблицы films_genre:film_id, genre_id - удаляем и перезаписываем
    public Film updateFilm(Film film) {
        log.info("Получен запрос на обновление фильма с id={} в БД", film.getId());
        //проверка наличия рейтинга в таблице ratings (MPA)
        //если он задан
        if (!isRatingsMpa(film.getMpa().getId())) {
            throw new ValidationException("Не найден рейтинг фильма с id=" + film.getMpa().getId());
        }

        //поиск жанра в таблице genres
        //если получено пустое поле с жанрами, то игнорируем проверку
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            if (!isGenres(film.getGenres())) {
                throw new ValidationException("Для обновляемого фильма не найдены все жанры.");
            }
        }

        //обновляем данные в таблице films
        log.debug("Формируем sql запрос...");
        String updateFilmSql = "UPDATE films SET name=?,description=?,release_date=?,duration=?,rate=?," +
                "rating_id=? WHERE film_id=?;";
        Object[] args = new Object[]{film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getRate(), film.getMpa().getId(), film.getId()};

        int updateRow = jdbcTemplate.update(updateFilmSql, args);
        if (updateRow <= 0) {
            log.debug("Фильм с id={} для обновления не найден.", film.getId());
            throw new FilmNotFoundException("Фильм с id=" + film.getId() + " для обновления не найден.");
        }
        log.debug("Фильм с id={} обновлён.", film.getId());
        //если все жанры найдены в БД, то сначала удаляем записи из films_genre
        // потом добавляем записи о жанрах в таблицу films_genre
        genreDao.delFilmGenre(film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> genres=film.getGenres().stream()
                                                .map(Genre::getId)
                                                .collect(Collectors.toSet());
            for (int gr : genres) {
                genreDao.addFilmGenre(film.getId(), gr);
            }
        }
        return getFilm(film.getId());
    }

    @Override
    public void deleteFilm(long filmId) {
        log.debug("Получен запрос на удаление фильма с id={}", filmId);
        String deleteFilmSql = "delete from films where film_id= ?";
        Object[] args = new Object[]{filmId};
        int delRow = jdbcTemplate.update(deleteFilmSql, args);
        if (delRow <= 0) {
            log.debug("Фильм с id={} для удаления не найден.", filmId);
            throw new FilmNotFoundException("Фильм с id=" + filmId + " для удаления не найден.");
        }
        log.debug("Фильм с id={} удалён.", filmId);
    }

    @Override
    //возвращаемые поля:
    //из таблицы films: film_id, name, description, release_date, duration, rate,
    //genre - Set: genre.id...
    //из таблицы ratings_mpa: mpa.id,mpa.name
    public Film getFilm(long filmId) {
        log.debug("Получен запрос на фильм с id={};", filmId);
        String getFilmSql = "select * from films where film_id=?";
        Film film = jdbcTemplate.query(getFilmSql, (rs, rowNum) -> filmMapper(rs), filmId).stream().findAny().orElse(null);
        if (film == null) {
            log.debug("С id={} фильм не найден.", filmId);
            throw new FilmNotFoundException("С id="+filmId+" фильм не найден.");
        }
        log.debug("С id={} возвращён фильм: {}", filmId, film.getName());
        return film;
    }

    @Override
    public List<Film> getFilms() {
        log.debug("Получен запрос на чтение всех фильмов");
        String getFilmSql = "select * from films;";
        List<Film> films = jdbcTemplate.query(getFilmSql, (rs, rowNum) -> filmMapper(rs));
        if (films == null) {
            log.debug("Фильмы не найдены.");
            throw new FilmNotFoundException("Фильмы не найдены.");
        }
        log.debug("Найдено фильмов: {} шт.", films.size());
        return films;
    }

    @Override
    public List<Film> getPopularFilms(long maxCount) {
        String popFilmSql="SELECT f.* FROM FILMS f LEFT JOIN (SELECT FILM_ID,COUNT(*) cLike FROM FILMS_LIKE GROUP BY FILM_ID) fl" +
                " ON fl.FILM_ID=f.FILM_ID ORDER BY fl.cLike DESC LIMIT (?);";
        return jdbcTemplate.query(popFilmSql, (rs, rowNum) -> filmMapper(rs),maxCount);
    }
    private Film filmMapper(ResultSet rs) throws SQLException {
        //перебираем записи результирующего набора
        long id = rs.getLong("film_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
        int duration = rs.getInt("duration");
        int rate = rs.getInt("rate");
        MPA mpa = new MPA();
        mpa.setId(rs.getInt("rating_id"));
        //запрос в таблицу rating_mpa
        MPA ratingMpa = mpaDao.getRating(mpa.getId());
        if (ratingMpa != null) {
            mpa.setName(ratingMpa.getName());
        }
        //запрос в таблицу Films_genre
        Set<Genre> genres = genreDao.getGengesFilm(id).stream().collect(Collectors.toSet());
        return new Film(id, name, description, releaseDate, duration, rate, mpa, genres);
    }


    //проверка наличие видов рейтингов добавляемого/обновляемого фильма в БД
    private boolean isRatingsMpa(int mpaId) {
        MPA ratingMpa = mpaDao.getRating(mpaId);
        if (ratingMpa == null) {
            log.debug("Не найден рейтинг фильма с id={}", mpaId);
            return false;
        }
        return true;
    }

    //проверка наличие видов жанров добавляемого/обновляемого фильма в БД
    private boolean isGenres(Set<Genre> genres) {
        for (Genre gr : genres) {
            Genre genre = genreDao.getGenge(gr.getId());
            if (genre == null) {
                log.debug("Для фильма не найден жанр с id=" + gr.getId());
                return false;
            }
        }
        log.debug("Для фильма не найден все добавляемые (обновляемые) жанры.");
        return true;
    }
}
