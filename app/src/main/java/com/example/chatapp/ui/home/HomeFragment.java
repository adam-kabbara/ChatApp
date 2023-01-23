package com.example.chatapp.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.chatapp.Contact;
import com.example.chatapp.ListViewAdapter;
import com.example.chatapp.MainActivity;
import com.example.chatapp.R;
import com.example.chatapp.databinding.FragmentHomeBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ListenerRegistration;

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
import java.util.Map;
import java.util.Objects;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private Context context;
    private String contactsFileName;
    private String unreadContactsFileName;
    private GoogleSignInAccount signedInAccount;
    private MainActivity mainActivity;
    private ListView listView;
    private ListViewAdapter adapter;
    private SearchView searchView;
    private DialogInterface.OnClickListener dialogClickListener;
    private ListenerRegistration registration;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        context = requireActivity().getApplicationContext();
        mainActivity = (MainActivity)requireContext();
        signedInAccount = GoogleSignIn.getLastSignedInAccount(context);
        contactsFileName = signedInAccount.getId()+"-"+getResources().getString(R.string.contacts_file_name);
        unreadContactsFileName = signedInAccount.getId()+"-"+getResources().getString(R.string.unread_contacts_file_name);
        binding.fab.setOnClickListener(view -> {
            mainActivity.displayView(R.id.nav_new_contact);
        });
        initPage();
        // get data from new contact click in NewContactFragment
        getParentFragmentManager().setFragmentResultListener
                ("requestKey", this, (requestKey, bundle) -> {
                    Contact newContact = (Contact) bundle.getSerializable("newContactKey");
                    try{
                        saveNewContactLocally(newContact);
                        initPage();
                    }
                    catch (IOException | JSONException e){
                        e.printStackTrace();
                    }
                });

        ArrayList<String> receivedKeys = new ArrayList<>(); // to handel callback function when data is removed from db
        registration = mainActivity.db.collection("messages").document(Objects.requireNonNull(signedInAccount.getId()))
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        System.out.println("Listen failed." + e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Map<String, Object> data = Objects.requireNonNull(snapshot.getData());
                        for (String key : data.keySet()) {
                            if(!receivedKeys.contains(key)){
                                receivedKeys.add(key);
                                HashMap<String, Object> message = (HashMap<String, Object>) snapshot.get(key);
                                assert message != null;
                                try {
                                    String messageFileName = signedInAccount.getId()+"-"+message.get("sender")+"-"+mainActivity.getResources().getString(R.string.messages_file_name);
                                    mainActivity.saveMessageLocally((String) message.get("message"), false, (long) message.get("time"), messageFileName);
                                } catch (IOException | JSONException ex) {
                                    ex.printStackTrace();
                                }
                                // inform that there's a new msg
                                //adapter.refreshListview();
                                showNewMessageSticker((String) message.get("sender"));
                                try {
                                    addUnreadContact((String) message.get("sender"));
                                } catch (IOException | JSONException ex) {
                                    ex.printStackTrace();
                                }
                                // delete messages from db
                                DocumentReference docRef = mainActivity.db.collection("messages").document(signedInAccount.getId());
                                Map<String, Object> updates = new HashMap<>();
                                updates.put(key, FieldValue.delete());
                                docRef.update(updates);
                                // reorganize contacts so most recent sender is at top todo make sure applies wth time
                                for (int i=0; i<mainActivity.contacts.size();i++){
                                    Contact c = mainActivity.contacts.get(i);
                                    if (c.getId().equals(message.get("sender"))){
                                        mainActivity.contacts.remove(i);
                                        mainActivity.contacts.add(0, c);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                });

        return root;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
        registration.remove();
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
    private void deleteContactLocally(Contact contact) throws IOException, JSONException {
        // delete contact file cs: delete
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

        // delete messages
        String messageFileName = signedInAccount.getId()+"-"+contact.getId()+"-"+getResources().getString(R.string.messages_file_name);
        file = new File(context.getFilesDir(), messageFileName);
        if (file.exists())
            file.delete();
    }

    private void initPage(){
        Bundle result = new Bundle();
        try {
            mainActivity.contacts = mainActivity.readContacts();
        } catch (FileNotFoundException | JSONException e) {
            e.printStackTrace();
        }
        // show unread stickers
        ArrayList<String> contactsAddUnreadSticker = new ArrayList<>();
        try{
            File file = new File(context.getFilesDir(), unreadContactsFileName);
            JSONArray data;
            if (file.exists()){
                data = new JSONArray(mainActivity.loadJSONFromAsset(context, unreadContactsFileName));
                for (int i=0; i<data.length(); i++){
                    contactsAddUnreadSticker.add((String) data.get(i));
                    //showNewMessageSticker((String) data.get(i));
                }
            }
        }
        catch (java.io.IOException | org.json.JSONException e){
            e.printStackTrace();
        }

        if (mainActivity.contacts != null){
            listView = binding.homeListView;
            // Pass results to ListViewAdapter Class
            adapter = new ListViewAdapter(context, mainActivity.contacts, false, contactsAddUnreadSticker);
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
                ImageView newMessageIV = view.findViewById(R.id.imageView2);
                newMessageIV.setVisibility(View.INVISIBLE);
                try {
                    removeUnreadContact(((Contact)view.getTag(R.id.contactTag)).getId());
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                mainActivity.displayView(R.id.nav_chat);
            });

            listView.setOnItemLongClickListener((arg0, arg1, pos, id) -> {
                deleteContactAlert(pos);
                return true;
            });
        }

    }

    private void deleteContactAlert(int position){
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

    private void showNewMessageSticker(String senderId){
        for (int i=0; i<listView.getChildCount(); i++){
            View contactView = listView.getChildAt(i);
            if (((Contact)contactView.getTag(R.id.contactTag)).getId().equals(senderId)){
                ImageView newMessageIV = contactView.findViewById(R.id.imageView2);
                newMessageIV.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    private void addUnreadContact(String contactID) throws IOException, JSONException {
        File file = new File(context.getFilesDir(), unreadContactsFileName);
        JSONArray data;
        if (file.exists())
            data = new JSONArray(mainActivity.loadJSONFromAsset(context, unreadContactsFileName));
        else
            data = new JSONArray();

        for (int i=0; i<data.length(); i++){
            if(data.getString(i).equals(contactID))
                return;
        }
        data.put(contactID);
        String userString = data.toString();
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(userString);
        bufferedWriter.close();
    }

    private void removeUnreadContact(String contactID) throws IOException, JSONException {
        File file = new File(context.getFilesDir(), unreadContactsFileName);
        JSONArray data;
        JSONArray newData = new JSONArray();
        if (file.exists())
            data = new JSONArray(mainActivity.loadJSONFromAsset(context, unreadContactsFileName));
        else
            data = new JSONArray();

        for (int i=0; i<data.length(); i++){
            if(!data.getString(i).equals(contactID))
                newData.put(data.getString(i));
        }
        String userString = newData.toString();
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(userString);
        bufferedWriter.close();
    }
}