package com.charlesdrews.neighborhoodguide.places;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.charlesdrews.neighborhoodguide.R;

/**
 * Created by charlie on 2/2/16.
 */
public class PlaceDbOpenHelper extends SQLiteOpenHelper {
    //TODO - make this a singleton!!!

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "places_db";

    public static final String TABLE_PLACES = "places";

    public static final String COL_ID = "_id";
    public static final String COL_TITLE = "title";
    public static final String COL_LOCATION = "location";
    public static final String COL_NEIGHBORHOOD = "neighborhood";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_IS_FAVORITE = "is_favorite";

    private static final String SQL_DROP_PLACES_TABLE = "DROP TABLE IF EXISTS " + TABLE_PLACES;
    private static final String SQL_CREATE_PLACES_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_PLACES + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_TITLE + " TEXT, "
                    + COL_LOCATION + " TEXT, "
                    + COL_NEIGHBORHOOD + " TEXT, "
                    + COL_DESCRIPTION + " TEXT, "
                    + COL_IS_FAVORITE + " INTEGER)";

    public PlaceDbOpenHelper(Context context) {
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

    public Cursor getAllPlaces() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(
                TABLE_PLACES,       // table
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
                TABLE_PLACES,       // table
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
                TABLE_PLACES,       // table
                null,               // columns (null = *)
                COL_TITLE + " LIKE ? OR " + COL_LOCATION + " LIKE ?", // selection: WHERE title LIKE '%query%'
                new String[]{"%"+query+"%", "%"+query+"%"}, // selectionArgs
                null,               // group by
                null,               // having
                COL_TITLE           // order by
        );
    }

    public Place getPlace(int placeId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_PLACES,       // table
                null,               // columns (null = *)
                COL_ID + " = ?",    // selection: WHERE _id = ?
                new String[]{String.valueOf(placeId)}, // selectionArgs: WHERE _id = id
                null,               // group by
                null,               // having
                null                // order by
        );
        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        } else {
            String title = cursor.getString(cursor.getColumnIndex(COL_TITLE));
            String location = cursor.getString(cursor.getColumnIndex(COL_LOCATION));
            String neighborhood = cursor.getString(cursor.getColumnIndex(COL_NEIGHBORHOOD));
            String description = cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION));
            int isFavorite = cursor.getInt(cursor.getColumnIndex(COL_IS_FAVORITE));
            cursor.close();
            return new Place(title, location, neighborhood, description, (isFavorite == 1));
        }
    }

    public void insertPlace(String title, String location, String neighborhood,
                            String description, boolean isFavorite)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, title);
        values.put(COL_LOCATION, location);
        values.put(COL_NEIGHBORHOOD, neighborhood);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_IS_FAVORITE, (isFavorite ? 1 : 0));
        db.insert(TABLE_PLACES, null, values);
    }

    public void deletePlace(int placeId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PLACES + " WHERE " + COL_ID + " = " + placeId);
    }

    public void setFavoriteStatus(int placeId, boolean isFavorite) {
        SQLiteDatabase db = getWritableDatabase();
        //TODO - is it better to user db.update()???
        db.execSQL("UPDATE " + TABLE_PLACES
                + " SET " + COL_IS_FAVORITE + " = " + (isFavorite ? 1 : 0)
                + " WHERE " + COL_ID + " = " + placeId);
    }

    public void initializeDbForTesting(Context context) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PLACES);
        for (int i = 1; i <= 10; i++) {
            insertPlace(
                    "Place " + i,
                    "Address " + i,
                    "Neighborhood " + i,
                    context.getString(R.string.large_text),
                    false
            );
        }
    }
}
