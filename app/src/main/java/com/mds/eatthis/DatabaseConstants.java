package com.mds.eatthis;

import android.provider.BaseColumns;

/**
 * Created by þïèÐú†ÇH on 1/30/2017.
 */

public interface DatabaseConstants extends BaseColumns {
    public static final String TABLE_NAME = "locationdetails";

    //columns in the database
    public static final String RestaurantName = "RestaurantName";
    public static final String RestaurantLocation = "RestaurantLocation";
    public static final String PlaceID = "PlaceID";
    public static final String RestaurantLat = "RestaurantLat";
    public static final String RestaurantLong = "RestaurantLong";
    //maybe can store the coordinate of the place so can just put this into mapviewfrag when called from fav
}
