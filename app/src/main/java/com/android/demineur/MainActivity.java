package com.android.demineur;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private DemineurModel model;
    private GridLayout gridLayout;
    private Animation animation;
    private SeekBar widthSeekBar;
    private SeekBar heightSeekBar;
    private SeekBar minesSeekBar;
    private Dialog settingsDialog;
    private AlertDialog.Builder replayDialog;
    private View settingsLayout;
    private TextView widthValueText;
    private TextView heightValueText;
    private TextView minesValueText;
    private ImageButton flagButton;
    private TextView minesCountText;
    private long startTime;
    private Handler customHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_layout);
        animation = AnimationUtils.loadAnimation(this, R.anim.move);
        gridLayout = (GridLayout) findViewById(R.id.gridId);
        flagButton = (ImageButton) findViewById(R.id.flagButtonId);
        flagButton.setOnClickListener(setFlagModeListener);
        minesCountText = (TextView) findViewById(R.id.minesId);
        replayDialog = new AlertDialog.Builder(this);
        replayDialog.setMessage(getResources().getString(R.string.dialog_replay)).
                setPositiveButton(getResources().getString(R.string.yes), settingsDialogListener).
                setNegativeButton(getResources().getString(R.string.no), settingsDialogListener);
        customHandler = new Handler();
        startTime = 0L;
        newGame(5, 5, 4);
        initSettingsDialog();
    }

    private void initSettingsDialog() {
        settingsDialog = new Dialog(this);
        settingsDialog.setTitle(getResources().getString(R.string.settings));
        // Il faut désérialiser le layout correspondant aux paramètres pour pouvoir y accéder (inflater)
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        settingsLayout = inflater.inflate(R.layout.settings_layout, (ViewGroup) findViewById(R.id.layoutSettingsId));
        settingsDialog.setContentView(settingsLayout);
        widthSeekBar = (SeekBar) settingsLayout.findViewById(R.id.widthSeekBarSettingsId);
        heightSeekBar = (SeekBar) settingsLayout.findViewById(R.id.heightSeekBarSettingsId);
        minesSeekBar = (SeekBar) settingsLayout.findViewById(R.id.minesSeekBarSettingsId);
        widthSeekBar.setOnSeekBarChangeListener(seekBarListener);
        heightSeekBar.setOnSeekBarChangeListener(seekBarListener);
        minesSeekBar.setOnSeekBarChangeListener(seekBarListener);
        Button acceptButton = (Button) settingsLayout.findViewById(R.id.buttonAcceptSettingsId);
        Button cancelButton = (Button) settingsLayout.findViewById(R.id.buttonCancelSettingsId);
        acceptButton.setOnClickListener(settingsClickListener);
        cancelButton.setOnClickListener(settingsClickListener);
        widthValueText = (TextView) settingsLayout.findViewById(R.id.widthValueId);
        heightValueText = (TextView) settingsLayout.findViewById(R.id.heightValueId);
        minesValueText = (TextView) settingsLayout.findViewById(R.id.minesValueId);
        widthSeekBar.setProgress(model.getWidth());
        heightSeekBar.setProgress(model.getHeight());
        minesSeekBar.setProgress(model.getMines());
        widthSeekBar.setMax(DemineurModel.MAX_WIDTH);
        heightSeekBar.setMax(DemineurModel.MAX_HEIGHT);
        minesSeekBar.setMax(model.getHeight() * model.getWidth() - (model.getHeight() + model.getHeight()));
    }

    public void newGame(int width, int height, int mines) {
        model = new DemineurModel(width, height, mines);
        initGrid();
        gridLayout.startAnimation(animation);
        minesCountText.setText(getResources().getString(R.string.count_mines, model.getRemainingCountMines()));
        flagButton.setBackgroundResource(R.color.gameBackground);
        startTime = SystemClock.uptimeMillis();
        customHandler.postDelayed(updateTimerThread, 0);
    }

    private void initGrid() {
        gridLayout.removeAllViews();
        //gridLayout.setHorizontalScrollBarEnabled(true);
        //gridLayout.setVerticalScrollBarEnabled(true);
        gridLayout.setColumnCount(model.getWidth());
        gridLayout.setRowCount(model.getHeight());
        for(int i = 0; i < model.getHeight(); i++) {
            for(int j = 0; j < model.getWidth(); j++) {
                ImageView imageView = new ImageView(this);
                imageView.setImageResource(R.drawable.case_normale);
                imageView.setPadding(0, 0, 0, 0);
                imageView.setAdjustViewBounds(true);
                imageView.setTag(i + "-" + j);
                imageView.setOnClickListener(cellListener);
                gridLayout.addView(imageView, i * model.getWidth() + j);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newGameMenuId:
                newGame(model.getWidth(), model.getHeight(), model.getMines());
                return true;
            case R.id.settingsMenuId:
                settingsDialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(seekBar.getId() == R.id.widthSeekBarSettingsId)
                widthValueText.setText(getResources().getString(R.string.width_value, progress));
            else if(seekBar.getId() == R.id.heightSeekBarSettingsId)
                heightValueText.setText(getResources().getString(R.string.height_value, progress));
            else if(seekBar.getId() == R.id.minesSeekBarSettingsId)
                minesValueText.setText(getResources().getString(R.string.mines_value, progress));
            minesSeekBar.setMax(widthSeekBar.getProgress() * heightSeekBar.getProgress() - (widthSeekBar.getProgress() + heightSeekBar.getProgress()));
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };

    Button.OnClickListener settingsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.buttonAcceptSettingsId) {
                int width = Math.max(widthSeekBar.getProgress(), DemineurModel.MIN_WIDTH);
                int height = Math.max(heightSeekBar.getProgress(), DemineurModel.MIN_HEIGHT);
                int mines = Math.max(minesSeekBar.getProgress(), DemineurModel.MIN_MINES);
                newGame(width, height, mines);
                settingsDialog.dismiss();
            }
            else if(v.getId() == R.id.buttonCancelSettingsId) {
                settingsDialog.dismiss();
            }
        }
    };

    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            long timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            int secs = (int) (timeInMilliseconds / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            TextView timer = (TextView) findViewById(R.id.timeId);
            timer.setText(getResources().getString(R.string.timer, mins, secs));
            customHandler.postDelayed(this, 0);
        }
    };

    View.OnClickListener setFlagModeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            model.setFlagMode(!model.isFlagMode());

            if(model.isFlagMode())
                flagButton.setBackgroundResource(R.color.flagPressed);
            else
                flagButton.setBackgroundResource(R.color.gameBackground);
        }
    };

    DialogInterface.OnClickListener settingsDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    newGame(model.getWidth(), model.getHeight(), model.getMines());
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.dismiss();
                    break;
            }
        }
    };

    View.OnClickListener cellListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            ImageView imageView = (ImageView) v;
            String[] position = ((String) imageView.getTag()).split("-");
            int i = Integer.parseInt(position[0] + "");
            int j = Integer.parseInt(position[1] + "");
            model.move(i, j);
            minesCountText.setText(getResources().getString(R.string.count_mines, model.getRemainingCountMines()));
            updateGrid();
        }

    };

    private void updateGrid() {
        for(int i = 0; i < model.getHeight(); i++) {
            for (int j = 0; j < model.getWidth(); j++) {
                ImageView imageView = (ImageView) gridLayout.getChildAt(i * model.getWidth() + j);
                if(model.isMarked(i, j))
                    imageView.setImageResource(R.drawable.case_marquee_minee);
                else if(!model.isDiscovered(i, j)) {
                    imageView.setImageResource(R.drawable.case_normale);
                }
                else {
                    switch(model.getCell(i, j)) {
                        case MINE:
                            assert(false);
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
                if((model.isLost() || model.isWon()) ) {
                    if(model.getCell(i, j) == DemineurModel.Cell.MINE) {
                        if (!model.isDiscovered(i, j))
                            imageView.setImageResource(R.drawable.case_mine); // show all the mines if the game is over
                        else
                            imageView.setImageResource(R.drawable.case_mine_explosee); // the player lost here
                    }
                    else {
                        if(model.isMarked(i, j))
                            imageView.setImageResource(R.drawable.wrong_flag); // the player put a flag on a cell that is not a mine
                    }
                    imageView.setClickable(false);
                }
            }
        }
        if (model.isLost() || model.isWon()) {
            String result = model.isWon() ? getResources().getString(R.string.won) : getResources().getString(R.string.lost);
            Toast.makeText(MainActivity.this, getResources().getString(R.string.result, result), Toast.LENGTH_SHORT).show();
            replayDialog.setTitle(getResources().getString(R.string.result, result));
            replayDialog.show();
        }
    }

}