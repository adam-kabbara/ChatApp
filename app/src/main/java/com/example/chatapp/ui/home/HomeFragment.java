package com.example.chatapp.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.chatapp.ListViewAdapter;
import com.example.chatapp.MainActivity;
import com.example.chatapp.R;
import com.example.chatapp.databinding.FragmentHomeBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private Context context;
    private String contactsFileName;
    private GoogleSignInAccount signedInAccount;
    private MainActivity mainActivity;
    private ListView listView;
    private ListViewAdapter adapter;
    private SearchView searchView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        context = requireActivity().getApplicationContext();
        mainActivity = (MainActivity)requireContext();
        contactsFileName = getResources().getString(R.string.contacts_file_name);
        signedInAccount = GoogleSignIn.getLastSignedInAccount(context);
        contactsFileName = signedInAccount.getId()+"-"+getResources().getString(R.string.contacts_file_name);

        binding.fab.setOnClickListener(view -> {
          //  Snackbar.make(view, "Creating New Contact", Snackbar.LENGTH_LONG)
          //           .setAction("Action", null).show();
            try { // todo create contact from info from firebase
                createNewContact();
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            mainActivity.displayView(R.id.nav_new_contact);
        });

        listView = binding.homeListView;
        // Pass results to ListViewAdapter Class
        adapter = new ListViewAdapter(context, mainActivity.contacts, false);
        listView.setAdapter(adapter);
        searchView = binding.homeSearchView;
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

        listView.setOnItemClickListener((parent, view, position, id) ->
                Snackbar.make(view, "Clicked: "+mainActivity.contacts.get(position).getName(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show());

        return root;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }



    private void createNewContact() throws JSONException, IOException { // todo fix this so add contact correctly
        File file = new File(context.getFilesDir(), contactsFileName);
        JSONArray data;
        if (file.exists())
            data = new JSONArray(mainActivity.loadJSONFromAsset(context, contactsFileName));
        else
            data = new JSONArray();
        Random rand = new Random(); // for filler data todo fix
        JSONObject contact = new JSONObject();
        contact.put("id", signedInAccount.getId()); // ""+rand.nextInt(500)
        contact.put("email", +rand.nextInt(50000)+signedInAccount.getEmail());
        contact.put("name", signedInAccount.getDisplayName());
        contact.put("pfp_url", "https://picsum.photos/"+(rand.nextInt(100)+200));
        data.put(contact);

        String userString = data.toString();
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(userString);
        bufferedWriter.close();
    }
}