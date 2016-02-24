package com.charlesdrews.neighborhoodguide;

import android.database.Cursor;
import android.os.AsyncTask;

/**
 * Extend ListBaseActivity & override changeAdapterCursor to use PlaceDbOpenHelper methods that
 * include all places, rather than restrict to only favorite places
 */
public class MainActivity extends ListBaseActivity {

    /**
     * Use the database open helper methods with "AllPlaces" in the name in order to NOT restrict
     * query results to favorites only
     */
    @Override
    protected void changeAdapterCursor() {
        ChangeCursorAsyncTask task = new ChangeCursorAsyncTask();
        task.execute();

        if (mCategoryFilterValue != null && !mCategoryFilterValue.equals("All")) {
            mMenu.findItem(R.id.action_filter).setIcon(R.drawable.filter);
        } else {
            mMenu.findItem(R.id.action_filter).setIcon(R.drawable.filter_outline);
        }
    }

    private class ChangeCursorAsyncTask extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Void... params) {
            if (mCategoryFilterValue != null && mUserQuery != null) {

                return mHelper.searchAllPlacesByCategory(mUserQuery, mCategoryFilterValue);

            } else if (mCategoryFilterValue != null) { // && mUserQuery == null

                return mHelper.getAllPlacesByCategory(mCategoryFilterValue);

            } else if (mUserQuery != null) { // && mCategoryFilterValue == null

                return mHelper.searchAllPlaces(mUserQuery);

            } else { // mCategoryFilterValue == null && mUserQuery == null

                return mHelper.getAllPlaces();
            }
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            mAdapter.changeCursor(cursor);
        }
    }
}
