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
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class UserControllerTest {

    private final User userTest1 = new User(1, "userOne@ya.ru", "User_Test_1_Login"
            , "User_Test_1_Name", LocalDate.of(1990,12,1));

    private final User userTest2 = new User(2, "userTwo@ya.ru", "User_Test_2_Login"
            , "", LocalDate.of(2000,8,5));

    private final User userTest2_New = new User(2, "userTwoNew@ya.ru", "User_Test_2_Login_New"
            , "User_Test_2_Name_New", LocalDate.of(2002,7,3));

    private final User userTestBadEmail = new User(2, "@", "User_LoginBadEmail"
            , "User_NameBadEmail", LocalDate.of(1990,12,1));

    private final User userTestBadLogin = new User(2, "123@ya.ru", " User LoginBadLogin"
            , "User_NameBadLogin", LocalDate.of(1990,12,1));

    private final User userTestNullName = new User(2, "123@ya.ru", "User_LoginBadName"
            , "", LocalDate.of(1990,12,1));

    private final User userTestBadBirthday = new User(2, "123@ya.ru", "User_LoginBadName"
            , "User_NameBadBirthday", LocalDate.of(2023,12,1));

    private final User userTestBadId = new User(9999, "123@ya.ru", "User_LoginBadId"
            , "User_NameBadId", LocalDate.of(2000,12,1));

    private final int port=8080;

    @Autowired
    private UserController controller;

    @Autowired
    private TestRestTemplate restTemplate;

    //класс преобразовывает объект в JSON-строку
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void clearUsers() {
        this.controller.users.clear();
        this.controller.maxId = 0;
    }

    //добавляем пользователя, который не должен проходить валидацию:
    // - неверная электронная почта — email: электронная почта не может быть пустой и должна содержать символ @
    // - некорректный логин пользователя — login: логин не может быть пустым и содержать пробелы
    // - пустое имя — name: имя для отображения может быть пустым — в таком случае будет использован логин
    // - некорректная дата рождения — birthday: дата рождения не может быть в будущем
    //добавляем фильм, который должен проходить валидацию

    @Test
    public void addUserBadEmail() throws Exception {
        //добавляем пользователя с некорректным email
        this.mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userTestBadEmail))
                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest());
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addUserBadLogin() throws Exception {
        //добавляем пользователя с некорректным login
        this.mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userTestBadLogin))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addUserBadBirthday() throws Exception {
        //добавляем пользователя с некорректной датой рождения
        this.mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userTestBadBirthday))
                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest());
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addUserValid() throws Exception {

        //добавляем 2 пользователя, которые должны проходить валидацию
        this.mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userTest1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").isNumber())
                .andExpect(jsonPath("id").value(1))
                .andExpect(jsonPath("name").isString())
                .andExpect(jsonPath("name").value("User_Test_1_Name"))
                .andExpect(jsonPath("login").isString())
                .andExpect(jsonPath("login").value("User_Test_1_Login"))
                .andExpect(jsonPath("email").isString())
                .andExpect(jsonPath("email").value("userOne@ya.ru"))
                .andExpect(jsonPath("birthday").isString())
                .andExpect(jsonPath("birthday").value("1990-12-01"));


        //добавляем 2 пользователя, которые должны проходить валидацию
        this.mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userTest2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").isNumber())
                .andExpect(jsonPath("id").value(2))
                .andExpect(jsonPath("name").isString())
                .andExpect(jsonPath("name").value("User_Test_2_Login"))
                .andExpect(jsonPath("login").isString())
                .andExpect(jsonPath("login").value("User_Test_2_Login"))
                .andExpect(jsonPath("email").isString())
                .andExpect(jsonPath("email").value("userTwo@ya.ru"))
                .andExpect(jsonPath("birthday").isString())
                .andExpect(jsonPath("birthday").value("2000-08-05"));

        //считываем добавленных пользователей
        this.mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").isString())
                .andExpect(jsonPath("$[0].name").value("User_Test_1_Name"))
                .andExpect(jsonPath("$[0].login").isString())
                .andExpect(jsonPath("$[0].login").value("User_Test_1_Login"))
                .andExpect(jsonPath("$[0].email").isString())
                .andExpect(jsonPath("$[0].email").value("userOne@ya.ru"))
                .andExpect(jsonPath("$[0].birthday").isString())
                .andExpect(jsonPath("$[0].birthday").value("1990-12-01"))
                .andExpect(jsonPath("$[1].id").isNumber())
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").isString())
                .andExpect(jsonPath("$[1].name").value("User_Test_2_Login"))
                .andExpect(jsonPath("$[1].login").isString())
                .andExpect(jsonPath("$[1].login").value("User_Test_2_Login"))
                .andExpect(jsonPath("$[1].email").isString())
                .andExpect(jsonPath("$[1].email").value("userTwo@ya.ru"))
                .andExpect(jsonPath("$[1].birthday").isString())
                .andExpect(jsonPath("$[1].birthday").value("2000-08-05"));
    }

    //добавляем 2 пользователя
    //обновляем данные пользователей, которые не должны проходить валидацию:
    // - неверная электронная почта — email: электронная почта не может быть пустой и должна содержать символ @
    // - некорректный логин пользователя — login: логин не может быть пустым и содержать пробелы
    // - пустое имя — name: имя для отображения может быть пустым — в таком случае будет использован логин
    // - некорректная дата рождения — birthday: дата рождения не может быть в будущем
    //обновляем данные пользователей, которые должны проходить валидацию
        @Test
    public void updateUserBadId() throws Exception {
            //добавляем 2 фильма
            this.restTemplate.postForEntity("http://localhost:" + port + "/users", userTest1, User.class);
            this.restTemplate.postForEntity("http://localhost:" + port + "/users", userTest2, User.class);

            //обновляем пользователя с неверным идентификатором
            this.mockMvc.perform(put("/users")
                            .content(objectMapper.writeValueAsString(userTestBadId))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());
//                    .andExpect(status().isBadRequest());
        }

    @Test
    public void updateUserBadEmail() throws Exception {
        //добавляем 2 фильма
        this.restTemplate.postForEntity("http://localhost:" + port + "/users", userTest1, User.class);
        this.restTemplate.postForEntity("http://localhost:" + port + "/users", userTest2, User.class);

        //обновляем пользователя с некорректной почтой
        this.mockMvc.perform(put("/users")
                        .content(objectMapper.writeValueAsString(userTestBadEmail))
                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isInternalServerError());
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateUserBadLogin() throws Exception {
        //добавляем 2 фильма
        this.restTemplate.postForEntity("http://localhost:" + port + "/users", userTest1, User.class);
        this.restTemplate.postForEntity("http://localhost:" + port + "/users", userTest2, User.class);

        //обновляем пользователя с неверным login
        this.mockMvc.perform(put("/users")
                        .content(objectMapper.writeValueAsString(userTestBadLogin))
                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isInternalServerError());
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateUserNullName() throws Exception {
        //добавляем 2 фильма
        this.restTemplate.postForEntity("http://localhost:" + port + "/users", userTest1, User.class);
        this.restTemplate.postForEntity("http://localhost:" + port + "/users", userTest2, User.class);

        this.mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userTest1))
                        .contentType(MediaType.APPLICATION_JSON));
