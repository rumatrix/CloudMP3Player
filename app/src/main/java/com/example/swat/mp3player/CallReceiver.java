package com.example.swat.mp3player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallReceiver extends BroadcastReceiver {

    private MP3Player mp3Player = new MP3Player();

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
                    .listen(new PhoneStateListener() {
                        public void onCallStateChanged(int state, String incomingNumber) {
                            switch (state) {
                                case TelephonyManager.CALL_STATE_IDLE:
                                    if (mp3Player.getMediaPlayer() != null && !mp3Player
                                            .getMediaPlayer().isPlaying()) {
                                        mp3Player.getMediaPlayer().start();
                                    }
                                    Log.d("myLogs", "IDLE");
                                    break;
                                case TelephonyManager.CALL_STATE_OFFHOOK:
                                    Log.d("myLogs", "OFFHOOK");
                                case TelephonyManager.CALL_STATE_RINGING:
                                    if (mp3Player.getMediaPlayer() != null && mp3Player
                                            .getMediaPlayer().isPlaying()) {
                                        mp3Player.getMediaPlayer().pause();
                                    }
                                    Log.d("myLogs", "RINGING");
                                    break;
                                default:
                                    return;
                            }
                        }
                    }, PhoneStateListener.LISTEN_CALL_STATE);
        } catch (Throwable th) {
        }
    }
}
