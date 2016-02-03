package com.charlesdrews.neighborhoodguide;

import android.app.SearchManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_text);

        mListView = (ListView) findViewById(R.id.list_view);
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
                //TODO - make this a separate method
                getSupportActionBar().setTitle(R.string.action_search);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                //TODO implement search function
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
        Snackbar.make(
                findViewById(R.id.coordinator_layout_main),
                "home/up button clicked",
                Snackbar.LENGTH_SHORT
        ).show();
        //TODO - update listview to be home screen again (not favorites or search results)
        // or maybe favorites should be the home screen????
    }

    private void resetToFavoritesScreen() {
        getSupportActionBar().setTitle(R.string.action_favorites);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //TODO - update listview to show favorites
    }
}
