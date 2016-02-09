package com.charlesdrews.neighborhoodguide.places;

/**
 * Models a Place object representing a physical location in the real world
 * Created by charlie on 2/2/16.
 */
public class Place {
    private int mId;
    private String mTitle;
    private String mLocation;
    private String mNeighborhood;
    private String mCategory;
    private String mDescription;
    private String mImageRes;
    private String mImageCredit;
    private boolean mIsFavorite;
    private float mRating;
    private String mNote;

    public Place (int id, String title, String location, String neighborhood, String category,
                  String description, String imageRes, String imageCredit, boolean isFavorite,
                  float rating, String note)
    {
        mId = id;
        mTitle = title;
        mLocation = location;
        mNeighborhood = neighborhood;
        mCategory = category;
        mDescription = description;
        mImageRes = imageRes;
        mImageCredit = imageCredit;
        mIsFavorite = isFavorite;
        mRating = rating;
        mNote = note;
    }

    public int getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getLocation() {
        return mLocation;
    }

    public String getNeighborhood() {
        return mNeighborhood;
    }

    public String getCategory() {
        return mCategory;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getImageRes() {
        return mImageRes;
    }

    public String getImageCredit() {
        return mImageCredit;
    }

    public boolean isFavorite() {
        return mIsFavorite;
    }

    public void setFavoriteStatus(boolean isFavorite) {
        mIsFavorite = isFavorite;
    }

    public float getRating() {
        return mRating;
    }

    public void setRating(float rating) {
        mRating = rating;
    }

    public String getNote() {
        if (mNote == null) {
            return "";
        } else {
            return mNote;
        }
    }

    public void setNote(String note) {
        mNote = note;
    }
}
