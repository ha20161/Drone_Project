package com.example.dronepet;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Haleema on 26/02/2018.
 */

public class ControlFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_controls, container, false);

        Button forward = (Button) view.findViewById(R.id.forwardBttn);
        Button backward = (Button) view.findViewById(R.id.backwardsBttn);
        Button rotateLeft = (Button) view.findViewById(R.id.rotateLeftBttn);
        Button rotateright = (Button) view.findViewById(R.id.RotateRightBttn);
        Button up = (Button) view.findViewById(R.id.upBttn);
        Button left = (Button) view.findViewById(R.id.leftBttn);
        Button right = (Button) view.findViewById(R.id.rightBttn);
        Button down = (Button) view.findViewById(R.id.downBttn);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handles menu item actions
        switch (item.getItemId()){

            case R.id.camera_icon:

                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
