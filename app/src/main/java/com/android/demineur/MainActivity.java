package com.android.demineur;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private DemineurModel model;
    private GridLayout gridLayout;
    private Animation animation;
    private SeekBar widthSeekBar;
    private SeekBar heightSeekBar;
    private SeekBar minesSeekBar;
    private Dialog settingsDialog;
    private AlertDialog.Builder replayDialog;
    private Menu menu;
    private TextView widthValueText;
    private TextView heightValueText;
    private TextView minesValueText;
    private ImageButton flagButton;
    private TextView minesCountText;
    private TextView timeText;
    private CustomHandler customHandler;
    private Timer timer;
    private SharedPreferences preferences;

    private ListView musicList;
    private Cursor musicCursor;
    private Dialog musicDialog;
    private ArrayAdapter<String> musicAdapter;
    private MediaPlayer mediaPlayer;
    private Button resumeButton;
    private Button pauseButton;
    private Button stopButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setContentView(R.layout.grid_layout);
        gridLayout = (GridLayout) findViewById(R.id.gridId);
        flagButton = (ImageButton) findViewById(R.id.flagButtonId);
        flagButton.setOnClickListener(setFlagModeListener);
        minesCountText = (TextView) findViewById(R.id.minesId);
        timeText = (TextView) findViewById(R.id.timeId);
        replayDialog = new AlertDialog.Builder(this);
        replayDialog.setMessage(getResources().getString(R.string.dialog_replay)).
                setPositiveButton(getResources().getString(R.string.yes), settingsDialogListener).
                setNegativeButton(getResources().getString(R.string.no), settingsDialogListener);
        customHandler = new CustomHandler();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mediaPlayer = new MediaPlayer();
        initModel();
        initSettingsDialog();
        initMusicDialog();
    }

    @Override
    protected void onPause() {
        super.onPause();
        model.setPause(true);
        stopTimer();
        stopMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(model.isGameOver())
            return;
        model.setPause(false);
        initTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        model.setPause(true);
        stopTimer();
        stopMusic();
        Gson gson = new Gson();
        String json = gson.toJson(model);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putString("model", json);
        preferencesEditor.apply();
    }

    /**
     * If the configuration changes, saves the model
     * @return the model
     */
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        stopTimer();
        return model;
    }

    /**
     * Retrieves the model if it has been saved, otherwise creates a new model
     */
    private void initModel() {
        model = (DemineurModel) getLastCustomNonConfigurationInstance();
        if(model == null) {
            Gson gson = new Gson();
            String json = preferences.getString("model", "");
            model = gson.fromJson(json, DemineurModel.class);
            if(model != null && model.isGameOver())
                model = null;
        }
        if(model != null)
            restartGame();
        else {
            String[] gridSizes = getResources().getStringArray(R.array.grid_size_array);
            String pref = preferences.getString("gridSizePrefId", gridSizes[0]);
            if (pref.equals(gridSizes[0]))
                newGame(9, 9, 10);
            else if (pref.equals(gridSizes[1]))
                newGame(16, 16, 40);
            else if (pref.equals(gridSizes[2]))
                newGame(30, 16, 99);
        }
    }

    private void initSettingsDialog() {
        settingsDialog = new Dialog(this);
        settingsDialog.setTitle(getResources().getString(R.string.settings));
        // Il faut désérialiser le layout correspondant aux paramètres pour pouvoir y accéder (inflater)
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View settingsLayout = inflater.inflate(R.layout.settings_layout, (ViewGroup) findViewById(R.id.layoutSettingsId));
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
        minesSeekBar.setMax(model.getMaxMines(model.getHeight(), model.getWidth()));
    }

    private void initMusicDialog() {
        musicDialog = new Dialog(this);
        musicDialog.setTitle(getResources().getString(R.string.musicDialog));
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View musicLayout = inflater.inflate(R.layout.music_layout, (ViewGroup) findViewById(R.id.musicLayoutId));
        musicDialog.setContentView(musicLayout);
        String[] projection = { MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE,
                MediaStore.Audio.Media.DURATION};
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        musicCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, MediaStore.Audio.Media.TITLE + " ASC");
        musicList = (ListView) musicLayout.findViewById(R.id.musicListId);
        List<String> list = new ArrayList<>();
        while(musicCursor.moveToNext()) {
            list.add(musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
        }
        musicAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        musicList.setAdapter(musicAdapter);
        musicList.setOnItemClickListener(musicListener);
        resumeButton = (Button) musicLayout.findViewById(R.id.resumeMusicButtonId);
        pauseButton = (Button) musicLayout.findViewById(R.id.pauseMusicButtonId);
        stopButton = (Button) musicLayout.findViewById(R.id.stopMusicButtonId);
        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                musicDialog.dismiss();
                resumeButton.setEnabled(false);
                pauseButton.setEnabled(true);
                stopButton.setEnabled(true);
                MenuItem musicItem = menu.findItem(R.id.musicMenuId);
                musicItem.setIcon(R.drawable.musique_bleu);
            }
        });
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying())
                    mediaPlayer.pause();
                resumeButton.setEnabled(true);
                pauseButton.setEnabled(false);
                stopButton.setEnabled(true);
                MenuItem musicItem = menu.findItem(R.id.musicMenuId);
                musicItem.setIcon(R.drawable.musique);
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying())
                    mediaPlayer.reset();
                musicDialog.dismiss();
                resumeButton.setEnabled(false);
                pauseButton.setEnabled(false);
                stopButton.setEnabled(false);
                MenuItem musicItem = menu.findItem(R.id.musicMenuId);
                musicItem.setIcon(R.drawable.musique);
            }
        });
        resumeButton.setEnabled(false);
        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);

    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            MenuItem musicItem = menu.findItem(R.id.musicMenuId);
            musicItem.setIcon(R.drawable.musique);
        }
        musicCursor.close();
    }

    public void newGame(int width, int height, int mines) {
        stopTimer();
        model = new DemineurModel(width, height, mines);
        initGrid();
        if(preferences.getBoolean("animPrefId", true)) {
            animation = AnimationUtils.loadAnimation(this, R.anim.move);
            gridLayout.startAnimation(animation);
        }
        minesCountText.setText(getResources().getString(R.string.count_mines, model.getRemainingCountMines()));
        timeText.setText(getResources().getString(R.string.timer, 0, 0));
        flagButton.setImageResource(R.drawable.just_flag);
        if(menu != null)
            updateJokerButton();
    }

    private void restartGame() {
        initGrid();
        updateGrid();
        updateFlagButton();
        int time = model.getElapsedTime();
        Message msg = customHandler.obtainMessage(0, time/60, time%60, timeText);
        customHandler.sendMessage(msg);
    }

    private void lose() {
        if(preferences.getBoolean("animPrefId", true)) {
            animation = AnimationUtils.loadAnimation(this, R.anim.explosion);
            gridLayout.startAnimation(animation);
        }
        if(preferences.getBoolean("vibrationPrefId", true)) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(500);
        }
    }

    private void initGrid() {
        gridLayout.removeAllViews();
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
                imageView.setOnLongClickListener(cellLongListener);
                gridLayout.addView(imageView, i * model.getWidth() + j);
            }
        }
    }

    /**
     * Initialize the timer, increments the time each second
     */
    private void initTimer() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if(!model.isPause()) {
                    int time = model.getElapsedTime();
                    time++;
                    model.setElapsedTime(time);
                    Message msg = customHandler.obtainMessage(0, time/60, time%60, timeText);
                    customHandler.sendMessage(msg);
                }
            }
        };
        timer = new Timer();
        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    private void stopTimer() {
        if(timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        this.menu = menu;
        updateJokerButton();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.restartMenuId:
                newGame(model.getWidth(), model.getHeight(), model.getMines());
                return true;
            case R.id.newBeginnerMenuId:
                newGame(9, 9, 10);
                return true;
            case R.id.newIntermediateMenuId:
                newGame(16, 16, 40);
                return true;
            case R.id.newExpertMenuId:
                newGame(30, 16, 99);
                return true;
            case R.id.newRandomMenuId:
                Random rand = new Random();
                int width = rand.nextInt(DemineurModel.MAX_WIDTH - DemineurModel.MIN_WIDTH) + DemineurModel.MIN_WIDTH;
                int height = rand.nextInt(DemineurModel.MAX_HEIGHT - DemineurModel.MIN_HEIGHT) + DemineurModel.MIN_HEIGHT;
                int mines = rand.nextInt(model.getMaxMines(height, width) - DemineurModel.MIN_MINES) + DemineurModel.MIN_MINES;
                newGame(width, height, mines);
                return true;
            case R.id.newCustomMenuId:
                settingsDialog.show();
                break;
            case R.id.musicMenuId:
                musicDialog.show();
                break;
            case R.id.safeJokerMenuId:
                model.activateSafeModeJoker();
                updateJokerButton();
                return true;
            case R.id.burstJokerMenuId:
                model.activateBurstModeJoker();
                updateJokerButton();
                return true;
            case R.id.settingsMenuId:
                Intent prefActivity = new Intent(this, DemineurPreference.class);
                startActivity(prefActivity);
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
            minesSeekBar.setMax(model.getMaxMines(heightSeekBar.getProgress(), widthSeekBar.getProgress()));
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

    View.OnClickListener setFlagModeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            model.setFlagMode(!model.isFlagMode());
            updateFlagButton();
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

    AdapterView.OnItemClickListener musicListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            int musicIndex = musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            musicCursor.moveToPosition(position);
            String filename = musicCursor.getString(musicIndex);
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(filename);
                mediaPlayer.prepare();
                mediaPlayer.start();
                musicDialog.dismiss();
                resumeButton.setEnabled(false);
                pauseButton.setEnabled(true);
                stopButton.setEnabled(true);
                MenuItem musicItem = menu.findItem(R.id.musicMenuId);
                musicItem.setIcon(R.drawable.musique_bleu);
            } catch(Exception e) {
                System.out.println(e);
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
            updateJokerButton();
            if(timer == null)
                initTimer(); // the cloak starts after the first move
            updateGrid();
        }

    };

    View.OnLongClickListener cellLongListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            boolean flag = model.isFlagMode();
            model.setFlagMode(true);
            v.performClick();
            model.setFlagMode(flag);
            if(preferences.getBoolean("vibrationPrefId", true)) {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(50);
            }
            return true;
        }
    };

    private void updateFlagButton() {
        if(model.isFlagMode())
            flagButton.setImageResource(R.drawable.flag_red);
        else
            flagButton.setImageResource(R.drawable.just_flag);
    }

    private void updateJokerButton() {
        MenuItem jokerItem = menu.findItem(R.id.jokerMenuId);
        MenuItem safeJokerItem = menu.findItem(R.id.safeJokerMenuId);
        MenuItem burstJokerItem = menu.findItem(R.id.burstJokerMenuId);
        jokerItem.setIcon(R.drawable.joker);
        if(model.isSafeModeJoker() || model.isBurstModeJoker()) {
            jokerItem.setIcon(R.drawable.joker_jaune);
            if(model.isSafeModeJoker())
                Toast.makeText(this, getResources().getString(R.string.safeModeActivated), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, getResources().getString(R.string.burstModeActivated), Toast.LENGTH_SHORT).show();
        }
        else
            jokerItem.setIcon(R.drawable.joker);
        if(model.isSafeJokerUsed()) {
            safeJokerItem.setIcon(R.drawable.croix);
            safeJokerItem.setEnabled(false);
        }
        else {
            safeJokerItem.setIcon(null);
            safeJokerItem.setEnabled(true);
        }
        if(model.isBurstJokerUsed()){
            burstJokerItem.setIcon(R.drawable.croix);
            burstJokerItem.setEnabled(false);
        }
        else {
            burstJokerItem.setIcon(null);
            burstJokerItem.setEnabled(true);
        }
    }

    private void updateGrid() {
        minesCountText.setText(getResources().getString(R.string.count_mines, model.getRemainingCountMines()));
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
                    stopTimer();
                    if(model.getCell(i, j) == DemineurModel.Cell.MINE) {
                        if (!model.isDiscovered(i, j)) {
                            if (model.isMarked(i, j))
                                imageView.setImageResource(R.drawable.case_mine_trouvee); // the player put a flag on a cell that is a mine
                            else
                                imageView.setImageResource(R.drawable.case_mine); // show all the mines if the game is over
                        }
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
        if (model.isWon()) {
            String result = model.isWon() ? getResources().getString(R.string.won) : getResources().getString(R.string.lost);
            Toast.makeText(MainActivity.this, getResources().getString(R.string.result, result), Toast.LENGTH_SHORT).show();
            replayDialog.setTitle(getResources().getString(R.string.result, result));
            replayDialog.show();
        }
        else if(model.isLost()) {
            lose();
        }
    }

}