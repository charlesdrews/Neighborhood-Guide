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

    public Place (String title, String location, String neighborhood,
                  String description, boolean isFavorite)
    {
        mTitle = title;
        mLocation = location;
        mNeighborhood = neighborhood;
        mDescription = description;
        mIsFavorite = isFavorite;
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
}
