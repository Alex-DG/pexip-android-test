package com.gearedapp.ajentapexip;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.widget.RelativeLayout;

import com.pexip.android.wrapper.PexView;


public class MainActivity extends AppCompatActivity {

    RelativeLayout layout;
    PexView pexView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // HANDLE PERMISSIONS
        // TODO You have to restart this app once the permissions are all setup
        // will improve that later
        int PERMISSION_ALL = 1;

        String[] PERMISSIONS = {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        };

        if(!hasPermissions(this, PERMISSIONS)){
            Log.d("initPexip", "Permissions required");
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            Log.d("initPexip", "Permissions success");
            initPexip();
        }
    }

    private void initPexip() {
        pexView = new PexView(this);
        layout = (RelativeLayout) findViewById(R.id.pex_layout);
        layout.addView(pexView);

        pexView.setEvent("onSetup", pexView.new PexEvent() {
            @Override
            public void onEvent(String[] strings) {
                Log.d("ALEX", "onSetup...");
                pexView.setSelfViewVideo(strings[0]);
                pexView.evaluateFunction("connect");
            }
        });

        pexView.setEvent("onConnect", pexView.new PexEvent() {
            @Override
            public void onEvent(String[] strings) {
                Log.d("ALEX", "onConnect...");
                if (strings.length > 0 && strings[0] != null) {
                    pexView.setVideo(strings[0]);
                }

            }
        });

        pexView.addPageLoadedCallback(pexView.new PexCallback() {
            @Override
            public void callback(String args) {
                Log.d("ALEX", "addPageLoadedCallback...");
                // make a call
                pexView.evaluateFunction("makeCall", "pex-pool.vscene.net", "john_vmr", "Alex-webview");
            }
        });


        pexView.load();
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}
