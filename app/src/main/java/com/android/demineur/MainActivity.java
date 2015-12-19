package com.android.demineur;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridView;

public class MainActivity extends AppCompatActivity {

    private DemineurModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_layout);

        model = new DemineurModel(11, 11);
        GridView gridView = (GridView) findViewById(R.id.gridId);
        gridView.setNumColumns(model.getWidth());
        DemineurAdapter demineurAdapter = new DemineurAdapter(this, model, gridView);
        gridView.setAdapter(demineurAdapter);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.move);
        gridView.startAnimation(animation);
    }

}