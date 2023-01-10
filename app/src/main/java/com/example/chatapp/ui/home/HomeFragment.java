package com.example.chatapp.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.example.chatapp.MainActivity;
import com.example.chatapp.R;
import com.example.chatapp.Utils;
import com.example.chatapp.databinding.FragmentHomeBinding;
import com.example.chatapp.ui.new_contact.NewContactFragment;
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
import java.util.HashMap;
import java.util.Random;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private Context context;
    private String contactsFileName;
    private GoogleSignInAccount signedInAccount;
    private HashMap<View, String> contactsViewIdHashMap = new HashMap<View, String>();
    private View.OnClickListener clickListenerContactView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);


        context = requireActivity().getApplicationContext();
        contactsFileName = getResources().getString(R.string.contacts_file_name);
        signedInAccount = GoogleSignIn.getLastSignedInAccount(context);
        contactsFileName = signedInAccount.getId()+"-"+getResources().getString(R.string.contacts_file_name);

        binding.fab.setOnClickListener(view -> {
            try { // todo create contact from info from firebase
                createNewContact();
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            Utils.redirect(getActivity().getSupportFragmentManager(), ((AppCompatActivity)getActivity()).getSupportActionBar(),
                    navigationView, R.id.nav_new_contact, getString(R.string.app_name));
        });

        clickListenerContactView = v -> {// todo redirect to contact chat
            System.out.println("Click on view");
            System.out.println(contactsViewIdHashMap.get(v));
        };

        return root;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // create contact boxes
        try {
            Contact[] contacts = readContacts();
            if (contacts != null) {
                for (Contact contact : contacts){
                    View contactView = addContactBox(contact.getName(), contact.getPfpUrl());
                    contactsViewIdHashMap.put(contactView, contact.getId());
                    contactView.setOnClickListener(clickListenerContactView);

                }
            }
        } catch (FileNotFoundException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Custom Methods
    public View addContactBox(String name, String pfpUrl){
        View contactView = getLayoutInflater().inflate(R.layout.contact_box, null);
        ImageView pfpView = contactView.findViewById(R.id.pfp);
        TextView nameView = contactView.findViewById(R.id.contactNameTextView);
        // this is a cache image viewer thing todo make sure that the image updates if it changes in the server
        Glide.with(this)// https://stackoverflow.com/questions/33443146/remove-image-from-cache-in-glide-library
                .load(pfpUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_menu_gallery)
                .into(pfpView);

        nameView.setText(name);
        binding.contactScrollable.addView(contactView);
        return contactView;
    }

    public Contact[] readContacts() throws FileNotFoundException, JSONException {
        File file = new File(context.getFilesDir(), contactsFileName);
        if (file.exists()){
            JSONArray contactsJSON = new JSONArray(Utils.loadJSONFromAsset(context, contactsFileName));
            Contact[] contactsArray = new Contact[contactsJSON.length()];
            for (int i=0; i<contactsJSON.length(); i++){
                JSONObject c = contactsJSON.getJSONObject(i);
                contactsArray[i] = new Contact(c.getString("id"), c.getString("email"), c.getString("name"), c.getString("pfp_url"));
            }
            return contactsArray;
        }
        return null;
    }

    private void createNewContact() throws JSONException, IOException { // todo fix this so add contact correctly
        File file = new File(context.getFilesDir(), contactsFileName);
        JSONArray data;
        if (file.exists())
            data = new JSONArray(Utils.loadJSONFromAsset(context, contactsFileName));
        else
            data = new JSONArray();
        Random rand = new Random(); // for filler data todo fix
        JSONObject contact = new JSONObject();
        contact.put("id", signedInAccount.getId()); // ""+rand.nextInt(500)
        contact.put("email", signedInAccount.getEmail());
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