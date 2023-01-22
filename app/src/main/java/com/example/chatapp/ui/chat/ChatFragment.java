package com.example.chatapp.ui.chat;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.chatapp.Contact;
import com.example.chatapp.MainActivity;
import com.example.chatapp.R;
import com.example.chatapp.databinding.FragmentChatBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;
    private GoogleSignInAccount signedInAccount;
    private MainActivity mainActivity;
    private Context context;
    private Contact receiverContact;
    private String messageFileName;
    private ScrollView scrollView;
    private ListenerRegistration registration;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mainActivity = (MainActivity)requireContext();
        context = requireActivity().getApplicationContext();
        signedInAccount = GoogleSignIn.getLastSignedInAccount(context);

        ImageView sendButton = binding.sendButton;
        EditText editText = binding.plainTextInput;
        scrollView = binding.scrollView;
        initPage();

        sendButton.setOnClickListener(view -> {
            String message = String.valueOf(editText.getText());
            long time = new Date().getTime();
            if (!message.equals("")){
                addMessageBox(message, true, time);
                scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                sendMessageFire(message, time);
                try {
                    saveMessageLocally(message, true, time);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                editText.setText("");
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
                                if (((String) message.get("sender")).equals(receiverContact.getId())) {
                                    addMessageBox((String) message.get("message"), false, (long) message.get("time"));
                                    try {
                                        saveMessageLocally((String) message.get("message"), false, (Long) message.get("time"));
                                    } catch (IOException | JSONException ex) {
                                        ex.printStackTrace();
                                    }
                                    // delete messages from db
                                    DocumentReference docRef = mainActivity.db.collection("messages").document(signedInAccount.getId());
                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put(key, FieldValue.delete());
                                    docRef.update(updates);
                                }
                            }
                        }
                    }
                });

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registration.remove();
    }

    private void initPage(){
        getParentFragmentManager().setFragmentResultListener
                ("requestKey2", this, (requestKey, bundle) -> {
                    receiverContact = (Contact) bundle.getSerializable("pressedContactKey");
                    mainActivity.getSupportActionBar().setTitle(receiverContact.getName());
                    messageFileName = signedInAccount.getId()+"-"+receiverContact.getId()+"-"+getResources().getString(R.string.messages_file_name);
                    try {
                        JSONArray data = new JSONArray(mainActivity.loadJSONFromAsset(context, messageFileName));
                        for (int i=0; i<data.length(); i++){
                            JSONObject msgObj = data.getJSONObject(i);
                            addMessageBox(msgObj.getString("message"), msgObj.getBoolean("is_sender"), -1);
                        }
                    } catch (JSONException | FileNotFoundException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void addMessageBox(String message, boolean isSender, long time){
        View messageView = getLayoutInflater().inflate(R.layout.text_message, null);
        LinearLayout messageLayout = messageView.findViewById(R.id.horizontalMsgLayout);
        TextView textView = messageView.findViewById(R.id.textViewMessage);
        textView.setText(message);
        messageView.setTag(time); // tag of textview will hold its time data
        if (isSender){
            textView.setBackgroundColor(getResources().getColor(R.color.teal_200));
            messageLayout.setGravity(Gravity.END); //todo fix so that sender messages are at end
        }
        else{
            textView.setBackgroundColor(getResources().getColor(R.color.peach_200));
        }
        if (time == -1) // so we are reading from a file and messages are already sorted
            binding.linearLayout.addView(messageView);
        else{
            boolean saved = false;
            if (binding.linearLayout.getChildCount() > 0) {
                for (int i=binding.linearLayout.getChildCount()-1; i >= 0; i--) {
                    View tv = binding.linearLayout.getChildAt(i);
                    System.out.println(time);
                    System.out.println(tv.getTag());
                    System.out.println(time > (long) tv.getTag());
                    if (time > (long) tv.getTag()) {
                        binding.linearLayout.addView(messageView, i+1);
                        saved = true;
                        break;
                    }
                }
                if (!saved) {
                    binding.linearLayout.addView(messageView, 0);
                }
            }
            else{
                binding.linearLayout.addView(messageView);
            }
        }

        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void sendMessageFire(String message, long time){
        HashMap<String, Object> metaData = new HashMap<>();
        HashMap<String, Object> data = new HashMap<>();
        data.put("sender", signedInAccount.getId());
        data.put("message", message);
        data.put("time", time);
        metaData.put(UUID.randomUUID().toString(), data); // uuid not 100% unique
        mainActivity.db.collection("messages").document(receiverContact.getId())
                .update(metaData).addOnFailureListener(e -> mainActivity.db.collection("messages")
                         .document(receiverContact.getId()).set(metaData));
    }

    private void saveMessageLocally(String message, boolean isSender, long time) throws IOException, JSONException {
        File file = new File(context.getFilesDir(), messageFileName);
        JSONArray data;
        JSONObject messageJson = new JSONObject();
        messageJson.put("time", time);
        messageJson.put("message", message);
        messageJson.put("is_sender", isSender);

        if (file.exists()){
            data = new JSONArray(mainActivity.loadJSONFromAsset(context, messageFileName));
            boolean saved = false;
            for (int i=data.length()-1; i>=0; i--){
                JSONObject msgObj = data.getJSONObject(i);
                System.out.println(time);
                System.out.println(msgObj.getLong("time"));
                System.out.println(time > msgObj.getLong("time"));
                if (time > msgObj.getLong("time")) {
                    for (int j = data.length(); j > i+1; j--){
                        data.put(j, data.get(j-1));
                    }
                    data.put(i+1, messageJson);
                    saved = true;
                    break;
                }
            }
            if (!saved){
                for (int j = data.length(); j > 0; j--){
                    data.put(j, data.get(j-1));
                }
                data.put(0, messageJson);
            }
        }
        else {
            data = new JSONArray();
            data.put(messageJson);
        }

        String userString = data.toString();
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(userString);
        bufferedWriter.close();
    }

}