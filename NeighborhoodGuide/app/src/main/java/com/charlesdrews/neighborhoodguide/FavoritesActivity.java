package com.charlesdrews.neighborhoodguide;

/**
 * Extend ListBaseActivity & override changeAdapterCursor to use PlaceDbOpenHelper methods that
 * restrict queries to only favorite places, rather than all places
 */
public class FavoritesActivity extends ListBaseActivity {

    /**
     * Use the database open helper methods with "AllPlaces" in the name in order to restrict
     * query results to favorites only
     */
    @Override
    protected void changeAdapterCursor() {
        if (mCategoryFilterValue != null && mUserQuery != null) {

            mAdapter.changeCursor(mHelper.searchFavoritePlacesByCategory(mUserQuery, mCategoryFilterValue));

        } else if (mCategoryFilterValue != null) { // && mUserQuery == null

            mAdapter.changeCursor(mHelper.getFavoritePlacesByCategory(mCategoryFilterValue));

        } else if (mUserQuery != null) { // && mCategoryFilterValue == null

            mAdapter.changeCursor(mHelper.searchFavoritePlaces(mUserQuery));

        } else { // mCategoryFilterValue == null && mUserQuery == null

            mAdapter.changeCursor(mHelper.getFavoritePlaces());

        }

        if (mCategoryFilterValue != null && !mCategoryFilterValue.equals("All")) {
            mMenu.findItem(R.id.action_filter).setIcon(R.drawable.filter);
        } else {
            mMenu.findItem(R.id.action_filter).setIcon(R.drawable.filter_outline);
        }
    }
}
