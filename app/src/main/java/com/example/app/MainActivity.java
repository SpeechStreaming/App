package com.example.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Util;

import MusicServer.MusicManagerPrx;
import MusicServer.MusicNotFoundError;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SongAdapter.OnTitleClickListener {
    RecyclerView recyclerView;
    TextView textSongTitle;
    TextView textSongArtist;
    SongAdapter adapter;
    int lastPlayed = -1;
    ImageButton buttonPrevious;
    ImageButton buttonNext;
    ImageButton buttonPlay;
    MusicManagerPrx musicManager;
    MediaPlayer mediaPlayer;
    FloatingActionButton buttonMircro;

    private SpeechRecognizer speechRecognizer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = this.findViewById(R.id.recyclerView);
        textSongTitle = this.findViewById(R.id.textSongTitle);
        textSongArtist = this.findViewById(R.id.textSongArtist);
        buttonPrevious = this.findViewById(R.id.buttonPrevious);
        buttonNext = this.findViewById(R.id.buttonNext);
        buttonPlay = this.findViewById(R.id.buttonPlay);
        buttonMircro = this.findViewById(R.id.buttonMircro);

        buttonPrevious.setOnClickListener(v -> {
            if (lastPlayed > 0) {
                int newPosition = lastPlayed - 1;
                onTitleClick(newPosition);
            }
        });

        buttonNext.setOnClickListener(v -> {
            if (lastPlayed < adapter.getItemCount() - 1) {
                int newPosition = lastPlayed + 1;
                onTitleClick(newPosition);
            }
        });

        buttonPlay.setOnClickListener(v -> {
            musicManager.pauseMusic();
        });

        buttonMircro.setOnClickListener(v -> {
            if (speechRecognizer != null) {
                // Créez un Intent pour la reconnaissance vocale
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR"); // Langue française
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant...");

                // Démarrez la reconnaissance vocale
                speechRecognizer.startListening(intent);
            }
        });


        String[] customArgs = new String[]{"--Ice.MessageSizeMax=0"};
        Communicator communicator = Util.initialize(customArgs);
        musicManager = MusicManagerPrx.checkedCast(communicator.stringToProxy("MusicManager:default -h 192.168.0.19 -p 10000"));
        MusicServer.MusicInfo[] musicInfos = musicManager.listAllMusic();

        List<Song> songs = new ArrayList<>();
        for (MusicServer.MusicInfo music: musicInfos) {
            songs.add(new Song(music.title, music.artist));

        }

        adapter = new SongAdapter(songs);
        adapter.setOnTitleClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        mediaPlayer = new MediaPlayer();



        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {
                // Résultats de la reconnaissance vocale
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0); // Récupérez la première correspondance (la plus probable)
                    // Utilisez spokenText comme vous le souhaitez, par exemple, affichez-le dans un TextView
                    System.out.println(spokenText);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });
    }


    @Override
    public void onTitleClick(int position) {
        Song song = adapter.getItem(position);
        textSongTitle.setText(song.getTitle());
        textSongArtist.setText(song.getArtist());
        if (lastPlayed != -1) {
            adapter.getItem(lastPlayed).setPlaying(false);
        }
        song.setPlaying(true);
        adapter.notifyItemChanged(lastPlayed);
        adapter.notifyItemChanged(position);
        lastPlayed = position;
        adapter.getItem(0);
        try {
            musicManager.playMusic(song.getTitle(), song.getArtist());
//            mediaPlayer.reset();
//            mediaPlayer.setDataSource("rtsp://192.168.0.19:554/stream"); // Replace with your RTSP URL
//            mediaPlayer.prepare();
//            mediaPlayer.start();
        } catch (MusicServer.MusicNotFoundError e) {
            System.out.println("La musique n'a pas été trouvée.");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        mediaPlayer.release();
    }
}
