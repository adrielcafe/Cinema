package cafe.adriel.popularmovies.util;

import java.util.List;

import cafe.adriel.popularmovies.model.Movie;

public interface MoviesCallback {
    void success(List<Movie> movies);
    void error(Exception error);
}