package com.mds.eatthis;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

//db Stuff
import static android.R.id.list;
import static android.provider.BaseColumns._ID;
import static com.mds.eatthis.DatabaseConstants.TABLE_NAME;
import static com.mds.eatthis.DatabaseConstants.RestaurantName;
import static com.mds.eatthis.DatabaseConstants.RestaurantLocation;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;

import static com.mds.eatthis.DatabaseConstants.TABLE_NAME;

/**
 * Created by Darren on 1/22/2017.
 */


public class FavouritesFrag extends Fragment {
    // /Relating to database Stuff

    private static String[] FROM =
            {_ID, DatabaseConstants.RestaurantName, DatabaseConstants.RestaurantLocation, DatabaseConstants.PlaceID, DatabaseConstants.RestaurantLat, DatabaseConstants.RestaurantLong};
    private static String ORDER_BY = DatabaseConstants.RestaurantName + " ASC";
    private DatabaseEventsData locationdetails;

    TextView text;
    ListView list;
    static String[] arrayresname;
    static String[] arrayresloc;
    static String[] arrayresid;
    static double[] arrayreslong;
    static double[] arrayreslat;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        View v = inflater.inflate(R.layout.fragment_menu_favourites, container, false);
       /* text = (TextView) v.findViewById(R.id.textabc);*/

        locationdetails = new DatabaseEventsData(getActivity());
        try{
            Cursor cursor = getEvents();
            showEvents(cursor);
        }finally {
            locationdetails.close();
        }
        list = (ListView) v.findViewById(R.id.listView);
        list.setAdapter(new VivzAdapter(FavouritesFrag.this.getActivity()));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SingleRow data = (SingleRow) list.getItemAtPosition(position);

                System.out.println(data.title);
                System.out.println(data.description);
                System.out.println(data.id);
                System.out.println(data.coords[0]); // Latitude
                System.out.println(data.coords[1]); // Longtitude
            }
        });
        return v;
    }
    public Cursor getEvents(){
        SQLiteDatabase db = locationdetails.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, FROM, null, null, null, null, ORDER_BY);
        return cursor;
    }

/*    private void showEvents(Cursor cursor){
        StringBuilder builder = new StringBuilder();
        while (cursor.moveToNext()){
            String RestaurantName = cursor.getString(1);
            String RestaurantLocation = cursor.getString(2);
            builder.append(RestaurantName).append("\n");
            builder.append(RestaurantLocation).append("\n\n");
        }

        text.setText(builder);
    }*/
        private void showEvents(Cursor cursor){
            arrayresname = new String[cursor.getCount()];
            arrayresloc = new String[cursor.getCount()];
            arrayresid = new String[cursor.getCount()];
            arrayreslat = new double[cursor.getCount()];
            arrayreslong = new double[cursor.getCount()];
            int o = 0;
            while(cursor.moveToNext()){
                String allfavresname = cursor.getString(cursor.getColumnIndex(DatabaseConstants.RestaurantName));
                String allfavresloc = cursor.getString(cursor.getColumnIndex(DatabaseConstants.RestaurantLocation));
                String allfavresid = cursor.getString(cursor.getColumnIndex(DatabaseConstants.PlaceID));
                double allfavreslong = cursor.getDouble(cursor.getColumnIndex(DatabaseConstants.RestaurantLong));
                double allfavreslat = cursor.getDouble(cursor.getColumnIndex(DatabaseConstants.RestaurantLat));
                arrayresname[o] = allfavresname;
                arrayresloc[o] = allfavresloc;
                arrayresid[o] = allfavresid;
                arrayreslong[o] = allfavreslong;
                arrayreslat[o] = allfavreslat;
                o++;
            }

/*        StringBuilder builder = new StringBuilder();
        while (cursor.moveToNext()){
            String RestaurantName = cursor.getString(1);
            String RestaurantLocation = cursor.getString(2);
            builder.append(RestaurantName).append("\n");
            builder.append(RestaurantLocation).append("\n\n");*/
        }
    public static String[] returnArrayname()
    {
        return(arrayresname);
    }
    public static String[] returnArraylocation()
    {
        return(arrayresloc);
    }
    public static String[] returnArrayId() {return (arrayresid);}
    public static double[] returnArraylatitude()
    {
        return(arrayreslat);
    }
    public static double[] returnArraylongtitude()
    {
        return(arrayreslong);
    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Favourites");
    }

    public void replaceFragment(Fragment fragment){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();
    }
}

class SingleRow
{
    String title;
    String description;
    String id;
    double[] coords;
    SingleRow(String title, String description, String id, double[] coords)
    {
        this.title=title;
        this.description=description;
        this.id = id;
        this.coords = coords;
    }
}
class VivzAdapter extends BaseAdapter {
    ArrayList<SingleRow> list;
    Context context;

    VivzAdapter(Context c) {
        context = c;
        list = new ArrayList<SingleRow>();
        Resources res = c.getResources();
        String[] ArraysofResNames = FavouritesFrag.returnArrayname();
        String[] ArraysofResLocation = FavouritesFrag.returnArraylocation();
        String[] ArraysofResId = FavouritesFrag.returnArrayId();
        double[] ArraysofResLatitude = FavouritesFrag.returnArraylatitude();
        double[] ArraysofResLongtitude = FavouritesFrag.returnArraylongtitude();

        for (int i = 0; i < ArraysofResNames.length; i++) {
            list.add(new SingleRow(ArraysofResNames[i], ArraysofResLocation[i], ArraysofResId[i], new double[]{ArraysofResLatitude[i], ArraysofResLongtitude[i]}));
        }

    }
    
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.rows_favourites, viewGroup, false);
        TextView title = (TextView) row.findViewById(R.id.textView);
        TextView description = (TextView) row.findViewById(R.id.textView2);

        SingleRow temp = list.get(i);

        title.setText(temp.title);
        description.setText(temp.description);

        return row;
    }
}
