package com.charlesdrews.neighborhoodguide;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.charlesdrews.neighborhoodguide.places.Place;
import com.charlesdrews.neighborhoodguide.places.PlaceDbOpenHelper;

public class DetailActivity extends AppCompatActivity {
    private TextView mLocationView;
    private TextView mNeighborhoodView;
    private TextView mDescriptionView;
    private FloatingActionButton mFab;
    private PlaceDbOpenHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLocationView = (TextView) findViewById(R.id.detail_location);
        mNeighborhoodView = (TextView) findViewById(R.id.detail_neighborhood);
        mDescriptionView = (TextView) findViewById(R.id.detail_description);
        mHelper = new PlaceDbOpenHelper(DetailActivity.this);

        int selectedPlaceId = getIntent().getExtras().getInt(MainActivity.SELECTED_PLACE_KEY, -1);
        if (selectedPlaceId == -1) {
            finish(); // can't show detail w/o selected place id
        }
        Place selectedPlace = mHelper.getPlace(selectedPlaceId);

        getSupportActionBar().setTitle(selectedPlace.getTitle());
        mLocationView.setText(selectedPlace.getLocation());
        mNeighborhoodView.setText(selectedPlace.getNeighborhood());
        mDescriptionView.setText(selectedPlace.getDescription());

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        if (selectedPlace.isFavorite()) {
            mFab.setImageResource(R.drawable.ic_favorite_white_24dp); // filled in heart if favorite
        } else {
            mFab.setImageResource(R.drawable.ic_favorite_border_white_24dp); // otherwise just outline
        }

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
}
