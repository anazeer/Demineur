package com.android.demineur;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.GridView;

public class MainActivity extends AppCompatActivity {

    private DemineurModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_layout);
        model = new DemineurModel(20, 20);
        GridView gridView = (GridView) findViewById(R.id.gridId);
        gridView.setNumColumns(model.getWidth());
        DemineurAdapter demineurAdapter = new DemineurAdapter(this, model);
        gridView.setAdapter(demineurAdapter);
    }

}