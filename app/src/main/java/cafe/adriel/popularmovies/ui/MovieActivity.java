package cafe.adriel.popularmovies.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cafe.adriel.popularmovies.R;
import cafe.adriel.popularmovies.callback.ReviewsCallback;
import cafe.adriel.popularmovies.event.ShowMovieEvent;
import cafe.adriel.popularmovies.event.UpdateFavoritesEvent;
import cafe.adriel.popularmovies.model.Movie;
import cafe.adriel.popularmovies.model.Review;
import cafe.adriel.popularmovies.ui.adapter.ReviewsAdapter;
import cafe.adriel.popularmovies.util.MoviesUtil;
import cafe.adriel.popularmovies.util.Util;
import icepick.State;

public class MovieActivity extends BaseActivity {

    @State
    Movie movie;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.backdrop)
    ImageView backdropView;
    @BindView(R.id.release_date)
    TextView releaseDateView;
    @BindView(R.id.rating)
    TextView ratingView;
    @BindView(R.id.overview)
    TextView overviewView;
    @BindView(R.id.reviews)
    RecyclerView reviewsView;
    @BindView(R.id.favorite)
    FloatingActionButton favoriteView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");

        if(movie != null){
            init();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.movie, menu);
        menu.findItem(R.id.share)
                .setIcon(new IconicsDrawable(this)
                        .icon(GoogleMaterial.Icon.gmd_share)
                        .color(Color.BLACK)
                        .sizeDp(24));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.share:
                shareMovie();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe(sticky = true)
    public void onShowMovieEvent(ShowMovieEvent event){
        EventBus.getDefault().removeStickyEvent(event);
        movie = event.movie;
        init();
    }

    @OnClick(R.id.trailer)
    public void playTrailer(){
        Util.openLinkInExternalApp(this, movie.getTrailerUrl());
    }

    @OnClick(R.id.favorite)
    public void toggleFavorite(){
        boolean isFavorite = MoviesUtil.toggleFavorite(this, movie);
        updateFavoriteFab(isFavorite);
        EventBus.getDefault().postSticky(new UpdateFavoritesEvent());
    }

    @Override
    protected void init() {
        setTitle(movie.getTitle());
        Glide.with(this)
                .load(movie.getBackdropUrl())
                .into(backdropView);
        reviewsView.setLayoutManager(new LinearLayoutManager(this));
        reviewsView.setHasFixedSize(false);
        releaseDateView.setText(Util.toPrettyDate(movie.getReleaseDate()));
        ratingView.setText(movie.getRating()+"");
        overviewView.setText(movie.getOverview());
        updateFavoriteFab(MoviesUtil.isFavorite(this, movie));
        loadReviews();
    }

    private void updateFavoriteFab(boolean isFavorite){
        GoogleMaterial.Icon favoriteIcon = isFavorite ?
                GoogleMaterial.Icon.gmd_favorite : GoogleMaterial.Icon.gmd_favorite_border;
        favoriteView.setImageDrawable(new IconicsDrawable(this)
                .icon(favoriteIcon)
                .color(Color.WHITE)
                .sizeDp(48));
    }

    private void shareMovie() {
        String text = String.format("%s\n%s", movie.getTitle(), movie.getTrailerUrl());
        Util.shareText(this, text);
    }

    private void loadReviews() {
        MoviesUtil.getReviewsFromApi(this, movie, new ReviewsCallback() {
            @Override
            public void success(List<Review> reviews) {
                reviewsView.setAdapter(new ReviewsAdapter(MovieActivity.this, reviews));
            }
            @Override
            public void error(Exception error) {
                error.printStackTrace();
            }
        });
    }

}