//                .andExpect(status().isOk());
        this.mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(userTest2))
                .contentType(MediaType.APPLICATION_JSON));

        //обновляем пользователя с пустым name
        this.mockMvc.perform(put("/users")
                        .content(objectMapper.writeValueAsString(userTestNullName))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void updateUserBadBirthday() throws Exception {
        //добавляем 2 фильма
        this.restTemplate.postForEntity("http://localhost:" + port + "/users", userTest1, User.class);
        this.restTemplate.postForEntity("http://localhost:" + port + "/users", userTest2, User.class);

        //обновляем пользователя с неверной датой рождения
        this.mockMvc.perform(put("/users")
                        .content(objectMapper.writeValueAsString(userTestBadBirthday))
                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isInternalServerError());
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateUser() throws Exception {
        //добавляем 2 фильма
        this.restTemplate.postForEntity("http://localhost:" + port + "/users", userTest1, User.class);
        this.restTemplate.postForEntity("http://localhost:" + port + "/users", userTest2, User.class);

        //обновляем данные пользователей, которые должны проходить валидацию
        this.mockMvc.perform(put("/users")
                        .content(objectMapper.writeValueAsString(userTest2_New))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").isNumber())
                .andExpect(jsonPath("id").value(2))
                .andExpect(jsonPath("name").isString())
                .andExpect(jsonPath("name").value("User_Test_2_Name_New"))
                .andExpect(jsonPath("login").isString())
                .andExpect(jsonPath("login").value("User_Test_2_Login_New"))
                .andExpect(jsonPath("email").isString())
                .andExpect(jsonPath("email").value("userTwoNew@ya.ru"))
                .andExpect(jsonPath("birthday").isString())
                .andExpect(jsonPath("birthday").value("2002-07-03"));

        //считываем добавленных пользователей для проверки изменений
        this.mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").isString())
                .andExpect(jsonPath("$[0].name").value("User_Test_1_Name"))
                .andExpect(jsonPath("$[0].login").isString())
                .andExpect(jsonPath("$[0].login").value("User_Test_1_Login"))
                .andExpect(jsonPath("$[0].email").isString())
                .andExpect(jsonPath("$[0].email").value("userOne@ya.ru"))
                .andExpect(jsonPath("$[0].birthday").isString())
                .andExpect(jsonPath("$[0].birthday").value("1990-12-01"))
                .andExpect(jsonPath("$[1].id").isNumber())
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").isString())
                .andExpect(jsonPath("$[1].name").value("User_Test_2_Name_New"))
                .andExpect(jsonPath("$[1].login").isString())
                .andExpect(jsonPath("$[1].login").value("User_Test_2_Login_New"))
                .andExpect(jsonPath("$[1].email").isString())
                .andExpect(jsonPath("$[1].email").value("userTwoNew@ya.ru"))
                .andExpect(jsonPath("$[1].birthday").isString())
                .andExpect(jsonPath("$[1].birthday").value("2002-07-03"));
    }

    //возвращаем пустой список: проверяем статус и тело запроса
    //возвращаем список из 1 фильма
    //возвращаем список из нескольких фильмов
    @Test
    public void getUsers() throws Exception {
        //возвращаем пустой список
        this.mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("[]")));

        //возвращаем список из 1 фильма
        //добавляем фильм
        this.restTemplate.postForEntity("http://localhost:" + port + "/users", userTest1, User.class);
        this.mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").isString())
                .andExpect(jsonPath("$[0].name").value("User_Test_1_Name"))
                .andExpect(jsonPath("$[0].login").isString())
                .andExpect(jsonPath("$[0].login").value("User_Test_1_Login"))
                .andExpect(jsonPath("$[0].email").isString())
                .andExpect(jsonPath("$[0].email").value("userOne@ya.ru"))
                .andExpect(jsonPath("$[0].birthday").isString())
                .andExpect(jsonPath("$[0].birthday").value("1990-12-01"));

        //добавляем 2-й фильм
        this.restTemplate.postForEntity("http://localhost:" + port + "/users", userTest2, User.class);
        this.mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].id").isNumber())
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").isString())
                .andExpect(jsonPath("$[1].name").value("User_Test_2_Login"))
                .andExpect(jsonPath("$[1].login").isString())
                .andExpect(jsonPath("$[1].login").value("User_Test_2_Login"))
                .andExpect(jsonPath("$[1].email").isString())
                .andExpect(jsonPath("$[1].email").value("userTwo@ya.ru"))
                .andExpect(jsonPath("$[1].birthday").isString())
                .andExpect(jsonPath("$[1].birthday").value("2000-08-05"));
    }
}
