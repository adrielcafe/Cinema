package cafe.adriel.popularmovies.util;

import java.util.List;

import cafe.adriel.popularmovies.model.Review;

public interface ReviewsCallback {

    void success(List<Review> reviews);

    void error(Exception error);

}