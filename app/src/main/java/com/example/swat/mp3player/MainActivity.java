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

    private static final int ACTION_UPDATE = 11;
    private static final int ACTION_LIST_CYCLE = 12;
    private static final int ACTION_TRACK_CYCLE = 13;
    MenuItem actionListCycle;
    MenuItem actionTrackCycle;
    private boolean listCycle;
    private boolean trackCycle;
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
            case ACTION_LIST_CYCLE:
                listCycle = !listCycle;
                fragmentPlayer.setListCycle(listCycle);

                actionListCycle.setIcon(listCycle ? R.drawable.ic_infinite
                        : R.drawable.ic_infinite_outline);

                Toast.makeText(this, listCycle ? getText(R.string.list_cycle_on).toString() :
                        getText(R.string.list_cycle_off).toString(), Toast.LENGTH_SHORT).show();

                if (trackCycle) {
                    trackCycle = !trackCycle;
                    fragmentPlayer.setTrackCycle(trackCycle);
                    actionTrackCycle.setIcon(trackCycle ? R.drawable.ic_loop_strong
                            : R.drawable.ic_loop);
                }

                break;
            case ACTION_TRACK_CYCLE:
                trackCycle = !trackCycle;
                fragmentPlayer.setTrackCycle(trackCycle);

                actionTrackCycle.setIcon(trackCycle ? R.drawable.ic_loop_strong
                        : R.drawable.ic_loop);

                Toast.makeText(this, trackCycle ? getText(R.string.track_cycle_on).toString() :
                        getText(R.string.track_cycle_off).toString(), Toast.LENGTH_SHORT).show();

                if (listCycle) {
                    listCycle = !listCycle;
                    fragmentPlayer.setListCycle(listCycle);
                    actionListCycle.setIcon(listCycle ? R.drawable.ic_infinite
                            : R.drawable.ic_infinite_outline);
                }

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

        menu.add(0, ACTION_LIST_CYCLE, 0, getText(R.string.repeat_playlist).toString())
                .setIcon(R.drawable.ic_infinite_outline)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, ACTION_TRACK_CYCLE, 0, getText(R.string.repeat_track).toString())
                .setIcon(R.drawable.ic_loop)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, ACTION_UPDATE, 0, getText(R.string.update).toString())
                .setIcon(R.drawable.ic_cloud_download_outline)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        actionListCycle = menu.findItem(ACTION_LIST_CYCLE);
        actionTrackCycle = menu.findItem(ACTION_TRACK_CYCLE);

        return super.onCreateOptionsMenu(menu);
    }

}
