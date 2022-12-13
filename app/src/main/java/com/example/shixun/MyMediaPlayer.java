package com.example.shixun;

import android.content.Context;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MyMediaPlayer extends MediaPlayer {
    private MediaPlayer play = null;
    private Context mContext = null;
    private String src_playing = null;

    public MyMediaPlayer(Context mContext){
        play = new MediaPlayer();
        this.mContext = mContext;
        //Log.i("MyMediaPlayer","实例创建成功...");
    }

    public void play(String wavPath, ImageView img){
        if (src_playing != null && src_playing.equals(wavPath)) {
            play.start();
            return;
        }
        play.reset();
        try {
            play.setDataSource(wavPath);
            play.setAudioStreamType(AudioManager.STREAM_MUSIC);
            play.prepareAsync();
            play.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                    src_playing = wavPath;
                }
            });
            play.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.stop();
                    //Log.i("MyMediaPlayer",play.isPlaying()+"");
                    img.setTag("pause");
                    img.setImageDrawable(mContext.getResources().getDrawable(R.drawable.play));
                    src_playing = null;
                }
            });
        } catch (Exception e) {
            Toast.makeText(mContext, "没找到对应路径", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }
    }

    public void pause(){
        play.pause();
    }

    public void releaseMediaPlayer(){
        if(play!=null){
            play.stop();
            play.release();
            play = null;
        }
    }

    public void reset(){
        super.reset();
        src_playing = null;
    }
}
