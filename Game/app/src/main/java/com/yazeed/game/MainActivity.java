package com.yazeed.game;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity {
    private GLSurfaceView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = new GLSurfaceView(this);
        final ActivityManager aM = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo cI = aM.getDeviceConfigurationInfo();
        final boolean supportEs2 = cI.reqGlEsVersion >= 0x200000;

        view.setEGLContextClientVersion(2);
        view.setRenderer(new FirstRenderer(getApplicationContext()));
       /* if(supportEs2) {
            view.setEGLContextClientVersion(2);
            view.setRenderer(new FirstRenderer());
        }
        else{
            return;
        }*/
        setContentView(view);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        view.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        view.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
