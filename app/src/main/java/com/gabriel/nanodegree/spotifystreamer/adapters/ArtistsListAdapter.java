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

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

public class ArtistsListAdapter extends RecyclerView.Adapter<ArtistsListAdapter.MyViewHolder> {
    List<Artist> data = Collections.emptyList();
    private Context context;
    private ClickListener clickListener;

    public ArtistsListAdapter(List<Artist> data, Context context) {
        this.data = data;
        this.context = context;
    }

    public void updateList(List<Artist> updated){
        data.clear();
        data.addAll(updated);
        notifyDataSetChanged();
    }

    @Override
    public ArtistsListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.artist_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Artist currentItem = data.get(position);

        holder.artistView.setText("" + currentItem.name);

        if (currentItem.images.size()>0) {
            String imageUrl = currentItem.images.get(0).url;
            for (Image im: currentItem.images) {
                if(im.width > 190 && im.width < 210){
                    imageUrl = im.url;
                }
            }
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.with(context).load(imageUrl).into(holder.imageView);
            }
        }
    }

    public void setClickListener(ClickListener clickListener){
        this.clickListener = clickListener;
    }

    public Artist getItem(int postion) {
        return data.get(postion);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView artistView;
        ImageView imageView;

        public MyViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            artistView = (TextView) v.findViewById(R.id.title);
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