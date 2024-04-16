package com.example.app;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private List<Song> songs;
    private OnTitleClickListener listener;
    private ColorStateList oldColors;

    public SongAdapter(List<Song> songs) {
        this.songs = songs;
    }

    public void setOnTitleClickListener(OnTitleClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.textSongTitle.setText(song.getTitle());
        holder.textSongArtist.setText(song.getArtist());
        if (song.isPlaying()) {
            holder.textSongTitle.setTextColor(Color.GREEN);
        } else {
            holder.textSongTitle.setTextColor(oldColors);
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public Song getItem(int position) {
        return songs.get(position);
    }

    public class SongViewHolder extends RecyclerView.ViewHolder {
        public TextView textSongTitle;
        public TextView textSongArtist;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            textSongTitle = itemView.findViewById(R.id.textSongTitle);
            textSongArtist = itemView.findViewById(R.id.textSongArtist);
            oldColors = textSongArtist.getTextColors();

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onTitleClick(position);
                    }
                }
            });
        }
    }

    public interface OnTitleClickListener {
        void onTitleClick(int position);
    }
}
