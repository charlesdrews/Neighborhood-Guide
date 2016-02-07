package com.charlesdrews.neighborhoodguide;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.charlesdrews.neighborhoodguide.places.Place;
import com.charlesdrews.neighborhoodguide.places.PlaceDbOpenHelper;

public class DetailActivity extends AppCompatActivity {
    private int mSelectedPlaceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setStatusBarColor(R.color.detailStatusBar);

        mSelectedPlaceId = getIntent().getExtras().getInt(MainActivity.SELECTED_PLACE_KEY, -1);

        if (mSelectedPlaceId >= 0) {

            TextView locationView = (TextView) findViewById(R.id.detail_location);
            TextView neighborhoodView = (TextView) findViewById(R.id.detail_neighborhood);
            TextView descriptionView = (TextView) findViewById(R.id.detail_description);

            final PlaceDbOpenHelper helper = PlaceDbOpenHelper.getInstance(DetailActivity.this);
            final Place selectedPlace = helper.getPlace(mSelectedPlaceId);

            getSupportActionBar().setTitle(selectedPlace.getTitle());

            String locationText = "Location: " + selectedPlace.getLocation();
            locationView.setText(locationText);

            String neighborhoodText = "Neighborhood: " + selectedPlace.getNeighborhood();
            neighborhoodView.setText(neighborhoodText);

            descriptionView.setText(selectedPlace.getDescription());

            final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            setFabFavIcon(fab, selectedPlace.isFavorite());

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean isFavorite = !selectedPlace.isFavorite(); // toggle to opposite value
                    selectedPlace.setFavoriteStatus(isFavorite);
                    helper.setFavoriteStatusById(mSelectedPlaceId, isFavorite);
                    setFabFavIcon(fab, isFavorite);
                    if (isFavorite) {
                        Snackbar.make(view, selectedPlace.getTitle() + " favorited", Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(view, selectedPlace.getTitle() + " unfavorited", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            TextView locationView = (TextView) findViewById(R.id.detail_location);
            locationView.setText(getString(R.string.err_msg_item_not_found));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
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

    private void setStatusBarColor(int colorResource) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = DetailActivity.this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(DetailActivity.this, colorResource));
        }
    }
}
