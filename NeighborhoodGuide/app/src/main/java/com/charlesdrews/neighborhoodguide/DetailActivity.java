package com.charlesdrews.neighborhoodguide;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

public class DetailActivity extends AppCompatActivity {
    private static final String ERR_MSG_RATING_NOT_SAVED = "Database error: your rating was not saved";
    public static final String ERR_MSG_FAVORITE_STATUS_NOT_SAVED = "Database error: favorite status not saved";
    public static final String ERR_MSG_NOTE_NOT_SAVED = "Database error: your note was not saved";

    private int mSelectedPlaceId;
    private PlaceDbOpenHelper mHelper;
    private TextView mNoteView;
    private String mNoteDraft = "";
    private boolean mChangeToFavStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setStatusBarColor(R.color.detailStatusBar);

        mSelectedPlaceId = getIntent().getExtras().getInt(MainActivity.SELECTED_PLACE_KEY, -1);

        if (mSelectedPlaceId >= 0) {

            mHelper = PlaceDbOpenHelper.getInstance(DetailActivity.this);
            final Place selectedPlace = mHelper.getPlaceById(mSelectedPlaceId);

            getSupportActionBar().setTitle(selectedPlace.getTitle());

            ImageView imageView = (ImageView) findViewById(R.id.detail_image_view);
            int resId = getResources().getIdentifier(
                    selectedPlace.getImageRes(),    // file name w/o extension
                    "raw",                          // file stored in res/raw/
                    getPackageName()
            );
            if (resId != 0) { // getIdentifier returns 0 if resource not found
                Bitmap image = BitmapFactory.decodeResource(getResources(), resId);
                imageView.setImageBitmap(image);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imageView.setTransitionName(
                        getString(R.string.card_transition_name) //+
                        //getIntent().getExtras().getInt(ListBaseActivity.POSITION_KEY)
                );
            }

            TextView overviewView = (TextView) findViewById(R.id.detail_overview);
            String overviewText = selectedPlace.getCategory() + " | "
                    + selectedPlace.getLocation() + " | "
                    + selectedPlace.getNeighborhood();
            overviewView.setText(overviewText);

            mNoteView = (TextView) findViewById(R.id.detail_note);
            if (selectedPlace.getNote().isEmpty()) {
                mNoteView.setText(getString(R.string.detail_msg_click_to_add_note));
            } else {
                String note = "Your note: " + selectedPlace.getNote();
                mNoteView.setText(note);
            }
            mNoteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchAddOrEditNoteDialog(selectedPlace);
                }
            });

            TextView descriptionView = (TextView) findViewById(R.id.detail_description);
            descriptionView.setText(selectedPlace.getDescription());

            if (!selectedPlace.getImageCredit().isEmpty()) {
                String credit = "Photo credit: " + selectedPlace.getImageCredit();
                TextView imageCreditView = (TextView) findViewById(R.id.detail_image_credit);
                imageCreditView.setText(credit);
            }

            RatingBar ratingBar = (RatingBar) findViewById(R.id.detail_rating_bar);
            ratingBar.setRating(selectedPlace.getRating());

            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    selectedPlace.setRating(rating);
                    String msg;

                    if (mHelper.setRatingById(mSelectedPlaceId, rating)) {
                        msg = "Your rating of " + rating + " stars was saved for "
                                + selectedPlace.getTitle();
                    } else {
                        msg = ERR_MSG_RATING_NOT_SAVED;
                    }
                    Snackbar.make(
                            findViewById(R.id.coordinator_layout_detail),
                            msg,
                            Snackbar.LENGTH_SHORT
                    ).show();
                }
            });

            final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            setFabFavIcon(fab, selectedPlace.isFavorite());

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean isFavorite = !selectedPlace.isFavorite(); // toggle to opposite value
                    selectedPlace.setFavoriteStatus(isFavorite);
                    String msg;

                    if (mHelper.setFavoriteStatusById(mSelectedPlaceId, isFavorite)) {
                        setFabFavIcon(fab, isFavorite);
                        if (isFavorite) {
                            msg = selectedPlace.getTitle() + " favorited";
                        } else {
                            msg = selectedPlace.getTitle() + " unfavorited";
                        }
                        mChangeToFavStatus = !mChangeToFavStatus; // toggle to indicate change and allow change to be "undone" by next change
                    } else {
                        msg = ERR_MSG_FAVORITE_STATUS_NOT_SAVED;
                    }
                    Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show();
                }
            });

        } else {
            TextView locationView = (TextView) findViewById(R.id.detail_overview);
            locationView.setText(getString(R.string.err_msg_item_not_found));
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

    private void finishWithResultCode() {
        if (mChangeToFavStatus) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    private void setFabFavIcon(FloatingActionButton fab, boolean isFavorite) {
        if (isFavorite) {
            fab.setImageResource(R.drawable.ic_favorite_white_24dp); // filled in heart if favorite
        } else {
            fab.setImageResource(R.drawable.ic_favorite_border_white_24dp); // otherwise just outline
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

    private void  launchAddOrEditNoteDialog(final Place place) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
        final EditText input = new EditText(DetailActivity.this);

        if (place.getNote().isEmpty()) {
            builder.setTitle("Add a note");
        } else {
            builder.setTitle("Edit your note");
        }

        if (mNoteDraft == null || mNoteDraft.isEmpty()) {
            input.setText(place.getNote());
        } else {
            input.setText(mNoteDraft);
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
                String msg;

                place.setNote(note);
                if (mHelper.setNoteById(place.getId(), note)) {
                    if (note.isEmpty()) {
                        mNoteView.setText(getString(R.string.detail_msg_click_to_add_note));
                    } else {
                        note = "Your note: " + note;
                        mNoteView.setText(note);
                    }
                    msg = "Your note was saved to " + place.getTitle();
                } else {
                    msg = ERR_MSG_NOTE_NOT_SAVED;
                }

                dialog.dismiss();
                Snackbar.make(
                        findViewById(R.id.coordinator_layout_detail),
                        msg,
                        Snackbar.LENGTH_SHORT
                ).show();
            }
        });

        builder.show();
    }
}
