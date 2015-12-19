package com.android.demineur;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
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
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private DemineurModel model;
    private GridView gridView;
    private Animation animation;
    private SeekBar widthSeekBar;
    private SeekBar heightSeekBar;
    private SeekBar minesSeekBar;
    private Dialog dialog;
    private View settingsLayout;
    private TextView widthValueText;
    private TextView heightValueText;
    private TextView minesValueText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_layout);
        animation = AnimationUtils.loadAnimation(this, R.anim.move);
        gridView = (GridView) findViewById(R.id.gridId);
        newGame(11, 11, 28);
        initSettingsDialog();
    }

    private void initSettingsDialog() {
        dialog = new Dialog(this);
        dialog.setTitle(getResources().getString(R.string.settings));
        // Il faut désérialiser le layout correspondant aux paramètres pour pouvoir y accéder (inflater)
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        settingsLayout = inflater.inflate(R.layout.settings_layout, (ViewGroup) findViewById(R.id.layoutSettingsId));
        dialog.setContentView(settingsLayout);
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
                dialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void newGame(int width, int height, int mines) {
        model = new DemineurModel(width, height, mines);
        gridView.setAdapter(new DemineurAdapter(this, model, gridView));
        gridView.setNumColumns(model.getWidth());
        gridView.startAnimation(animation);
    }

    SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(seekBar.getId() == R.id.widthSeekBarSettingsId)
                widthValueText.setText("" + progress);
            else if(seekBar.getId() == R.id.heightSeekBarSettingsId)
                heightValueText.setText("" + progress);
            else if(seekBar.getId() == R.id.minesSeekBarSettingsId)
                minesValueText.setText("" + progress);
            minesSeekBar.setMax(widthSeekBar.getProgress() * heightSeekBar.getProgress() - (widthSeekBar.getProgress() + heightSeekBar.getProgress()));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    Button.OnClickListener settingsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.buttonAcceptSettingsId) {
                int width = Math.max(widthSeekBar.getProgress(), DemineurModel.MIN_WIDTH);
                int height = Math.max(heightSeekBar.getProgress(), DemineurModel.MIN_HEIGHT);
                int mines = Math.max(minesSeekBar.getProgress(), DemineurModel.MIN_MINES);
                newGame(width, height, mines);
                dialog.dismiss();
            }
            else if(v.getId() == R.id.buttonCancelSettingsId) {
                dialog.dismiss();
            }
        }
    };
}