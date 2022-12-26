package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.validator.DateBefore;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@AllArgsConstructor
//генерирует @Getter,@Setter,@ToString,@EqualsAndHashCode,@RequiredArgsConstructor
public class Film {
    private int id; //целочисленный идентификатор

    @NotBlank
    private String name; //название

    @Size(max = 200)
    private String description; //описание

    @DateBefore
    private LocalDate releaseDate; //дата релиза

    @Positive
    private int duration; //продолжительность фильма

    @PositiveOrZero
    private int rate; //рейтинг фильма
}
