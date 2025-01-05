package com.example.meet_fit.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meet_fit.R;
import com.example.meet_fit.models.Info;
import com.example.meet_fit.models.dataAdapter;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private final Context context;
    private List<Info> recycleModels;
    private OnCallButtonClickListener onCallButtonClickListener;

    public interface OnCallButtonClickListener {
        void onCallButtonClick(String phoneNumber);
    }

    public RecyclerAdapter(Context context, List<Info> recycleModels,OnCallButtonClickListener listener) {
        this.context = context;
        this.recycleModels = recycleModels;
        this.onCallButtonClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        // Inflate the item layout (profiles_recycler.xml)
        View view = inflater.inflate(R.layout.profiles_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Info info = recycleModels.get(position);

        // Safely set text fields
        holder.age.setText(info.getAge() != null ? info.getAge() : "N/A");
        holder.fitLevel.setText(info.getFitLevel() != null ? info.getFitLevel() : "N/A");
        holder.aboutMe.setText(info.getAboutMe() != null ? info.getAboutMe() : "N/A");
        holder.location.setText(info.getLocation() != null ? info.getLocation() : "N/A");
        holder.userName.setText(info.getUserName() != null ? info.getUserName() : "N/A");

        // Handle the activities list
        List<String> activities = info.getActivities();
        if (activities != null && !activities.isEmpty()) {
            holder.activity1.setText(activities.size() > 0 ? activities.get(0) : "");
            holder.activity2.setText(activities.size() > 1 ? activities.get(1) : "");
            holder.activity3.setText(activities.size() > 2 ? activities.get(2) : "");
            holder.activity4.setText(activities.size() > 3 ? activities.get(3) : "");
            holder.activity5.setText(activities.size() > 4 ? activities.get(4) : "");
            holder.activity6.setText(activities.size() > 5 ? activities.get(5) : "");
        } else {
            holder.activity1.setText("");
            holder.activity2.setText("");
            holder.activity3.setText("");
        }

        // Decode and set Base64 image

        dataAdapter.base64ToImageView(info.getPhoto(), holder.photo);

        if (info.getPhoneNumber() == null || info.getPhoneNumber().isEmpty()) {
            holder.btnContactMe.setEnabled(false); // Disable the button
            holder.btnContactMe.setText("No Number");
        } else {
            holder.btnContactMe.setEnabled(true); // Enable the button


            // Debug the phone number being passed
            Log.d("RecyclerAdapter", "Phone number for " + info.getUserName() + ": " + info.getPhoneNumber());

            holder.btnContactMe.setOnClickListener(v -> {
                if (onCallButtonClickListener != null) {
                    Log.d("RecyclerAdapter", "Click detected for " + info.getPhoneNumber());
                    onCallButtonClickListener.onCallButtonClick(info.getPhoneNumber());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return recycleModels.size();
    }

    // Optionally provide a way to update the data set




    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView age;
        private final TextView fitLevel;
        private final TextView aboutMe;
        private final TextView location;
        private final TextView activity1;
        private final TextView activity2;
        private final TextView activity3;
        private final TextView activity4;
        private final TextView activity5;
        private final TextView activity6;
        private final ImageView photo;
        private final TextView  userName;
        private final Button btnContactMe;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            age = itemView.findViewById(R.id.tvAgeValue);
            fitLevel = itemView.findViewById(R.id.tvFitLevelValue);
            aboutMe = itemView.findViewById(R.id.tvAboutMe);
            location = itemView.findViewById(R.id.tvLocationStatValue);
            activity1 = itemView.findViewById(R.id.tvActivity1);
            activity2 = itemView.findViewById(R.id.tvActivity2);
            activity3 = itemView.findViewById(R.id.tvActivity3);
            activity4 = itemView.findViewById(R.id.tvActivity4);
            activity5 = itemView.findViewById(R.id.tvActivity5);
            activity6 = itemView.findViewById(R.id.tvActivity6);
            photo = itemView.findViewById(R.id.ivProfilePicture);
            userName =  itemView.findViewById(R.id.tvUserName);
            btnContactMe = itemView.findViewById(R.id.btnContactMe);
        }
    }

}
