package com.charlesdrews.neighborhoodguide;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
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
    public static final String SELECTED_PLACE_KEY = MainActivity.class.getCanonicalName() + ".selectedPlaceKey";
    public static final String FROM_FAVORITES_KEY = MainActivity.class.getCanonicalName() + ".fromFavoritesKey";
    public static final int REQUEST_CODE = 0;

    private PlaceDbOpenHelper mHelper;
    private CursorAdapter mAdapter;
    private boolean mStartDetailFromFavs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_text);

        mStartDetailFromFavs = false;
        ListView listView = (ListView) findViewById(R.id.list_view);
        mHelper = new PlaceDbOpenHelper(MainActivity.this); //TODO - make db helper a singleton

        //TODO - remove this db initialization when done testing
        mHelper.initializeDbForTesting(MainActivity.this);

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

        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra(SELECTED_PLACE_KEY, (int) id);
                intent.putExtra(FROM_FAVORITES_KEY, mStartDetailFromFavs);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        //TODO - add an onItemLongClick to launch dialog asking if user wants to fav/unfav the item (as appropriate)

        // handle search action
        //handleIntent(getIntent());
    }

    /*
    @Override
    protected void onNewIntent(Intent intent) {
        //super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Snackbar.make(findViewById(R.id.coordinator_layout_main), "Search for " + query, Snackbar.LENGTH_SHORT).show();

            //TODO - perform actual search
            mAdapter.swapCursor(mHelper.searchPlaces(query));
        }
    }
    */

    /*
    @Override
    public void onBackPressed() {
        //TODO - check if not on "home" screen (if on favs or search) and if so, just go back to home
        // otherwise call super and potentially exit the app
        super.onBackPressed();
    }
    */

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // RESULT_OK indicates either the user went from favorites to detail and needs to return to favorites,
            // or user favorited a place and needs to go to favorites
            resetToFavoritesScreen();
        } else {
            // RESULT_CANCELED indicates user did not go to detail from favorites, and did not favorite a place
            resetToHomeScreen();
            //TODO - have this go back to prior search results instead of home
        }
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        /*
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(searchMenuItem,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        //resetToHomeScreen();
                        return true;
                    }
                });
                */
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                updateCursorWithSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                updateCursorWithSearch(newText);
                return true;
            }
        });
        //return super.onCreateOptionsMenu(menu);
        return true;
    }

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                //resetToSearchScreen(); //TODO - if handleIntent handles this, maybe delete this case
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
*/

    private void resetToHomeScreen() {
        getSupportActionBar().setTitle(R.string.title_text);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mStartDetailFromFavs = false;
        mAdapter.swapCursor(mHelper.getAllPlaces()); // refresh adapter w/ all places
    }

    private void resetToFavoritesScreen() {
        getSupportActionBar().setTitle(R.string.action_favorites);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mStartDetailFromFavs = true;
        mAdapter.swapCursor(mHelper.getFavoritePlaces()); // refresh adapter w/ only favorite places
    }

    private void resetToSearchScreen() {
        getSupportActionBar().setTitle(R.string.action_search);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mStartDetailFromFavs = false;

        //TODO implement search function & show results in list view
        /*
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(MainActivity.this, "SEARCHING", Toast.LENGTH_SHORT).show();
            Snackbar.make(findViewById(R.id.coordinator_layout_main), "Search for " + query, Snackbar.LENGTH_SHORT);
            //TODO - perform actual search
            //mAdapter.swapCursor(mHelper.getAllPlaces());
        }
        */
    }

    public void updateCursorWithSearch(String query) {
        mAdapter.swapCursor(mHelper.searchPlaces(query));
    }
}
