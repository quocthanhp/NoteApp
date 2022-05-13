package com.example.noteapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.noteapp.Database.RoomDB;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    private RecyclerView notesRecView;
    private NotesRecViewAdapter adapter;
    private List<Note> notes = new ArrayList<>();
    private RoomDB database;
    private FloatingActionButton fab_add;
    private SearchView searchView_home;
    private Note selectedNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notesRecView = findViewById(R.id.notesRecView);
        fab_add = findViewById(R.id.fab_add);
        searchView_home = findViewById(R.id.searchView_home);

        database = RoomDB.getInstance(this);
        notes = database.mainDAO().getAll();

        updateRecycler(notes);

        // add item when click
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // initiate activity
                Intent intent = new Intent(MainActivity.this, NotesTakerActivity.class);
                startActivityForResult(intent, 101); // add new note

            }
        });

        searchView_home.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String newText) {
        List<Note> filteredList = new ArrayList<>();
        for (Note singleNote : notes) {
            if (singleNote.getTitle().toLowerCase().contains(newText.toLowerCase()) ||
                    singleNote.getNotes().toLowerCase().contains(newText.toLowerCase())) {
                filteredList.add(singleNote);
            }
        }
        adapter.filterList(filteredList);
    }

    // receive data from note-taker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==101) {
            if (resultCode == Activity.RESULT_OK) {
                Note new_notes = (Note) data.getSerializableExtra("note"); // name in putExtra
                // add to database
                database.mainDAO().insert(new_notes);

                notes.clear();
                // add to notes array
                notes.addAll(database.mainDAO().getAll());

                adapter.notifyDataSetChanged();

            }
        }
        else if (requestCode==102) {
            if (resultCode == Activity.RESULT_OK) {
                Note new_notes = (Note) data.getSerializableExtra("note");
                database.mainDAO().update(new_notes.getID(), new_notes.getTitle(), new_notes.getNotes());
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void updateRecycler(List<Note> notes) {
        notesRecView.setHasFixedSize(true);
        notesRecView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        adapter = new NotesRecViewAdapter(notes, MainActivity.this, noteClickListener);
        notesRecView.setAdapter(adapter);

    }

    private final NoteClickListener noteClickListener = new NoteClickListener() {
        @Override
        public void onClick(Note note) {
            // For edit notes
            Intent intent = new Intent(MainActivity.this, NotesTakerActivity.class);
            intent.putExtra("old_note", note);
            startActivityForResult(intent, 102); // edit code
        }

        @Override
        public void onLongClick(Note note, CardView cardView) {
            // For delete noted
            selectedNote = new Note();
            selectedNote = note;
            showPopup(cardView);

        }
    };

    private void showPopup(CardView cardView) {
        PopupMenu popupMenu = new PopupMenu(this, cardView);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case (R.id.delete):
                database.mainDAO().delete(selectedNote);
                notes.remove(selectedNote);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }
}
