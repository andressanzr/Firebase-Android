package com.example.firebase_proyect.Fragment;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.example.firebase_proyect.R;

public class GruposFragment extends Fragment implements View.OnClickListener {
    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;
    public GruposFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_grupos, container, false);

        return v;
    }
    @Override
    public void onClick(View view){
    }

}