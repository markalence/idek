package com.example.mark.ms;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ContactInfoFragment extends Fragment {

    RecyclerView recyclerView;
    ContactInfoAdapter cia;

    public ContactInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View RootView = inflater.inflate(R.layout.fragment_contact_info, container, false);
        recyclerView = RootView.findViewById(R.id.contactRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cia = new ContactInfoAdapter();
        recyclerView.setAdapter(cia);
        return RootView;
    }

}
