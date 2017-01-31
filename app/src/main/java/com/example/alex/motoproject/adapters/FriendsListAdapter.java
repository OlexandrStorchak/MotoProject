package com.example.alex.motoproject.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.models.FriendsListModel;

import java.util.List;



public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.VH> {

    private List<FriendsListModel> friendsList;

    public FriendsListAdapter(List<FriendsListModel> friendsList) {
        this.friendsList = friendsList;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.navigation_friends_list_row, parent, false);
        return new VH(view);
    }

    public void setList(List<FriendsListModel> newList){
        friendsList = newList;

    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        holder.name.setText("");
        holder.status.setText(friendsList.get(position).getEmail());
        holder.email.setText("");

    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    class VH extends RecyclerView.ViewHolder{
        ImageView avatar;
        TextView name,status, email;
        VH(View itemView) {
            super(itemView);


            avatar = (ImageView) itemView.findViewById(R.id.friends_list_ava);
            name = (TextView) itemView.findViewById(R.id.userName);
            status = (TextView) itemView.findViewById(R.id.userStatus);
            email = (TextView) itemView.findViewById(R.id.userInfo);

        }
    }
}
