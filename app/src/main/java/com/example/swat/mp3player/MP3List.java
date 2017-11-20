package com.example.swat.mp3player;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import static android.content.Context.MODE_PRIVATE;

public class MP3List extends Fragment implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener {

    private SharedPreferences sPref;
    private Intent intent;
    private Handler handler;
    private Runnable runnable;
    private MP3Player mp3Player;
    private SeekBar seekBar;
    private Button btnFastForward;
    private Button btnRewind;
    private Button btnBackward;
    private Button btnPlay;
    private Button btnForward;
    private TextView tvScroll;
    private View view;
    private Bitmap image[];
    private String listMusic[];
    private ImageView imagePreview;
    private LinearLayout llArt;
    private LinearLayout llDetails;
    private ListView lvMusic;
    private ListView lvAboutAudio;
    private SimpleAdapter slAdapter;
    private ArrayList<String> linkList;
    private ArrayList<Map<String, Object>> audioList = new ArrayList<>();
    private ArrayList<Map<String, Object>> aboutList = new ArrayList<>();
    private HashMap<String, Object> hashMap = new HashMap<>();
    private static int trackId;
    private static int countFile;
    private static int defaultImg = R.drawable.ic_music;
    private static boolean state;
    private static boolean listCycle;
    private static boolean trackCycle;
    private static final int BUFFER_SIZE = 4096;
    private static final String STATE = "state";
    private static final String MAP_NAME_TITLE = "title";
    private static final String MAP_NAME_ARTIST = "artist";
    private static final String MAP_NAME_ALBUM = "album";
    private static final String MAP_NAME_YEAR = "year";
    private static final String MAP_NAME_GENRE = "genre";
    private static final String MAP_NAME_COMPOSER = "composer";
    private static final String MAP_NAME_TRACK = "track";
    private static final String MAP_NAME_DISC = "disc";
    private static final String MAP_DETAILS_AUDIO = "details";
    private static final String MAP_NAME_IMAGE = "image";
    private static final String MAP_DURATION_AUDIO = "duration";
    private static final String LOG_TAG = "myLogs";
    private static final String METHOD_GET = "GET";
    private static final String HYPHEN = " – ";
    private static final String AUDIO_BITRATE = " kbps";
    private static final String DEFAULT_ROOT_DIR = "/MP3Player/Music";
    private static final String DEFAULT_AUDIO_FORMAT = ".mp3";
    private static final String DEFAULT_AUDIO_NAME = "tmp.mp3";
    private static final String DEFAULT_URL_PATH_LIST_MUSIC = "https://drive.google.com/uc?id=" +
            "0Bz-93TMz91xGeG5tMkt6UlNHRFk&export=download";
    private static final String DEFAULT_AUDIO_PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/MP3Player/Music/";

    public static void setListCycle(boolean listCycle) {
        MP3List.listCycle = listCycle;
    }

    public static void setTrackCycle(boolean trackCycle) {
        MP3List.trackCycle = trackCycle;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_mp3list, container, false);
        lvMusic = (ListView) view.findViewById(R.id.lvMusic);
        lvAboutAudio = (ListView) view.findViewById(R.id.lvAboutAudio);
        imagePreview = (ImageView) view.findViewById(R.id.imageView);
        llArt = (LinearLayout) view.findViewById(R.id.llArt);
        llDetails = (LinearLayout) view.findViewById(R.id.llDetails);
        btnBackward = (Button) view.findViewById(R.id.btnBackward);
        btnPlay = (Button) view.findViewById(R.id.btnPlay);
        btnForward = (Button) view.findViewById(R.id.btnForward);
        btnFastForward = (Button) view.findViewById(R.id.btnFastward);
        btnRewind = (Button) view.findViewById(R.id.btnRewind);
        tvScroll = (TextView) view.findViewById(R.id.tvScroll);

        mp3Player = new MP3Player();

        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setEnabled(false);

        btnBackward.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnForward.setOnClickListener(this);
        btnRewind.setOnClickListener(this);
        btnFastForward.setOnClickListener(this);

        tvScroll.setSelected(true);

        imagePreview.setImageResource(R.drawable.default_image);
        lvMusic.setOnItemClickListener(onItemClickListener);

        sPref = getActivity().getPreferences(MODE_PRIVATE);
        state = sPref.getBoolean(STATE, state);

