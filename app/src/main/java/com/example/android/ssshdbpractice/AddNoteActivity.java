package com.example.android.ssshdbpractice;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


public class AddNoteActivity extends AppCompatActivity {

    public static final String EXTRA_ID =
            "com.example.android.ssshdbpractice.EXTRA_ID";
    public static final String EXTRA_TITLE =
            "com.example.android.ssshdbpractice.EXTRA_TITLE";
    public static final String EXTRA_DESCRIPTION =
            "com.example.android.ssshdbpractice.EXTRA_DESCRIPTION";
    public static final String EXTRA_RADIUS =
            "com.example.android.ssshdbpractice.EXTRA_RADIUS";
    public static final String EXTRA_MODE =
            "com.example.android.ssshdbpractice.EXTRA_MODE";
    public static final String EXTRA_LATITUDE =
            "com.example.android.ssshdbpractice.EXTRA_LATITUDE";
    public static final String EXTRA_LONGITUDE =
            "com.example.android.ssshdbpractice.EXTRA_LONGITUDE";
    public static final String EXTRA_PLACE_ID =
            "com.example.android.ssshdbpractice.EXTRA_PLACE_ID";

    private EditText editTextTitle;
    private EditText editTextDescription;
    private EditText editTextRadius;

    private RadioGroup radioGroup ;
    private RadioButton generalRb ,silentRb ,vibrateRb;

    private final String DEFAULT_TITLE = "No Title";
    private final String DEFAULT_DESCRIPTION = "No Notes";
    private final int DEFAULT_RADIUS = 150 ;

    private double currentLatitude, currentLongitude;
    private String currentPlaceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        editTextTitle = findViewById(R.id.title);
        editTextDescription = findViewById(R.id.description);
        editTextRadius = findViewById(R.id.radius);
        radioGroup = findViewById(R.id.radio_group);
        generalRb = findViewById(R.id.mode_general);
        silentRb = findViewById(R.id.mode_silent);
        vibrateRb = findViewById(R.id.mode_airplane);

        generalRb.setChecked(true);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        Intent intent = getIntent();

        currentLatitude = intent.getDoubleExtra(EXTRA_LATITUDE, 0);
        currentLongitude = intent.getDoubleExtra(EXTRA_LONGITUDE, 0);
        currentPlaceId = intent.getStringExtra(EXTRA_PLACE_ID);

        if (intent.hasExtra(EXTRA_ID)) {
            setTitle("Edit Note");

            editTextTitle.setText(intent.getStringExtra(EXTRA_TITLE));
            editTextDescription.setText(intent.getStringExtra(EXTRA_DESCRIPTION));
            editTextRadius.setText(String.valueOf(intent.getIntExtra(EXTRA_RADIUS,150)));

            String mode = intent.getStringExtra(EXTRA_MODE);

            if(mode.equals("general")) {
                generalRb.setChecked(true);
            }
            else if(mode.equals("silent")) {
                silentRb.setChecked(true);
            }
            else if(mode.equals("vibrate")) {
                vibrateRb.setChecked(true);
            }


        } else {
            setTitle("Add Note");
        }
    }

    private void saveNote() {

        int radius = DEFAULT_RADIUS;
        String title = DEFAULT_TITLE;
        String description = DEFAULT_DESCRIPTION;

        if(!(TextUtils.isEmpty(editTextRadius.getText().toString()))){
            radius = Integer.parseInt(editTextRadius.getText().toString());
        }

        if(!(TextUtils.isEmpty(editTextTitle.getText().toString()))){
            title = editTextTitle.getText().toString();
        }

        if(!(TextUtils.isEmpty(editTextDescription.getText().toString()))){
            description = editTextDescription.getText().toString();
        }

        String mode = "general";

        if(generalRb.isChecked()){
            mode = "general";
        }

        else if(silentRb.isChecked()){
            mode = "silent";
        }

        else if(vibrateRb.isChecked()){
            mode = "vibrate";
        }

//        if (title.trim().isEmpty() || description.trim().isEmpty()) {
//            Toast.makeText(this, "Please insert a title and description", Toast.LENGTH_SHORT).show();
//            return;
//        }

        Intent data = new Intent(this, NotesListActivity.class);
        data.putExtra(EXTRA_TITLE, title);
        data.putExtra(EXTRA_DESCRIPTION, description);
        data.putExtra(EXTRA_RADIUS, radius);
        data.putExtra(EXTRA_LATITUDE, currentLatitude);
        data.putExtra(EXTRA_LONGITUDE, currentLongitude);
        data.putExtra(EXTRA_MODE, mode);
        data.putExtra(EXTRA_PLACE_ID, currentPlaceId);

        int id = getIntent().getIntExtra(EXTRA_ID, -1);
        if (id != -1) {
            data.putExtra(EXTRA_ID, id);
        }

        if(getSupportActionBar().getTitle().equals("Add Note")){
            NoteViewModel noteViewModel;
            noteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);

            noteViewModel.deleteAllNotes();

            Note note = new Note(title, description, radius, mode, currentLatitude, currentLongitude, currentPlaceId);
            noteViewModel.insert(note);

            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();

        }
        else{
            setResult(RESULT_OK, data);
        }

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_note_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_note:
                saveNote();
                return true;

            case R.id.delete_note:

//                NoteViewModel noteViewModel;
//                noteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);
//
//                int id = getIntent().getIntExtra(EXTRA_ID, -1);
//
//                if(id != -1){
//                    Note note = new Note();
//                    note.setId(id);
//                    noteViewModel.delete(note);
//                }
//
//                Toast.makeText(this, "Note Deleted", Toast.LENGTH_SHORT).show();
//                //noteViewModel.delete(adapter.getNoteAt(viewHolder.getAdapterPosition()));
//                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
