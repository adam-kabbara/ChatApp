package com.example.chatapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListViewAdapter extends BaseAdapter {

    // Declare Variables

    Context mContext;
    LayoutInflater inflater;
    private List<Contact> contactsList = null;
    private boolean searchEmail;
    private ArrayList<Contact> arraylist;

    public ListViewAdapter(Context context, List<Contact> contactsList, boolean searchEmail) {
        mContext = context; // if searchEmail is true, ListView Adaptor filters by contact email, else it filters by name
        this.contactsList = contactsList;
        inflater = LayoutInflater.from(mContext);
        this.arraylist = new ArrayList<>();
        this.arraylist.addAll(contactsList);
        this.searchEmail = searchEmail;
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
            holder.name = view.findViewById(R.id.contactNameTextView); // todo add image pfp
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
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
}