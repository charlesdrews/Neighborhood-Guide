package com.charlesdrews.neighborhoodguide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.charlesdrews.neighborhoodguide.places.PlaceDbOpenHelper;


/**
 * Implements an Adapter for a RecyclerView based on data from a Cursor
 * Created by charlie on 2/4/16.
 */
public class RecyclerCursorAdapter extends RecyclerView.Adapter<RecyclerCursorAdapter.ViewHolder> {
    private static final String ERR_MSG_ITEM_NOT_FOUND = "Error: item not found";
    private static final StrikethroughSpan STRIKE_THROUGH_SPAN = new StrikethroughSpan();

    private Context mContext;
    private Cursor mCursor;
    private Drawable mFavIcon;
    private Drawable mNonFavIcon;
    private Drawable mRemoveFavIcon;
    private Drawable mAddFavIcon;
    private boolean mContextIsFavs;
    private PlaceDbOpenHelper mHelper;

    //TODO - can I make this inner class private?
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;
        public TextView mTitleTextView;
        public TextView mOverviewTextView;
        public ImageView mIconImgView;

        public ViewHolder(View itemView) {
            super(itemView);
            mCardView = (CardView) itemView.findViewById(R.id.card_place);
            mTitleTextView = (TextView) itemView.findViewById(R.id.card_place_title);
            mOverviewTextView = (TextView) itemView.findViewById(R.id.card_place_overview);
            mIconImgView = (ImageView) itemView.findViewById(R.id.card_fav_icon);
        }
    }

    public RecyclerCursorAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mFavIcon = ContextCompat.getDrawable(context, R.drawable.ic_favorite_pink_a200_24dp);
        mNonFavIcon = ContextCompat.getDrawable(context, R.drawable.ic_favorite_border_grey_800_24dp);
        mRemoveFavIcon = ContextCompat.getDrawable(context, R.drawable.ic_remove_circle_outline_grey_800_24dp);
        mAddFavIcon = ContextCompat.getDrawable(context, R.drawable.ic_add_circle_outline_grey_800_24dp);
        mContextIsFavs = (context instanceof FavoritesActivity);
        mHelper = PlaceDbOpenHelper.getInstance(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.place_card_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (mCursor.moveToPosition(position)) {
            holder.mCardView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.cardBg));

            String title = mCursor.getString(mCursor.getColumnIndex(PlaceDbOpenHelper.COL_TITLE));
            holder.mTitleTextView.setText(title, TextView.BufferType.SPANNABLE);

            String overview = mCursor.getString(mCursor.getColumnIndex(PlaceDbOpenHelper.COL_CATEGORY))
                    + " | " + mCursor.getString(mCursor.getColumnIndex(PlaceDbOpenHelper.COL_LOCATION))
                    + " | " + mCursor.getString(mCursor.getColumnIndex(PlaceDbOpenHelper.COL_NEIGHBORHOOD));

            holder.mOverviewTextView.setText(overview, TextView.BufferType.SPANNABLE);

            final int id = mCursor.getInt(mCursor.getColumnIndex(PlaceDbOpenHelper.COL_ID));
            boolean isFav = (mCursor.getInt(mCursor.getColumnIndex(PlaceDbOpenHelper.COL_IS_FAVORITE)) == 1);
            holder.mIconImgView.setImageDrawable(pickIconDrawable(isFav));

            holder.mIconImgView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            return true;
                        case MotionEvent.ACTION_UP:
                            onIconImgViewClick(holder, ((ImageView) v), id);
                            return true; // this consumes the entire touch event; will not trigger CardView's onclick()
                    }
                    return false;
                }
            });

            holder.mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, DetailActivity.class);
                    intent.putExtra(MainActivity.SELECTED_PLACE_KEY, id);
                    mContext.startActivity(intent);
                }
            });
        } else {
            // if mCursor.moveToPosition(position) fails
            holder.mTitleTextView.setText(ERR_MSG_ITEM_NOT_FOUND);
        }
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        if (mCursor != null) {
            mCursor.moveToPosition(position);
            return (long) mCursor.getInt(mCursor.getColumnIndex(PlaceDbOpenHelper.COL_ID));
        }
        return -1;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void changeCursor(Cursor newCursor) {
        Cursor oldCursor = mCursor;
        mCursor = newCursor;
        notifyDataSetChanged();
        oldCursor.close();
    }

    private void onIconImgViewClick(ViewHolder holder, ImageView imgView, int id) {
        // get isFavorite status of item
        boolean isFav = mHelper.isFavoriteById(id);

        // toggle isFavorite status in database (and local indicator)
        mHelper.setFavoriteStatusById(id, !isFav);
        isFav = !isFav;

        // set icon
        imgView.setImageDrawable(pickIconDrawable(isFav));

        // set text strikethru if context is favs & item is un-faved, otherwise not strikethru
        // also set card background to same grey as recyclerview background
        Spannable titleSpannable = (Spannable) holder.mTitleTextView.getText();
        Spannable overviewSpannable = (Spannable) holder.mOverviewTextView.getText();

        if (mContextIsFavs & !isFav) {
            titleSpannable.setSpan(STRIKE_THROUGH_SPAN, 0, titleSpannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            overviewSpannable.setSpan(STRIKE_THROUGH_SPAN, 0, overviewSpannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.mCardView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.cardUnFavedBg));
        } else {
            titleSpannable.removeSpan(STRIKE_THROUGH_SPAN);
            overviewSpannable.removeSpan(STRIKE_THROUGH_SPAN);
            holder.mCardView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.cardBg));
        }

        // launch a Snackbar to notify user of success
        View rootView;
        if (mContextIsFavs) {
            rootView = ((Activity) mContext).findViewById(R.id.coordinator_layout_favs);
        } else {
            rootView = ((Activity) mContext).findViewById(R.id.coordinator_layout_main);
        }
        String msg = holder.mTitleTextView.getText().toString() + (isFav ? " favorited" : " unfavorited");
        Snackbar.make(rootView, msg, Snackbar.LENGTH_SHORT).show();
    }

    private Drawable pickIconDrawable(boolean isFav) {
        if (isFav) {
            if (mContextIsFavs) {
                return mRemoveFavIcon;
            } else {
                return mFavIcon;
            }
        } else {
            if (mContextIsFavs) {
                return mAddFavIcon;
            } else {
                return mNonFavIcon;
            }
        }
    }
}
