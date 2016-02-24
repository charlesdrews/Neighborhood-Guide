package com.charlesdrews.neighborhoodguide;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
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

/**
 * Parent class for MainActivity and FavoritesActivity - implements the functionality that is
 * common to both child activities, and implements some differences using "instanceof"
 * Created by charlie on 2/10/16.
 */
public abstract class ListBaseActivity extends AppCompatActivity {
    public static final String SELECTED_PLACE_KEY = "selected_place_key";
    public static final String CATEGORY_FILTER_VALUE_KEY = "category_filter_value_key";
    public static final String SEARCH_QUERY_KEY = "search_query_key";

    protected Menu mMenu;
    protected PlaceDbOpenHelper mHelper;
    protected RecyclerCursorAdapter mAdapter;
    protected String mCategoryFilterValue;
    protected String mUserQuery;

    private SearchView mSearchView;
    private Cursor mCursor;
    private RecyclerView mRecyclerView;
    private boolean mMenuLoading = true;
    private ArrayList<String> mCategories;
    private Spinner mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set values that differ between Main and Favorites activities
        boolean isFavoritesActivity;
        String titleText;
        int statusBarColorRes;

        if (ListBaseActivity.this instanceof MainActivity) {
            isFavoritesActivity = false;
            setContentView(R.layout.activity_main);
            titleText = getString(R.string.title_text_main);
            statusBarColorRes = R.color.mainStatusBar;
        } else { // if not MainActivity, then its FavoritesActivity
            isFavoritesActivity = true;
            setContentView(R.layout.activity_favorites);
            titleText = getString(R.string.title_text_favs);
            statusBarColorRes = R.color.favsStatusBar;
        }

        // set up recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ListBaseActivity.this);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        // update mCursor and set adapter on worker thread
        GetCursorAndSetAdapterAsyncTask task = new GetCursorAndSetAdapterAsyncTask();
        task.execute(isFavoritesActivity);

        // set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(titleText);
            getSupportActionBar().setDisplayHomeAsUpEnabled(isFavoritesActivity);
        }
        setStatusBarColor(statusBarColorRes);

        // retrieve saved instance state values (if any)
        if (savedInstanceState != null) {
            mCategoryFilterValue = savedInstanceState.getString(CATEGORY_FILTER_VALUE_KEY); // might return null
            mUserQuery = savedInstanceState.getString(SEARCH_QUERY_KEY); // might return null
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the appropriate menu for Main or Favorites activity
        if (ListBaseActivity.this instanceof MainActivity) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_favs, menu);
        }

        mMenu = menu;
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
                mSearchView.clearFocus(); // close the soft keyboard
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

        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mUserQuery = null;
                changeAdapterCursor();
                return true;
            }
        });

        return true;
    }

    /**
     * On device rotation, need to update the SearchView to contain the same user input it had
     * prior to rotation. Menu must be inflated before this can be done, so do this here.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // once menu is set up, incorporate pre-existing filter & query values if present
        if (mUserQuery != null) {
            MenuItemCompat.expandActionView(mMenu.findItem(R.id.action_search));
            mSearchView.setQuery(mUserQuery, false);
            mSearchView.clearFocus(); // close the soft keyboard
            changeAdapterCursor();
        } else if (mCategoryFilterValue != null) {
            changeAdapterCursor();
        }

        mMenuLoading = false;
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                launchFilterDialog();
                return true;

            case R.id.action_favorites:
                Intent intent = new Intent(ListBaseActivity.this, FavoritesActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_refresh:
                changeAdapterCursor();
                Snackbar.make(
                        findViewById(R.id.coordinator_layout),
                        "Favorites refreshed",
                        Snackbar.LENGTH_SHORT
                ).show();
                return true;

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(ListBaseActivity.this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Save filter value and/or user search input if either is present so they can be
     * persisted after a device rotation
     */
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

    /**
     * When returning from the detail activity, refresh the cursor if a change was made to a
     * place's favorite status, otherwise no need to update the cursor
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) { // indicates user changed place's fav status on detail screen
            changeAdapterCursor();
        }
    }

    private void setStatusBarColor(int colorResource) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = ListBaseActivity.this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(ListBaseActivity.this, colorResource));
        }
    }

    /**
     * Use an alert dialog to gather user input on which place category filter to apply, or whether
     * to clear the filter.
     */
    private void launchFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ListBaseActivity.this);
        builder.setTitle("Filter by category");

        mSpinner = new Spinner(ListBaseActivity.this);

        // get list of categories & set up spinner & adapter on worker thread
        GetCategoriesAsyncTask task = new GetCategoriesAsyncTask();
        task.execute();

        RelativeLayout relativeLayout = new RelativeLayout(ListBaseActivity.this);
        relativeLayout.setPadding(0, 40, 0, 0); // left, top, right, bottom
        relativeLayout.setGravity(Gravity.CENTER);
        relativeLayout.addView(mSpinner);
        builder.setView(relativeLayout);

        builder.setPositiveButton("Set Filter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCategoryFilterValue = mSpinner.getSelectedItem().toString();
                if (mCategoryFilterValue.equals("All")) {
                    mCategoryFilterValue = null;
                }
                changeAdapterCursor();
            }
        });

        builder.setNegativeButton("Clear Filter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCategoryFilterValue = null;
                changeAdapterCursor();
            }
        });

        builder.show();
    }

    /**
     * Require each child class to implement this method - main activity should update the cursor
     * to include all places, not just favorites while favorites activity should limit cursor to
     * include only favorites
     */
    protected abstract void changeAdapterCursor();


    private class GetCursorAndSetAdapterAsyncTask extends AsyncTask<Boolean, Void, Void> {

        @Override
        protected Void doInBackground(Boolean... params) {
            // set up DB Asset Helper & Open Helper
            PlaceDbAssetHelper dbAssetHelper = new PlaceDbAssetHelper(ListBaseActivity.this);
            dbAssetHelper.getReadableDatabase();
            mHelper = PlaceDbOpenHelper.getInstance(ListBaseActivity.this);

            // params[0] = true if this is instance of FavoritesActivity, else false
            if (params[0]) {
                mCursor = mHelper.getFavoritePlaces();
            } else {
                mCursor = mHelper.getAllPlaces();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mAdapter = new RecyclerCursorAdapter(ListBaseActivity.this, mCursor);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    private class GetCategoriesAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mCategories = mHelper.getCategories();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    ListBaseActivity.this,
                    android.R.layout.simple_spinner_item,
                    mCategories
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinner.setAdapter(adapter);

            if (mCategoryFilterValue != null) {
                mSpinner.setSelection(mCategories.indexOf(mCategoryFilterValue));
            }

        }
    }
}
