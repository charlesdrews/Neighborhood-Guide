package com.charlesdrews.neighborhoodguide;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.charlesdrews.neighborhoodguide.places.Place;
import com.charlesdrews.neighborhoodguide.places.PlaceDbOpenHelper;

public class DetailActivity extends AppCompatActivity {
    private TextView mLocationView;
    private TextView mNeighborhoodView;
    private TextView mDescriptionView;
    private int mSelectedPlaceId;
    private boolean mGoBackToFavoritesScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLocationView = (TextView) findViewById(R.id.detail_location);
        mNeighborhoodView = (TextView) findViewById(R.id.detail_neighborhood);
        mDescriptionView = (TextView) findViewById(R.id.detail_description);
        final PlaceDbOpenHelper helper = new PlaceDbOpenHelper(DetailActivity.this);

        mSelectedPlaceId = getIntent().getExtras().getInt(MainActivity.SELECTED_PLACE_KEY, -1);
        if (mSelectedPlaceId == -1) {
            finish(); // can't show detail w/o selected place id
        }
        final Place selectedPlace = helper.getPlace(mSelectedPlaceId);
        mGoBackToFavoritesScreen = getIntent().getExtras().getBoolean(MainActivity.FROM_FAVORITES_KEY);

        getSupportActionBar().setTitle(selectedPlace.getTitle());
        mLocationView.setText("Location: " + selectedPlace.getLocation());
        mNeighborhoodView.setText("Neighborhood: " + selectedPlace.getNeighborhood());
        mDescriptionView.setText(selectedPlace.getDescription());

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        setFabFavIcon(fab, selectedPlace.isFavorite());

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isFavorite = !selectedPlace.isFavorite(); // toggle to opposite value
                selectedPlace.setFavoriteStatus(isFavorite);
                helper.setFavoriteStatus(mSelectedPlaceId, isFavorite);
                setFabFavIcon(fab, isFavorite);
                if (isFavorite) {
                    mGoBackToFavoritesScreen = true;
                    Snackbar.make(view, selectedPlace.getTitle() + " favorited", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(view, selectedPlace.getTitle() + " unfavorited", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mGoBackToFavoritesScreen) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }
        super.onBackPressed();
    }

    private void setFabFavIcon(FloatingActionButton fab, boolean isFavorite) {
        if (isFavorite) {
            fab.setImageResource(R.drawable.ic_favorite_white_24dp); // filled in heart if favorite
        } else {
            fab.setImageResource(R.drawable.ic_favorite_border_white_24dp); // otherwise just outline
        }
    }
}
