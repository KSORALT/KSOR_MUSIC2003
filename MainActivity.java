package com.ksor.music2003;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    final int BG = Color.rgb(23, 19, 46), PANEL = Color.rgb(45, 37, 82);
    final int PINK = Color.rgb(255, 143, 210), WHITE = Color.rgb(245, 235, 255);
    final ArrayList<Song> songs = new ArrayList<>();
    final Handler handler = new Handler(Looper.getMainLooper());
    MediaPlayer player;
    int current = -1;
    boolean seeking = false;
    TextView now, subtitle, count, time;
    Button play;
    SeekBar seek;
    LinearLayout list;
    final Runnable ticker = new Runnable() {
        @Override public void run() {
            if (player != null) {
                try {
                    int duration = player.getDuration(), pos = player.getCurrentPosition();
                    if (!seeking) seek.setProgress(pos);
                    seek.setMax(Math.max(duration, 1));
                    time.setText(format(pos) + " / " + format(duration));
                } catch (IllegalStateException ignored) {}
            }
            handler.postDelayed(this, 500);
        }
    };
    static class Song {
        String title, artist; Uri uri;
        Song(String t, String a, Uri u) { title=t; artist=a; uri=u; }
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        draw();
        handler.post(ticker);
        requestMusicPermission();
    }
    GradientDrawable bg(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(radius); return d;
    }
    TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return t;
    }
    Button button(String label) {
        Button b = new Button(this); b.setText(label); b.setTextColor(BG);
        b.setAllCaps(false); b.setTextSize(16); b.setBackground(bg(PINK, 24)); return b;
    }
    void draw() {
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(BG);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(22,32,22,24);
        scroll.addView(root);
        TextView header = text("✦ KSOR_MUSIC2003", 27, PINK, true); root.addView(header);
        TextView tagline = text("YOUR POCKET JUKEBOX  •  EST. 2003", 11, WHITE, false);
        root.addView(tagline);
        LinearLayout playerBox = new LinearLayout(this); playerBox.setOrientation(LinearLayout.VERTICAL);
        playerBox.setPadding(20,20,20,20); playerBox.setBackground(bg(PANEL,30));
        LinearLayout.LayoutParams boxLp = new LinearLayout.LayoutParams(-1,-2); boxLp.topMargin=28;
        root.addView(playerBox, boxLp);
        TextView disc = text("◉  ♪  ◉", 58, PINK, true); disc.setGravity(Gravity.CENTER); playerBox.addView(disc);
        now = text("Nada reproduciéndose", 21, WHITE, true); now.setGravity(Gravity.CENTER); playerBox.addView(now);
        subtitle = text("Selecciona una canción ↓", 14, PINK, false);
        subtitle.setGravity(Gravity.CENTER); playerBox.addView(subtitle);
        seek = new SeekBar(this); seek.setProgressTintList(android.content.res.ColorStateList.valueOf(PINK));
        seek.setThumbTintList(android.content.res.ColorStateList.valueOf(PINK));
        playerBox.addView(seek);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar s) { seeking=true; }
            public void onStopTrackingTouch(SeekBar s) { if(player!=null) player.seekTo(s.getProgress()); seeking=false; }
            public void onProgressChanged(SeekBar s,int p,boolean user) {}
        });
        time = text("00:00 / 00:00", 12, WHITE, false); time.setGravity(Gravity.CENTER); playerBox.addView(time);
        LinearLayout controls = new LinearLayout(this); controls.setGravity(Gravity.CENTER); controls.setPadding(0,14,0,0);
        Button prev=button("|◀"), next=button("▶| "); play=button("▶");
        for(Button b:new Button[]{prev,play,next}) {
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,55,1); p.setMargins(4,0,4,0); controls.addView(b,p);
        }
        prev.setOnClickListener(v->skip(-1)); next.setOnClickListener(v->skip(1)); play.setOnClickListener(v->toggle());
        playerBox.addView(controls);
        count=text("MY MUSIC LIBRARY",19,PINK,true);
        LinearLayout.LayoutParams countLp=new LinearLayout.LayoutParams(-1,-2); countLp.topMargin=30;
        root.addView(count,countLp);
        Button refresh=button("↻  Actualizar biblioteca");
        LinearLayout.LayoutParams refreshLp=new LinearLayout.LayoutParams(-1,50); refreshLp.topMargin=12;
        root.addView(refresh,refreshLp); refresh.setOnClickListener(v->requestMusicPermission());
        list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams listLp=new LinearLayout.LayoutParams(-1,-2); listLp.topMargin=12;
        root.addView(list,listLp);
        setContentView(scroll);
    }
    void requestMusicPermission() {
        String permission=Build.VERSION.SDK_INT>=33 ? Manifest.permission.READ_MEDIA_AUDIO : Manifest.permission.READ_EXTERNAL_STORAGE;
        if(checkSelfPermission(permission)==PackageManager.PERMISSION_GRANTED) loadSongs();
        else requestPermissions(new String[]{permission},100);
    }
    @Override public void onRequestPermissionsResult(int code,String[] permissions,int[] results) {
        super.onRequestPermissionsResult(code,permissions,results);
        if(code==100 && results.length>0 && results[0]==PackageManager.PERMISSION_GRANTED) loadSongs();
        else Toast.makeText(this,"Se necesita permiso para leer tu música",Toast.LENGTH_LONG).show();
    }
    void loadSongs() {
        songs.clear();
        Uri collection=MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] columns={MediaStore.Audio.Media._ID,MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ARTIST};
        try(Cursor c=getContentResolver().query(collection,columns,MediaStore.Audio.Media.IS_MUSIC+" != 0",null,MediaStore.Audio.Media.TITLE+" COLLATE NOCASE ASC")) {
            if(c!=null) while(c.moveToNext()) {
                long id=c.getLong(0);
                songs.add(new Song(c.getString(1),c.getString(2),ContentUris.withAppendedId(collection,id)));
            }
        } catch(Exception e) { Toast.makeText(this,"Error leyendo música: "+e.getMessage(),Toast.LENGTH_LONG).show(); }
        count.setText("MY MUSIC LIBRARY  •  "+songs.size()+" canciones");
        list.removeAllViews();
        if(songs.isEmpty()) list.addView(text("No se encontraron canciones. Guarda archivos de audio en el teléfono y pulsa Actualizar.",14,WHITE,false));
        for(int i=0;i<songs.size();i++) {
            final int index=i; Song s=songs.get(i);
            LinearLayout item=new LinearLayout(this); item.setOrientation(LinearLayout.VERTICAL);
            item.setPadding(16,13,16,13); item.setBackground(bg(PANEL,18));
            TextView title=text("♫  "+s.title,16,WHITE,true); title.setSingleLine(true); title.setEllipsize(android.text.TextUtils.TruncateAt.END);
            TextView artist=text(s.artist==null?"Artista desconocido":s.artist,12,PINK,false);
            item.addView(title); item.addView(artist);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.bottomMargin=8;
            list.addView(item,lp); item.setOnClickListener(v->startSong(index));
        }
    }
    void startSong(int index) {
        if(index<0 || index>=songs.size()) return;
        if(player!=null) { player.release(); player=null; }
        current=index; Song s=songs.get(index);
        now.setText(s.title); subtitle.setText(s.artist); play.setText("Ⅱ");
        try {
            player=new MediaPlayer();
            player.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
            player.setDataSource(this,s.uri); player.setOnCompletionListener(mp->skip(1));
            player.prepare(); player.start();
        } catch(Exception e) {
            if(player!=null) { player.release(); player=null; }
            play.setText("▶"); Toast.makeText(this,"No se pudo reproducir: "+s.title,Toast.LENGTH_LONG).show();
        }
    }
    void toggle() {
        if(player==null) { if(!songs.isEmpty()) startSong(current<0?0:current); return; }
        if(player.isPlaying()) { player.pause(); play.setText("▶"); }
        else { player.start(); play.setText("Ⅱ"); }
    }
    void skip(int delta) { if(!songs.isEmpty()) startSong((current+delta+songs.size())%songs.size()); }
    String format(int ms) { int seconds=Math.max(ms,0)/1000; return String.format(Locale.US,"%02d:%02d",seconds/60,seconds%60); }
    @Override protected void onDestroy() { handler.removeCallbacks(ticker); if(player!=null) player.release(); super.onDestroy(); }
}
