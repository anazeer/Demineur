package com.android.demineur;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {

    /**
     * Game operations
     */
    private DemineurModel model;
    private AlertDialog.Builder replayDialog;
    private Menu menu;

    /**
     * UI operations
     */
    private GridLayout gridLayout;
    private ImageButton flagButton;
    private TextView minesCountText;
    private Animation animation;

    /**
     * Settings operation
     */
    private Dialog settingsDialog;
    private SeekBar widthSeekBar;
    private SeekBar heightSeekBar;
    private SeekBar minesSeekBar;
    private TextView widthValueText;
    private TextView heightValueText;
    private TextView minesValueText;

    /**
     * Timer operations
     */
    private Timer timer;
    private TextView timeText;
    private CustomHandler customHandler;

    /**
     * Scores operations
     */
    private Dialog scoreDialog;
    private Dialog winDialog;

    /**
     * Help operations
     */
    private Dialog helpDialog;

    /**
     * Music operations
     */
    private MediaPlayer mediaPlayer;
    private Cursor musicCursor;
    private Dialog musicDialog;
    private Button resumeButton;
    private Button pauseButton;
    private Button stopButton;

    /**
     * Preferences operations
     */
    private SharedPreferences preferences;
    public final static String prefMusic = "musicPrefId";
    public final static String prefMusicPaused = "musicPaused";
    public final static String prefMusicTitle = "lastMusicTitle";
    public final static String prefMusicLength = "musicLength";
    public final static String prefModel = "model";
    public final static String prefScoreBeginner = "beginnerScore";
    public final static String prefScoreIntermediate = "intermediateScore";
    public final static String prefScoreExpert = "expertScore";
    public final static String prefGridSize = "gridSizePrefId";
    public final static String prefAnimation = "animPrefId";
    public final static String prefVibration = "vibrationPrefId";
    public final static String prefGameTotal = "gameTotal";
    public final static String prefGameWin = "gameWin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getSupportActionBar() != null; // Prevent from the possible NullPointerException warning
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Un-show game title in the action bar
        setContentView(R.layout.grid_layout);
        gridLayout = (GridLayout) findViewById(R.id.gridId);
        flagButton = (ImageButton) findViewById(R.id.flagButtonId);
        flagButton.setOnClickListener(flagModeListener);
        minesCountText = (TextView) findViewById(R.id.minesId);
        timeText = (TextView) findViewById(R.id.timeId);
        replayDialog = new AlertDialog.Builder(this);
        replayDialog.setTitle(getResources().getString(R.string.game_win));
        replayDialog.setMessage(getResources().getString(R.string.dialog_replay)).
                setPositiveButton(getResources().getString(R.string.yes), settingsDialogListener).
                setNegativeButton(getResources().getString(R.string.no), settingsDialogListener);
        customHandler = new CustomHandler();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        initModel();
        initSettingsDialog();
        initScoreDialog();
        initNewBestScoreDialog();
        initHelpDialog();
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
        initMusic();
        if(preferences.getBoolean(prefMusic, false) && preferences.getBoolean(prefMusicPaused, false))
            startMusic(preferences.getString(prefMusicTitle, ""), preferences.getInt(prefMusicLength, 0));
        if(!model.isGameOver())
            initTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        model.setPause(true);
        stopTimer();
        Gson gson = new Gson();
        String json = gson.toJson(model);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putString(prefModel, json);
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
     * Initialize the custom game dialog interface
     */
    private void initSettingsDialog() {
        settingsDialog = new Dialog(this);
        settingsDialog.setTitle(getResources().getString(R.string.settings));
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View settingsLayout = inflater.inflate(R.layout.settings_layout, (ViewGroup) findViewById(R.id.layoutSettingsId)); // We need to inflate the layout in order to access its elements
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
        settingsDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                widthSeekBar.setProgress(model.getWidth());
                heightSeekBar.setProgress(model.getHeight());
                minesSeekBar.setProgress(model.getMines());
                widthSeekBar.setMax(DemineurModel.MAX_WIDTH);
                heightSeekBar.setMax(DemineurModel.MAX_HEIGHT);
                minesSeekBar.setMax(model.getMaxMines(model.getHeight(), model.getWidth()));
            }
        });
    }

    /**
     * Initialize the scores dialog interface
     */
    private void initScoreDialog() {
        scoreDialog = new Dialog(this);
        scoreDialog.setTitle(getResources().getString(R.string.score));
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View scoreLayout = inflater.inflate(R.layout.score_layout, (ViewGroup) findViewById(R.id.scoreLayoutId)); // We need to inflate the layout in order to access its elements
        scoreDialog.setContentView(scoreLayout);
        final TextView[] scoreTextViews = new TextView[9];
        scoreTextViews[0] = (TextView) scoreLayout.findViewById(R.id.firstBeginnerScoreId);
        scoreTextViews[1] = (TextView) scoreLayout.findViewById(R.id.secondBeginnerScoreId);
        scoreTextViews[2] = (TextView) scoreLayout.findViewById(R.id.thirdBeginnerScoreId);
        scoreTextViews[3] = (TextView) scoreLayout.findViewById(R.id.firstIntermediateScoreId);
        scoreTextViews[4] = (TextView) scoreLayout.findViewById(R.id.secondIntermediateScoreId);
        scoreTextViews[5] = (TextView) scoreLayout.findViewById(R.id.thirdIntermediateScoreId);
        scoreTextViews[6] = (TextView) scoreLayout.findViewById(R.id.firstExpertScoreId);
        scoreTextViews[7] = (TextView) scoreLayout.findViewById(R.id.secondExpertScoreId);
        scoreTextViews[8] = (TextView) scoreLayout.findViewById(R.id.thirdExpertScoreId);
        final TextView totalScoreTextView = (TextView) scoreLayout.findViewById(R.id.totalScoreId);
        final TextView winScoreTextView = (TextView) scoreLayout.findViewById(R.id.winScoreId);
        final TextView winPercentageScoreTextView = (TextView) scoreLayout.findViewById(R.id.percentageScoreId);
        final String[] keys = {prefScoreBeginner, prefScoreIntermediate, prefScoreExpert};
        Button clearButton = (Button) scoreLayout.findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(prefScoreBeginner, null).putString(prefScoreIntermediate, null).putString(prefScoreExpert, null).
                        putInt(prefGameWin, 0).putInt(prefGameTotal, 0).apply();
                for(int i = 0; i < keys.length; i++) {
                    String[] bestScores = getBestScores(keys[i]);
                    for(int j = 0; j < scoreTextViews.length / keys.length; j++)
                        scoreTextViews[i * keys.length + j].setText(bestScores[j]);
                }
                int totalGameCount = preferences.getInt(prefGameTotal, 0);
                int winGameCount = preferences.getInt(prefGameWin, 0);
                double winPercentage = totalGameCount == 0 ? 0 : (double) winGameCount * 100 / (double) totalGameCount;
                totalScoreTextView.setText(getResources().getString(R.string.scoreTotalLine, totalGameCount));
                winScoreTextView.setText(getResources().getString(R.string.scoreWinLine, winGameCount));
                winPercentageScoreTextView.setText(getResources().getString(R.string.scorePercentageLine, winPercentage));
            }
        });
        scoreDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                for(int i = 0; i < keys.length; i++) {
                    String[] bestScores = getBestScores(keys[i]);
                    for(int j = 0; j < scoreTextViews.length / keys.length; j++)
                        scoreTextViews[i * keys.length + j].setText(bestScores[j]);
                }
                int totalGameCount = preferences.getInt(prefGameTotal, 0);
                int winGameCount = preferences.getInt(prefGameWin, 0);
                double winPercentage = totalGameCount == 0 ? 0 : (double) winGameCount * 100 / (double) totalGameCount;
                totalScoreTextView.setText(getResources().getString(R.string.scoreTotalLine, totalGameCount));
                winScoreTextView.setText(getResources().getString(R.string.scoreWinLine, winGameCount));
                winPercentageScoreTextView.setText(getResources().getString(R.string.scorePercentageLine, winPercentage));
            }
        });
    }

    /**
     * Initialize the new best score dialog interface
     */
    private void initNewBestScoreDialog() {
        winDialog = new Dialog(this);
        winDialog.setTitle(getResources().getString(R.string.game_win));
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View winLayout = inflater.inflate(R.layout.win_layout, (ViewGroup) findViewById(R.id.winLayoutId));
        winDialog.setContentView(winLayout);
        Button replayButton = (Button) winLayout.findViewById(R.id.replayButtonId);
        Button notReplayButton = (Button) winLayout.findViewById(R.id.notReplayButtonId);
        replayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newGame(model.getWidth(), model.getHeight(), model.getMines());
                winDialog.dismiss();
            }
        });
        notReplayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                winDialog.dismiss();
            }
        });
        winDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                TextView modeText = (TextView) winLayout.findViewById(R.id.modeTextId);
                TextView []scores = new TextView[3];
                scores[0] = (TextView) winLayout.findViewById(R.id.firstScoreId);
                scores[1] = (TextView) winLayout.findViewById(R.id.secondScoreId);
                scores[2] = (TextView) winLayout.findViewById(R.id.thirdScoreId);

                String[] bestScores;
                if (model.getWidth() == 9) {
                    modeText.setText(getResources().getString(R.string.beginner));
                    bestScores = getBestScores(prefScoreBeginner);
                } else if (model.getWidth() == 16) {
                    modeText.setText(getResources().getString(R.string.intermediate));
                    bestScores = getBestScores(prefScoreIntermediate);
                } else {
                    modeText.setText(getResources().getString(R.string.expert));
                    bestScores = getBestScores(prefScoreExpert);
                }
                // Show the three best scores and make the new score line bold
                boolean bold = false;
                for(int i = 0; i < 3; i++) {
                    scores[i].setText(bestScores[i]);
                    String[] times = bestScores[i].substring(2).split(":");
                    if(times.length == 2) {
                        int min = Integer.parseInt(times[0]);
                        int sec = Integer.parseInt(times[1]);
                        if(min * 60 + sec == model.getElapsedTime() && !bold) {
                            scores[i].setTypeface(null, Typeface.BOLD);
                            bold = true;
                        }
                        else
                            scores[i].setTypeface(null, Typeface.NORMAL);
                    }
                    else
                        scores[i].setTypeface(null, Typeface.NORMAL);
                }
            }
        });
    }

    /**
     * Initialize the help dialog interface
     */
    private void initHelpDialog() {
        helpDialog = new Dialog(this);
        helpDialog.setTitle(getResources().getString(R.string.help));
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View helpLayout = inflater.inflate(R.layout.help_layout, (ViewGroup) findViewById(R.id.helpLayoutId)); // We need to inflate the layout in order to access its elements
        helpDialog.setContentView(helpLayout);
    }

    /**
     * Initialize the music chooser dialog interface
     */
    private void initMusicDialog() {
        musicDialog = new Dialog(this);
        musicDialog.setTitle(getResources().getString(R.string.musicDialog));
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View musicLayout = inflater.inflate(R.layout.music_layout, (ViewGroup) findViewById(R.id.musicLayoutId)); // We need to inflate the layout in order to access its elements
        musicDialog.setContentView(musicLayout);

        // Initialize the music list
        initMusic();
        List<String> list = new ArrayList<>();
        while(musicCursor != null && musicCursor.moveToNext())
            list.add(musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
        ArrayAdapter<String> musicAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        ListView musicList = (ListView) musicLayout.findViewById(R.id.musicListId);
        musicList.setAdapter(musicAdapter);
        musicList.setOnItemClickListener(musicListener);

        // Initialize the resume, pause and stop buttons of the dialog
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
                if (mediaPlayer.isPlaying())
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
                if (mediaPlayer.isPlaying())
                    mediaPlayer.reset();
                musicDialog.dismiss();
                resumeButton.setEnabled(false);
                pauseButton.setEnabled(false);
                stopButton.setEnabled(false);
                MenuItem musicItem = menu.findItem(R.id.musicMenuId);
                musicItem.setIcon(R.drawable.musique);
            }
        });
        musicDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if (mediaPlayer.isPlaying()) { // The media is playing
                    pauseButton.setEnabled(true);
                    stopButton.setEnabled(true);
                    resumeButton.setEnabled(false);
                } else if (resumeButton.isEnabled()) { // The media is paused
                    resumeButton.setEnabled(true);
                    pauseButton.setEnabled(false);
                    stopButton.setEnabled(true);
                } else { // The media is stopped
                    resumeButton.setEnabled(false);
                    pauseButton.setEnabled(false);
                    stopButton.setEnabled(false);
                }
            }
        });
        resumeButton.setEnabled(false);
        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);
    }

    /**
     * Initialize a new grid with fresh ImageViews
     */
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
        model.setPause(false);
    }

    /**
     * Properly stop the timer
     */
    private void stopTimer() {
        if(timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    /**
     * Create the music cursor
     */
    private void initMusic() {
        mediaPlayer = new MediaPlayer();
        String[] projection = { MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION};
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        musicCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, MediaStore.Audio.Media.TITLE + " ASC");
    }

    /**
     * Start the given music
     */
    private void startMusic(String filename, int time) {
        try {
            preferences.edit().putString(prefMusicTitle, filename).apply();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(filename);
            mediaPlayer.prepare();
            mediaPlayer.seekTo(time);
            mediaPlayer.start();
            musicDialog.dismiss();
            resumeButton.setEnabled(false);
            pauseButton.setEnabled(true);
            stopButton.setEnabled(true);
        } catch(Exception e) {
            Log.e("MainActivity", "StartMusic exception", e);
            resumeButton.setEnabled(false);
            pauseButton.setEnabled(false);
            stopButton.setEnabled(false);
        }
    }

    /**
     * Stop music service before exiting the application
     */
    private void stopMusic() {
        SharedPreferences.Editor edit = preferences.edit();
        if(mediaPlayer.isPlaying()) {
            edit.putBoolean(prefMusicPaused, true);
            edit.putInt(prefMusicLength, mediaPlayer.getCurrentPosition());
        }
        else {
            edit.putBoolean(prefMusicPaused, false);
        }
        edit.apply();
        if(mediaPlayer != null)
            mediaPlayer.release();
        if(musicCursor != null)
            musicCursor.close();
    }

    /**
     * Retrieves the model if it has been saved, otherwise creates a new model
     */
    private void initModel() {
        model = (DemineurModel) getLastCustomNonConfigurationInstance();
        if(model == null) {
            Gson gson = new Gson();
            String json = preferences.getString(prefModel, "");
            model = gson.fromJson(json, DemineurModel.class);
            if(model != null && model.isGameOver()) // Begin a new game if the old one is done or hasn't started
                model = null;
        }
        if(model != null)
            restartGame();
        else {
            String[] gridSizes = getResources().getStringArray(R.array.grid_size_array);
            String pref = preferences.getString(prefGridSize, gridSizes[0]);
            if (pref.equals(gridSizes[0]))
                newGame(9, 9, 10);
            else if (pref.equals(gridSizes[1]))
                newGame(16, 16, 40);
            else if (pref.equals(gridSizes[2]))
                newGame(30, 16, 99);
        }
    }

    /**
     * Begin a new game with a new model
     */
    private void newGame(int width, int height, int mines) {
        stopTimer();
        model = new DemineurModel(width, height, mines);
        initGrid();
        if(preferences.getBoolean(prefAnimation, true)) {
            animation = AnimationUtils.loadAnimation(this, R.anim.move);
            gridLayout.startAnimation(animation);
        }
        minesCountText.setText(getResources().getString(R.string.count_mines, model.getRemainingCountMines()));
        timeText.setText(getResources().getString(R.string.timer, 0, 0));
        flagButton.setImageResource(R.drawable.just_flag);
        if(menu != null)
            updateJokerButton();
    }

    /**
     * Initialize the UI with an existing model
     */
    private void restartGame() {
        initGrid();
        updateGrid();
        updateFlagButton();
        int time = model.getElapsedTime();
        Message msg = customHandler.obtainMessage(0, time/ 60, time%60, timeText);
        customHandler.sendMessage(msg);
    }

    /**
     * Show the winning dialog and replay buttons
     */
    private void win() {
        boolean updated = updateScore();
        Toast.makeText(MainActivity.this, getResources().getString(R.string.game_win), Toast.LENGTH_SHORT).show();
        SharedPreferences.Editor edit = preferences.edit();
        edit.putInt(prefGameWin, preferences.getInt(prefGameWin, 0) + 1);
        edit.putInt(prefGameTotal, preferences.getInt(prefGameTotal, 0) + 1);
        edit.apply();
        if(updated)
            winDialog.show();
        else
            replayDialog.show();
    }

    /**
     * Play the lose animation
     */
    private void lose() {
        if(preferences.getBoolean(prefAnimation, true)) {
            animation = AnimationUtils.loadAnimation(this, R.anim.explosion);
            gridLayout.startAnimation(animation);
        }
        if(preferences.getBoolean(prefVibration, true)) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(500);
        }
        preferences.edit().putInt(prefGameTotal, preferences.getInt(prefGameTotal, 0) + 1).apply();
    }

    /**
     * Update high score
     * @return true if scores have been updated
     */
    private boolean updateScore() {
        String key = null;
        if(model.getWidth() == 9 && model.getHeight() == 9 && model.getMines() == 10) {
            key = prefScoreBeginner;
        }
        else if(model.getWidth() == 16 && model.getHeight() == 16 && model.getMines() == 40) {
            key = prefScoreIntermediate;
        }
        else if(model.getWidth() == 30 && model.getHeight() == 16 && model.getMines() == 99) {
            key = prefScoreExpert;
        }
        if(key == null)
            return false;
        String newScore = model.getElapsedTime() + "";
        Set<String> score = preferences.getStringSet(key, null);
        TreeSet<String> orderedScore = new TreeSet<>(new Comparator<String>(){
            public int compare(String a, String b){
                return Integer.valueOf(a).compareTo(Integer.valueOf(b));
            }
        });
        if(score == null)
            orderedScore.add(newScore);
        else {
            orderedScore.addAll(score);
            if(orderedScore.size() == 3 ) {
                String higher = orderedScore.higher(newScore);
                if(higher != null) {
                    if(!orderedScore.contains(newScore)) {
                        orderedScore.remove(orderedScore.last());
                        orderedScore.add(newScore);
                    }
                }
                else
                    return false;
            }
            else
                orderedScore.add(newScore);
        }
        preferences.edit().putStringSet(key, orderedScore).apply();
        return true;
    }

    /**
     * Update the given mode score displayed in the score dialog
     * @param key : the mode key
     * @return the string array containing the three best scores in the given mode
     */
    private String[] getBestScores(String key) {
        Set<String> scores = preferences.getStringSet(key, null);
        String[] result = new String[3];
        if(scores == null)
            for(int i = 0; i < 3; i++)
                result[i] = getResources().getString(R.string.noScore);
        else {
            TreeSet<String> orderedScore = new TreeSet<>(new Comparator<String>(){
                public int compare(String a, String b){
                    return Integer.valueOf(a).compareTo(Integer.valueOf(b));
                }
            });
            orderedScore.addAll(scores);
            int i = 0;
            for(String score : orderedScore) {
                int intScore = Integer.parseInt(score);
                result[i] = getResources().getString(R.string.timer, intScore / 60, intScore % 60);
                i++;
            }
            for(; i < 3; i++) {
               result[i] = getResources().getString(R.string.noScore);
            }
        }
        return result;
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
    public boolean onMenuOpened(int featureId, Menu menu) {
        boolean bool = super.onMenuOpened(featureId, menu);
        updateMusicButton();
        return bool;
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
            case R.id.scoreMenuId:
                scoreDialog.show();
                break;
            case R.id.helpMenuId:
                helpDialog.show();
                break;
            case R.id.musicMenuId:
                musicDialog.show();
                updateMusicButton();
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

    /**
     * The custom game menu SeekBars listener
     */
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

    /**
     * The custom game settings dialog listener
     */
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

    /**
     * The flag button listener
     */
    View.OnClickListener flagModeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            model.setFlagMode(!model.isFlagMode());
            if(model.isBurstModeJoker() || model.isSafeModeJoker()) {
                model.deactivateJoker();
                updateJokerButton();
            }
            updateFlagButton();
        }
    };

    /**
     * The replay dialog buttons listener
     */
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

    /**
     * The music list listener
     */
    AdapterView.OnItemClickListener musicListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            int musicIndex = musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            musicCursor.moveToPosition(position);
            String filename = musicCursor.getString(musicIndex);
            startMusic(filename, 0);
        }
    };

    /**
     * The cells click listener
     */
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

    /**
     * The cells long click listener
     */
    View.OnLongClickListener cellLongListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            boolean flag = model.isFlagMode();
            model.setFlagMode(true);
            v.performClick();
            model.setFlagMode(flag);
            if(preferences.getBoolean(prefVibration, true)) {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(50);
            }
            return true;
        }
    };

    /**
     * Update the flag button icon
     */
    private void updateFlagButton() {
        if(model.isFlagMode())
            flagButton.setImageResource(R.drawable.flag_red);
        else
            flagButton.setImageResource(R.drawable.just_flag);
    }

    /**
     * Update the joker menu buttons
     */
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
        if(model.isSafeJokerUsed() || model.isWon() || model.isLost()) {
            safeJokerItem.setIcon(R.drawable.croix);
            safeJokerItem.setEnabled(false);
        }
        else {
            safeJokerItem.setIcon(null);
            safeJokerItem.setEnabled(true);
        }
        if(model.isBurstJokerUsed() || model.isWon() || model.isLost()){
            burstJokerItem.setIcon(R.drawable.croix);
            burstJokerItem.setEnabled(false);
        }
        else {
            burstJokerItem.setIcon(null);
            burstJokerItem.setEnabled(true);
        }
    }

    /**
     * Update the music menu button
     */
    private void updateMusicButton() {
        MenuItem musicItem = menu.findItem(R.id.musicMenuId);
        if(mediaPlayer.isPlaying())
            musicItem.setIcon(R.drawable.musique_bleu);
        else
            musicItem.setIcon(R.drawable.musique);
    }

    /**
     * Update the grid depending on the current model state
     */
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
                            //assert(false);
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
                    imageView.setEnabled(false);
                }
            }
        }
        if (model.isWon()) {
            win();
        }
        else if(model.isLost()) {
            lose();
        }
    }
}