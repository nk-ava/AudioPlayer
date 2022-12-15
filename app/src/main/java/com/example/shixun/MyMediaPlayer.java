package com.example.shixun;

import android.content.Context;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.security.PublicKey;
import java.util.Timer;
import java.util.TimerTask;

public class MyMediaPlayer{
    private Handler mHandler = null;
    private MediaPlayer play = null;
    private Context mContext = null;
    private String src_playing = null;
    private Timer timer = new Timer();
    private TimerTask timerTask = null;

    public MyMediaPlayer(Context mContext, Handler mHandler){
        play = new MediaPlayer();
        this.mContext = mContext;
        this.mHandler = mHandler;
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
            play.prepareAsync();
            play.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                    addTimer();
                    src_playing = wavPath;
                }
            });
            play.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
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
        play.stop();
        play.reset();
        if(timerTask!=null){
            timerTask.cancel();
            timerTask = null;
            timer.purge();
        }
        src_playing = null;
    }

    private void addTimer(){
        if(timerTask!=null){
            timerTask.cancel();
            timerTask = null;
            timer.purge();
        }
        timerTask =  new TimerTask() {
            @Override
            public void run() {
                int duration = play.getDuration()/1000;
                int currentPosition = play.getCurrentPosition()/1000;
                Bundle bundle = new Bundle();
                bundle.putInt("duration",duration);
                bundle.putInt("currentPosition",currentPosition);
                Message msg = mHandler.obtainMessage();
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            }
        };
        timer.schedule(timerTask,5,500);
    }
}
