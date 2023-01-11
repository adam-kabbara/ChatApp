package com.example.chatapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.chatapp.ui.home.Contact;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListViewAdapter extends BaseAdapter {

    // Declare Variables

    Context mContext;
    LayoutInflater inflater;
    private List<Contact> contactsList = null;
    private ArrayList<Contact> arraylist;

    public ListViewAdapter(Context context, List<Contact> contactsList) {
        mContext = context;
        this.contactsList = contactsList;
        inflater = LayoutInflater.from(mContext);
        this.arraylist = new ArrayList<>();
        this.arraylist.addAll(contactsList);
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
            view = inflater.inflate(R.layout.list_view_items, null);
            holder.name = view.findViewById(R.id.name);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.name.setText(contactsList.get(position).getEmail());
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
                if (wp.getEmail().toLowerCase(Locale.getDefault()).contains(charText)) {
                    contactsList.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }

}