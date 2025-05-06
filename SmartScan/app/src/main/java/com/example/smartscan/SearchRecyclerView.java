package com.example.smartscan;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchRecyclerView extends RecyclerView.Adapter<SearchRecyclerView.MyViewHolder> {
    private Context context;
    private List<ResponseData> dataList;
    public SearchRecyclerView(Context context, List<ResponseData> dataList){
        this.dataList = dataList;
        this.context = context;
    }
    @NonNull
    @Override
    public SearchRecyclerView.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cardview, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchRecyclerView.MyViewHolder holder, int position) {
        ResponseData item = dataList.get(position);
        holder.link.setText(item.getLink());
        holder.title.setText(item.getTitle());
        holder.snippet.setText(item.getSnippet());
        if (item.getLink() == null || item.getLink().isEmpty() || item.getLink() == "") {
            holder.itemView.setOnClickListener(null);
        } else {
            holder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getLink()));
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView title, snippet, link;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            snippet = itemView.findViewById(R.id.snippet);
            link = itemView.findViewById(R.id.link);
        }
    }
}
