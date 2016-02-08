package com.charlesdrews.neighborhoodguide.places;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Provides access to the database places.db saved in the assets/databases directory
 * Created by charlie on 2/6/16.
 */
public class PlaceDbAssetHelper extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "places.db";
    private static final int DATABASE_VERSION = 1;

    public PlaceDbAssetHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}
