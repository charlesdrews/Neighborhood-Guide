package com.charlesdrews.neighborhoodguide.places;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Provides access to the table "places" in the database places.db
 * Created by charlie on 2/2/16.
 */
public class PlaceDbOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "places.db";
    private static final int DATABASE_VERSION = 1;

    private static final String ALL_CATEGORIES = "All";
    private static final String UNCATEGORIZED = "Uncategorized";

    public static final String TABLE_NAME_PLACES = "places";
    public static final String COL_ID = "_id";
    public static final String COL_TITLE = "title";
    public static final String COL_LOCATION = "location";
    public static final String COL_NEIGHBORHOOD = "neighborhood";
    public static final String COL_CATEGORY = "category";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_IS_FAVORITE = "is_favorite";
    public static final String COL_RATING = "rating";
    public static final String COL_IMAGE = "image";
    public static final String COL_NOTE = "note";

    private static final String[] SEARCH_RESULT_COLUMNS = new String[]{
            COL_ID, COL_TITLE, COL_CATEGORY, COL_LOCATION, COL_NEIGHBORHOOD, COL_IS_FAVORITE};

    private static final String SQL_DROP_PLACES_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME_PLACES;
    private static final String SQL_CREATE_PLACES_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_PLACES + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_TITLE + " TEXT, "
                    + COL_LOCATION + " TEXT, "
                    + COL_NEIGHBORHOOD + " TEXT, "
                    + COL_CATEGORY + " TEXT, "
                    + COL_DESCRIPTION + " TEXT, "
                    + COL_IS_FAVORITE + " INTEGER, "
                    + COL_RATING + " REAL, "
                    + COL_IMAGE + " BLOB, "
                    + COL_NOTE + " TEXT)";

    private static PlaceDbOpenHelper mInstance;

    public static PlaceDbOpenHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PlaceDbOpenHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    private PlaceDbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PLACES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_PLACES_TABLE);
        onCreate(db);
    }

    /**
     * Queries the db for places based on isFavorite status and category
     * @param favoritesOnly - boolean status for desired results
     * @param category - value to be used to constrain results (can be null)
     * @return - a Cursor of db query results
     */
    private Cursor getPlaces(boolean favoritesOnly, String category) {
        StringBuilder selectionStrBuilder = new StringBuilder();
        ArrayList<String> selectionArgsList = new ArrayList<>();

        if (favoritesOnly) {
            selectionStrBuilder.append(COL_IS_FAVORITE + "=1");
        }

        if (category != null && !category.equals(ALL_CATEGORIES)) {
            if (selectionStrBuilder.length() > 0) {
                selectionStrBuilder.append(" AND ");
            }

            if (category.equals(UNCATEGORIZED)) {
                selectionStrBuilder.append(COL_CATEGORY + " IS NULL");
            } else {
                selectionStrBuilder.append(COL_CATEGORY + "=?");
                selectionArgsList.add(category);
            }
        }

        String selection = selectionStrBuilder.toString();
        String[] selectionArgs;
        if (selectionArgsList.size() > 0) {
            selectionArgs = new String[selectionArgsList.size()];
            selectionArgs = selectionArgsList.toArray(selectionArgs);
        } else {
            selectionArgs = null;
        }

        SQLiteDatabase db = getReadableDatabase();
        return db.query(
                TABLE_NAME_PLACES,      // table
                SEARCH_RESULT_COLUMNS,  // columns
                selection,              // selection
                selectionArgs,          // selectionArgs
                null,                   // group by
                null,                   // having
                COL_TITLE,              // order by
                null                    // limit
        );
    }

    public Cursor getAllPlaces() {
        return getPlaces(false, null);
    }

    public Cursor getAllPlacesByCategory(String category) {
        return getPlaces(false, category);
    }

    public Cursor getFavoritePlaces() {
        return getPlaces(true, null);
    }

    public Cursor getFavoritePlacesByCategory(String category) {
        return getPlaces(true, category);
    }

    private Cursor searchPlaces(String query, boolean favoritesOnly, String category) {
        String[] queryTokens = query.split(" ");

        StringBuilder selectionStrBuilder = new StringBuilder();
        ArrayList<String> selectionArgsList = new ArrayList<>();

        selectionStrBuilder.append("(" + COL_TITLE + " LIKE ? OR "+ COL_LOCATION + " LIKE ? OR "
                + COL_NEIGHBORHOOD + " LIKE ?)");
        selectionArgsList.add("%" + queryTokens[0] + "%");
        selectionArgsList.add("%" + queryTokens[0] + "%");
        selectionArgsList.add("%" + queryTokens[0] + "%");

        if (queryTokens.length > 1) {
            for (int i = 1; i < queryTokens.length; i++) {
                selectionStrBuilder.append(" AND (" + COL_TITLE + " LIKE ? OR " + COL_LOCATION
                        + " LIKE ? OR " + COL_NEIGHBORHOOD + " LIKE ?)");
                selectionArgsList.add("%" + queryTokens[i] + "%");
                selectionArgsList.add("%" + queryTokens[i] + "%");
                selectionArgsList.add("%" + queryTokens[i] + "%");
            }
        }

        if (favoritesOnly) {
            selectionStrBuilder.append(" AND " + COL_IS_FAVORITE + "=1");
        }

        if (category != null) {
            selectionStrBuilder.append(" AND " + COL_CATEGORY + "=?");
            selectionArgsList.add(category);
        }

        String selection = selectionStrBuilder.toString();
        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgs = selectionArgsList.toArray(selectionArgs);

        SQLiteDatabase db = getReadableDatabase();
        return db.query(
                TABLE_NAME_PLACES,      // table
                SEARCH_RESULT_COLUMNS,  // columns
                selection,              // selection
                selectionArgs,          // selectionArgs
                null,                   // group by
                null,                   // having
                COL_TITLE,              // order by
                null                    // limit
        );
    }

    public Cursor searchAllPlaces(String query) {
        return searchPlaces(query, false, null);
    }

    public Cursor searchAllPlacesByCategory(String query, String category) {
        return searchPlaces(query, false, category);
    }

    public Cursor searchFavoritePlaces(String query) {
        return searchPlaces(query, true, null);
    }

    public Cursor searchFavoritePlacesByCategory(String query, String category) {
        return searchPlaces(query, true, category);
    }

    public Place getPlaceById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_NAME_PLACES,  // table
                null,               // columns (null = *)
                COL_ID + "=?",      // selection: WHERE _id = ?
                new String[]{String.valueOf(id)}, // selectionArgs: WHERE _id = id
                null,               // group by
                null,               // having
                null,               // order by
                "1"                 // limit
        );
        if (cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndex(COL_TITLE));
            String location = cursor.getString(cursor.getColumnIndex(COL_LOCATION));
            String neighborhood = cursor.getString(cursor.getColumnIndex(COL_NEIGHBORHOOD));
            String category = cursor.getString(cursor.getColumnIndex(COL_CATEGORY));
            String description = cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION));
            Boolean isFavorite = (cursor.getInt(cursor.getColumnIndex(COL_IS_FAVORITE)) == 1);
            Float rating = cursor.getFloat(cursor.getColumnIndex(COL_RATING));
            String note = cursor.getString(cursor.getColumnIndex(COL_NOTE));

            cursor.close();
            return new Place(id, title, location, neighborhood, category, description, isFavorite,
                    rating, note);
        } else {
            cursor.close();
            return null;
        }
    }

    /**
     * Inserts a Place object into the database
     * @param place - Place object to be inserted
     * @return true if Place inserted successfully, false otherwise
     */
    public boolean insertPlace(Place place) {
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, place.getTitle());
        values.put(COL_LOCATION, place.getLocation());
        values.put(COL_NEIGHBORHOOD, place.getNeighborhood());
        values.put(COL_CATEGORY, place.getCategory());
        values.put(COL_DESCRIPTION, place.getDescription());
        values.put(COL_IS_FAVORITE, (place.isFavorite() ? 1 : 0));
        values.put(COL_RATING, place.getRating());
        values.put(COL_NOTE, place.getNote());

        SQLiteDatabase db = getWritableDatabase();
        long newRowId = db.insert(TABLE_NAME_PLACES, null, values);

        return (newRowId != -1); // true if OK, false if error and db.insert returned -1
    }

    /**
     * Deletes the specified Place from the database
     * @param id - uniqe ID of the Place to be deleted
     * @return - true if db updated, false otherwise
     */
    public boolean deletePlaceById(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = db.delete(
                TABLE_NAME_PLACES,
                COL_ID + "=?",
                new String[]{String.valueOf(id)}
        );
        return (rowsAffected > 0);
    }

    /**
     * Get a list of category values present in the database for use in category spinner
     * (list will also include "All" at head of list and "Uncategorized" at tail of list)
     * @return - ArrayList of Strings
     */
    public ArrayList<String> getCategories() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                true,
                TABLE_NAME_PLACES,
                new String[]{COL_CATEGORY},
                null,
                null,
                COL_CATEGORY,
                null,
                COL_CATEGORY,
                null
        );

        ArrayList<String> categories = new ArrayList<>();
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                categories.add(cursor.getString(cursor.getColumnIndex(COL_CATEGORY)));
                cursor.moveToNext();
            }
            Collections.sort(categories);
        }

        categories.add(0, ALL_CATEGORIES);  // add'l item a start of list
        categories.add(UNCATEGORIZED);      // add'l item at end of list

        cursor.close();
        return categories;
    }

    public String getCategoryById(int id) {
        String category = null;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_PLACES, // table
                new String[]{COL_CATEGORY},         // columns
                COL_ID + "=?",                      // selection
                new String[]{String.valueOf(id)},   // selectionArgs
                null,                               // group by
                null,                               // having
                null                                // order by
        );
        if (cursor.moveToFirst()) {
            category = cursor.getString(cursor.getColumnIndex(COL_CATEGORY));
        }
        cursor.close();
        return category;
    }

    public boolean isFavoriteById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_PLACES,      // table
                new String[]{COL_IS_FAVORITE},      // columns
                COL_ID + "=?",                      // selection
                new String[]{String.valueOf(id)},   // selectionArgs
                null,                               // group by
                null,                               // having
                null                                // order by
        );
        boolean isFav = false; // return false if id not found in db
        if (cursor.moveToFirst()) {
            isFav = (cursor.getInt(cursor.getColumnIndex(COL_IS_FAVORITE)) == 1);
        }
        cursor.close();
        return isFav;
    }

    /**
     * Sets the isFavorite status for the specified Place
     * @param id - unique id of the Place to be updated
     * @param isFavorite - boolean status to be set
     * @return true if db updated, false otherwise
     */
    public boolean setFavoriteStatusById(int id, boolean isFavorite) {
        ContentValues values = new ContentValues();
        values.put(COL_IS_FAVORITE, isFavorite);

        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = db.update(
                TABLE_NAME_PLACES,
                values,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
        return (rowsAffected > 0);
    }

    /**
     * Sets the rating for the specified Place
     * @param id - unique id of the Place to be updated
     * @param rating - value to be set
     * @return true if db updated, false otherwise
     */
    public boolean setRatingById(int id, float rating) {
        if (rating < 0.0) {
            rating = (float) 0.0;
        } else if (rating > 5.0) {
            rating = (float) 5.0;
        }

        ContentValues values = new ContentValues();
        values.put(COL_RATING, rating);

        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = db.update(
                TABLE_NAME_PLACES,
                values,
                COL_ID + "=?",
                new String[]{String.valueOf(id)}
        );
        return (rowsAffected > 0);
    }

    public String getNoteById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_NAME_PLACES,      // table
                new String[]{COL_NOTE}, // columns
                COL_ID + " = ?",        // selection
                new String[]{String.valueOf(id)}, // selectionArgs
                null, null, null, null  // group by, having, order by, limit
        );

        String note = null;
        if (cursor.moveToFirst()) {
            note = cursor.getString(cursor.getColumnIndex(COL_NOTE));
        }
        cursor.close();
        return note;
    }

    /**
     * Sets the note for the specified Place
     * @param id - unique id of the Place to be updated
     * @param note - value to be set
     * @return true if db updated, false otherwise
     */
    public boolean setNoteById(int id, String note) {
        ContentValues values = new ContentValues();
        values.put(COL_NOTE, note);

        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = db.update(
                TABLE_NAME_PLACES,
                values,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
        return (rowsAffected > 0);
    }
}
