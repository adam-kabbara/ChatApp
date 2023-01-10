package com.example.chatapp;

import androidx.appcompat.app.ActionBar;
import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.chatapp.ui.home.HomeFragment;
import com.example.chatapp.ui.new_contact.NewContactFragment;
import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Utils {
    public static String loadJSONFromAsset(Context context, String fileName) throws FileNotFoundException {
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
    // redirect to pages not in drawer
    public static void redirect(FragmentManager fragmentManager, Fragment toFragmentClass) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_content_main, toFragmentClass);
        fragmentTransaction.commit();
    }
    // redirect to pages in drawer
    public static void redirect(FragmentManager fragmentManager, ActionBar actionBar,
                                NavigationView navigationView, int viewId, String title){

        Fragment fragment = null;
        switch (viewId) {
            case R.id.nav_home:
                fragment = new HomeFragment();
                break;
            case R.id.nav_new_contact:
                fragment = new NewContactFragment();
                title = "New Contact";
                break;
        }
        if (fragment != null) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.fragment_content_main, fragment, String.valueOf(viewId));
            ft.commit();
        }
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
        navigationView.setCheckedItem(viewId);
    }
}
