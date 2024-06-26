package com.example.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import MusicServer.MusicManagerPrx;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

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

    private ProgressDialog progressDialog;
    boolean isPlaying = false;


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

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Parlez maintenant...");
        progressDialog.setCancelable(false); // Empêcher l'utilisateur de fermer la boîte de dialogue


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
            isPlaying = !isPlaying;
            changePausePlayButton();
            musicManager.pauseMusic();
        });

        buttonMircro.setOnClickListener(v -> {
            if (speechRecognizer != null) {
                progressDialog.show();
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
        for (MusicServer.MusicInfo music : musicInfos) {
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
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                // Résultats de la reconnaissance vocale
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches!= null &&!matches.isEmpty()) {
                    String spokenText = matches.get(0); // Récupérez la première correspondance (la plus probable)
                    // Utilisez spokenText comme vous le souhaitez, par exemple, affichez-le dans un TextView
                    System.out.println(spokenText);

                    new ExtractTask().execute(spokenText);
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

    private class ExtractTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... spokenText) {
            OkHttpClient client = new OkHttpClient();

            // Create a JSON object with a "text" key
            JSONObject jsonObjectRequest = new JSONObject();
            try {
                jsonObjectRequest.put("text", spokenText[0]);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            System.out.println(spokenText[0]);

            // Convert the JSON object to a string
            String jsonString = jsonObjectRequest.toString();

            RequestBody requestBody = RequestBody.create(MediaType.get("application/json"), jsonString);

            Request request = new Request.Builder()
                    .url("http://192.168.0.19:6543/extract")
                    .post(requestBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                // Parse the response
                if (response.isSuccessful()) {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    String action = jsonObject.getString("action");
                    String musicName = jsonObject.getString("music_name");
                    // Do something with the extracted action and music name
                    return "Action: " + action + ", Music Name: " + musicName;
                } else {
                    // Handle error
                    return "Error: " + response.code() + " message: " + response.message();
                }
            } catch (IOException | JSONException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println(result);
            String[] parts = result.split(", ");
            String action = parts[0].replace("Action: ", "");
            String musicName = parts[1].replace("Music Name: ", "");

            if (action.equals("jouer")) {
                // Find the song in the list that matches the music name
                for (int i = 0; i < adapter.getItemCount(); i++) {
                    Song song = adapter.getItem(i);
                    if (song.getTitle().equals(musicName)) {
                        onTitleClick(i); // Play the song
                        break;
                    }
                }
            }
            if (action.equals("pause")) {
                isPlaying = !isPlaying;
                changePausePlayButton();
                musicManager.pauseMusic();
            }
        }
    }


    @Override
    public void onTitleClick(int position) {
        isPlaying = true;
        changePausePlayButton();
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

    public void changePausePlayButton() {
        if (isPlaying) {
            buttonPlay.setImageResource(R.drawable.ic_pause);
        } else {
            buttonPlay.setImageResource(R.drawable.ic_play);
        }
    }
}
