package cafe.adriel.popularmovies.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import cafe.adriel.popularmovies.R;
import cafe.adriel.popularmovies.event.ShowMovieEvent;
import cafe.adriel.popularmovies.model.Movie;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe(sticky = true)
    public void showMovieEvent(ShowMovieEvent event){
        EventBus.getDefault().removeStickyEvent(event);
        movie = event.movie;
        init();
    }

    @Override
    protected void init() {
        setTitle(movie.getTitle());
        Glide.with(this)
                .load(movie.getBackdropUrl())
                .into(backdropView);
        releaseDateView.setText(Util.prettyDate(this, movie.getReleaseDate()));
        ratingView.setText(movie.getRating()+"");
        overviewView.setText(movie.getOverview());
    }
}