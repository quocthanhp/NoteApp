package com.example.noteapp;

import androidx.cardview.widget.CardView;

public interface NoteClickListener {
    void onClick(Note note);
    void onLongClick(Note note, CardView cardView);
}
