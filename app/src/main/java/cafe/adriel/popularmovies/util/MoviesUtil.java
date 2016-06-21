package cafe.adriel.popularmovies.util;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.goebl.david.Webb;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cafe.adriel.popularmovies.R;
import cafe.adriel.popularmovies.model.Movie;
import cafe.adriel.popularmovies.provider.MovieContract;

public class MoviesUtil {
    private static final Webb WEBB = Webb.create();

    private static final String TMDB_API_MOVIES_URL     = "http://api.themoviedb.org/3/movie/%s?api_key=%s&page=%s";
    private static final String TMDB_API_VIDEOS_URL     = "http://api.themoviedb.org/3/movie/%s/videos?api_key=%s";
    private static final String TMDB_API_REVIEWS_URL    = "http://api.themoviedb.org/3/movie/%s/reviews?api_key=%s";
    private static final String TMDB_POSTER_URL         = "https://image.tmdb.org/t/p/w185%s";
    private static final String TMDB_BACKDROP_URL       = "https://image.tmdb.org/t/p/w300%s";

    private static final String TYPE_POPULAR    = "popular";
    private static final String TYPE_TOP_RATED  = "top_rated";
    private static final String TYPE_FAVORITES  = "favorites";

    public static boolean isFavorite(Context context, Movie movie){
        Cursor cursor = context.getContentResolver()
                .query(
                        MovieContract.CONTENT_URI,
                        null,
                        MovieContract.MOVIE_ID + " = ? and " + MovieContract.TYPE + " = ?",
                        new String[]{ movie.getId() + "", TYPE_FAVORITES },
                        null
                );
        boolean isFavorite = cursor.getCount() > 0;
        cursor.close();
        return isFavorite;
    }

    public static boolean toggleFavorite(Context context, Movie movie){
        if(isFavorite(context, movie)) {
            context.getContentResolver()
                    .delete(MovieContract.CONTENT_URI,
                            MovieContract.MOVIE_ID + " = ? and " + MovieContract.TYPE + " = ?",
                            new String[]{ movie.getId() + "", TYPE_FAVORITES });
            return false;
        } else {
            saveMovie(context, TYPE_FAVORITES, movie);
            return true;
        }
    }

    public static void getPopularMovies(Activity activity, MoviesCallback callback){
        getMovies(activity, TYPE_POPULAR, callback);
    }

    public static void getTopRatedMovies(Activity activity, MoviesCallback callback){
        getMovies(activity, TYPE_TOP_RATED, callback);
    }

    public static void getFavoritesMovies(Activity activity, MoviesCallback callback){
        getMovies(activity, TYPE_FAVORITES, callback);
    }

    private static void getMovies(final Activity activity, final String type, final MoviesCallback callback){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if(Util.isConnected(activity, false) && !type.equals(TYPE_FAVORITES)){
                    getMoviesFromApi(activity, type);
                }
                getMoviesFromDb(activity, type, callback);
            }
        });
    }

    private static void getMoviesFromApi(Activity activity, String type){
        String apiUrl = String.format(TMDB_API_MOVIES_URL, type, activity.getString(R.string.tmdb_api_key), 1);
        try {
            JSONArray moviesJson = WEBB.get(apiUrl)
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody()
                    .getJSONArray("results");
            List<Movie> movies = toMovies(moviesJson);
            deleteMovies(activity, type);
            saveMovies(activity, type, movies);
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private static void getMoviesFromDb(Activity activity, String type, final MoviesCallback callback){
        try {
            Cursor cursor = activity.getContentResolver()
                    .query(
                            MovieContract.CONTENT_URI,
                            null,
                            MovieContract.TYPE + " = ?",
                            new String[]{ type },
                            null
                    );
            final List<Movie> movies = toMovies(cursor);
            cursor.close();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.success(movies);
                }
            });
        } catch (final Exception e){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.error(e);
                }
            });
        }
    }

    private static void saveMovie(Context context, String type, Movie movie){
        List<Movie> movies = new ArrayList<>();
        movies.add(movie);
        saveMovies(context, type, movies);
    }

    private static void saveMovies(Context context, String type, List<Movie> movies){
        if(movies != null) {
            ContentValues[] moviesValues = new ContentValues[movies.size()];
            for (int i = 0; i < movies.size(); i++) {
                try {
                    Movie movie = movies.get(i);
                    ContentValues movieValues = new ContentValues();
                    movieValues.put(MovieContract.MOVIE_ID, movie.getId());
                    movieValues.put(MovieContract.TYPE, type);
                    movieValues.put(MovieContract.TITLE, movie.getTitle());
                    movieValues.put(MovieContract.OVERVIEW, movie.getOverview());
                    movieValues.put(MovieContract.POSTER_URL, movie.getPosterUrl());
                    movieValues.put(MovieContract.BACKDROP_URL, movie.getBackdropUrl());
                    movieValues.put(MovieContract.RELEASE_DATE, Util.toDbDate(movie.getReleaseDate()));
                    movieValues.put(MovieContract.RATING, movie.getRating());
                    movieValues.put(MovieContract.ADULT, movie.isAdult() ? 1 : 0);
                    moviesValues[i] = movieValues;
                } catch (Exception ignore){ }
            }
            context.getContentResolver()
                    .bulkInsert(MovieContract.CONTENT_URI, moviesValues);
        }
    }

    private static void deleteMovies(Context context, String type){
        context.getContentResolver()
                .delete(MovieContract.CONTENT_URI,
                        MovieContract.TYPE + " = ?",
                        new String[]{ type });
    }

    private static List<Movie> toMovies(Cursor cursor){
        List<Movie> movies = new ArrayList<>();
        while(cursor.moveToNext()){
            Movie movie = new Movie();
            movie.setId(cursor.getInt(
                    cursor.getColumnIndex(MovieContract.MOVIE_ID)));
            movie.setTitle(cursor.getString(
                    cursor.getColumnIndex(MovieContract.TITLE)));
            movie.setOverview(cursor.getString(
                    cursor.getColumnIndex(MovieContract.OVERVIEW)));
            movie.setPosterUrl(cursor.getString(
                    cursor.getColumnIndex(MovieContract.POSTER_URL)));
            movie.setBackdropUrl(cursor.getString(
                    cursor.getColumnIndex(MovieContract.BACKDROP_URL)));
            movie.setReleaseDate(Util.toDate(cursor.getString(
                    cursor.getColumnIndex(MovieContract.RELEASE_DATE))));
            movie.setRating(cursor.getFloat(
                    cursor.getColumnIndex(MovieContract.RATING)));
            movie.setAdult(cursor.getInt(
                    cursor.getColumnIndex(MovieContract.ADULT)) == 1);
            movies.add(movie);
        }
        return movies;
    }

    private static List<Movie> toMovies(JSONArray jsonMovies){
        List<Movie> movies = new ArrayList<>();
        if(jsonMovies != null) {
            for (int i = 0; i < jsonMovies.length(); i++) {
                try {
                    JSONObject jsonMovie = jsonMovies.getJSONObject(i);
                    Movie movie = new Movie();
                    movie.setId(jsonMovie.getInt("id"));
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