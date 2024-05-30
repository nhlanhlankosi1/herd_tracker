package com.nhlanhlankosi.tablayoutdemo.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nhlanhlankosi.tablayoutdemo.R;
import com.nhlanhlankosi.tablayoutdemo.infrastructure.CustomRecyclerView;
import com.nhlanhlankosi.tablayoutdemo.infrastructure.SharedPreferencesHelper;
import com.nhlanhlankosi.tablayoutdemo.listAdapters.NotificationsAdapter;
import com.nhlanhlankosi.tablayoutdemo.models.Notification;
import com.nhlanhlankosi.tablayoutdemo.models.User;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class NotificationsFragment extends Fragment {
    public static final int ITEM_VIEW_CACHE_SIZE = 20;
    ArrayList<Notification> newNotificationsList = new ArrayList<>();

    ArrayList<Notification> previouslySavedNotificationsList = new ArrayList<>();

    private CustomRecyclerView myNotificationsRecyclerView;

    private DatabaseReference userNotificationsRef;
    private ValueEventListener userNotificationsRefListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        User currentUser = SharedPreferencesHelper.getUser(this.requireContext());
        userNotificationsRef = FirebaseDatabase.getInstance().getReference("notifications")
                .child(currentUser.getUserId());
        boolean areNotificationsAlreadySaved = (SharedPreferencesHelper.getNotifications(requireContext()) != null);
        if (areNotificationsAlreadySaved) {
            previouslySavedNotificationsList = SharedPreferencesHelper.getNotifications(requireContext());
        }

    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_notifications, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        myNotificationsRecyclerView = view.findViewById(R.id.notifications_recycler_view);
        myNotificationsRecyclerView.setItemViewCacheSize(ITEM_VIEW_CACHE_SIZE);
        myNotificationsRecyclerView.setDrawingCacheEnabled(true);
        myNotificationsRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        myNotificationsRecyclerView.setLayoutManager(layoutManager);
        myNotificationsRecyclerView.setHasFixedSize(true);

        View emptyView = view.findViewById(R.id.empty_view);
        ImageView emptyViewIcon = view.findViewById(R.id.empty_view_icon);
        TextView emptyViewText = view.findViewById(R.id.empty_view_text);

        emptyViewIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.no_notifications, null));

        emptyViewIcon.setVisibility(View.GONE);
        emptyViewText.setVisibility(View.GONE);

        myNotificationsRecyclerView.setEmptyView(emptyView);

        userNotificationsRefListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    newNotificationsList.clear();

                    for (DataSnapshot notificationSnapShot : snapshot.getChildren()) {
                        Notification notification = notificationSnapShot.getValue(Notification.class);

                        if (notification == null) {
                            return;
                        }

                        newNotificationsList.add(notification);

                        // Check if the notification is not already in the list
                        if (!previouslySavedNotificationsList.contains(notification)) {
                            previouslySavedNotificationsList.add(notification);

                            // Show a notification
                        }
                    }

                    SharedPreferencesHelper.saveNotifications(requireContext(), previouslySavedNotificationsList);
                    setUpAdapter();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        userNotificationsRef.addValueEventListener(userNotificationsRefListener);

    }

    private void setUpAdapter() {

        NotificationsAdapter notificationsAdapter = new NotificationsAdapter(this.requireContext(), newNotificationsList);

        myNotificationsRecyclerView.setAdapter(notificationsAdapter);

    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (userNotificationsRef != null && userNotificationsRefListener != null) {
            userNotificationsRef.removeEventListener(userNotificationsRefListener);
        }

    }

}