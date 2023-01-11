package com.example.chatapp.ui.new_contact;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.chatapp.ListViewAdapter;
import com.example.chatapp.MainActivity;
import com.example.chatapp.databinding.FragmentNewContactBinding;
import com.example.chatapp.ui.home.Contact;

import java.util.ArrayList;

public class NewContactFragment extends Fragment {

    private FragmentNewContactBinding binding;
    private Context context;
    private ListView listView;
    private ListViewAdapter adapter;
    private SearchView searchView;
    private MainActivity mainActivity;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNewContactBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mainActivity = (MainActivity)requireContext();
        context = requireActivity().getApplicationContext();
        //assert getArguments() != null;

        listView = binding.newContactListView;
        // Pass results to ListViewAdapter Class
        adapter = new ListViewAdapter(context, mainActivity.contacts); //todo get contacts form firbase users
        listView.setAdapter(adapter);
        searchView = binding.newContactSearchView;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String arg0) {
                return false; // true
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String text = newText;
                adapter.filter(text);
                return false;
            }
        });

       return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}