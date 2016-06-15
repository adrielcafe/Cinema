package cafe.adriel.popularmovies.event;

import cafe.adriel.popularmovies.model.Movie;

public class ShowMovieEvent {
    public final Movie movie;

    public ShowMovieEvent(Movie movie){
        this.movie = movie;
    }
}