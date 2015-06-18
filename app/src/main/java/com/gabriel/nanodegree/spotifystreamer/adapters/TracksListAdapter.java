package com.gabriel.nanodegree.spotifystreamer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gabriel.nanodegree.spotifystreamer.R;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class TracksListAdapter extends RecyclerView.Adapter<TracksListAdapter.MyViewHolder> {
    List<Track> data = Collections.emptyList();
    private Context context;
    private ClickListener clickListener;

    public TracksListAdapter(List<Track> data, Context context) {
        this.data = data;
        this.context = context;
    }

    public void updateList(List<Track> updated){
        data.clear();
        data.addAll(updated);
        notifyDataSetChanged();
    }

    @Override
    public TracksListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.track_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Track currentItem = data.get(position);
        holder.albumView.setText("" + currentItem.album.name);
        holder.trackView.setText("" + currentItem.name);

        if (currentItem.album.images.size()>0) {
            String imageUrl = currentItem.album.images.get(0).url;
            for (Image im:currentItem.album.images) {
                if(im.width >= 200 && im.width <= 300){
                    imageUrl = im.url;
                }
            }
            Picasso.with(context).load(imageUrl).into(holder.imageView);
        }
    }

    public void setClickListener(ClickListener clickListener){
        this.clickListener = clickListener;
    }

    public Track getItem(int postion) {
        return data.get(postion);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView albumView, trackView;
        ImageView imageView;

        public MyViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            albumView = (TextView) v.findViewById(R.id.album);
            trackView = (TextView) v.findViewById(R.id.track);
            imageView = (ImageView) v.findViewById(R.id.image);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null){
                clickListener.itemClicked(v,getPosition());
            }
        }
    }

    public interface ClickListener{
        void itemClicked(View view, int position);
    }
}