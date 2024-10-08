package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Builder
@Data
public class Film {
    private Long id;
    private String name;
    private String description;
    private List<Genre> genres;
    private Mpa mpa;
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private LocalDate releaseDate;
    private int duration;

    public boolean hasName() {
        return name != null && !name.isEmpty();
    }
    public boolean hasDescription() {
        return description != null && !description.isEmpty();
    }
    public boolean hasMpa() {
        return mpa != null;
    }
    public boolean hasReleaseDate() {
        return releaseDate != null;
    }
    public boolean hasDuration() {
        return duration > 0;
    }
}
