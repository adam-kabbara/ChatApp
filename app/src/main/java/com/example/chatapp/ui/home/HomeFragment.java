package com.example.chatapp.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.chatapp.Contact;
import com.example.chatapp.ListViewAdapter;
import com.example.chatapp.MainActivity;
import com.example.chatapp.R;
import com.example.chatapp.databinding.FragmentHomeBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private Context context;
    private String contactsFileName;
    private GoogleSignInAccount signedInAccount;
    private MainActivity mainActivity;
    private ListView listView;
    private ListViewAdapter adapter;
    private SearchView searchView;
    private DialogInterface.OnClickListener dialogClickListener;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        context = requireActivity().getApplicationContext();
        mainActivity = (MainActivity)requireContext();
        contactsFileName = getResources().getString(R.string.contacts_file_name);
        signedInAccount = GoogleSignIn.getLastSignedInAccount(context);
        contactsFileName = signedInAccount.getId()+"-"+getResources().getString(R.string.contacts_file_name);
        binding.fab.setOnClickListener(view -> {
            mainActivity.displayView(R.id.nav_new_contact);
        });

        initPage();
        // get data from new contact click in NewContactFragment
        getParentFragmentManager().setFragmentResultListener
                ("requestKey", this, (requestKey, bundle) -> {
                    Contact newContact = (Contact) bundle.getSerializable("newContactKey");
                    try {
                        saveNewContactLocally(newContact);
                        initPage();
                        }
                    catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                });

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

    private void saveNewContactLocally(Contact contact) throws JSONException, IOException {
        File file = new File(context.getFilesDir(), contactsFileName);
        JSONArray data;
        if (file.exists())
            data = new JSONArray(mainActivity.loadJSONFromAsset(context, contactsFileName));
        else
            data = new JSONArray();
        JSONObject contactJson = new JSONObject();
        contactJson.put("id", contact.getId());
        contactJson.put("email", contact.getEmail());
        contactJson.put("name", contact.getName());
        contactJson.put("pfp_url", contact.getPfpUrl());
        data.put(contactJson);

        String userString = data.toString();
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(userString);
        bufferedWriter.close();
    }
    // maybe create a method to delete the paths where msgs travel from on contact
    // to another is db
    private void deleteContactLocally(Contact contact) throws IOException, JSONException { // todo also delete all msgs
        File file = new File(context.getFilesDir(), contactsFileName);
        JSONArray data = new JSONArray(mainActivity.loadJSONFromAsset(context, contactsFileName));
        JSONArray modifiedData = new JSONArray();

        for (int i=0;i< data.length();i++) {
            if (!contact.getId().equals(data.getJSONObject(i).getString("id")))
                modifiedData.put(data.get(i));
        }

        String userString = modifiedData.toString();
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(userString);
        bufferedWriter.close();
    }

    private void initPage(){
        Bundle result = new Bundle();
        try {
            mainActivity.contacts = mainActivity.readContacts();
        } catch (FileNotFoundException | JSONException e) {
            e.printStackTrace();
        }

        if (mainActivity.contacts != null){
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
            listView.setLongClickable(true);
            listView.setOnItemClickListener((parent, view, position, id) ->
            {
                result.putSerializable("pressedContactKey", mainActivity.contacts.get(position));
                getParentFragmentManager().setFragmentResult("requestKey2", result);
                mainActivity.displayView(R.id.nav_chat);
                System.out.println(position);
            });

            listView.setOnItemLongClickListener((arg0, arg1, pos, id) -> {
                dialogBoxSetup(pos);
                return true;
            });
        }

    }

    private void dialogBoxSetup(int position){
        Contact contact = mainActivity.contacts.get(position);
        dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    try {
                        deleteContactLocally(contact);
                        mainActivity.displayView(R.id.nav_home);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.dismiss();
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you sure you want to delete " + contact.getName()+ " from your local contacts?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
}