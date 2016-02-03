package com.charlesdrews.neighborhoodguide;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.charlesdrews.neighborhoodguide.places.PlaceDbOpenHelper;

public class MainActivity extends AppCompatActivity {
    public static final String SELECTED_PLACE_KEY = MainActivity.class.getPackage() + ".selectedPlaceKey";

    private ListView mListView;
    private PlaceDbOpenHelper mHelper;
    private CursorAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_text);

        mListView = (ListView) findViewById(R.id.list_view);
        mHelper = new PlaceDbOpenHelper(MainActivity.this);

        //TODO - remove this db initialization when done testing
        mHelper.initializeDbForTesting();

        Cursor cursor = mHelper.getAllPlaces();

        mAdapter = new CursorAdapter(MainActivity.this, cursor, 0) { // context, cursor, flags
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.place_list_item, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                TextView titleView = (TextView) view.findViewById(R.id.list_place_title);
                titleView.setText(
                        cursor.getString( cursor.getColumnIndex(PlaceDbOpenHelper.COL_TITLE) )
                );

                TextView locationView = (TextView) view.findViewById(R.id.list_place_location);
                locationView.setText(
                        cursor.getString( cursor.getColumnIndex(PlaceDbOpenHelper.COL_LOCATION) )
                );
            }
        };

        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra(SELECTED_PLACE_KEY, (int) id);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(searchMenuItem,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        resetToHomeScreen();
                        return true;
                    }
                });
        /*
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        */
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                resetToSearchScreen();
                return true;

            case R.id.action_favorites:
                resetToFavoritesScreen();
                return true;

            case R.id.action_settings:
                //TODO - remove if no settings in this app
                getSupportActionBar().setTitle(R.string.action_settings);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                return true;

            case android.R.id.home:
                resetToHomeScreen();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void resetToHomeScreen() {
        getSupportActionBar().setTitle(R.string.title_text);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // refresh adapter w/ all places
        mAdapter.swapCursor(mHelper.getAllPlaces());
    }

    private void resetToFavoritesScreen() {
        getSupportActionBar().setTitle(R.string.action_favorites);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // refresh adapter w/ only favorite places
        mAdapter.swapCursor(mHelper.getFavoritePlaces());
    }

    private void resetToSearchScreen() {
        getSupportActionBar().setTitle(R.string.action_search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //TODO implement search function & show results in list view
        Snackbar.make(findViewById(R.id.coordinator_layout_main), "SHOW SEARCH HERE", Snackbar.LENGTH_SHORT);
        mAdapter.swapCursor(mHelper.getAllPlaces());
    }
}
