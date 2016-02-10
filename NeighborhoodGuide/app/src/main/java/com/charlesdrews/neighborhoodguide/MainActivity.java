package com.charlesdrews.neighborhoodguide;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.charlesdrews.neighborhoodguide.places.PlaceDbAssetHelper;
import com.charlesdrews.neighborhoodguide.places.PlaceDbOpenHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final String SELECTED_PLACE_KEY = "selected_place_key";
    public static final String CATEGORY_FILTER_VALUE_KEY = "category_filter_value_key";
    public static final String SEARCH_QUERY_KEY = "search_query_key";

    private Menu mMenu;
    private PlaceDbOpenHelper mHelper;
    private RecyclerCursorAdapter mAdapter;
    private String mCategoryFilterValue;
    private String mUserQuery;
    private boolean mMenuLoading = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_text);
        }
        setStatusBarColor(R.color.mainStatusBar);

        // set up DB Asset Helper
        PlaceDbAssetHelper dbAssetHelper = new PlaceDbAssetHelper(MainActivity.this);
        dbAssetHelper.getReadableDatabase();

        // set up DB Open Helper
        mHelper = PlaceDbOpenHelper.getInstance(MainActivity.this);
        final Cursor cursor = mHelper.getAllPlaces();

        // set up recycler view
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view_main);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new RecyclerCursorAdapter(MainActivity.this, cursor);
        recyclerView.setAdapter(mAdapter);

        // retrieve saved instance state values (if any)
        if (savedInstanceState != null) {
            mCategoryFilterValue = savedInstanceState.getString(CATEGORY_FILTER_VALUE_KEY); // might return null
            mUserQuery = savedInstanceState.getString(SEARCH_QUERY_KEY); // might return null
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search_main).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!mMenuLoading) {
                    if (query.isEmpty()) {
                        mUserQuery = null;
                    } else {
                        mUserQuery = query;
                    }
                }
                changeAdapterCursor();
                searchView.clearFocus(); // close the soft keyboard
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!mMenuLoading) {
                    if (newText.isEmpty()) {
                        mUserQuery = null;
                    } else {
                        mUserQuery = newText;
                    }
                }
                changeAdapterCursor();
                return true;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mUserQuery = null;
                changeAdapterCursor();
                return true;
            }
        });

        // once menu is set up, incorporate pre-existing filter & query values if present
        if (mUserQuery != null) {
            MenuItemCompat.expandActionView(mMenu.findItem(R.id.action_search_main));
            searchView.setQuery(mUserQuery, false);
            searchView.clearFocus(); // close the soft keyboard
            changeAdapterCursor();
        } else if (mCategoryFilterValue != null) {
            changeAdapterCursor();
        }

        mMenuLoading = false;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter_main:
                launchFilterDialog();
                return true;

            case R.id.action_favorites_main:
                Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCategoryFilterValue != null) {
            outState.putString(CATEGORY_FILTER_VALUE_KEY, mCategoryFilterValue);
        }
        if (mUserQuery != null) {
            outState.putString(SEARCH_QUERY_KEY, mUserQuery);
        }
    }

    @Override
    protected void onDestroy() {
        Cursor cursor = mAdapter.getCursor();
        cursor.close();
        super.onDestroy();
    }

    private void setStatusBarColor(int colorResource) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = MainActivity.this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(MainActivity.this, colorResource));
        }
    }

    private void launchFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Filter by category");

        final Spinner spinner = new Spinner(MainActivity.this);
        ArrayList<String> categories = mHelper.getCategories();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                MainActivity.this,
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if (mCategoryFilterValue != null) {
            spinner.setSelection(categories.indexOf(mCategoryFilterValue));
        }

        RelativeLayout relativeLayout = new RelativeLayout(MainActivity.this);
        relativeLayout.setPadding(0, 40, 0, 0); // left, top, right, bottom
        relativeLayout.setGravity(Gravity.CENTER);
        relativeLayout.addView(spinner);
        builder.setView(relativeLayout);

        builder.setPositiveButton("Set Filter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCategoryFilterValue = spinner.getSelectedItem().toString();
                if (mCategoryFilterValue.equals("All")) {
                    mCategoryFilterValue = null;
                }
                changeAdapterCursor();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Clear Filter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCategoryFilterValue = null;
                changeAdapterCursor();
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void changeAdapterCursor() {
        if (mCategoryFilterValue != null && mUserQuery != null) {

            mAdapter.changeCursor(mHelper.searchAllPlacesByCategory(mUserQuery, mCategoryFilterValue));

        } else if (mCategoryFilterValue != null) { // && mUserQuery == null

            mAdapter.changeCursor(mHelper.getAllPlacesByCategory(mCategoryFilterValue));

        } else if (mUserQuery != null) { // && mCategoryFilterValue == null

            mAdapter.changeCursor(mHelper.searchAllPlaces(mUserQuery));

        } else { // mCategoryFilterValue == null && mUserQuery == null

            mAdapter.changeCursor(mHelper.getAllPlaces());

        }

        if (mCategoryFilterValue != null && !mCategoryFilterValue.equals("All")) {
            mMenu.findItem(R.id.action_filter_main).setIcon(R.drawable.ic_filter_list_cyan_a200_18dp);
        } else {

            mMenu.findItem(R.id.action_filter_main).setIcon(R.drawable.ic_filter_list_white_18dp);
        }
    }
}
