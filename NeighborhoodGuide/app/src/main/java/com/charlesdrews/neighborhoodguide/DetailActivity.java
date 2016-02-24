package com.charlesdrews.neighborhoodguide;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.charlesdrews.neighborhoodguide.places.Place;
import com.charlesdrews.neighborhoodguide.places.PlaceDbOpenHelper;

/**
 * Defines the UI of the detail screen
 */
public class DetailActivity extends AppCompatActivity {
    private static final String ERR_MSG_RATING_NOT_SAVED = "Database error: your rating was not saved";
    public static final String ERR_MSG_FAVORITE_STATUS_NOT_SAVED = "Database error: favorite status not saved";
    public static final String ERR_MSG_NOTE_NOT_SAVED = "Database error: your note was not saved";

    private Toolbar mToolbar;
    private int mSelectedPlaceId;
    private Place mSelectedPlace;
    private PlaceDbOpenHelper mHelper;
    private ImageView mImageView;
    private TextView mOverviewView, mNoteView, mDescriptionView;
    private RatingBar mRatingBar;
    private FloatingActionButton mFab;
    private String mNoteDraft = "";
    private boolean mChangeToFavStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setStatusBarColor(R.color.detailStatusBar);

        mImageView = (ImageView) findViewById(R.id.detail_image_view);
        mOverviewView = (TextView) findViewById(R.id.detail_overview);
        mNoteView = (TextView) findViewById(R.id.detail_note);
        mDescriptionView = (TextView) findViewById(R.id.detail_description);
        mRatingBar = (RatingBar) findViewById(R.id.detail_rating_bar);
        mFab = (FloatingActionButton) findViewById(R.id.fab);

        mSelectedPlaceId = getIntent().getExtras().getInt(MainActivity.SELECTED_PLACE_KEY, -1);

        if (mSelectedPlaceId >= 0) {

            GetSelectedPlaceAndSetViewsAsyncTask task = new GetSelectedPlaceAndSetViewsAsyncTask();
            task.execute();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mImageView.setTransitionName(getString(R.string.card_transition_name_image));
            }

            mNoteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchAddOrEditNoteDialog();
                }
            });

            mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    UpdateRatingAsyncTask task = new UpdateRatingAsyncTask();
                    task.execute(rating);
                }
            });

            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UpdateFavStatusAsyncClass task = new UpdateFavStatusAsyncClass();
                    task.execute();
                }
            });

        } else {
            mOverviewView.setText(getString(R.string.err_msg_item_not_found));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finishWithResultCode();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        finishWithResultCode();
    }

    /**
     * If the favorite status of the selected place was changed, return RESULT_OK so the parent
     * activity knows it must refresh its cursor, otherwise return RESULT_CANCELED so the parent
     * activity can save the overhead and not update its cursor.
     */
    private void finishWithResultCode() {
        if (mChangeToFavStatus) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    private void setFabFavIcon() {
        if (mSelectedPlace.isFavorite()) {
            mFab.setImageResource(R.drawable.ic_favorite_white_24dp); // filled in heart if favorite
        } else {
            mFab.setImageResource(R.drawable.ic_favorite_border_white_24dp); // otherwise just outline
        }
    }

    private void setStatusBarColor(int colorResource) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = DetailActivity.this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(DetailActivity.this, colorResource));
        }
    }

    /**
     * User alert dialog to gather input from user for a note to add to the selected place.
     * Pre-populate input with existing note if present, or with a previously entered but not-saved
     * note draft, if present.
     */
    private void  launchAddOrEditNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
        final EditText input = new EditText(DetailActivity.this);

        if (mSelectedPlace.getNote().isEmpty()) {
            builder.setTitle("Add a note");
        } else {
            builder.setTitle("Edit your note");
        }

        if (mNoteDraft == null || mNoteDraft.isEmpty()) {
            input.setText(mSelectedPlace.getNote());
        } else {
            input.setText(mNoteDraft);
        }

        // set content description
        if (input.getText().toString().isEmpty()) {
            input.setContentDescription("Input for your note. Input is currently blank.");
        } else {
            input.setContentDescription("Input for your note. Current text in input is " + input.getText());
        }

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                mNoteDraft = s.toString();
            }
        });

        builder.setView(input);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mNoteDraft = "";
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String note = input.getText().toString();

                UpdateNoteAsyncTask task = new UpdateNoteAsyncTask();
                task.execute(note);

                dialog.dismiss();
            }
        });

        builder.show();
    }

    private class GetSelectedPlaceAndSetViewsAsyncTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            mHelper = PlaceDbOpenHelper.getInstance(DetailActivity.this);
            mSelectedPlace = mHelper.getPlaceById(mSelectedPlaceId);
            return mSelectedPlace != null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(mSelectedPlace.getTitle());
                }
                mToolbar.setContentDescription(mSelectedPlace.getTitle());

                int resId = getResources().getIdentifier(
                        mSelectedPlace.getImageRes(),    // file name w/o extension
                        "raw",                          // file stored in res/raw/
                        getPackageName()
                );
                if (resId != 0) { // getIdentifier returns 0 if resource not found
                    Bitmap image = BitmapFactory.decodeResource(getResources(), resId);
                    mImageView.setImageBitmap(image);
                }

                String overviewText = mSelectedPlace.getCategory() + " | "
                        + mSelectedPlace.getLocation() + " | "
                        + mSelectedPlace.getNeighborhood();
                mOverviewView.setText(overviewText);

                if (mSelectedPlace.getNote().isEmpty()) {
                    mNoteView.setText(getString(R.string.detail_msg_click_to_add_note));
                } else {
                    String note = "Your note: " + mSelectedPlace.getNote();
                    mNoteView.setText(note);
                }

                mDescriptionView.setText(mSelectedPlace.getDescription());

                if (!mSelectedPlace.getImageCredit().isEmpty()) {
                    String credit = "Photo credit: " + mSelectedPlace.getImageCredit();
                    TextView imageCreditView = (TextView) findViewById(R.id.detail_image_credit);
                    imageCreditView.setText(credit);
                }

                mRatingBar.setRating(mSelectedPlace.getRating());
                if (mRatingBar.getRating() > 0) {
                    mRatingBar.setContentDescription("Rating bar: give this place a rating of 0 to 5 stars. Current rating is " + mRatingBar.getRating());
                } else {
                    mRatingBar.setContentDescription("Rating bar: give this place a rating of 0 to 5 stars. No rating set.");
                }

                setFabFavIcon();
                if (mSelectedPlace.isFavorite()) {
                    mFab.setContentDescription("Click to remove this place from favorites");
                } else {
                    mFab.setContentDescription("Click to add this place to favorites");
                }
            }
        }
    }

    private class UpdateRatingAsyncTask extends AsyncTask<Float, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Float... params) {
            mSelectedPlace.setRating(params[0]);
            return mHelper.setRatingById(mSelectedPlaceId, params[0]); // true if successful, else false
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            String msg;
            if (aBoolean) {
                msg = "Your rating of " + mSelectedPlace.getRating() + " stars was saved for "
                        + mSelectedPlace.getTitle();
            } else {
                msg = ERR_MSG_RATING_NOT_SAVED;
            }

            // reset content description
            if (mRatingBar.getRating() > 0) {
                mRatingBar.setContentDescription("Rating bar: give this place a rating of 0 to 5 stars. Current rating is "
                        + mSelectedPlace.getRating());
            } else {
                mRatingBar.setContentDescription("Rating bar: give this place a rating of 0 to 5 stars. No rating set.");
            }

            Snackbar.make(
                    findViewById(R.id.coordinator_layout_detail),
                    msg,
                    Snackbar.LENGTH_SHORT
            ).show();
        }
    }

    private class UpdateFavStatusAsyncClass extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            mSelectedPlace.setFavoriteStatus(!mSelectedPlace.isFavorite()); // toggle to opposite value
            return mHelper.setFavoriteStatusById(mSelectedPlaceId, mSelectedPlace.isFavorite());
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            String msg;
            if (aBoolean) {
                setFabFavIcon();
                if (mSelectedPlace.isFavorite()) {
                    msg = mSelectedPlace.getTitle() + " favorited";
                } else {
                    msg = mSelectedPlace.getTitle() + " unfavorited";
                }
                mChangeToFavStatus = !mChangeToFavStatus; // toggle to indicate change and allow change to be "undone" by next change
            } else {
                msg = ERR_MSG_FAVORITE_STATUS_NOT_SAVED;
            }

            // reset content descriptions on click
            if (mSelectedPlace.isFavorite()) {
                mFab.setContentDescription("Click to remove this place from favorites");
            } else {
                mFab.setContentDescription("Click to add this place to favorites");
            }

            Snackbar.make(
                    findViewById(R.id.coordinator_layout_detail),
                    msg,
                    Snackbar.LENGTH_SHORT
            ).show();
        }
    }

    private class UpdateNoteAsyncTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            mSelectedPlace.setNote(params[0]);
            return mHelper.setNoteById(mSelectedPlaceId, mSelectedPlace.getNote());
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            String msg;
            if (aBoolean) {
                String note = mSelectedPlace.getNote();
                if (note.isEmpty()) {
                    mNoteView.setText(getString(R.string.detail_msg_click_to_add_note));
                    mNoteView.setHint("Enter a note to add it to this place");
                } else {
                    note = "Your note: " + note;
                    mNoteView.setText(note);
                }
                msg = "Your note was saved to " + mSelectedPlace.getTitle();
            } else {
                msg = ERR_MSG_NOTE_NOT_SAVED;
            }

            Snackbar.make(
                    findViewById(R.id.coordinator_layout_detail),
                    msg,
                    Snackbar.LENGTH_SHORT
            ).show();
        }
    }
}
