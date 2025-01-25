package com.example.meet_fit.fragmetns;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meet_fit.R;
import com.example.meet_fit.activities.MainActivity;
import com.example.meet_fit.adapters.EventsAdapter;
import com.example.meet_fit.models.Event;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Events#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Events extends Fragment {
    private RecyclerView rv;
    private EventsAdapter eventsAdapter;
    private ArrayList<Event> allEventsList = new ArrayList<>();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Events() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Events.
     */
    // TODO: Rename and change types and number of parameters
    public static Events newInstance(String param1, String param2) {
        Events fragment = new Events();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        // Initialize RecyclerView
        rv = view.findViewById(R.id.rv2);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        eventsAdapter = new EventsAdapter(allEventsList);
        rv.setAdapter(eventsAdapter);

        // Either fetch data here or call a method in your Activity
        fetchAllEventsFromActivity();
        return view;

    }

    private void fetchAllEventsFromActivity() {
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.fetchAllEvents(new MainActivity.OnEventsFetchedListener() {
            @Override
            public void onEventsFetched(ArrayList<Event> events) {
                // This method is called after the Firebase data read is complete
                if (events.isEmpty()) {
                    Toast.makeText(requireContext(), "No events found.", Toast.LENGTH_SHORT).show();
                }

                // Update the local list and notify adapter
                allEventsList.clear();
                allEventsList.addAll(events);
                eventsAdapter.notifyDataSetChanged();
            }
        });
    }
}