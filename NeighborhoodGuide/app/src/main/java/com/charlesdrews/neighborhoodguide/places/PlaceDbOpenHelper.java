package com.charlesdrews.neighborhoodguide.places;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by charlie on 2/2/16.
 */
public class PlaceDbOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "places.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME_PLACES = "places";
    public static final String COL_ID = "_id";
    public static final String COL_TITLE = "title";
    public static final String COL_LOCATION = "location";
    public static final String COL_NEIGHBORHOOD = "neighborhood";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_IS_FAVORITE = "is_favorite";
    public static final String COL_RATING = "rating";
    public static final String COL_IMAGE = "image";
    public static final String COL_NOTE = "note";

    private static final String SQL_DROP_PLACES_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME_PLACES;
    private static final String SQL_CREATE_PLACES_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_PLACES + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_TITLE + " TEXT, "
                    + COL_LOCATION + " TEXT, "
                    + COL_NEIGHBORHOOD + " TEXT, "
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

    //TODO - add methods to return only columns needed, not all columns

    public Cursor getAllPlaces() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(
                TABLE_NAME_PLACES,       // table
                null,               // columns (null = *)
                null,               // selection (WHERE clause)
                null,               // selectionArgs
                null,               // group by
                null,               // having
                COL_TITLE           // order by
        );
    }

    public Cursor getFavoritePlaces() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(
                TABLE_NAME_PLACES,       // table
                null,               // columns (null = *)
                COL_IS_FAVORITE + " = 1", // selection: WHERE is_favorite = 1
                null,               // selectionArgs (hardcoded in selection)
                null,               // group by
                null,               // having
                COL_TITLE           // order by
        );
    }

    public Cursor searchPlaces(String query) {
        //TODO - search more fields, not just title & location
        SQLiteDatabase db = getReadableDatabase();
        return db.query(
                TABLE_NAME_PLACES,       // table
                null,               // columns (null = *)
                COL_TITLE + " LIKE ? OR " + COL_LOCATION + " LIKE ?", // selection: WHERE title LIKE '%query%'
                new String[]{"%"+query+"%", "%"+query+"%"}, // selectionArgs
                null,               // group by
                null,               // having
                COL_TITLE           // order by
        );
    }

    public Cursor searchFavorites(String query) {
        //TODO - search more fields, not just title & location
        SQLiteDatabase db = getReadableDatabase();
        return db.query(
                TABLE_NAME_PLACES,       // table
                null,               // columns (null = *)
                COL_IS_FAVORITE + "=1 AND (" + COL_TITLE + " LIKE ? OR " + COL_LOCATION + " LIKE ?)", // selection: WHERE title LIKE '%query%'
                new String[]{"%"+query+"%", "%"+query+"%"}, // selectionArgs
                null,               // group by
                null,               // having
                COL_TITLE           // order by
        );
    }

    public Place getPlaceById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_NAME_PLACES,       // table
                null,               // columns (null = *)
                COL_ID + " = ?",    // selection: WHERE _id = ?
                new String[]{String.valueOf(id)}, // selectionArgs: WHERE _id = id
                null,               // group by
                null,               // having
                null                // order by
        );
        if (cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndex(COL_TITLE));
            String location = cursor.getString(cursor.getColumnIndex(COL_LOCATION));
            String neighborhood = cursor.getString(cursor.getColumnIndex(COL_NEIGHBORHOOD));
            String description = cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION));
            Boolean isFavorite = (cursor.getInt(cursor.getColumnIndex(COL_IS_FAVORITE)) == 1);
            Float rating = cursor.getFloat(cursor.getColumnIndex(COL_RATING));
            String note = cursor.getString(cursor.getColumnIndex(COL_NOTE));

            cursor.close();
            return new Place(id, title, location, neighborhood, description, isFavorite, rating, note);
        } else {
            cursor.close();
            return null;
        }
    }

    public void insertPlace(String title, String location, String neighborhood, String description,
                            boolean isFavorite, float rating, String note)
    {
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, title);
        values.put(COL_LOCATION, location);
        values.put(COL_NEIGHBORHOOD, neighborhood);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_IS_FAVORITE, (isFavorite ? 1 : 0));
        values.put(COL_RATING, rating);
        values.put(COL_NOTE, note);

        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_NAME_PLACES, null, values);
    }

    public void deletePlaceById(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME_PLACES, COL_ID + " = ?", new String[]{String.valueOf(id)});
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
        if (cursor.moveToFirst()) {
            return (cursor.getInt(cursor.getColumnIndex(COL_IS_FAVORITE)) == 1);
        } else {
            return false;
        }
    }

    public void setFavoriteStatusById(int id, boolean isFavorite) {
        ContentValues values = new ContentValues();
        values.put(COL_IS_FAVORITE, isFavorite);

        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_NAME_PLACES, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void setRatingById(int id, float rating) {
        if (rating < 0.0) {
            rating = (float) 0.0;
        } else if (rating > 5.0) {
            rating = (float) 5.0;
        }

        ContentValues values = new ContentValues();
        values.put(COL_RATING, rating);

        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_NAME_PLACES, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
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
        return cursor.getString(cursor.getColumnIndex(COL_NOTE));
    }

    public void setNoteById(int id, String note) {
        ContentValues values = new ContentValues();
        values.put(COL_NOTE, note);

        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_NAME_PLACES, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }
}
