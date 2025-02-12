package com.example.meet_fit.fragmetns;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meet_fit.R;
import com.example.meet_fit.activities.MainActivity;
import com.example.meet_fit.adapters.EventsAdapter;
import com.example.meet_fit.models.Event;
import com.example.meet_fit.models.Info;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Events extends Fragment implements EventsAdapter.OnEventActionListener {

    private List<String> lastSelectedActivities = new ArrayList<>();
    private List<String> lastSelectedFitnessLevels = new ArrayList<>();
    private boolean lastFilterByLocation = false;

    private RecyclerView rv;
    private EventsAdapter eventsAdapter;
    // allEventsList is our master list – do not clear or override this during filtering
    private ArrayList<Event> allEventsList = new ArrayList<>();
    private static final int PERMISSIONS_REQUEST_CALENDAR = 100;
    // This flag indicates if the user wants to filter by location.
    private ArrayList<Event> originalEventsList = new ArrayList<>();

    private List<Info> originalList;
    private final List<String> selectedActivities = new ArrayList<>();
    private final List<String> selectedFitnessLevels = new ArrayList<>();
    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private Event pendingEvent = null;
    private static final int STATUS_INVITED = 4;
    // Fragment arguments (if used)
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public Events() {
        // Required empty public constructor
    }

    public static Events newInstance(String param1, String param2) {
        Events fragment = new Events();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    // ------------------------------------------------------------------
    // Fragment Lifecycle
    // ------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1) Initialize Firebase references
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_events, container, false);

        Button filterButton = (Button)view.findViewById(R.id.btnFilter);

        // 2) Set up RecyclerView (but no adapter yet)
        rv = view.findViewById(R.id.rv2);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // 3) First, fetch current user's name
        getUsernameOfCurrentUser(userName -> {
            if (userName == null) {
                Toast.makeText(requireContext(), "Could not fetch user name", Toast.LENGTH_SHORT).show();
                return;
            }

            // 4) Create & attach adapter once we have the username
            eventsAdapter = new EventsAdapter(allEventsList, Events.this, userName);
            rv.setAdapter(eventsAdapter);

            // 5) Remove past events from DB, then load valid events
            removePastEventsThen(() -> {
                // After removing old events, fetch the fresh events
                fetchAllEventsFromActivity();
            });
        });

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFilterDialog();
            }
        });

        return view;
    }

    // ------------------------------------------------------------------
    // A) Remove past events from Firebase, then run a callback
    // ------------------------------------------------------------------
    private void removePastEventsThen(@NonNull Runnable onComplete) {
        // 1) Reference to all users
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // For each user
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    // userSnap.getKey() is the uid
                    DataSnapshot eventsSnap = userSnap.child("events");
                    if (!eventsSnap.exists()) continue; // No events for this user

                    // 2) For each event (event1, event2, etc.) under that user
                    for (DataSnapshot singleEventSnap : eventsSnap.getChildren()) {
                        // Manually extract fields
                        Long dateLong = singleEventSnap.child("date").getValue(Long.class);
                        String timeString = singleEventSnap.child("time").getValue(String.class);
                        String userName = singleEventSnap.child("userNmae").getValue(String.class);
                        String eventId = singleEventSnap.child("eventId").getValue(String.class);

                        // Extract participants
                        List<String> participants = new ArrayList<>();
                        DataSnapshot participantsSnap = singleEventSnap.child("participants");
                        if (participantsSnap.exists()) {
                            for (DataSnapshot pSnap : participantsSnap.getChildren()) {
                                String participant = pSnap.getValue(String.class);
                                if (participant != null) {
                                    participants.add(participant);
                                }
                            }
                        }

                        // Convert 'dateLong' to Date
                        Date date = (dateLong != null) ? new Date(dateLong) : null;

                        // Parse 'timeString' to LocalTime
                        LocalTime time = null;
                        if (timeString != null) {
                            try {
                                time = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()));
                            } catch (DateTimeParseException e) {
                                e.printStackTrace();
                                Log.d("EventsFragment", "Invalid time format for event: " + singleEventSnap.getKey());
                                continue; // Skip this event due to invalid time format
                            }
                        }

                        // Create Event object manually
                        Event event = new Event();
                        event.setDate(date);
                        event.setTime(time);
                        event.setUserNmae(userName);
                        event.setEventId(eventId);
                        event.setParticipants(participants);

                        // 3) Check if event is in the past
                        if (isPastEvent(event)) {
                            // 4) Remove from this user’s events node
                            singleEventSnap.getRef().removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("EventsFragment", "Removed past event: " + singleEventSnap.getKey());
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("EventsFragment", "Failed to remove event: " + singleEventSnap.getKey(), e);
                                    });
                        }
                    }
                }
                // After we finish scanning all users, run callback
                onComplete.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                onComplete.run();
            }
        });
    }

    // ------------------------------------------------------------------
    // B) Fetch all events from Activity (which presumably loads from DB)
    // ------------------------------------------------------------------
    private void fetchAllEventsFromActivity() {
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.fetchAllEvents(new MainActivity.OnEventsFetchedListener() {
            @Override
            public void onEventsFetched(ArrayList<Event> events) {
                if (events.isEmpty()) {
                    Toast.makeText(requireContext(), "No events found.", Toast.LENGTH_SHORT).show();
                }

                allEventsList.clear();
                allEventsList.addAll(events);

                originalEventsList.clear();
                originalEventsList.addAll(events);

                // By now, eventsAdapter is definitely not null
                eventsAdapter.notifyDataSetChanged();
            }
        });
    }

    // ------------------------------------------------------------------
    // C) The "Join" Button Logic from EventsAdapter
    // ------------------------------------------------------------------
    @Override
    public void onJoinClicked(Event event) {
        getUsernameOfCurrentUser(userName -> {
            if (userName == null) {
                Toast.makeText(requireContext(), "Could not fetch user name", Toast.LENGTH_SHORT).show();
                return;
            }
            addUserToCreatorEventParticipants(event, userName);
        });
    }

    private void addUserToCreatorEventParticipants(@NonNull Event event, @NonNull String currentUserName) {
        String creatorUid = event.getUserNmae(); // Ensure this is correctly fetching the creator's UID
        String eventId = event.getEventId();
        if (creatorUid == null || eventId == null) {
            Toast.makeText(requireContext(), "Invalid event data (missing creatorUid/eventId)", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference eventsRef = usersRef.child(creatorUid).child("events");
        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String firebaseEventId = childSnapshot.child("eventId").getValue(String.class);

                    if (eventId.equals(firebaseEventId)) {
                        // Found the matching event
                        List<String> participantsList = new ArrayList<>();
                        DataSnapshot participantsSnap = childSnapshot.child("participants");
                        if (participantsSnap.exists()) {
                            for (DataSnapshot pSnap : participantsSnap.getChildren()) {
                                String participant = pSnap.getValue(String.class);
                                if (participant != null) {
                                    participantsList.add(participant);
                                }
                            }
                        }

                        // Add current user if not already in the list
                        if (!participantsList.contains(currentUserName)) {
                            participantsList.add(currentUserName);
                        }

                        DatabaseReference participantsRef = childSnapshot.child("participants").getRef();
                        participantsRef.setValue(participantsList)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(requireContext(), "Joined event successfully!", Toast.LENGTH_SHORT).show();

                                    // Update local event object & refresh
                                    event.getParticipants().clear();
                                    event.getParticipants().addAll(participantsList);
                                    eventsAdapter.notifyDataSetChanged();

                                    showSaveToCalendarDialog(event);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireContext(), "Failed to join: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                        break; // No need to keep looping
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "DB error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ------------------------------------------------------------------
    // D) Fetch the current user's name from DB
    // ------------------------------------------------------------------
    public interface OnUsernameFetchedListener {
        void onUsernameFetched(String userName);
    }

    private void getUsernameOfCurrentUser(OnUsernameFetchedListener listener) {
        if (mAuth == null || mAuth.getCurrentUser() == null) {
            listener.onUsernameFetched(null);
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userNameRef = usersRef.child(userId).child("info").child("userName");
        userNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.getValue(String.class);
                if (listener != null) {
                    listener.onUsernameFetched(userName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (listener != null) {
                    listener.onUsernameFetched(null);
                }
            }
        });
    }

    // ------------------------------------------------------------------
    // E) isPastEvent check (Date-based)
    // ------------------------------------------------------------------
    private boolean isPastEvent(Event event) {
        if (event.getDate() == null || event.getTime() == null) {
            // If either date or time is missing, treat the event as past

            Log.d("EventsFragment", "Event missing date/time: " + event.getEventId());
            return true;
        }

        // Convert 'date' (Date) to LocalDate
        Instant instant = event.getDate().toInstant();
        LocalDate eventLocalDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();

        // Get 'time' as LocalTime
        LocalTime eventLocalTime = event.getTime();

        // Combine LocalDate and LocalTime to LocalDateTime
        LocalDateTime eventDateTime = LocalDateTime.of(eventLocalDate, eventLocalTime);

        // Get current LocalDateTime
        ZoneId israelZone = ZoneId.of("Asia/Jerusalem");
        LocalDateTime israelTime = LocalDateTime.now(israelZone);


        Log.d("EventsFragment", "Comparing eventDateTime=" + eventDateTime + " to now=" + israelTime);

        return eventDateTime.isBefore(israelTime);
    }

    private void checkCalendarPermissions(Event event) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CALENDAR)
                        != PackageManager.PERMISSION_GRANTED) {

            // Store the event temporarily
            pendingEvent = event;

            // Request the permissions
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR},
                    PERMISSIONS_REQUEST_CALENDAR);
        } else {
            // Permissions already granted, proceed with calendar operations
            saveEventToCalendar(event);
        }
    }

    // Handle the permission request response
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CALENDAR) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, proceed with calendar operations
                if (pendingEvent != null) {
                    saveEventToCalendar(pendingEvent);
                    pendingEvent = null;
                }
            } else {
                // Permissions denied, notify the user
                Toast.makeText(requireContext(),
                        "Calendar permissions are required to save events.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ------------------------------------------------------------------
    // Save Event to Calendar Using Calendar Provider API
    // ------------------------------------------------------------------
    private void saveEventToCalendar(Event event) {
        // Ensure that date and time are not null
        if (event.getDate() == null || event.getTime() == null) {
            Toast.makeText(requireContext(), "Event date/time is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize Calendar instances for start and end times
        Calendar startTime = Calendar.getInstance();
        startTime.setTime(event.getDate());
        startTime.set(Calendar.HOUR_OF_DAY, event.getTime().getHour());
        startTime.set(Calendar.MINUTE, event.getTime().getMinute());
        startTime.set(Calendar.SECOND, 0);
        startTime.set(Calendar.MILLISECOND, 0);

        // Assume the event lasts for 1 hour; adjust as needed
        Calendar endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 1);

        // Log the event details for debugging
        Log.d("EventsFragment", "Saving Event to Calendar:");
        Log.d("EventsFragment", "Title: " + event.getActivity());
        Log.d("EventsFragment", "Description: " + event.getAboutEvent());
        Log.d("EventsFragment", "Location: " + event.getLocation());
        Log.d("EventsFragment", "Start Time: " + startTime.getTime().toString());
        Log.d("EventsFragment", "End Time: " + endTime.getTime().toString());

        // Retrieve the primary calendar ID
        long calID = getPrimaryCalendarId();
        if (calID == -1) {
            // No suitable calendar found
            return;
        }

        // Prepare event details
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startTime.getTimeInMillis());
        values.put(CalendarContract.Events.DTEND, endTime.getTimeInMillis());
        values.put(CalendarContract.Events.TITLE, "Sports Event: " + event.getActivity());
        values.put(CalendarContract.Events.DESCRIPTION, event.getAboutEvent());
        values.put(CalendarContract.Events.EVENT_LOCATION, event.getLocation());
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

        // Insert the event
        Uri uri = requireContext().getContentResolver().insert(CalendarContract.Events.CONTENT_URI, values);

        if (uri != null) {
            long eventId = Long.parseLong(uri.getLastPathSegment());
            Toast.makeText(requireContext(), "Event saved to calendar!", Toast.LENGTH_SHORT).show();
            Log.d("EventsFragment", "Event inserted with ID: " + eventId);

            // Optionally, add attendees or reminders here
            // addAttendees(eventId, event.getParticipants());
            // addReminders(eventId);
        } else {
            Toast.makeText(requireContext(), "Failed to save event to calendar.", Toast.LENGTH_SHORT).show();
            Log.e("EventsFragment", "Failed to insert event.");
        }
    }

    // ------------------------------------------------------------------
    // Retrieve Primary Calendar ID
    // ------------------------------------------------------------------
    private long getPrimaryCalendarId() {
        String[] projection = new String[]{
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.IS_PRIMARY
        };

        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = CalendarContract.Calendars.VISIBLE + " = 1";

        Cursor cursor = requireContext().getContentResolver().query(uri, projection, selection, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long calID = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID));
                int isPrimary = cursor.getInt(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.IS_PRIMARY));

                if (isPrimary == 1) {
                    cursor.close();
                    return calID;
                }
            }
            cursor.close();
        }

        // Fallback: Return the first visible calendar ID
        cursor = requireContext().getContentResolver().query(uri, projection, CalendarContract.Calendars.VISIBLE + " = 1", null, null);
        if (cursor != null && cursor.moveToFirst()) {
            long calID = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID));
            cursor.close();
            return calID;
        }

        // No suitable calendar found
        Toast.makeText(requireContext(), "No suitable calendar found.", Toast.LENGTH_SHORT).show();
        Log.e("EventsFragment", "No suitable calendar found.");
        return -1;
    }

    // ------------------------------------------------------------------
    // Optional: Add Attendees to the Event
    // ------------------------------------------------------------------
    private void addAttendees(long eventId, List<String> attendees) {
        for (String attendeeEmail : attendees) {
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Attendees.EVENT_ID, eventId);
            values.put(CalendarContract.Attendees.ATTENDEE_EMAIL, attendeeEmail);
            values.put(CalendarContract.Attendees.ATTENDEE_TYPE, CalendarContract.Attendees.TYPE_REQUIRED);
            values.put(CalendarContract.Attendees.ATTENDEE_STATUS, STATUS_INVITED);

            Uri uri = requireContext().getContentResolver().insert(CalendarContract.Attendees.CONTENT_URI, values);
            if (uri == null) {
                Log.e("EventsFragment", "Failed to add attendee: " + attendeeEmail);
            }
        }
    }

    // ------------------------------------------------------------------
    // Optional: Add Reminders to the Event
    // ------------------------------------------------------------------
    private void addReminders(long eventId) {
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Reminders.EVENT_ID, eventId);
        values.put(CalendarContract.Reminders.MINUTES, 15); // Reminder 15 minutes before
        values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);

        Uri uri = requireContext().getContentResolver().insert(CalendarContract.Reminders.CONTENT_URI, values);
        if (uri == null) {
            Log.e("EventsFragment", "Failed to add reminder.");
        }
    }

    private void showSaveToCalendarDialog(Event event) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Save to Calendar")
                .setMessage("Would you like to save this event to your calendar?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // User chose to save the event
                    checkCalendarPermissions(event);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // User chose not to save the event
                    Toast.makeText(requireContext(), "Event not saved to calendar.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    private void showFilterDialog() {
        // Inflate the filter dialog view from XML
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.filter_dialog_events, null);

        // Get references to the checkboxes from the dialog view
        MaterialCheckBox cbGym = dialogView.findViewById(R.id.cbGym);
        MaterialCheckBox cbBike = dialogView.findViewById(R.id.cbBike);
        MaterialCheckBox cbHiking = dialogView.findViewById(R.id.cbHiking);
        MaterialCheckBox cbRun = dialogView.findViewById(R.id.cbRun);
        MaterialCheckBox cbSwim = dialogView.findViewById(R.id.cbSwim);

        MaterialCheckBox cbPro = dialogView.findViewById(R.id.cbPro);
        MaterialCheckBox cbGood = dialogView.findViewById(R.id.cbGood);
        MaterialCheckBox cbMid = dialogView.findViewById(R.id.cbMid);
        MaterialCheckBox cbBeginner = dialogView.findViewById(R.id.cbBeginner);

        MaterialCheckBox cbLocation = dialogView.findViewById(R.id.cbLocation);

        if (lastSelectedActivities.contains("gym")) {
            cbGym.setChecked(true);
        }
        if (lastSelectedActivities.contains("cycling")) {
            cbBike.setChecked(true);
        }
        if (lastSelectedActivities.contains("hiking")) {
            cbHiking.setChecked(true);
        }
        if (lastSelectedActivities.contains("running")) {
            cbRun.setChecked(true);
        }
        if (lastSelectedActivities.contains("swimming")) {
            cbSwim.setChecked(true);
        }

        if (lastSelectedFitnessLevels.contains("professional")) {
            cbPro.setChecked(true);
        }
        if (lastSelectedFitnessLevels.contains("very good")) {
            cbGood.setChecked(true);
        }
        if (lastSelectedFitnessLevels.contains("mediocre")) {
            cbMid.setChecked(true);
        }
        if (lastSelectedFitnessLevels.contains("beginner")) {
            cbBeginner.setChecked(true);
        }

        // Pre-check the location filter checkbox
        cbLocation.setChecked(lastFilterByLocation);

        new AlertDialog.Builder(requireContext())
                .setTitle("Filter Events")
                .setView(dialogView)
                .setPositiveButton("Apply", (dialog, which) -> {
                    // Build lists of selected filters
                    List<String> selectedActivities = new ArrayList<>();
                    if (cbGym.isChecked()) {
                        // In the adapter we use lower-case strings for comparison
                        selectedActivities.add("gym");
                    }
                    if (cbBike.isChecked()) {
                        selectedActivities.add("cycling");
                    }
                    if (cbHiking.isChecked()) {
                        selectedActivities.add("hiking");
                    }
                    if (cbRun.isChecked()) {
                        selectedActivities.add("running");
                    }
                    if (cbSwim.isChecked()) {
                        selectedActivities.add("swimming");
                    }

                    List<String> selectedFitnessLevels = new ArrayList<>();
                    if (cbPro.isChecked()) {
                        selectedFitnessLevels.add("professional");
                    }
                    if (cbGood.isChecked()) {
                        selectedFitnessLevels.add("very good");
                    }
                    if (cbMid.isChecked()) {
                        selectedFitnessLevels.add("mediocre");
                    }
                    if (cbBeginner.isChecked()) {
                        selectedFitnessLevels.add("beginner");
                    }

                    boolean filterLocation = cbLocation.isChecked();

                    lastSelectedActivities.clear();
                    lastSelectedActivities.addAll(selectedActivities);
                    lastSelectedFitnessLevels.clear();
                    lastSelectedFitnessLevels.addAll(selectedFitnessLevels);
                    lastFilterByLocation = filterLocation;

                    // Apply the filters based on the selected checkboxes
                    applyFilters(selectedActivities, selectedFitnessLevels, filterLocation);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    /**
     * Applies the filters. If no filters are selected the original list is restored.
     * If the location filter is active, the current user's location is fetched and used.
     */
    private void applyFilters(List<String> selectedActivities, List<String> selectedFitnessLevels, boolean filterByLocation) {
        // If no filters are selected, restore the original list
        if (selectedActivities.isEmpty() && selectedFitnessLevels.isEmpty() && !filterByLocation) {
            allEventsList.clear();
            allEventsList.addAll(originalEventsList);
            eventsAdapter.notifyDataSetChanged();
            Toast.makeText(requireContext(), "Filters cleared", Toast.LENGTH_SHORT).show();
            return;
        }
        if (filterByLocation) {
            // Fetch current user's location from Firebase before filtering
            getUserLocation(userLocation -> {
                performFiltering(selectedActivities, selectedFitnessLevels, userLocation);
            });
        } else {
            performFiltering(selectedActivities, selectedFitnessLevels, null);
        }
    }

    /**
     * Iterates over the original events list and adds events to the filtered list if they match
     * the selected activities, fitness levels, and (if applicable) the user’s location.
     */
    private void performFiltering(List<String> selectedActivities, List<String> selectedFitnessLevels, String userLocation) {
        ArrayList<Event> filteredList = new ArrayList<>();
        for (Event event : originalEventsList) {
            // Check activity match: if no activity filter then match; otherwise compare ignoring case.
            boolean matchesActivity = selectedActivities.isEmpty();
            if (!matchesActivity) {
                String eventActivity = event.getActivity();
                if (eventActivity != null) {
                    for (String activity : selectedActivities) {
                        if (eventActivity.equalsIgnoreCase(activity)) {
                            matchesActivity = true;
                            break;
                        }
                    }
                }
            }

            // Check fitness level match
            boolean matchesFitness = selectedFitnessLevels.isEmpty();
            if (!matchesFitness) {
                String eventFitLevel = event.getFitLevel();
                if (eventFitLevel != null) {
                    for (String fitLevel : selectedFitnessLevels) {
                        if (eventFitLevel.equalsIgnoreCase(fitLevel)) {
                            matchesFitness = true;
                            break;
                        }
                    }
                }
            }

            // Check location match: if a location filter is applied then event location must match user location.
            boolean matchesLocation = (userLocation == null); // if no location filter, treat as match.
            if (userLocation != null) {
                String eventLocation = event.getLocation();
                matchesLocation = (eventLocation != null && eventLocation.equalsIgnoreCase(userLocation));
            }

            if (matchesActivity && matchesFitness && matchesLocation) {
                filteredList.add(event);
            }
        }
        allEventsList.clear();
        allEventsList.addAll(filteredList);
        eventsAdapter.notifyDataSetChanged();
    }

    /**
     * Interface for receiving the user’s location once it has been fetched.
     */
    private interface OnLocationFetchedListener {
        void onLocationFetched(String location);
    }

    /**
     * Fetches the current user's location from Firebase (from users/uid/info/location) and
     * passes it to the provided listener.
     */
    private void getUserLocation(OnLocationFetchedListener listener) {
        if (mAuth == null || mAuth.getCurrentUser() == null) {
            listener.onLocationFetched(null);
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference locationRef = usersRef.child(userId).child("info").child("location");
        locationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String location = snapshot.getValue(String.class);
                listener.onLocationFetched(location);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onLocationFetched(null);
            }
        });
    }


}
