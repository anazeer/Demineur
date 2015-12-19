package com.android.demineur;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

public class DemineurAdapter extends BaseAdapter {

    private Context context;
    private DemineurModel model;
    private AlertDialog.Builder dialog;
    private Resources r;
    private GridView gridView;

    public DemineurAdapter(Context context, DemineurModel model, GridView gridView) {
        super();
        this.context = context;
        this.model = model;
        this.gridView = gridView;
        dialog = new AlertDialog.Builder(context);
        r = context.getResources();
        dialog.setMessage(r.getString(R.string.dialog_replay)).setPositiveButton(r.getString(R.string.yes), dialogListener).setNegativeButton(r.getString(R.string.no), dialogListener);
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
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ImageButton imageButton;
        if (convertView == null) {
            imageButton = new ImageButton(context);
            final int i = position / model.getWidth();
            final int j = position % model.getHeight();
            if((model.isLost() || model.isWon()) && model.getCell(i, j) == DemineurModel.Cell.MINE && !model.isDiscovered(i, j))
                imageButton.setImageResource(R.drawable.case_mine); // show all the mines if the game is lost
            else if(!model.isDiscovered(i, j)) {
                imageButton.setImageResource(R.drawable.case_normale);
            }
            else {
                switch(model.getCell(i, j)) {
                    case MINE:
                        if(!model.isLost())
                            imageButton.setImageResource(R.drawable.case_mine);
                        else
                            imageButton.setImageResource(R.drawable.case_mine_explosee); // the player lost here
                        break;
                    case EMPTY:
                        imageButton.setImageResource(R.drawable.case_0);
                        break;
                    case ONE:
                        imageButton.setImageResource(R.drawable.case_1);
                        break;
                    case TWO:
                        imageButton.setImageResource(R.drawable.case_2);
                        break;
                    case THREE:
                        imageButton.setImageResource(R.drawable.case_3);
                        break;
                    case FOUR:
                        imageButton.setImageResource(R.drawable.case_4);
                        break;
                    case FIVE:
                        imageButton.setImageResource(R.drawable.case_5);
                        break;
                    case SIX:
                        imageButton.setImageResource(R.drawable.case_6);
                        break;
                    case SEVEN:
                        imageButton.setImageResource(R.drawable.case_7);
                        break;
                    case EIGHT:
                        imageButton.setImageResource(R.drawable.case_8);
                        break;
                }
            }
            imageButton.setPadding(0, 0, 0, 0);
            imageButton.setAdjustViewBounds(true);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GridView gridView = (GridView) parent;
                    model.move(i, j);
                    //Toast.makeText(context, "(" + position / model.getWidth() + ", " + position % model.getHeight() + ")", Toast.LENGTH_SHORT).show();
                    //DemineurAdapter.this.notifyDataSetChanged();
                    if(model.isLost() || model.isWon()) {
                        String result = model.isWon() ? r.getString(R.string.won) : r.getString(R.string.lost);
                        Toast.makeText(context, r.getString(R.string.result, result), Toast.LENGTH_SHORT).show();
                        dialog.setTitle(r.getString(R.string.result, result));
                        dialog.show();
                    }
                    gridView.setAdapter(new DemineurAdapter(DemineurAdapter.this.context, model, gridView));
                }
            });
        }
        else
            imageButton = (ImageButton) convertView;
        if(model.isLost() || model.isWon()) {
            imageButton.setClickable(false);
        }
        return imageButton;
    }

    private void newGame(GridView gridView) {
        model = new DemineurModel(model.getWidth(), model.getHeight());
        gridView.setAdapter(new DemineurAdapter(context, model, gridView));
        gridView.setNumColumns(model.getWidth());
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.move);
        gridView.startAnimation(animation);
    }

    DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    newGame(gridView);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.dismiss();
                    break;
            }
        }
    };

}