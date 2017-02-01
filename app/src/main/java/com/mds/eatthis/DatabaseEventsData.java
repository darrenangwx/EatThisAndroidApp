package com.mds.eatthis;
import static android.provider.BaseColumns._ID;
import static com.mds.eatthis.DatabaseConstants.PlaceID;
import static com.mds.eatthis.DatabaseConstants.RestaurantLat;
import static com.mds.eatthis.DatabaseConstants.RestaurantLong;
import static com.mds.eatthis.DatabaseConstants.TABLE_NAME;
import static com.mds.eatthis.DatabaseConstants.RestaurantName;
import static com.mds.eatthis.DatabaseConstants.RestaurantLocation;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by þïèÐú†ÇH on 1/30/2017.
 */

public class DatabaseEventsData extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "Restaurant_Detail";
    private static final int DATABASE_VERSION = 1;

    public DatabaseEventsData(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +TABLE_NAME+
                " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + RestaurantName +
                " TEXT NOT NULL," +RestaurantLocation+" TEXT NOT NULL," +PlaceID+" TEXT NOT NULL,"+RestaurantLat+" REAL NOT NULL,"+RestaurantLong+" REAL NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