        if (!new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + DEFAULT_ROOT_DIR).exists() || !state) {
            if (isOnline()) {
                btnBackward.setEnabled(false);
                btnPlay.setEnabled(false);
                btnForward.setEnabled(false);
                new ReadLinkList().execute();
            } else {
                Toast.makeText(getActivity(), R.string.is_online, Toast.LENGTH_LONG).show();
            }
        } else {
            File list[] = new File(DEFAULT_AUDIO_PATH).listFiles();
            listMusic = new String[list.length];
            image = new Bitmap[listMusic.length];

            for (int i = 0; i < list.length; i++) {
                listMusic[i] = (list[i].getName());
                listUtils(DEFAULT_AUDIO_PATH + listMusic[i], true);
            }
        }
        return view;
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() == null) {
            return false;
        } else {
            return true;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void listUtils(String dataSource, boolean created) {

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(dataSource);

        MediaExtractor mediaExtractor = new MediaExtractor();
        try {
            mediaExtractor.setDataSource(dataSource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        MediaFormat mediaFormat = mediaExtractor.getTrackFormat(0);

        int sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        int channels = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);

        String mode = channels == 2 ?
                getText(R.string.channels_two).toString() :
                getText(R.string.channels_one).toString();

        String info = String.valueOf(", " + sampleRate +
                getText(R.string.frequency).toString() + mode);

        byte[] albumArt = mmr.getEmbeddedPicture();

        if (albumArt != null) {
            image[countFile] = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
        } else {
            image[countFile] = BitmapFactory.decodeResource(getResources(),
                    R.drawable.default_image);
        }

        String audioName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                + HYPHEN + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                + DEFAULT_AUDIO_FORMAT;

        String duration = getDuration(Long.parseLong(mmr.extractMetadata
                (MediaMetadataRetriever.METADATA_KEY_DURATION)));

        String bitrate = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.
                METADATA_KEY_BITRATE)) / 1000 + AUDIO_BITRATE + info;

        if (!created) {
            File newAudioName = new File(DEFAULT_AUDIO_PATH + audioName);
            File tmpAudio = new File(DEFAULT_AUDIO_PATH + DEFAULT_AUDIO_NAME);

            tmpAudio.renameTo(newAudioName);
        }

        hashMap = new HashMap<>();
        hashMap.put(MAP_NAME_IMAGE, defaultImg);
        hashMap.put(MAP_NAME_ARTIST, countFile + 1 + ". " + audioName);
        hashMap.put(MAP_DETAILS_AUDIO, bitrate);
        hashMap.put(MAP_DURATION_AUDIO, duration);

        if (created) {
            audioList.add(hashMap);
        } else {
            audioList.set(countFile, hashMap);
        }

        if (created) {
            slAdapter = new MySimpleAdapter(getActivity(), audioList, R.layout.play_list_item,
                    new String[]{MAP_NAME_IMAGE, MAP_NAME_ARTIST, MAP_DETAILS_AUDIO,
                            MAP_DURATION_AUDIO},
                    new int[]{R.id.ivTrack, R.id.tvAudioName, R.id.tvDetails, R.id.tvDuration});
            lvMusic.setAdapter(slAdapter);
        }

        slAdapter.notifyDataSetChanged();

        String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        String year = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
        String genre = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
        String composer = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER);
        String track = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
        String disc = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER);

        hashMap = new HashMap<>();
        hashMap.put(MAP_NAME_IMAGE, R.id.ivArt);
        hashMap.put(MAP_NAME_TITLE, title);
        hashMap.put(MAP_NAME_ARTIST, artist);
        hashMap.put(MAP_NAME_ALBUM, album);
        hashMap.put(MAP_NAME_YEAR, year);
        hashMap.put(MAP_NAME_GENRE, genre);
        hashMap.put(MAP_NAME_COMPOSER, composer);
        hashMap.put(MAP_NAME_TRACK, track);
        hashMap.put(MAP_NAME_DISC, disc);

        aboutList.add(hashMap);

        countFile++;

        if (created){
            if (countFile == listMusic.length){
                countFile = 0;
            }
        }
    }

    private String getDuration(long dur) {
        String sec = String.valueOf((dur % 60000) / 1000);
        String min = String.valueOf(dur / 60000);
        String duration;

        if (sec.length() == 1) {
            duration = "0" + min + ":0" + sec;
        } else {
            duration = "0" + min + ":" + sec;
        }
        return duration;
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            startMediaPlayer(position);
        }
    };

    public void startMediaPlayer(int id) {

        trackId = id;
        seekBar.setEnabled(true);

        ArrayList<Map<String, Object>> listCurrentItem = new ArrayList<>();
        listCurrentItem.add(aboutList.get(id));

        MySimpleAdapter slAdapter = new MySimpleAdapter(getActivity(), listCurrentItem,
                R.layout.about_audio,
                new String[]{MAP_NAME_IMAGE, MAP_NAME_TITLE, MAP_NAME_ARTIST,
                        MAP_NAME_ALBUM, MAP_NAME_YEAR, MAP_NAME_GENRE,
                        MAP_NAME_COMPOSER, MAP_NAME_TRACK, MAP_NAME_DISC},
                new int[]{R.id.ivArt, R.id.text1, R.id.text2, R.id.text3,
                        R.id.text4, R.id.text5, R.id.text6, R.id.text7, R.id.text8});

        llArt.setVisibility(View.GONE);
        llDetails.setVisibility(View.GONE);

        lvAboutAudio.setAdapter(slAdapter);

        File list[] = new File(DEFAULT_AUDIO_PATH).listFiles();

        listMusic = new String[list.length];
        for (int i = 0; i < list.length; i++) {
            listMusic[i] = (list[i].getName());
        }

        intent = new Intent(getActivity(), MP3Player.class)
                .putExtra(getText(R.string.list_music).toString(), listMusic)
                .putExtra(getText(R.string.track_id).toString(), id);

        getActivity().startService(intent);
        tvScroll.setText(listMusic[trackId]);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateSeekBar();
                seekBar.setMax(mp3Player.getMediaPlayer().getDuration());
            }
        }, 100);
    }

    public void updateSeekBar() {
        handler = new Handler();
        seekBar.setProgress(mp3Player.getMediaPlayer().getCurrentPosition());
        if (mp3Player.getMediaPlayer().isPlaying()) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    updateSeekBar();
                    if (mp3Player.getMediaPlayer().getCurrentPosition() >= seekBar.getMax() - 1000) {
                        seekBar.setMax(100000000);
                        onClick(btnForward);
                    }
                    if (mp3Player.getMediaPlayer().isPlaying()) {
                        updateButtonPlay();
                    }
                    if (trackId >= listMusic.length - 1) {
                        btnForward.setClickable(false);
                    } else btnForward.setClickable(true);
                }
            };
            handler.postDelayed(runnable, 100);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnBackward:
                if (trackId != 0) {
                    trackId--;
                    startMediaPlayer(trackId);
                }
                break;
            case R.id.btnPlay:
                if (mp3Player.getMediaPlayer() == null) {
                    startMediaPlayer(trackId);
                    updateButtonPlay();
                } else if (mp3Player.getMediaPlayer() != null && mp3Player.getMediaPlayer()
                        .isPlaying()) {
                    btnPlay.setBackgroundResource(R.drawable.button_pause);
                    mp3Player.getMediaPlayer().pause();
                } else {
                    updateButtonPlay();
                    mp3Player.getMediaPlayer().start();
                    seekBar.setMax(mp3Player.getMediaPlayer().getDuration());
                    updateSeekBar();
                }
                break;
            case R.id.btnForward:
                updateButtonPlay();
                trackId++;
                if (trackId >= listMusic.length) {
                    if (!listCycle && !trackCycle) {
                        trackId--;
                        seekBar.setMax(0);
                        seekBar.setProgress(0);
                        btnPlay.setBackgroundResource(R.drawable.button_play);
                        break;
                    }
                    if (listCycle) {
                        trackId = 0;
                        startMediaPlayer(trackId);
                    }
                }
                if (trackId < listMusic.length) {
                    if (trackCycle) {
                        trackId--;
                    }
                    startMediaPlayer(trackId);
                }
                if (trackId >= listMusic.length && trackCycle) {
                    trackId--;
                    startMediaPlayer(trackId);
                }
                break;
            case R.id.btnRewind:
                if (mp3Player.getMediaPlayer() != null && mp3Player.getMediaPlayer().isPlaying()) {
                    mp3Player.getMediaPlayer()
                            .seekTo(mp3Player.getMediaPlayer().getCurrentPosition() - 15000);
                }
                break;
            case R.id.btnFastward:
                if (mp3Player.getMediaPlayer() != null && mp3Player.getMediaPlayer().isPlaying()) {
                    mp3Player.getMediaPlayer()
                            .seekTo(mp3Player.getMediaPlayer().getCurrentPosition() + 15000);
                }
                break;
        }
    }

    public void updateButtonPlay() {
        btnPlay.setBackgroundResource(R.drawable.button_pause_outline);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mp3Player.getMediaPlayer().seekTo(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onDestroy() {
        getActivity().stopService(new Intent(getActivity(), MP3Player.class));
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mp3Player.getMediaPlayer() != null) {
            updateSeekBar();
        }
    }

    class MySimpleAdapter extends SimpleAdapter {

        public MySimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource,
                               String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }

        @Override
        public Object getItem(int position) {
            return super.getItem(position);
        }

        @Override
        public void setViewImage(ImageView v, int value) {
            super.setViewImage(v, value);
            if (value == R.id.ivArt) {
                v.setImageBitmap(image[trackId]);
            }
        }
    }

    class ReadLinkList extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                URL url = new URL(DEFAULT_URL_PATH_LIST_MUSIC);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod(METHOD_GET);
                urlConnection.connect();
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(urlConnection
                                .getInputStream()));
                String readLine;
                linkList = new ArrayList<>();
                while ((readLine = bufferedReader.readLine()) != null) {
                    linkList.add(countFile, readLine);
                    countFile++;
                }
                image = new Bitmap[countFile];
                bufferedReader.close();
                urlConnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            countFile = 0;
            for (int i = 0; i < linkList.size(); i++) {
                hashMap = new HashMap<>();
                hashMap.put(MAP_NAME_IMAGE, defaultImg);
                hashMap.put(MAP_NAME_ARTIST, linkList.get(countFile));
                hashMap.put(MAP_DETAILS_AUDIO, "");
                hashMap.put(MAP_DURATION_AUDIO, "");

                audioList.add(hashMap);
                Log.d(LOG_TAG, "audioList = " + audioList.get(countFile));
                countFile++;
            }

            slAdapter = new SimpleAdapter(getActivity(), audioList, R.layout.play_list_item,
                    new String[]{MAP_NAME_IMAGE, MAP_NAME_ARTIST,
                            MAP_DETAILS_AUDIO, MAP_DURATION_AUDIO},
                    new int[]{R.id.ivTrack, R.id.tvAudioName, R.id.tvDetails, R.id.tvDuration});

            lvMusic.setAdapter(slAdapter);

            countFile = 0;

            try {
                for (int i = 0; i < linkList.size(); i++) {
                    URL url = new URL(linkList.get(i));
                    Log.d(LOG_TAG, "URL = " + url);
                    new DownloadFile(url).execute();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    class DownloadFile extends AsyncTask {

        public DownloadFile(URL url) {
            this.url = url;
        }

        private URL url = null;

        @Override
        protected Object doInBackground(Object[] params) {

            try {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                InputStream inputStream = httpsURLConnection.getInputStream();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                int downloadSize = 0;

                while ((bytesRead = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, bytesRead);
                    downloadSize += bytesRead;
                    Log.d(LOG_TAG, "Progress = " + downloadSize);
                }
                outputStream.close();
                httpsURLConnection.disconnect();

                byte[] result = outputStream.toByteArray();
                writeByteArrayToFile(result, DEFAULT_AUDIO_NAME);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void writeByteArrayToFile(byte[] byteArray, String outFileName) {

            Object internalPath = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + DEFAULT_ROOT_DIR);

            if (!((File) internalPath).exists()) {
                ((File) internalPath).mkdirs();
            }

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(
                        new File((File) internalPath, outFileName));

                fileOutputStream.write(byteArray);
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            listUtils(DEFAULT_AUDIO_PATH + DEFAULT_AUDIO_NAME, false);
            saveState(true);
        }
    }

    public void saveState(boolean state) {
        if (!state) {
            File list[] = new File(DEFAULT_AUDIO_PATH).listFiles();
            for (int i = 0; i < list.length; i++) {
                list[i].delete();
            }
        }
        btnBackward.setEnabled(true);
        btnPlay.setEnabled(true);
        btnForward.setEnabled(true);

        sPref = getActivity().getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();

        this.state = state;
        editor.putBoolean(STATE, state);
        editor.commit();
    }
}