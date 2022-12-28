package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class FilmControllerTest {
    private final Film filmTest1 = new Film(1, "Film_Test_1_name", "Film_Test_1_description"
            , LocalDate.of(2000, 8, 20), 120, 4);

    private final Film filmTest2 = new Film(2, "Film_Test_2_name", "Film_Test_2_description"
            , LocalDate.of(1997, 7, 1), 100, 8);

    private final Film filmTest2_New = new Film(2, "Film_Test_2_name_New", "Film_Test_2_description_New"
            , LocalDate.of(2020, 6, 1), 120, 7);

    private final Film filmTestBadName = new Film(2, null, "Film_BadName"
            , LocalDate.of(1985, 12, 28), 0, 8);

    private final Film filmTestBadDescription = new Film(2, "Интерстеллар", "Когда засуха, " +
            "пыльные бури и вымирание растений приводят человечество к продовольственному кризису, коллектив " +
            "исследователей и учёных отправляется сквозь червоточину (которая предположительно соединяет области " +
            "пространства-времени через большое расстояние) в путешествие, чтобы превзойти прежние ограничения для " +
            "космических путешествий человека и найти планету с подходящими для человечества условиями."
            , LocalDate.of(2014, 10, 26), 169, 8);
    private final Film filmTestBadDateRelease = new Film(2, "Film_Name_BadReleaseDate"
            , "Film_Description_BadReleaseDate"
            , LocalDate.of(1895, 12, 27), 100, 8);

    private final Film filmTestBadDuration = new Film(2, "Film_Name_BadDuration"
            , "Film_Description_BadDuration"
            , LocalDate.of(1895, 12, 29), -1, 8);

    private final Film filmTestBadId = new Film(9999, "Film_Name_BadId"
            , "Film_Description_BadId"
            , LocalDate.of(1895, 12, 29), 1, 8);

    private final int port=8080;

    @Autowired
    private FilmController controller;

    @Autowired
    private TestRestTemplate restTemplate;

    //класс преобразовывает объект в JSON-строку
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void clearFilms() {
        this.controller.films.clear();
        this.controller.maxId = 0;
    }
    //добавляем фильм, который не должен проходить валидацию:
    // - неверное название name
    // - неверное описание description
    // - некорректная дата релиза releaseDate
    // - некорректная продолжительность фильма duration;
    //добавляем фильм, который должен проходить валидацию

    @Test
    public void addFilmBadName() throws Exception {
        //добавляем фильм с неверным названием
        this.mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(filmTestBadName))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addFilmBadDescription() throws Exception {
        //добавляем фильм с неверным описанием
        this.mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(filmTestBadDescription))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addFilmBadDateRelease() throws Exception {
        //добавляем фильм с неверной датой релиза
        this.mockMvc.perform(post("/films")
                .content(objectMapper.writeValueAsString(filmTestBadDateRelease))
                .contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void addFilmBadDuration() throws Exception {
        //добавляем фильм с неверной продолжительностью
        this.mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(filmTestBadDuration))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addFilmTrueValidate() throws Exception {
        //добавляем 2 фильма, которые должны проходить валидацию
        this.mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(filmTest1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").isNumber())
                .andExpect(jsonPath("id").value(1))
                .andExpect(jsonPath("name").isString())
                .andExpect(jsonPath("name").value("Film_Test_1_name"))
                .andExpect(jsonPath("description").isString())
                .andExpect(jsonPath("description").value("Film_Test_1_description"))
                .andExpect(jsonPath("releaseDate").isString())
                .andExpect(jsonPath("releaseDate").value("2000-08-20"))
                .andExpect(jsonPath("duration").isNumber())
                .andExpect(jsonPath("duration").value(120));

        this.mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(filmTest2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").isNumber())
                .andExpect(jsonPath("id").value(2))
                .andExpect(jsonPath("name").isString())
                .andExpect(jsonPath("name").value("Film_Test_2_name"))
                .andExpect(jsonPath("description").isString())
                .andExpect(jsonPath("description").value("Film_Test_2_description"))
                .andExpect(jsonPath("releaseDate").isString())
                .andExpect(jsonPath("releaseDate").value("1997-07-01"))
                .andExpect(jsonPath("duration").isNumber())
                .andExpect(jsonPath("duration").value(100));

        //считываем добавленные фильмы
        this.mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").isString())
                .andExpect(jsonPath("$[0].name").value("Film_Test_1_name"))
                .andExpect(jsonPath("$[0].description").isString())
                .andExpect(jsonPath("$[0].description").value("Film_Test_1_description"))
                .andExpect(jsonPath("$[0].duration").isNumber())
                .andExpect(jsonPath("$[0].duration").value(120))
                .andExpect(jsonPath("$[0].releaseDate").value("2000-08-20"))
                .andExpect(jsonPath("$[1].id").isNumber())
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").isString())
                .andExpect(jsonPath("$[1].name").value("Film_Test_2_name"))
                .andExpect(jsonPath("$[1].description").isString())
                .andExpect(jsonPath("$[1].description").value("Film_Test_2_description"))
                .andExpect(jsonPath("$[1].duration").isNumber())
                .andExpect(jsonPath("$[1].duration").value(100))
                .andExpect(jsonPath("$[1].releaseDate").value("1997-07-01"));
    }

    //добавляем 2 фильма
    //обновляем данные фильмов, которые не должны проходить валидацию:
    // - неверный идентификатор
    // - неверное название name
    // - неверное описание description
    // - некорректная дата релиза releaseDate
    // - некорректная продолжительность фильма duration;
    //обновляем данные фильмов, которые должны проходить валидацию

    @Test
    public void updateFilmBadId() throws Exception {
        //добавляем 2 фильма
        this.restTemplate.postForEntity("http://localhost:" + port + "/films", filmTest1, Film.class);
        this.restTemplate.postForEntity("http://localhost:" + port + "/films", filmTest2, Film.class);

        //обновляем фильм с неверным идентификатором
        this.mockMvc.perform(put("/films")
                        .content(objectMapper.writeValueAsString(filmTestBadId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void updateFilmBadName() throws Exception {
        //добавляем 2 фильма
        this.restTemplate.postForEntity("http://localhost:" + port + "/films", filmTest1, Film.class);
        this.restTemplate.postForEntity("http://localhost:" + port + "/films", filmTest2, Film.class);

        //обновляем фильм с неверным названием
        this.mockMvc.perform(put("/films")
                        .content(objectMapper.writeValueAsString(filmTestBadName))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateFilmBadDescription() throws Exception {
        //добавляем 2 фильма
        this.restTemplate.postForEntity("http://localhost:" + port + "/films", filmTest1, Film.class);
        this.restTemplate.postForEntity("http://localhost:" + port + "/films", filmTest2, Film.class);

        //обновляем фильм с неверным описанием
        this.mockMvc.perform(put("/films")
                        .content(objectMapper.writeValueAsString(filmTestBadDescription))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateFilmBadDateRelease() throws Exception {
        //добавляем 2 фильма
        this.restTemplate.postForEntity("http://localhost:" + port + "/films", filmTest1, Film.class);
        this.restTemplate.postForEntity("http://localhost:" + port + "/films", filmTest2, Film.class);

        //обновляем фильм с неверной датой релиза
        this.mockMvc.perform(put("/films")
                        .content(objectMapper.writeValueAsString(filmTestBadDateRelease))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateFilmBadDuration() throws Exception {
        //добавляем 2 фильма
        this.restTemplate.postForEntity("http://localhost:" + port + "/films", filmTest1, Film.class);
        this.restTemplate.postForEntity("http://localhost:" + port + "/films", filmTest2, Film.class);

        //обновляем фильм с неверной продолжительностью
        this.mockMvc.perform(put("/films")
                        .content(objectMapper.writeValueAsString(filmTestBadDuration))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateFilmValidate() throws Exception {
        //добавляем 2 фильма
        this.restTemplate.postForEntity("http://localhost:" + port + "/films", filmTest1, Film.class);
        this.restTemplate.postForEntity("http://localhost:" + port + "/films", filmTest2, Film.class);

        //обновляем данные фильмов, которые должны проходить валидацию
        this.mockMvc.perform(put("/films")
                        .content(objectMapper.writeValueAsString(filmTest2_New))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").isNumber())
                .andExpect(jsonPath("id").value(2))
                .andExpect(jsonPath("name").isString())
                .andExpect(jsonPath("name").value("Film_Test_2_name_New"))
                .andExpect(jsonPath("description").isString())
                .andExpect(jsonPath("description").value("Film_Test_2_description_New"))
                .andExpect(jsonPath("releaseDate").isString())
                .andExpect(jsonPath("releaseDate").value("2020-06-01"))
                .andExpect(jsonPath("duration").isNumber())
                .andExpect(jsonPath("duration").value(120));

        //считываем фильмы для проверки изменений
        this.mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").isString())
                .andExpect(jsonPath("$[0].name").value("Film_Test_1_name"))
                .andExpect(jsonPath("$[0].description").isString())
                .andExpect(jsonPath("$[0].description").value("Film_Test_1_description"))
                .andExpect(jsonPath("$[0].duration").isNumber())
                .andExpect(jsonPath("$[0].duration").value(120))
                .andExpect(jsonPath("$[0].releaseDate").value("2000-08-20"))
                .andExpect(jsonPath("$[1].id").isNumber())
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").isString())
                .andExpect(jsonPath("$[1].name").value("Film_Test_2_name_New"))
                .andExpect(jsonPath("$[1].description").isString())
                .andExpect(jsonPath("$[1].description").value("Film_Test_2_description_New"))
                .andExpect(jsonPath("$[1].duration").isNumber())
                .andExpect(jsonPath("$[1].duration").value(120))
                .andExpect(jsonPath("$[1].releaseDate").value("2020-06-01"));
    }


    //возвращаем пустой список: проверяем статус и тело запроса
    //возвращаем список из 1 фильма
    //возвращаем список из нескольких фильмов
    @Test
    public void getFilms() throws Exception {
        //возвращаем пустой список
        this.mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("[]")));

        //возвращаем список из 1 фильма
        //добавляем фильм
        this.restTemplate.postForEntity("http://localhost:" + port + "/films", filmTest1, Film.class);
        this.mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").isString())
                .andExpect(jsonPath("$[0].name").value("Film_Test_1_name"))
                .andExpect(jsonPath("$[0].description").isString())
                .andExpect(jsonPath("$[0].description").value("Film_Test_1_description"))
                .andExpect(jsonPath("$[0].duration").isNumber())
                .andExpect(jsonPath("$[0].duration").value(120))
                .andExpect(jsonPath("$[0].releaseDate").value("2000-08-20"));

        //добавляем 2-й фильм
        this.restTemplate.postForEntity("http://localhost:" + port + "/films", filmTest2, Film.class);
        this.mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].id").isNumber())
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").isString())
                .andExpect(jsonPath("$[1].name").value("Film_Test_2_name"))
                .andExpect(jsonPath("$[1].description").isString())
                .andExpect(jsonPath("$[1].description").value("Film_Test_2_description"))
                .andExpect(jsonPath("$[1].duration").isNumber())
                .andExpect(jsonPath("$[1].duration").value(100))
                .andExpect(jsonPath("$[1].releaseDate").value("1997-07-01"));
    }
}
