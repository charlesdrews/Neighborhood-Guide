package com.charlesdrews.neighborhoodguide;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItem;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.charlesdrews.neighborhoodguide.places.PlaceDbOpenHelper;

public class MainActivity extends AppCompatActivity {
    public static final String SELECTED_PLACE_KEY = MainActivity.class.getCanonicalName() + ".selectedPlaceKey";
    public static final String FROM_FAVORITES_KEY = MainActivity.class.getCanonicalName() + ".fromFavoritesKey";
    public static final int REQUEST_CODE = 0;

    private MenuItem mMenuFavItem;
    private PlaceDbOpenHelper mHelper;
    private RecyclerCursorAdapter mAdapter;
    private SearchView mSearchView;
    private boolean mOnFavsScreen;
    private boolean mStartDetailFromFavs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_text);

        mOnFavsScreen = false;
        mStartDetailFromFavs = false;

        mHelper = PlaceDbOpenHelper.getInstance(MainActivity.this);
        final Cursor cursor = mHelper.getAllPlaces();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new RecyclerCursorAdapter(MainActivity.this, cursor);
        recyclerView.setAdapter(mAdapter);

        //TODO - add an onItemLongClick to launch dialog asking if user wants to fav/unfav the item (as appropriate)
        // if item not yet a favorite, title="Add to favorites?" button icons = heart & X
        // if item already a favorite, title="Remove from favorites?" button icons = trashcan & X
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenuFavItem = menu.findItem(R.id.action_favorites);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mOnFavsScreen) {
            resetToHomeScreen();
        } else {
            super.onBackPressed();
        }
    }

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
            if (!mSearchView.getQuery().toString().isEmpty()) {
                updateCursorWithSearch(mSearchView.getQuery().toString());
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorites:
                resetToFavoritesScreen();
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

        mMenuFavItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        mOnFavsScreen = false;
        mStartDetailFromFavs = false;

        mAdapter.changeCursor(mHelper.getAllPlaces()); // refresh adapter w/ all places
    }

    private void resetToFavoritesScreen() {
        getSupportActionBar().setTitle(R.string.action_favorites);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMenuFavItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        mOnFavsScreen = true;
        mStartDetailFromFavs = true;

        mAdapter.changeCursor(mHelper.getFavoritePlaces()); // refresh adapter w/ only favorite places
    }

    public void updateCursorWithSearch(String query) {
        mAdapter.changeCursor(mHelper.searchPlaces(query));
    }
}
