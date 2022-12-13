package com.example.shixun;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;

public class Sava_Dialog extends Dialog {
    private Button sava = null;
    private Button cancel = null;
    private EditText edt = null;
    private Spinner sp = null;

    public Sava_Dialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        setContentView(R.layout.dialog);
    }

    public Sava_Dialog(Context context) {
        super(context);
        setContentView(R.layout.dialog);
        setCanceledOnTouchOutside(false);
        sava = findViewById(R.id.dialog_save);
        cancel = findViewById(R.id.dialog_cancel);
        edt = findViewById(R.id.dialog_fileName);
        sp = findViewById(R.id.dialog_format);
    }

    public Button getSava() {
        return sava;
    }

    public Button getCancel() {
        return cancel;
    }

    public EditText getEdt() {
        return edt;
    }

    public Spinner getSp() {
        return sp;
    }
}
