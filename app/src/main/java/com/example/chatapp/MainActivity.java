package com.example.chatapp;

import static java.util.Objects.requireNonNull;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chatapp.ui.chat.ChatFragment;
import com.example.chatapp.ui.new_contact.NewContactFragment;
import com.example.chatapp.ui.home.HomeFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private Context context;
    private String contactsFileName;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount signedInAccount;
    private NavigationView navigationView;
    private View headerView;
    private ImageView headerViewImage;
    private boolean viewIsAtHome = true;
    public ArrayList<Contact> contacts;
    public FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = getApplicationContext();
        mAuth = FirebaseAuth.getInstance();
        createRequest(); // init mGoogleSignInClient
        signedInAccount = GoogleSignIn.getLastSignedInAccount(this);
        contactsFileName = signedInAccount.getId()+"-"+getResources().getString(R.string.contacts_file_name);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        db = FirebaseFirestore.getInstance();
        contacts = new ArrayList<>();


        setSupportActionBar(binding.appBarMain.toolbar);

        navigationView.setNavigationItemSelectedListener(this);
        displayView(R.id.nav_home);
        // Set nav bar header info
        NavigationView navigationView = binding.navView;
        headerView = navigationView.getHeaderView(0);
        TextView currentUserNameTextView = headerView.findViewById(R.id.currentUserName);
        TextView currentUserEmailTextView = headerView.findViewById(R.id.currentUserEmail);
        currentUserNameTextView.setText(signedInAccount.getDisplayName());
        currentUserEmailTextView.setText(signedInAccount.getEmail());
        //todo put pfp - when first login if no firebase accoutn already use google pfp as chatapp pfp
        headerViewImage = headerView.findViewById(R.id.imageView);
        setPfpFromFire(signedInAccount.getId(), headerViewImage);

        //db.collection("messages").document(Objects.requireNonNull(signedInAccount.getId()))
        //        .addSnapshotListener((snapshot, e) -> {
        //            if (e != null) {
        //                System.out.println("Listen failed."+ e);
        //                return;
        //            }
        //
        //            if (snapshot != null && snapshot.exists()) {
        //                Map<String, Object> data = Objects.requireNonNull(snapshot.getData());
        //                for (String key: data.keySet()){
        //                    System.out.println("jjj: "+key);
        //                    HashMap<String, Object> message = (HashMap<String, Object>) snapshot.get(key);
        //                    assert message != null;
        //                    try {                        //TODO FILE NAME
        //                        saveMessageLocallyOutsideListener("FILENAME", (String) message.get("message"),false, FieldValue.serverTimestamp());
        //                    }
        //                    catch (IOException | JSONException ex) {
        //                        ex.printStackTrace();
        //                    }
        //                    // delete messages from db
        //                    DocumentReference docRef = db.collection("messages").document(signedInAccount.getId());
        //                    Map<String,Object> updates = new HashMap<>();
        //                    updates.put(key, FieldValue.delete());
        //                    docRef.update(updates);
        //                }
        //            }
        //        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    // firebase related - mostly for sign out
    private void signOut(){
        mAuth.signOut();
        mGoogleSignInClient.signOut(); // this line and above remove the account so that it doesnt auto log in later
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(),GoogleLoginActivity.class);
        startActivity(intent);
    }

    private void createRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    // for drawer routes and btns
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_sign_out)
            signOut();
        else // handel navigation
            displayView(item.getItemId());
        return true;
    }
    public void displayView(int viewId) {
        boolean removeDrawerSelection = false;
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer,
            binding.appBarMain.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    setPfpFromFire(signedInAccount.getId(), headerViewImage);
                }
        };
        drawer.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState(); // animate hamburger btn

        // navigate roots
        switch (viewId) {
            case R.id.nav_home:
                fragment = new HomeFragment();
                viewIsAtHome = true;
                break;
            case R.id.nav_new_contact:
                fragment = new NewContactFragment();
                viewIsAtHome = false;
                title = "New Contact";
                break;
            case R.id.nav_chat:
                fragment = new ChatFragment();
                viewIsAtHome = false;
                removeDrawerSelection = true;
        }
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_content_main, fragment);
            ft.commit();
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
        navigationView.setCheckedItem(viewId);
        if (removeDrawerSelection)
            navigationView.getMenu().getItem(1).setChecked(false); // home is at index 1
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        if (!viewIsAtHome) { //if the current view is not the home fragment
            displayView(R.id.nav_home); //display the home fragment
        } else {
            moveTaskToBack(true);  //If view is in home fragment, exit application
        }
    }

    // shared methods
    public String loadJSONFromAsset(Context context, String fileName) throws FileNotFoundException {
        FileInputStream fis = context.openFileInput(fileName);
        InputStreamReader inputStreamReader =
                new InputStreamReader(fis, StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
        } catch (IOException e) {
            System.out.println(e); // Error occurred when opening raw file for reading.
        }
        return stringBuilder.toString();
    }

    public ArrayList<Contact> readContacts() throws FileNotFoundException, JSONException {
        File file = new File(context.getFilesDir(), contactsFileName);
        if (file.exists()){
            JSONArray contactsJSON = new JSONArray(loadJSONFromAsset(context, contactsFileName));
            ArrayList<Contact> contactsArray = new ArrayList<Contact>();
            for (int i=0; i<contactsJSON.length(); i++){
                JSONObject c = contactsJSON.getJSONObject(i);
                contactsArray.add(new Contact(c.getString("id"), c.getString("email"), c.getString("name"), c.getString("pfp_url")));
            }
            return contactsArray;
        }
        return new ArrayList<Contact>();
    }

    // given an imageView and a fire document id , retrieve an image from firestore and place it in imageveiw
    public void setPfpFromFire(String documentId, ImageView imageView){
        DocumentReference docRef = db.collection("users").document(documentId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                String imageUrl = "";
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        imageUrl = (String) document.get("pfp_url");
                    } else {
                        if (documentId.equals(signedInAccount.getId())){ // new usr
                            imageUrl = String.valueOf(signedInAccount.getPhotoUrl());
                            setNewUserFire();
                        }
                        else{ // todo show error message
                            System.out.println("User not found");
                        }
                    }
                    Glide.with(headerView)
                            .load(imageUrl)
                            .centerCrop()
                            .placeholder(R.drawable.ic_menu_gallery)
                            .into(imageView);
                } else {
                    System.out.println("get failed with "+ task.getException());
                }
            }
        });
    }

    public void setNewUserFire(){
        HashMap<String, String> data = new HashMap<>();
        data.put("name", signedInAccount.getDisplayName());
        data.put("email", signedInAccount.getEmail());
        data.put("pfp_url", String.valueOf(signedInAccount.getPhotoUrl()));
        db.collection("users").document(requireNonNull(signedInAccount.getId())).set(data);
    }

    public void saveMessageLocallyOutsideListener(String filename, String message, boolean isSender, FieldValue time) throws IOException, JSONException {
        // todo make sure to add in sequential time thing
        File file = new File(context.getFilesDir(), filename);
        JSONArray data;
        if (file.exists())
            data = new JSONArray(loadJSONFromAsset(context, filename));
        else
            data = new JSONArray();
        JSONObject messageJson = new JSONObject();
        messageJson.put("time", time);
        messageJson.put("message", message);
        messageJson.put("is_sender", isSender);
        data.put(messageJson);

        String userString = data.toString();
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(userString);
        bufferedWriter.close();
    }
}