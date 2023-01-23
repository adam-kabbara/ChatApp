package com.example.chatapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListViewAdapter extends BaseAdapter {

    // Declare Variables

    Context mContext;
    LayoutInflater inflater;
    private List<Contact> contactsList = null;
    private ArrayList<String> contactsToAddSticker;
    private boolean searchEmail;
    private ArrayList<Contact> arraylist;

    public ListViewAdapter(Context context, List<Contact> contactsList, boolean searchEmail, ArrayList<String> contactsToAddSticker) {
        mContext = context; // if searchEmail is true, ListView Adaptor filters by contact email, else it filters by name
        this.contactsList = contactsList;
        inflater = LayoutInflater.from(mContext);
        this.arraylist = new ArrayList<>();
        this.arraylist.addAll(contactsList);
        this.searchEmail = searchEmail;
        this.contactsToAddSticker = contactsToAddSticker;
    }
    public ListViewAdapter(Context context, List<Contact> contactsList, boolean searchEmail){
        this(context, contactsList, searchEmail, null);
    }

    public class ViewHolder {
        TextView name;
    }

    @Override
    public int getCount() {
        return contactsList.size();
    }

    @Override
    public Contact getItem(int position) {
        return contactsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.contact_box, null);
            holder.name = view.findViewById(R.id.contactNameTextView);
            view.setTag(R.id.holderTag, holder);
        } else {
            holder = (ViewHolder) view.getTag(R.id.holderTag);
        }
        // Set the results into TextViews
        if (this.searchEmail)
            holder.name.setText(contactsList.get(position).getEmail());
        else
            holder.name.setText(contactsList.get(position).getName());

        ImageView pfpView = view.findViewById(R.id.pfp);
        // this is a cache image viewer thing todo make sure that the image updates if it changes in the server
        Glide.with(view)// https://stackoverflow.com/questions/33443146/remove-image-from-cache-in-glide-library
                .load(contactsList.get(position).getPfpUrl())
                .centerCrop()
                .placeholder(R.drawable.ic_menu_gallery)
                .into(pfpView);

        ImageView newMessageIV = view.findViewById(R.id.imageView2);
        newMessageIV.setImageResource(R.drawable.ic_round_fiber_new_24);
        newMessageIV.setVisibility(View.INVISIBLE);
        view.setTag(R.id.contactTag, contactsList.get(position));

        if (contactsToAddSticker != null){
            for(String contactID: contactsToAddSticker){
                if (contactID.equals(((Contact)view.getTag(R.id.contactTag)).getId())){
                    newMessageIV.setVisibility(View.VISIBLE);
                }
            }
        }
        return view;
    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        contactsList.clear();
        if (charText.length() == 0) {
            contactsList.addAll(arraylist);
        } else {
            for (Contact wp : arraylist) {
                if (this.searchEmail) {
                    if (wp.getEmail().toLowerCase(Locale.getDefault()).contains(charText))
                        contactsList.add(wp);
                }
                else{
                    if (wp.getName().toLowerCase(Locale.getDefault()).contains(charText))
                        contactsList.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void refreshListview(){
        // reorganize contacts according to who sent message
        if (this.contactsToAddSticker != null){ // todo reorganize them according to time sent message if we recive a payload of msgs after being offline
            for (String id: this.contactsToAddSticker){
                for (int i=0; i<this.contactsList.size(); i++) {
                    Contact c = this.contactsList.get(i);
                    if (c.getId().equals(id)){
                        this.contactsList.remove(i);
                        this.contactsList.add(0, c);
                        break;
                    }
                }
            }
        }
    }
}