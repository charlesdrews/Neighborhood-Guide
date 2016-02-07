package com.charlesdrews.neighborhoodguide.places;

/**
 * Created by charlie on 2/2/16.
 */
public class Place {
    private String mTitle;
    private String mLocation;
    private String mNeighborhood;
    private String mDescription;
    private boolean mIsFavorite;
    private float mRating;

    public Place (String title, String location, String neighborhood,
                  String description, boolean isFavorite, float rating)
    {
        mTitle = title;
        mLocation = location;
        mNeighborhood = neighborhood;
        mDescription = description;
        mIsFavorite = isFavorite;
        mRating = rating;
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

    public String getDescription() {
        return mDescription;
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
}
