package com.example.shixun;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            Bundle bundle = msg.getData();
            int length = bundle.getInt("duration");
            int current = bundle.getInt("currentPosition");
            sk.setMax(length);
            sk.setProgress(current);
            cur.setText(timeToFormat((long) current));
            len.setText(timeToFormat((long) length));
        }
    };
    private static SeekBar sk;
    private static TextView cur,len;
    private LinearLayout skp = null;
    private Button record_btn = null;
    private ImageView imv = null;
    private RecyclerView rec = null;
    private MyAdapter ada = null;
    private List<Audio> audioList = null;
    private LinearLayout erase_opts = null;
    private LinearLayout all_choose = null;
    private LinearLayout cancel_choose = null;
    private LinearLayout in_choose = null;
    private LinearLayout delete = null;
    private LinearLayout exchange = null;
    private LinearLayout really = null;
    private int File_Max_pxValue;
    private LinearLayout tag = null;//上一个被伸长的LinearLayout
    private ImageView lastImg = null;//上一个被点击的ImageView
    private float scale;
    private PcmPlayer pcmPlayer;
    private MyMediaPlayer mediaPlayer;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private List<String> checkedFilePath = new ArrayList<>();

    private static final File EXTERNAL_STORAGE = Environment.getExternalStorageDirectory();
    private static String savaPath = EXTERNAL_STORAGE+"/record";

    private String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        scale = getResources().getDisplayMetrics().density;
        File_Max_pxValue = (int)(45.0f*scale+0.5f);
        Window win = getWindow();
        win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        win.setStatusBarColor(getResources().getColor(R.color.colorBackground));
        win.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        ActivityCompat.requestPermissions(this,permissions,222);
        File file = new File(savaPath);
        if(!file.exists()) file.mkdir();

        record_btn = findViewById(R.id.record_btn);
        imv = findViewById(R.id.main_options);
        skp = findViewById(R.id.search_bar);
        rec = findViewById(R.id.main_recyclerView);
        erase_opts = findViewById(R.id.main_opts);
        all_choose = findViewById(R.id.main_choose);
        cancel_choose = findViewById(R.id.main_cancel);
        in_choose = findViewById(R.id.main_inchoose);
        delete = findViewById(R.id.main_delete);
        exchange = findViewById(R.id.main_exchange);
        really = findViewById(R.id.main_really);

        pcmPlayer = new PcmPlayer(MainActivity.this);
        mediaPlayer = new MyMediaPlayer(MainActivity.this);
        skp.setOnClickListener(this);
        record_btn.setOnClickListener(this);
        imv.setOnClickListener(this);
        all_choose.setOnClickListener(this);
        cancel_choose.setOnClickListener(this);
        in_choose.setOnClickListener(this);
        delete.setOnClickListener(this);
        exchange.setOnClickListener(this);
        really.setOnClickListener(this);

        Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/msyh.ttf");
        record_btn.setTypeface(tf);
        rec.setLayoutManager(new LinearLayoutManager(this));

        reGetData(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.i("MainActivity","调用了onResume方法");
        reGetData(0);
        if(tag!=null){
            tag = null;
        }
    }

    @Override
    public void onClick(View view) {
        int len = 0;
        switch(view.getId()){
            case R.id.search_bar:
                startActivity(new Intent(MainActivity.this,Search.class));
                break;
            case R.id.record_btn:
                startActivity(new Intent(MainActivity.this,RecordActivity.class));
                break;
            case R.id.main_options:
                //Toast.makeText(MainActivity.this,"点击了菜单",Toast.LENGTH_SHORT).show();
                PopupMenu popupMenu = new PopupMenu(MainActivity.this,view);
                popupMenu.getMenuInflater().inflate(R.menu.main_menu,popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch(menuItem.getItemId()){
                            case R.id.opts:
                                record_btn.setVisibility(View.INVISIBLE);
                                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) erase_opts.getLayoutParams();
                                params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                                erase_opts.setLayoutParams(params);
                                reGetData(1);
                                break;
                        }
                        return false;
                    }
                });
                break;
            case R.id.main_really:
                record_btn.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) erase_opts.getLayoutParams();
                params.height = 0;
                erase_opts.setLayoutParams(params);
                reGetData(0);
                break;
            case R.id.main_delete:
                len = checkedFilePath.size();
                if(len == 0){
                    Toast.makeText(MainActivity.this,"请选择内容",Toast.LENGTH_SHORT).show();
                    break;
                }
                //Log.i("MainActivity","delete files:");
                for(int i=0;i<len;i++){
                    //Log.i("MainActivity",checkedFilePath.get(i));
                    new File(checkedFilePath.get(i)).delete();
                }
                reGetData(1);
                //Toast.makeText(MainActivity.this,"删除成功",Toast.LENGTH_SHORT).show();
                break;
            case R.id.main_exchange:
                len = checkedFilePath.size();
                if(len == 0){
                    Toast.makeText(MainActivity.this,"请选择内容",Toast.LENGTH_SHORT).show();
                    break;
                }
                for(int i=0;i<len;i++){
                    String path = checkedFilePath.get(i);
                    if(path.endsWith(".pcm")) {
                        AudioUtil.pcmToWav(path,path.substring(0, path.length() - 3) + "wav");
                    }
                }
                reGetData(1);
                //Toast.makeText(MainActivity.this,"已将选中的pcm文件转换成wav",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @NonNull
    public static List<Audio> getDate(String key){
        List<Audio> temp = new ArrayList<>();
        File[] list = new File(savaPath).listFiles();
        for(int i=0;i< list.length;i++){
            if(!list[i].isFile()) continue;
            String name = list[i].getName();
            String date = dateFormat.format(new Date(list[i].lastModified()));
            int format = 0;
            long time = 0;
            if(!key.equals("")&&!name.contains(key)){
                continue;
            }
            if(name.matches("(.*)\\.pcm$")){
                format = 1;
                time = (long) (list[i].length()/176400.0);
                //Log.i("MainActivity",time+"");
            }
            if(name.matches("(.*)\\.wav$")){
                format=1;
                time = (long) AudioUtil.getWavLength(list[i].getAbsolutePath()+"");
            }
            //if(name.matches("(.*)\\.mp3$")) format=1;
            if(format==0) continue;
            //Log.i("MainActivity",list[i].length()+"B");
            temp.add(new Audio(name,date,time,list[i].getAbsolutePath()+""));
        }
        return temp;
    }

    private void reGetData(int Type){
        if(Type==1) checkedFilePath.clear();
        audioList = getDate("");
        ada = new MyAdapter(Type);
        rec.setAdapter(ada);
    }

    class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private int mType;
        public MyAdapter(int mType){
            super();
            this.mType = mType;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder holder = null;
            View itemView = LayoutInflater.from(MainActivity.this).inflate(R.layout.file,parent,false);
            if(mType==0) holder = new MyViewHolder(itemView);
            else holder = new MyViewHolder_check(itemView);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Audio file = audioList.get(position);
            String tm = timeToFormat(file.getTime());
            if(mType==0) {
                ((MyViewHolder) holder).name.setText(file.getName());
                ((MyViewHolder) holder).date.setText(file.getDate());
                ((MyViewHolder) holder).time.setText(tm);
                LinearLayout lay = ((MyViewHolder) holder).group;
                ImageView img = ((MyViewHolder) holder).img;
                LinearLayout view = ((MyViewHolder) holder).view;
                SeekBar sb = ((MyViewHolder) holder).sb;
                TextView current = ((MyViewHolder) holder).current;
                TextView length = ((MyViewHolder) holder).length;
                img.setVisibility(View.VISIBLE);
                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) lay.getLayoutParams();
                        if (tag != lay && tag != null) {
                            LinearLayout.LayoutParams params_temp = (LinearLayout.LayoutParams) tag.getLayoutParams();
                            params_temp.height = 0;
                            tag.setLayoutParams(params_temp);
                            if (lastImg.getTag().equals("play")) {
                                lastImg.setImageDrawable(getResources().getDrawable(R.drawable.play));
                                lastImg.setTag("pause");
                            }
                        }
                        if (params.height != File_Max_pxValue) {
                            params.height = File_Max_pxValue;
                            lay.setLayoutParams(params);
                            tag = lay;
                        }
                        if (img.getTag().equals("pause")) {
                            img.setImageDrawable(getResources().getDrawable(R.drawable.pause));
                            img.setTag("play");
                            if (file.getName().matches("(.*)\\.pcm")) {
                                mediaPlayer.reset();
                                //Log.i("MainActivity","重置mediaPlayer成功");
                                sk = sb;
                                cur = current;
                                len = length;
                                pcmPlayer.play(file.getPath(), img);
                            }
                            if (file.getName().matches("(.*)\\.wav")) {
                                pcmPlayer.stopPlay();
                                //Log.i("MainActivity","停止pcmPlayer成功");
                                sk = sb;
                                cur = current;
                                len = length;
                                mediaPlayer.play(file.getPath(), img);
                            }
                        } else {
                            img.setImageDrawable(getResources().getDrawable(R.drawable.play));
                            img.setTag("pause");
                            if (file.getName().matches("(.*)\\.pcm")) {
                                pcmPlayer.pause();
                            }
                            if (file.getName().matches("(.*)\\.wav")) {
                                mediaPlayer.pause();
                            }
                        }
                        lastImg = img;
                    }
                });
                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        //Toast.makeText(MainActivity.this,"触发长按事件",Toast.LENGTH_SHORT).show();
                        Vibrator vib = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
                        vib.vibrate(300);
                        Caid_Dialog caid = new Caid_Dialog(MainActivity.this);
                        caid.getRemove().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                File rm_file = new File(file.getPath());
                                if (rm_file.delete()) {
                                    Toast.makeText(MainActivity.this, "已删除" + file.getName(), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "删除失败：" + file.getName(), Toast.LENGTH_SHORT).show();
                                }
                                caid.dismiss();
                                reGetData(0);
                            }
                        });
                        caid.getPlay().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        });
                        caid.getChange().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (!file.getName().endsWith("pcm")) {
                                    Toast.makeText(MainActivity.this, "不是pcm文件，不支持转wav", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("确认");
                                builder.setMessage("是否确认转化为wav文件");
                                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        AudioUtil.pcmToWav(file.getPath(), savaPath + "/" + file.getName().substring(0, file.getName().length() - 3) + "wav");
                                        Toast.makeText(MainActivity.this, "转换成功", Toast.LENGTH_SHORT).show();
                                        caid.dismiss();
                                        reGetData(0);
                                    }
                                });
                                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //不做任何操作
                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.setCanceledOnTouchOutside(false);
                                dialog.show();
                            }
                        });
                        caid.getCheck().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setMessage("文件路径：" + file.getPath());
                                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //不做任何操作
                                    }
                                });
                                caid.dismiss();
                                builder.create().show();
                            }
                        });
                        caid.show();
                        return true;
                    }
                });
            }else{
                ((MyViewHolder_check) holder).name.setText(file.getName());
                ((MyViewHolder_check) holder).date.setText(file.getDate());
                ((MyViewHolder_check) holder).time.setText(tm);
                CheckBox checkBox = ((MyViewHolder_check) holder).checkBox;
                checkBox.setVisibility(View.VISIBLE);
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if(b){
                            if(!checkedFilePath.contains(file.getPath())){
                                checkedFilePath.add(file.getPath());
                            }
                        }else{
                            if(checkedFilePath.contains(file.getPath())){
                                checkedFilePath.remove(file.getPath());
                            }
                        }
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return audioList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView name,date,time,current,length;
            ImageView img;
            LinearLayout group,view;
            SeekBar sb;
            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.file_name);
                date = itemView.findViewById(R.id.file_date);
                time = itemView.findViewById(R.id.file_time);
                img = itemView.findViewById(R.id.file_img);
                group = itemView.findViewById(R.id.file_playLayout);
                view = itemView.findViewById(R.id.file_view);
                sb = itemView.findViewById(R.id.file_seekbar);
                current = itemView.findViewById(R.id.file_current);
                length = itemView.findViewById(R.id.file_length);
            }
        }

        class MyViewHolder_check extends RecyclerView.ViewHolder{
            TextView name,date,time;
            CheckBox checkBox;
            public MyViewHolder_check(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.file_name);
                date = itemView.findViewById(R.id.file_date);
                time = itemView.findViewById(R.id.file_time);
                checkBox = itemView.findViewById(R.id.file_checkBox);
            }
        }
    }
    private static String timeToFormat(long time){
        long hh=0,mm=0,ss=time;
        if(ss>=60){
            mm=ss/60;
            ss%=60;
        }
        if(mm>=60){
            hh=mm/60;
            mm%=60;
        }
        String ret = String.format("%02d:%02d:%02d", hh,mm,ss);
        return ret;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(pcmPlayer!=null){
            pcmPlayer.releaseAudioTrack();
        }
        if(mediaPlayer!=null){
            mediaPlayer.releaseMediaPlayer();
        }
    }
}
