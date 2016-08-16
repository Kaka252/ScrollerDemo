package com.zhouyou.scroller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_horizontal_scroller).setOnClickListener(this);
        findViewById(R.id.btn_vertical_scroller).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_horizontal_scroller:
                startActivity(new Intent(this, HorizontalScrollerActivity.class));
                break;
            case R.id.btn_vertical_scroller:
                startActivity(new Intent(this, VerticalScrollerActivity.class));
                break;
            default:
                break;
        }
    }
}
