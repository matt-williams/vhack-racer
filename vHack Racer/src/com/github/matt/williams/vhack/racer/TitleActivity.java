package com.github.matt.williams.vhack.racer;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class TitleActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);
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
