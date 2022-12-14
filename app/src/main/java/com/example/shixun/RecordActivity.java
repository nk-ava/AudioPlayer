package com.example.shixun;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

public class RecordActivity extends AppCompatActivity implements View.OnClickListener {
    private AudioRecord audio = null;
    private Button sv = null;
    private RelativeLayout back = null;
    private byte[] buffer = null;
    private TextView recordText = null;
    private TextView recordTime = null;
    private Timer timer = new Timer();
    private TimerTask timerTask = null;

    private final Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            Bundle bundle = msg.getData();
            int time = bundle.getInt("time");
            int hh = time/3600;
            int mm = (time%3600)/60;
            int ss = time%60;
            recordTime.setText(String.format("%02d:%02d:%02d",hh,mm,ss));
        }
    };
    private static final int MY_PERMISSIONS_REQUEST = 200;
    private static final File external_path = Environment.getExternalStorageDirectory();
    private static final String save_path = external_path + "/record";
    private static final String pcmFileName = save_path+"/.temp";
    private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    private final static int AUDIO_SIMPLE_RATE = 44100;
    private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_STEREO;
    private final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private File file_temp = new File(pcmFileName);
    private boolean isRecording = true;
    private int recordBufferSize = 0;
    private int cnt = 0;
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record);

        Window win = getWindow();
        win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        win.setStatusBarColor(getResources().getColor(R.color.colorBackground));
        win.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST);
        start();

        sv = findViewById(R.id.save_btn);
        back = findViewById(R.id.record_back);
        recordText = findViewById(R.id.record_text);
        recordTime = findViewById(R.id.record_time);

        Animation animation = new AlphaAnimation(1,0);
        animation.setDuration(1000);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        recordText.startAnimation(animation);

        timerTask = new TimerTask() {
            @Override
            public void run() {
                Message msg = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putInt("time",cnt);
                cnt++;
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        };
        timer.schedule(timerTask,0,1000);

        sv.setOnClickListener(this);
        back.setOnClickListener(this);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/msyh.ttf");
        sv.setTypeface(tf);
    }

    private void start() {
        recordBufferSize = AudioRecord.getMinBufferSize(AUDIO_SIMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        audio = new AudioRecord(AUDIO_INPUT, AUDIO_SIMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING, recordBufferSize);
        //Log.i("RecordActivity","声道数:"+audio.getChannelCount());
        buffer = new byte[recordBufferSize];
        audio.startRecording();
        isRecording = true;
        new Thread(() -> {
            FileOutputStream os = null;
            try {
                if(!new File(save_path).exists()){
                    new File(save_path).mkdir();
                }
                if(file_temp.exists()){
                    file_temp.delete();
                }
                file_temp.createNewFile();
                os = new FileOutputStream(pcmFileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(os!=null){
                while(isRecording){
                    int read = audio.read(buffer,0,recordBufferSize);
                    if(read!=AudioRecord.ERROR_INVALID_OPERATION){
                        try {
                            os.write(buffer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            try{
                os.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    private void stop(){
        isRecording = false;
        if(audio!=null){
            audio.stop();
            audio.release();
            audio = null;
            Log.i("RecordActivity","AudioRecord释放成功");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save_btn:
                stop();
                if(timerTask!=null){
                    timerTask.cancel();
                    timer.purge();
                    timerTask = null;
                }
                Sava_Dialog dg = new Sava_Dialog(this);
                dg.getSava().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String fileName = dg.getEdt().getText().toString();
                        String format = dg.getSp().getSelectedItem().toString();
                        if (fileName.trim().equals("")) {
                            Toast.makeText(RecordActivity.this, "文件名不能为空", Toast.LENGTH_SHORT).show();
                        } else {
                            fileName += format.substring(1);
                            if(fileName.matches("(.*)\\.wav$")){
                                AudioUtil.pcmToWav(pcmFileName,save_path+"/"+fileName);
                                file_temp.delete();
                            }else if(fileName.matches("(.*)\\.pcm")){
                                file_temp.renameTo(new File(save_path+"/"+fileName));
                            }
                            dg.dismiss();
                            finish();
                        }
                    }
                });
                dg.getCancel().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dg.dismiss();
                        finish();
                    }
                });
                dg.show();
                break;
            case R.id.record_back:
                stop();
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop();
    }
}
