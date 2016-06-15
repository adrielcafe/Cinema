package cafe.adriel.popularmovies.util;

import android.app.Activity;
import android.os.AsyncTask;

import com.goebl.david.Webb;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cafe.adriel.popularmovies.R;
import cafe.adriel.popularmovies.model.Movie;

public class MoviesUtil {
    private static final Webb WEBB                  = Webb.create();
    private static final String TMDB_API_URL        = "http://api.themoviedb.org/3/movie/%s?api_key=%s&page=%s";
    private static final String TMDB_POSTER_URL     = "https://image.tmdb.org/t/p/w185%s";
    private static final String TMDB_BACKDROP_URL   = "https://image.tmdb.org/t/p/w300%s";
    private static final String TMDB_TYPE_POPULAR   = "popular";
    private static final String TMDB_TYPE_TOP_RATED = "top_rated";

    public static void getPopularMovies(final Activity activity, final MoviesCallback callback){
        getMovies(activity, TMDB_TYPE_POPULAR, callback);
    }

    public static void getTopRatedMovies(final Activity activity, final MoviesCallback callback){
        getMovies(activity, TMDB_TYPE_TOP_RATED, callback);
    }

    private static void getMovies(final Activity activity, final String type, final MoviesCallback callback){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                String apiUrl = String.format(TMDB_API_URL, type, activity.getString(R.string.tmdb_api_key), 1);
                try {
                    JSONArray jsonMovies= WEBB.get(apiUrl)
                            .ensureSuccess()
                            .asJsonObject()
                            .getBody()
                            .getJSONArray("results");
                    final List<Movie> movies = toMovies(jsonMovies);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.success(movies);
                        }
                    });
                } catch (final JSONException e){
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.error(e);
                        }
                    });
                }
            }
        });
    }

    private static List<Movie> toMovies(JSONArray jsonMovies){
        List<Movie> movies = new ArrayList<>();
        if(jsonMovies != null) {
            for (int i = 0; i < jsonMovies.length(); i++) {
                try {
                    JSONObject jsonMovie = jsonMovies.getJSONObject(i);
                    Movie movie = new Movie();
                    movie.setTitle(jsonMovie.getString("title"));
                    movie.setOverview(jsonMovie.getString("overview"));
                    movie.setPosterUrl(String.format(TMDB_POSTER_URL, jsonMovie.getString("poster_path")));
                    movie.setBackdropUrl(String.format(TMDB_BACKDROP_URL, jsonMovie.getString("backdrop_path")));
                    movie.setReleaseDate(Util.toDate(jsonMovie.getString("release_date")));
                    movie.setRating((float) jsonMovie.getDouble("vote_average"));
                    movie.setAdult(jsonMovie.getBoolean("adult"));
                    movies.add(movie);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return movies;
    }

}