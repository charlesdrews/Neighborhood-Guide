package com.charlesdrews.neighborhoodguide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.charlesdrews.neighborhoodguide.places.PlaceDbOpenHelper;


/**
 * Created by charlie on 2/4/16.
 */
public class RecyclerCursorAdapter extends RecyclerView.Adapter<RecyclerCursorAdapter.ViewHolder> {
    private Context mContext;
    private Cursor mCursor;
    private Drawable mFavIcon;
    private Drawable mNonFavIcon;

    //TODO - can I make this inner class private?
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;
        public TextView mTitleTextView;
        public TextView mLocationTextView;
        public ImageView mFavIconImgView;

        public ViewHolder(View itemView) {
            super(itemView);
            mCardView = (CardView) itemView.findViewById(R.id.card_place);
            mTitleTextView = (TextView) itemView.findViewById(R.id.card_place_title);
            mLocationTextView = (TextView) itemView.findViewById(R.id.card_place_location);
            mFavIconImgView = (ImageView) itemView.findViewById(R.id.card_fav_icon);
        }
    }

    public RecyclerCursorAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mFavIcon = ContextCompat.getDrawable(context, R.drawable.ic_favorite_pink_a200_24dp);
        mNonFavIcon = ContextCompat.getDrawable(context, R.drawable.ic_favorite_border_grey_800_24dp);
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

            String title = mCursor.getString(mCursor.getColumnIndex(PlaceDbOpenHelper.COL_TITLE));
            String location = mCursor.getString(mCursor.getColumnIndex(PlaceDbOpenHelper.COL_LOCATION));

            holder.mTitleTextView.setText(title);
            holder.mLocationTextView.setText(location);

            if (mCursor.getInt(mCursor.getColumnIndex(PlaceDbOpenHelper.COL_IS_FAVORITE)) == 1) {
                holder.mFavIconImgView.setImageDrawable(mFavIcon);
            } else {
                holder.mFavIconImgView.setImageDrawable(mNonFavIcon);
            }

            holder.mFavIconImgView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Toast.makeText(mContext, "you clicked a heart", Toast.LENGTH_SHORT).show();
                    return true; // this consumes the entire touch event; will not trigger viewHolder's onclick()
                }
            });

            holder.mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, DetailActivity.class);
                    mCursor.moveToPosition(position);
                    intent.putExtra(
                            MainActivity.SELECTED_PLACE_KEY,
                            mCursor.getInt(mCursor.getColumnIndex(PlaceDbOpenHelper.COL_ID))
                    );
                    intent.putExtra(
                            MainActivity.FROM_FAVORITES_KEY,
                            true //TODO - this is supposed to be the mStartDetailFromFavorites from MainActivity
                    );
                    ((Activity) mContext).startActivityForResult(intent, MainActivity.REQUEST_CODE);
                }
            });
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
}
