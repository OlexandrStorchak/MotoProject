package com.example.alex.motoproject.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.models.OnlineUser;
import com.example.alex.motoproject.utils.CircleTransform;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.example.alex.motoproject.MainActivity.mainActivity;
import static com.example.alex.motoproject.fragments.MapFragment.mapFragmentInstance;


public class OnlineUsersAdapter extends RecyclerView.Adapter<OnlineUsersAdapter.VH> {

    private static final String TAG = "log";
    private List<OnlineUser> friendsList;


    public OnlineUsersAdapter(List<OnlineUser> friendsList) {
        this.friendsList = friendsList;

    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.who_is_online_list_row_small, parent, false);
        return new VH(view);
    }

    public void setList(List<OnlineUser> newList) {
        friendsList = newList;

    }

    @Override
    public void onBindViewHolder(final VH holder, int position) {
        holder.name.setText(friendsList.get(position).getName());

        Picasso.with(holder.avatar.getContext())
                .load(friendsList.get(position).getAvatar())
                .resize(holder.avatar.getMaxWidth(), holder.avatar.getMaxHeight())
                .centerCrop()
                .transform(new CircleTransform())
                .into(holder.avatar);
        if (friendsList.get(position).getStatus() != null) {
            if (friendsList.get(position).getStatus().equals("public")) {
                holder.mapCur.setVisibility(View.VISIBLE);
            } else {
                holder.mapCur.setVisibility(View.GONE);
            }
        }
        holder.mapCur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.replaceFragment("fragmentMap");
//                Toast.makeText(holder.name.getContext(), holder.name.getText(), Toast.LENGTH_SHORT).show();
////                mapFragmentInstance.setMarker(friendsList.get(holder.getAdapterPosition()).getLat(),
////                        friendsList.get(holder.getAdapterPosition()).getLon(),
////                        friendsList.get(holder.getAdapterPosition()).getEmail()
////                );
                // TODO: 05.02.2017 get user`s uid
                mapFragmentInstance.moveToMarker("KAMUHSEh2VX2hLygvhkM9StSEa32");
// friendsList.get(holder.getAdapterPosition()).getEmail());
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
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
                    Log.d(TAG, "onClick: " + getAdapterPosition());

                }
            });

            avatar = (ImageView) itemView.findViewById(R.id.friends_list_ava);
            name = (TextView) itemView.findViewById(R.id.userName);
            mapCur = (ImageView) itemView.findViewById(R.id.friends_list_map_icon);

        }
    }
}
