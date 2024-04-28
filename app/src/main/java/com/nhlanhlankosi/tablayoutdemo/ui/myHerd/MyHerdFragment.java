package com.nhlanhlankosi.tablayoutdemo.ui.myHerd;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
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
import com.nhlanhlankosi.tablayoutdemo.listAdapters.SearchCattleAdapter;
import com.nhlanhlankosi.tablayoutdemo.listAdapters.SearchCattleListener;
import com.nhlanhlankosi.tablayoutdemo.models.Cow;
import com.nhlanhlankosi.tablayoutdemo.models.User;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MyHerdFragment extends Fragment implements SearchCattleListener {
    public static final int ITEM_VIEW_CACHE_SIZE = 20;
    ArrayList<Cow> allCattleList = new ArrayList<>();
    private Toolbar toolbar;
    private CustomRecyclerView myHerdRecyclerView;
    private SearchCattleAdapter searchCattleAdapter;
    private ImageView emptyViewIcon;
    private TextView emptyViewText;

    private DatabaseReference userHerdRef;
    private ValueEventListener userHerdRefListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        User currentUser = SharedPreferencesHelper.getUser(this.requireContext());
        userHerdRef = FirebaseDatabase.getInstance().getReference("herds")
                .child(currentUser.getUserId());
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.fragment_my_herd, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        myHerdRecyclerView = view.findViewById(R.id.my_herd_recycler_view);
        myHerdRecyclerView.setItemViewCacheSize(ITEM_VIEW_CACHE_SIZE);
        myHerdRecyclerView.setDrawingCacheEnabled(true);
        myHerdRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        myHerdRecyclerView.setLayoutManager(layoutManager);
        myHerdRecyclerView.setHasFixedSize(true);

        View emptyView = view.findViewById(R.id.empty_view);
        emptyViewIcon = view.findViewById(R.id.empty_view_icon);
        emptyViewText = view.findViewById(R.id.empty_view_text);

        emptyViewIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.no_cow_added, null));

        emptyViewIcon.setVisibility(View.GONE);
        emptyViewText.setVisibility(View.GONE);

        myHerdRecyclerView.setEmptyView(emptyView);

        userHerdRefListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    allCattleList.clear();

                    for (DataSnapshot cattleSnapShot : snapshot.getChildren()) {
                        Cow cow = cattleSnapShot.getValue(Cow.class);
                        allCattleList.add(cow);
                    }

                    setUpAdapter();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        userHerdRef.addValueEventListener(userHerdRefListener);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_my_herd_search_all_cattle_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem menuItem = menu.findItem(R.id.my_herd_fragment_search_view);
        menuItem.expandActionView();
        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                closeKeyboard();
                return true;
            }
        });

        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setQueryHint(getString(R.string.search_your_herd));

        // Change the text color of query text and hint
        int textColor = Color.parseColor("#888888"); // Light grey color
        int hintColor = Color.parseColor("#888888"); // Light grey color

        // Find the EditText inside the SearchView
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        // Change the text color
        searchEditText.setTextColor(textColor);
        // Change the hint color
        searchEditText.setHintTextColor(hintColor);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (TextUtils.isEmpty(newText)) {
                    searchCattleAdapter.filter(".", MyHerdFragment.this);
                } else {
                    searchCattleAdapter.filter(newText, MyHerdFragment.this);
                }
                return true;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void closeKeyboard() {
        View view = this.requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) this.requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void setUpAdapter() {

        searchCattleAdapter = new SearchCattleAdapter(this.requireContext(), allCattleList);

        myHerdRecyclerView.setAdapter(searchCattleAdapter);

    }

    @Override
    public void onEmptyResultReturnedFor(String searchText) {

        final String noHymnsForSearch = getString(R.string.no_cow_from_search_fmt, searchText);
        emptyViewText.setText(noHymnsForSearch);
        emptyViewIcon.setVisibility(View.VISIBLE);
        emptyViewText.setVisibility(View.VISIBLE);

    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (userHerdRef != null && userHerdRefListener != null) {
            userHerdRef.removeEventListener(userHerdRefListener);
        }

    }

}