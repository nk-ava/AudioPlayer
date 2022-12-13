package com.example.shixun;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.List;

public class Search extends AppCompatActivity {
    private SearchView searchView = null;
    private ScrollView layout = null;
    private RecyclerView recyclerView = null;
    private List<Audio> searchResult = null;
    private MyAdapter ada = null;
    private TextView message = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_demo);
        Window win = getWindow();
        win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        win.setStatusBarColor(getResources().getColor(R.color.colorBackground));
        win.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.searchResult);
        message = findViewById(R.id.message);
        layout = searchView.getHistoryLayout();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchView.setOnClickBack(new bCallBack() {
            @Override
            public void BackAction() {
                finish();
            }
        });
        searchView.setOnClickSearch(new ICallBack() {
            @Override
            public void SearchAction(String string) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
                params.height = 0;
                layout.setLayoutParams(params);
                searchAudio(string);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
        searchView.setEditClearListener(new Clear_opt() {
            @Override
            public void clearContent() {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
                params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                layout.setLayoutParams(params);
                recyclerView.setVisibility(View.INVISIBLE);
                message.setVisibility(View.INVISIBLE);
                //Toast.makeText(Search.this,"清空内容",Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void searchAudio(String key){
        searchResult = MainActivity.getDate(key);
        ada = new MyAdapter();
        recyclerView.setAdapter(ada);
        if (ada.getItemCount()==0) message.setVisibility(View.VISIBLE);
        else message.setVisibility(View.INVISIBLE);
    }

    class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(Search.this).inflate(R.layout.searchres,parent,false);
            return new MyViewHold(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Audio audio = searchResult.get(position);
            ((MyViewHold) holder).fileName.setText(audio.getName());
            ((MyViewHold) holder).fileModify.setText(audio.getDate());
        }

        @Override
        public int getItemCount() {
            return searchResult.size();
        }

        class MyViewHold extends RecyclerView.ViewHolder{
            TextView fileName;
            TextView fileModify;
            public MyViewHold(@NonNull View itemView) {
                super(itemView);
                fileName = itemView.findViewById(R.id.searchResult_fileName);
                fileModify = itemView.findViewById(R.id.searchResult_fileModify);
            }
        }
    }
}
