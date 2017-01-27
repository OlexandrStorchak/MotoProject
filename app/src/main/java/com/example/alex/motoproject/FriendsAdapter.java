package com.example.alex.motoproject;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Alex on 27.01.2017.
 */


public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.VH> {

    List friendsList;

    public FriendsAdapter(List friendsList) {
        this.friendsList = friendsList;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friends_list_row, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        holder.name.setText("Name "+position);
        holder.status.setText("Status "+position);
        holder.info.setText("Information | Details ");

    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    public class VH extends RecyclerView.ViewHolder{
        ImageView avatar;
        TextView name,status,info;
        public VH(View itemView) {
            super(itemView);


            avatar = (ImageView) itemView.findViewById(R.id.friends_list_ava);
            name = (TextView) itemView.findViewById(R.id.userName);
            status = (TextView) itemView.findViewById(R.id.userStatus);
            info = (TextView) itemView.findViewById(R.id.userInfo);

        }
    }
}
