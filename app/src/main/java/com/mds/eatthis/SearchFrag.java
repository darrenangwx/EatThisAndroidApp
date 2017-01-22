package com.mds.eatthis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;

/**
 * Created by Darren on 1/22/2017.
 */

public class SearchFrag extends Fragment {
    EditText editText;
    Switch switchffnm;
    CheckBox checkBox;
    Button buttonGo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {



        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        View v = inflater.inflate(R.layout.fragment_menu_search, container, false);

        //for text box
        editText = (EditText) v.findViewById(R.id.editText5);
        editText.setVisibility(View.VISIBLE);

        //for find food near me switch
        switchffnm = (Switch) v.findViewById(R.id.switchffnm);
        switchffnm.setOnClickListener(new Switch.OnClickListener() {
            @Override
            public void onClick(View view) {
                GPS(view);
            }
        });

        //for adv search checkbox
        checkBox = (CheckBox) v.findViewById(R.id.checkBox);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((CheckBox) view).isChecked()){
                    startActivity(new Intent(SearchFrag.this.getActivity(), AdvSearchActivity.class));
                }
            }
        });


        //for Go button
        buttonGo = (Button) v.findViewById(R.id.buttonGo);
        buttonGo.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new MapViewFrag();
                replaceFragment(fragment);
                hideKeyboard(getContext());

            }
        });
        return v;
    }

    //replace fragment when button is clicked
    public void replaceFragment(Fragment fragment){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Search");
    }

    public void GPS(View view){
        boolean checked = ((Switch)view).isChecked();
        if(checked){
            editText.setVisibility(View.INVISIBLE);
        }
        else{
            editText.setVisibility(View.VISIBLE);
        }
    }

    public static void hideKeyboard(Context ctx) {
        InputMethodManager inputManager = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }



}
