package com.example.shixun;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;

import androidx.annotation.NonNull;

public class Caid_Dialog extends Dialog {
    Button play,remove,change,rename,check;
    public Caid_Dialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.caid);
        play = findViewById(R.id.caid_play);
        remove = findViewById(R.id.caid_remove);
        change = findViewById(R.id.caid_change);
        rename = findViewById(R.id.caid_rename);
        check = findViewById(R.id.caid_path);
    }

    public Button getPlay() {
        return play;
    }

    public Button getRemove() {
        return remove;
    }

    public Button getChange() {
        return change;
    }

    public Button getRename() {
        return rename;
    }

    public Button getCheck() {
        return check;
    }
}
