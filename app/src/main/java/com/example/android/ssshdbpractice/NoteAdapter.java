package com.example.android.ssshdbpractice;

import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteHolder> {

    private List<Note> notes = new ArrayList<>();
    private OnItemClickListener listener;


    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new NoteHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        Note currentNote = notes.get(position);

        holder.textViewTitle.setText(currentNote.getTitle());

        holder.textViewLatitude.setText(String.valueOf(currentNote.getLatitude()));
        holder.textViewLongitude.setText(String.valueOf(currentNote.getLongitude()));

        String mode = currentNote.getMode();
        holder.textViewMode.setText(mode);

        if(mode.equals("general")) {
            holder.imageViewMode.setBackgroundResource(R.drawable.general_mode);
        }
        else if(mode.equals("silent")) {
            holder.imageViewMode.setBackgroundResource(R.drawable.silent_mode);
        }
        else if(mode.equals("vibrate")) {
            holder.imageViewMode.setBackgroundResource(R.drawable.vibrate_mode);
        }


    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void setNotes(List<Note> notes){
        this.notes = notes;
        notifyDataSetChanged();
    }

    public Note getNoteAt(int position) {
        return notes.get(position);
    }


    class NoteHolder extends RecyclerView.ViewHolder{

        private TextView textViewTitle;
        private TextView textViewMode;
        private TextView textViewLatitude;
        private TextView textViewLongitude;
        private ImageView imageViewMode;

        public NoteHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.title_list);
            textViewMode = itemView.findViewById(R.id.mode_list);
            textViewLatitude = itemView.findViewById(R.id.latitude_list);
            textViewLongitude = itemView.findViewById(R.id.longitude_list);
            imageViewMode = itemView.findViewById(R.id.mode_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(notes.get(position));
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
