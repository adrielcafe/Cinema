package cafe.adriel.popularmovies.util;

import org.json.JSONException;

import java.util.List;

import cafe.adriel.popularmovies.model.Movie;

public interface MoviesCallback {
    void success(List<Movie> movies);
    void error(JSONException error);
}