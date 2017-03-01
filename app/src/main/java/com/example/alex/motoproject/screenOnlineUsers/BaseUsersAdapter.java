package com.example.alex.motoproject.screenOnlineUsers;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.event.OpenMapEvent;
import com.example.alex.motoproject.event.ShowUserProfileEvent;
import com.example.alex.motoproject.util.CircleTransform;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;


abstract class BaseUsersAdapter extends RecyclerView.Adapter<BaseUsersAdapter.VH> {
    private List<OnlineUser> mUsers;

    BaseUsersAdapter() {

    }

    public void setUsersList(List<OnlineUser> users) {
        mUsers = users;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.who_is_online_list_row_small, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(final VH holder, int position) {
        holder.name.setText(mUsers.get(position).getName());

        Picasso.with(holder.avatar.getContext())
                .load(mUsers.get(position).getAvatar())
                .resize(45, 45)
                .centerCrop()
                .transform(new CircleTransform())
                .into(holder.avatar);
        if (mUsers.get(position).getStatus() != null) {
            if (mUsers.get(position).getStatus().equals("public")) {
                holder.mapCur.setVisibility(View.VISIBLE);
            } else {
                holder.mapCur.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }


    class VH extends RecyclerView.ViewHolder {
        ImageView avatar;
        ImageView mapCur;
        TextView name;

        VH(final View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //This click to show user profile
                    EventBus.getDefault().post(new ShowUserProfileEvent(
                            mUsers.get(getAdapterPosition()).getUid()));

                }
            });

            avatar = (ImageView) itemView.findViewById(R.id.friends_list_ava);
            name = (TextView) itemView.findViewById(R.id.userName);
            mapCur = (ImageView) itemView.findViewById(R.id.friends_list_map_icon);
            mapCur.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("log", "onClick: " + getAdapterPosition() + " User Id is :  "
                            + mUsers.get(getAdapterPosition()).getUid());
                    EventBus.getDefault().post(new OpenMapEvent(
                            mUsers.get(getAdapterPosition()).getUid()));
                }

            });
        }
    }
}
