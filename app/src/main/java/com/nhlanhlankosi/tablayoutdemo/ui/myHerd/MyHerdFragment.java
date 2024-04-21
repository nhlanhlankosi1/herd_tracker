package com.nhlanhlankosi.tablayoutdemo.ui.myHerd;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.nhlanhlankosi.tablayoutdemo.R;
import com.nhlanhlankosi.tablayoutdemo.infrastructure.CustomRecyclerView;
import com.nhlanhlankosi.tablayoutdemo.listAdapters.SearchAllHymnsAdapter;
import com.nhlanhlankosi.tablayoutdemo.models.Cow;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class MyHerdFragment extends Fragment {
    public static final int ITEM_VIEW_CACHE_SIZE = 20;

    private Toolbar toolbar;
    private CustomRecyclerView myHerdRecyclerView;

    ArrayList<Cow> allHymnsList = new ArrayList<>();
    private SearchAllHymnsAdapter searchAllHymnsAdapter;
    private ImageView emptyViewIcon;
    private TextView emptyViewText;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_my_herd, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        myHerdRecyclerView = view.findViewById(R.id.my_herd_recycler_view);
        myHerdRecyclerView.setItemViewCacheSize(ITEM_VIEW_CACHE_SIZE);
        myHerdRecyclerView.setDrawingCacheEnabled(true);
        myHerdRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        myHerdRecyclerView.addItemDecoration(new DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL));
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

        allHymnsList.addAll(Arrays.asList(AllHymns.allHymns));

        setUpAdapter();

    }

}