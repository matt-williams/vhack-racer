package com.github.matt.williams.vhack.racer.activity;

import com.github.matt.williams.vhack.racer.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TitleActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getPackageManager().hasSystemFeature("com.google.android.tv")) {
            setContentView(R.layout.activity_title_tv);
        } else {
            setContentView(R.layout.activity_title);
        }
        Button standalone = (Button)findViewById(R.id.standalone);
        if (standalone != null) {
            standalone.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    Intent intent = new Intent(TitleActivity.this, GameActivity.class);
                    intent.putExtra(GameActivity.EXTRA_CONNECT, false);
                    startActivity(intent);
                }
            });
        }
        Button connect = (Button)findViewById(R.id.connect);
        if (connect != null) {
            connect.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    Intent intent = new Intent(TitleActivity.this, GameActivity.class);
                    intent.putExtra(GameActivity.EXTRA_CONNECT, true);
                    startActivity(intent);
                }
            });
        }
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_title, menu);
        return true;
    }
*/    
}
