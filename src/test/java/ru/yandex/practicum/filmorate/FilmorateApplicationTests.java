package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.GenreDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmGenreMapper;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class FilmorateApplicationTests {
    FilmController filmController = new FilmController(new FilmService(new FilmDbStorage(new JdbcTemplate(), new RowMapper<Film>() {
        @Override
        public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
            return null;
        }
    }, new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            return null;
        }
    }), new GenreService(new GenreDbStorage(new JdbcTemplate(), new GenreMapper(), new FilmGenreMapper()))));
    UserController userController = new UserController(new UserService(new UserDbStorage(new JdbcTemplate(), new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            return null;
        }
    })));

    @Test
    public void testGetFilms() throws IOException, InterruptedException {
        Film film = Film.builder()
                .name("Название")
                .description("Описание")
                .releaseDate(LocalDate.now())
                .duration(3)
                .build();
        filmController.create(film);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/films");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }

    @Test
    public void testGetUsers() throws IOException, InterruptedException {
        User user = User.builder()
                .name("Имя")
                .login("Логин")
                .email("Почта@")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        userController.create(user);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/users");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }

    @Test
    public void testCreateUsersWithWrongEmail() throws IOException, InterruptedException {
        User user = User.builder()
                .name("Имя")
                .login("Логин")
                .email(" ")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        assertThrows(ValidationException.class, () -> {
            userController.create(user);
        });
    }

    @Test
    public void testCreateUsersWithWrongLogin() throws IOException, InterruptedException {
        User user = User.builder()
                .name("Имя")
                .login(" ")
                .email("Почта@")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        assertThrows(ValidationException.class, () -> {
            userController.create(user);
        });
    }

    @Test
    public void testCreateUsersWithWrongBirthday() throws IOException, InterruptedException {
        User user = User.builder()
                .name("Имя")
                .login("Логин")
                .email("Почта@")
                .birthday(LocalDate.of(3000, 1, 1))
                .build();

        assertThrows(ValidationException.class, () -> {
            userController.create(user);
        });
    }

    @Test
    public void testCreateFilmWithWrongName() throws IOException, InterruptedException {
        Film film = Film.builder()
                .name(" ")
                .build();

        assertThrows(ConditionsNotMetException.class, () -> {
            filmController.create(film);
        });
    }

    @Test
    public void testCreateFilmWithLongDescription() throws IOException, InterruptedException {
        String descr = "1";
        for (int i = 0; i < 200; i++) {
            descr += "1";
        }

        Film film = Film.builder()
                .name("Название")
                .description(descr)
                .build();

        assertThrows(ValidationException.class, () -> {
            filmController.create(film);
        });
    }

    @Test
    public void testCreateFilmWithWrongReleaseDate() throws IOException, InterruptedException {
        Film film = Film.builder()
                .name("Название")
                .description("Описание")
                .releaseDate(LocalDate.of(1700, 12, 28))
                .build();

        assertThrows(ValidationException.class, () -> {
            filmController.create(film);
        });
    }

    @Test
    public void testCreateFilmWithWrongDuration() throws IOException, InterruptedException {
        Film film = Film.builder()
                .name("Название")
                .description("Описание")
                .releaseDate(LocalDate.of(2010, 12, 28))
                .duration(-100)
                .build();

        assertThrows(ValidationException.class, () -> {
            filmController.create(film);
        });
    }
}
