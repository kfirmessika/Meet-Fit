package com.example.meet_fit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meet_fit.R;
import com.example.meet_fit.models.Event;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    public interface OnEventActionListener {
        void onJoinClicked(Event event);
    }

    private final ArrayList<Event> eventsList;
    private final OnEventActionListener eventActionListener;

    private final String currentUserName;

    public EventsAdapter(ArrayList<Event> eventsList,
                         OnEventActionListener listener,
                         String currentUserName) {
        this.eventsList = eventsList;
        this.eventActionListener = listener;
        this.currentUserName = currentUserName;  // store the current user's name
    }
    public void updateData(List<Event> newEvents) {
        eventsList.clear();
        eventsList.addAll(newEvents);
        notifyDataSetChanged();
    }



    // Constructor


    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate item_event layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.events_recycler, parent, false);

        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        // Get the Event object
        Event event = eventsList.get(position);

        boolean userAlreadyJoined = false;
        if (event.getParticipants() != null) {
            userAlreadyJoined = event.getParticipants().contains(currentUserName);
        }

        if (userAlreadyJoined) {
            // Already joined -> Disable the button
            holder.btnJoin.setEnabled(false);
            holder.btnJoin.setText("Joined");
        } else {
            // Not joined -> Enable the button
            holder.btnJoin.setEnabled(true);
            holder.btnJoin.setText("Join");
        }


        // 1) Update placeholders (activity images)
        String activity = event.getActivity();
        setActivityImage(holder.profileContainer, activity);

        // 2) Populate the text fields
        // Date
        if (event.getDate() != null) {
            // Format the date as dd/MM/yyyy
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String dateStr = sdf.format(event.getDate());
            holder.tvDateValue.setText(dateStr);
        } else {
            holder.tvDateValue.setText("--/--/----");
        }

        // Time
        LocalTime time = event.getTime();
        if (time != null) {
            // Display as HH:mm
            String timeStr = String.format(Locale.getDefault(), "%02d:%02d", time.getHour(), time.getMinute());
            holder.tvTimeValue.setText(timeStr);
        } else {
            holder.tvTimeValue.setText("--:--");
        }

        // Location, FitLevel, About
        holder.tvLocationStatValue.setText(event.getLocation());
        holder.tvFitLevelValue.setText(event.getFitLevel());
        holder.tvAboutValue.setText(event.getAboutEvent());

        // 3) We don’t have participants count in the Event model, so set a placeholder or 0
        holder.tvParticipentsValue.setText(
                String.valueOf(event.getParticipants().size())
        ); // or dynamic if you have that data

        // 4) Button “Join” click example
        holder.btnJoin.setOnClickListener(v -> {
            if (eventActionListener != null) {
                eventActionListener.onJoinClicked(event);
                holder.btnJoin.setEnabled(false);
                holder.btnJoin.setText("Joined");

            }
        });


    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    /**
     * Dynamically sets the image or background based on the event's activity.
     */
    private void setActivityImage(LinearLayout profileContainer, String activity) {
        // Placeholder approach (switch or if-else)
        if (activity == null) activity = "";
        activity = activity.toLowerCase();

        switch (activity) {
            case "running":
                // placeholder: R.drawable.running_placeholder
                profileContainer.setBackgroundResource(R.drawable.running);
                break;
            case "swimming":
                profileContainer.setBackgroundResource(R.drawable.swimming);
                break;
            case "cycling":
                profileContainer.setBackgroundResource(R.drawable.cycling);
                break;
            case "hiking":
                profileContainer.setBackgroundResource(R.drawable.hiking);
                break;
            case "gym":
                profileContainer.setBackgroundResource(R.drawable.gym);
                break;
            default:
                // Fallback or a generic image
                profileContainer.setBackgroundResource(R.drawable.running);
                break;
        }
    }

    // ViewHolder class
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        LinearLayout profileContainer;
        MaterialCardView cardStats;
        TextView tvDateValue, tvTimeValue, tvLocationStatValue;
        TextView tvFitLevelValue, tvParticipentsValue, tvAboutValue;
        Button btnJoin;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

            profileContainer       = itemView.findViewById(R.id.profileContainer);
            cardStats             = itemView.findViewById(R.id.cardStats);
            tvDateValue           = itemView.findViewById(R.id.tvDateValue);
            tvTimeValue           = itemView.findViewById(R.id.tvTimeValue);
            tvLocationStatValue   = itemView.findViewById(R.id.tvLocationStatValue);
            tvFitLevelValue       = itemView.findViewById(R.id.tvFitLevelValue);
            tvParticipentsValue   = itemView.findViewById(R.id.tvParticipentsValue);
            tvAboutValue          = itemView.findViewById(R.id.tvAboutValue);
            btnJoin               = itemView.findViewById(R.id.btnJoin);
        }




    }
}


