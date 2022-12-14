package com.example.shixun;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PcmPlayer {
    private static final int AUDIO_SAMPLE_RATE = 44100;
    private static final int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_STEREO;
    private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static int bufferSize = 0;
    private static AudioTrack mAudioTrack = null;
    private DataInputStream dis = null;
    private Thread playThread = null;
    private boolean isPlaying = false;
    private String src_playing = null;
    private File source = null;
    private Object lock = new Object();
    private boolean isPause = false;
    private Timer timer = new Timer();
    private TimerTask timerTask = null;
    private Context mContext;
    private int num;

    public PcmPlayer(Context mContext){
        this.mContext = mContext;
    }

    static {
        bufferSize = AudioTrack.getMinBufferSize(AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING, bufferSize, AudioTrack.MODE_STREAM);
        //Log.i("PcmPlayer", "AudioTrack inited...");
    }

    public void play(String src, ImageView img) {
        if (src_playing != null && src.equals(src_playing) && isPause) {
            //Log.i("PcmPlayer","同一音频继续播放");
            synchronized (lock) {
                lock.notify();
            }
            return;
        }
        //Log.i("PcmPlayer","不同音频新建播放");
        stopPlay();
        source = new File(src);
        //Log.i("PcmPlayer", "AudioTrack状态-" + mAudioTrack.getState() + "");
        mAudioTrack.play();
        isPlaying = true;
        src_playing = src;
        num = 0;
        addTimer();
        //Log.i("PcmPlayer", "开始播放....");
        try {
            FileInputStream fis = new FileInputStream(source);
            dis = new DataInputStream(new BufferedInputStream(fis));
            //Log.i("PcmPlayer", "已打开dis输入流");
            playThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] tempBuffer = new byte[bufferSize];
                        while (isPlaying && dis.available() > 0) {
                            if (isPause) {
                                synchronized (lock) {
                                    try {
                                        mAudioTrack.pause();
                                        lock.wait();
                                        mAudioTrack.play();
                                        isPause = false;
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            int readCount = dis.read(tempBuffer);
                            if (readCount == AudioTrack.ERROR_INVALID_OPERATION ||
                                    readCount == AudioTrack.ERROR_BAD_VALUE) {
                                continue;
                            }
                            if (readCount != 0 && readCount != -1) {
                                mAudioTrack.write(tempBuffer, 0, readCount);
                                num+=readCount;
                            }
                        }
                        img.setTag("pause");
                        img.setImageDrawable(mContext.getResources().getDrawable(R.drawable.play));
                        //Log.i("PcmPlayer","播放了"+num);
                        src_playing = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (dis != null) {
                                dis.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            playThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void releaseAudioTrack() {
        stopPlay();
        mAudioTrack.release();
        //Log.i("PcmPlayer", "成功释放AudioTrack...");
    }

    public void stopPlay() {
        isPlaying = false;
        if (mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
            mAudioTrack.stop();
        }
        if (playThread != null && playThread.getState() == Thread.State.RUNNABLE) {
            playThread.interrupt();
        }
        if (dis != null) {
            try {
                dis.close();
                //Log.i("PcmPlayer", "已关闭输入流");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(timerTask!=null){
            timerTask.cancel();
            timerTask = null;
            timer.purge();
        }
        src_playing = null;
        isPause = false;
    }

    public void pause() {
        isPause = true;
    }

    private void addTimer(){
        if(timerTask!=null){
            timerTask.cancel();
            timerTask = null;
            timer.purge();
        }
        timerTask = new TimerTask() {
            @Override
            public void run() {
                int duration = (int) (source.length()/176400.0);
                int currentPosition = (int) (num/176400.0);
                Bundle bundle = new Bundle();
                bundle.putInt("duration",duration);
                bundle.putInt("currentPosition",currentPosition);
                Message msg = MainActivity.handler.obtainMessage();
                msg.setData(bundle);
                MainActivity.handler.sendMessage(msg);
            }
        };
        timer.schedule(timerTask,5,500);
    }
}
