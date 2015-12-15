package com.android.demineur;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.Toast;

public class DemineurAdapter extends BaseAdapter {

    private Context context;
    private DemineurModel model;

    public DemineurAdapter(Context context, DemineurModel model) {
        super();
        this.context = context;
        this.model = model;
    }

    @Override
    public int getCount() {
        return model.getHeight() * model.getWidth();
    }

    @Override
    public Object getItem(int position) {
        int i = position / model.getWidth();
        int j = position % model.getHeight();
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ImageButton imageButton;
        if (convertView == null) {
            imageButton = new ImageButton(context);
            imageButton.setImageResource(R.drawable.case_normale);
            imageButton.setPadding(0, 0, 0, 0);
            imageButton.setAdjustViewBounds(true);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "(" + position / model.getWidth() + ", " + position % model.getHeight() + ")", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
            imageButton = (ImageButton) convertView;
        return imageButton;
    }
}