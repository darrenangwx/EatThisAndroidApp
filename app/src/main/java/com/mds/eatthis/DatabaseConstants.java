package com.mds.eatthis;

import android.provider.BaseColumns;

/**
 * Created by Darren, Ming Kiang and Stanley.
 */

public interface DatabaseConstants extends BaseColumns {
    public static final String TABLE_NAME = "locationdetails";

    //columns in the database
    public static final String RestaurantName = "RestaurantName";
    public static final String RestaurantLocation = "RestaurantLocation";
    public static final String PlaceID = "PlaceID";
    public static final String RestaurantLat = "RestaurantLat";
    public static final String RestaurantLong = "RestaurantLong";
}
