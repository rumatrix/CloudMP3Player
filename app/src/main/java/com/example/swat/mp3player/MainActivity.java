package com.example.swat.mp3player;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int ACTION_UPDATE = 12;
    private MP3List fragmentPlayer = new MP3List();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, fragmentPlayer)
                    .commit();
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case ACTION_UPDATE:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false)
                        .setIcon(R.drawable.ic_cloud_add)
                        .setTitle(R.string.alert_title)
                        .setMessage(R.string.message)
                        .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                fragmentPlayer.saveState(false);
                                Toast.makeText(MainActivity.this, R.string.message_ok,
                                        Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                AlertDialog dialog = builder.show();
                dialog.show();
                break;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(KeyEvent.KEYCODE_BREAK, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, ACTION_UPDATE, 0, "Обновить").setIcon(R.drawable.ic_cloud_download_outline)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

}
