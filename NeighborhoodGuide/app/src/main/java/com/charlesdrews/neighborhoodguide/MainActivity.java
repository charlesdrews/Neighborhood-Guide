package com.charlesdrews.neighborhoodguide;

/**
 * Extend ListBaseActivity & override changeAdapterCursor to use PlaceDbOpenHelper methods that
 * include all places, rather than restrict to only favorite places
 */
public class MainActivity extends ListBaseActivity {

    @Override
    protected void changeAdapterCursor() {
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
            mMenu.findItem(R.id.action_filter).setIcon(R.drawable.filter);
        } else {

            mMenu.findItem(R.id.action_filter).setIcon(R.drawable.filter_outline);
        }
    }
}
