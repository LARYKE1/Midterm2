package com.example.midterm2;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.lang.ref.ReferenceQueue;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder> {

    private List<Contact> contactList;
    ContactListener listener;

    public ContactAdapter(Context context, List<Contact> contactList, ContactListener listener) {
        this.contactList = contactList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_row, parent, false);
        return new MyViewHolder(view, listener);

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.txtName.setText(contact.getName());
        holder.phoneTxt.setText(contact.getPhone());
        holder.contact = contact;

    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public void filterList(List<Contact> filterList) {
        contactList = filterList;
        notifyDataSetChanged();
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView txtName, phoneTxt;
        private ImageView imgEdit, imgDelete;
        Contact contact;
        ContactListener mListener;
        public MyViewHolder(@NonNull View itemView, ContactListener mlistener) {
            super(itemView);

            this.mListener = mlistener;
            txtName = itemView.findViewById(R.id.textName);
            imgEdit = itemView.findViewById(R.id.btnEdit);
            imgDelete = itemView.findViewById(R.id.btnDelete);
            phoneTxt = itemView.findViewById(R.id.phoneNumber);

            itemView.setOnClickListener(view -> {

                if (phoneTxt.getVisibility() == View.VISIBLE) {
                    txtName.setTextSize(24);
                    phoneTxt.setVisibility(View.INVISIBLE);
                } else {
                    txtName.setTextSize(18);
                    phoneTxt.setVisibility(View.VISIBLE);
                }
            });
            imgEdit.setOnClickListener(view -> {
                mlistener.receiveContact(contact);
            });
            imgDelete.setOnClickListener(view -> {
                mListener.deleteContact(contact.getId());
            });
        }
    }
}
