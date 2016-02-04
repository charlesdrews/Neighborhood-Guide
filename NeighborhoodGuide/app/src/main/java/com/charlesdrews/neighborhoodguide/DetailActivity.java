package com.charlesdrews.neighborhoodguide;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.charlesdrews.neighborhoodguide.places.Place;
import com.charlesdrews.neighborhoodguide.places.PlaceDbOpenHelper;

public class DetailActivity extends AppCompatActivity {
    private int mSelectedPlaceId;
    private boolean mGoBackToFavoritesScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView mLocationView = (TextView) findViewById(R.id.detail_location);
        TextView mNeighborhoodView = (TextView) findViewById(R.id.detail_neighborhood);
        TextView mDescriptionView = (TextView) findViewById(R.id.detail_description);
        final PlaceDbOpenHelper helper = PlaceDbOpenHelper.getInstance(DetailActivity.this);

        mSelectedPlaceId = getIntent().getExtras().getInt(MainActivity.SELECTED_PLACE_KEY, -1);
        if (mSelectedPlaceId == -1) {
            //TODO - don't finish, instead populate text views w/ an error message
            finish(); // can't show detail w/o selected place id
        }
        final Place selectedPlace = helper.getPlace(mSelectedPlaceId);
        mGoBackToFavoritesScreen = getIntent().getExtras().getBoolean(MainActivity.FROM_FAVORITES_KEY);

        getSupportActionBar().setTitle(selectedPlace.getTitle());
        //TODO - don't concatenate these
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
        setFavsResult();
        super.onBackPressed();
        //finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setFavsResult();
                //NavUtils.navigateUpFromSameTask(this); // this causes MainActivity.onCreate to run; don't want that
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setFabFavIcon(FloatingActionButton fab, boolean isFavorite) {
        if (isFavorite) {
            fab.setImageResource(R.drawable.ic_favorite_white_24dp); // filled in heart if favorite
        } else {
            fab.setImageResource(R.drawable.ic_favorite_border_white_24dp); // otherwise just outline
        }
    }

    private void setFavsResult() {
        if (mGoBackToFavoritesScreen) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }
    }
}
