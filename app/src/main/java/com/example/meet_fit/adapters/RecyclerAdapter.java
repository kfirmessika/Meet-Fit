package com.example.meet_fit.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meet_fit.R;
import com.example.meet_fit.models.Info;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private final Context context;
    private List<Info> recycleModels;

    public RecyclerAdapter(Context context, List<Info> recycleModels) {
        this.context = context;
        this.recycleModels = recycleModels;
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
        setBase64Image(info.getPhoto(), holder.photo);
    }

    @Override
    public int getItemCount() {
        return recycleModels.size();
    }

    // Optionally provide a way to update the data set


    public void setBase64Image(String base64Image, ImageView imageView) {
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                imageView.setImageBitmap(bitmap);
            } catch (IllegalArgumentException e) {
                // Handle invalid Base64 string
                e.printStackTrace();
                imageView.setImageResource(R.drawable.meet_fit_logo);
            }
        } else {
            // If empty or null, set a placeholder
            imageView.setImageResource(R.drawable.meet_fit_logo);
        }
    }

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
        }
    }


    // Method to update the list and notify adapter
    public void setFilteredList(List<Info> filteredList){
        this.recycleModels = filteredList;
        notifyDataSetChanged();
    }
}
