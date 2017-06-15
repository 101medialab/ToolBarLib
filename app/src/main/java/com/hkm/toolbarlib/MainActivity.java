package com.hkm.toolbarlib;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

/**
 * Created by hesk on 3/8/15.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_menu);
    }

    private void startApp(Class<?> k) {
        Log.d(TAG, String.format("start activity: %s", k.getSimpleName()));
        Intent g = new Intent(this, k);
        startActivity(g);
    }

    public void main3(View view) {
        startApp(TopBarManagerExample.class);
    }

    public void main2(View view) {
        startApp(TopBarManagerExampleFull.class);
    }

    public void candybarimple(View view) {
        startApp(TestOfCandyBar.class);
    }

    public void beastbarv1(View view) {
        startApp(BeastBarDemo_v1.class);
    }
}
