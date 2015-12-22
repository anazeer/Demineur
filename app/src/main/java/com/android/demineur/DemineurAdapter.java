package com.android.demineur;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

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
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            final int i = position / model.getWidth();
            final int j = position % model.getHeight();
            if((model.isLost() || model.isWon()) && model.getCell(i, j) == DemineurModel.Cell.MINE && !model.isDiscovered(i, j))
                imageView.setImageResource(R.drawable.case_mine); // show all the mines if the game is lost
            else if(model.isMarked(i, j))
                imageView.setImageResource(R.drawable.case_marquee_minee);
            else if(!model.isDiscovered(i, j)) {
                imageView.setImageResource(R.drawable.case_normale);
            }
            else {
                switch(model.getCell(i, j)) {
                    case MINE:
                        if(!model.isLost())
                            imageView.setImageResource(R.drawable.case_mine);
                        else
                            imageView.setImageResource(R.drawable.case_mine_explosee); // the player lost here
                        break;
                    case EMPTY:
                        imageView.setImageResource(R.drawable.case_0);
                        break;
                    case ONE:
                        imageView.setImageResource(R.drawable.case_1);
                        break;
                    case TWO:
                        imageView.setImageResource(R.drawable.case_2);
                        break;
                    case THREE:
                        imageView.setImageResource(R.drawable.case_3);
                        break;
                    case FOUR:
                        imageView.setImageResource(R.drawable.case_4);
                        break;
                    case FIVE:
                        imageView.setImageResource(R.drawable.case_5);
                        break;
                    case SIX:
                        imageView.setImageResource(R.drawable.case_6);
                        break;
                    case SEVEN:
                        imageView.setImageResource(R.drawable.case_7);
                        break;
                    case EIGHT:
                        imageView.setImageResource(R.drawable.case_8);
                        break;
                }
            }
            imageView.setPadding(0, 0, 0, 0);
            imageView.setAdjustViewBounds(true);
        }
        else
            imageView = (ImageView) convertView;
        notifyDataSetChanged();
        return imageView;
    }
}