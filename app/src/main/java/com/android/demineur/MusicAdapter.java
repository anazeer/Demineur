package com.android.demineur;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MusicAdapter extends BaseAdapter {

    private Context context;
    private int musicIndex;
    private Cursor musicCursor;

    public MusicAdapter(Context c, Cursor musicCursor) {
        context = c;
        this.musicCursor = musicCursor;
    }

    public int getCount() {
        return musicCursor.getCount();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public int getMusicIndex() {
        return musicIndex;
    }

    public void setMusicIndex(int musicIndex) {
        this.musicIndex = musicIndex;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tv = new TextView(context.getApplicationContext());
        String id = null;
        if (convertView == null) {
            musicIndex = musicCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            musicCursor.moveToPosition(position);
            id = musicCursor.getString(musicIndex);
            musicIndex = musicCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            musicCursor.moveToPosition(position);
            id += " Size(KB):" + musicCursor.getString(musicIndex);
            tv.setText(id);
        } else
            tv = (TextView) convertView;
        return tv;
    }
}
