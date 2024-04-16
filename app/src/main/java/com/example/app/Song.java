package com.example.app;

public class Song {
    private String title;
    private String artist;

    private boolean isPlaying;

    public Song(String title, String artist) {
        this.title = title;
        this.artist = artist;
        this.isPlaying = false;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }
}