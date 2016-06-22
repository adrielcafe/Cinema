package cafe.adriel.popularmovies.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.rohit.recycleritemclicksupport.RecyclerItemClickSupport;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cafe.adriel.popularmovies.R;
import cafe.adriel.popularmovies.callback.MoviesCallback;
import cafe.adriel.popularmovies.event.ShowMovieEvent;
import cafe.adriel.popularmovies.event.UpdateFavoritesEvent;
import cafe.adriel.popularmovies.model.Movie;
import cafe.adriel.popularmovies.ui.adapter.MoviesAdapter;
import cafe.adriel.popularmovies.util.MoviesUtil;
import icepick.Icepick;
import icepick.State;

public class MoviesFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, RecyclerItemClickSupport.OnItemClickListener {
    private static final String ARG_FRAG_TYPE = "fragType";

    public enum Type {
        POPULAR,
        TOP_RATED,
        FAVORITES
    }

    @State
    Type fragType;
    List<Movie> movies;

    @BindView(R.id.refresh)
    SwipeRefreshLayout refreshView;
    @BindView(R.id.movies)
    RecyclerView moviesView;

    public static MoviesFragment newInstance(Type fragType) {
        MoviesFragment fragment = new MoviesFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FRAG_TYPE, fragType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
        if(fragType == null && getArguments() != null){
            fragType = (Type) getArguments().getSerializable(ARG_FRAG_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        init();
        return rootView;
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
    public void onRefresh() {
        movies = null;
        updateMovies();
    }

    @Override
    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
        showMovieAtPosition(position);
    }

    @Subscribe(sticky = true)
    public void onUpdateFavoritesEvent(UpdateFavoritesEvent event){
        if(fragType == Type.FAVORITES){
            EventBus.getDefault().removeStickyEvent(UpdateFavoritesEvent.class);
            onRefresh();
        }
    }

    @Override
    protected void init(){
        RecyclerItemClickSupport.addTo(moviesView)
                .setOnItemClickListener(this);
        moviesView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        moviesView.setHasFixedSize(true);
        refreshView.setOnRefreshListener(this);
        updateMovies();
    }

    private void updateMovies(){
        if(movies == null) {
            MoviesCallback callback = new MoviesCallback() {
                @Override
                public void success(List<Movie> result) {
                    movies = result;
                    moviesView.setAdapter(new MoviesAdapter(getContext(), movies));
                    refreshView.setRefreshing(false);
                }
                @Override
                public void error(Exception error) {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                    refreshView.setRefreshing(false);
                }
            };
            switch (fragType) {
                case POPULAR:
                    MoviesUtil.getPopularMovies(getActivity(), callback);
                    break;
                case TOP_RATED:
                    MoviesUtil.getTopRatedMovies(getActivity(), callback);
                    break;
                case FAVORITES:
                    MoviesUtil.getFavoritesMovies(getActivity(), callback);
                    break;
            }
        } else {
            moviesView.setAdapter(new MoviesAdapter(getContext(), movies));
            refreshView.setRefreshing(false);
        }
    }

    private void showMovieAtPosition(int position){
        Movie movie = movies.get(position);
        startActivity(new Intent(getContext(), MovieActivity.class));
        EventBus.getDefault().postSticky(new ShowMovieEvent(movie));
    }
}