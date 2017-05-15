package com.example.administrator.lsys_camera.recycler.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.administrator.lsys_camera.R;
import com.example.administrator.lsys_camera.recycler.list.ItemListFilter;

import java.util.List;

public class RecyclerAdapterFilter extends RecyclerView.Adapter<RecyclerAdapterFilter.ViewHolder> {

    Context context;

    private List<ItemListFilter> albumList;
    private int itemLayout;

    //아이템 클릭 콜백 등록
    private ItemClick itemClick;

    public interface ItemClick {
        public void onClick(int position);
    }

    public void setItemClick(ItemClick itemClick) {
        this.itemClick = itemClick;
    }


    public RecyclerAdapterFilter(Context context, List<ItemListFilter> albumList, int itemLayout) {
        this.context = context;
        this.albumList = albumList;
        this.itemLayout = itemLayout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(itemLayout, viewGroup, false);
        return new ViewHolder(view);
    }

     // listView getView 를 대체, 넘겨 받은 데이터를 화면에 출력하는 역할
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        ItemListFilter item = albumList.get(position);

        viewHolder.textTitle.setText(item.getTitle());

        viewHolder.img.setBackgroundResource(item.getImage());
        Glide.with(context).load(albumList.get(position).getImage())
                .into(viewHolder.img);

        viewHolder.itemView.setTag(item);

        final int finalPosition = position;
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemClick != null){
                    itemClick.onClick(finalPosition);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    // 뷰 재활용을 위한 ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView img;
        public TextView textTitle;

        public ViewHolder(View itemView) {
            super(itemView);

            img = (ImageView) itemView.findViewById(R.id.img);
            textTitle = (TextView) itemView.findViewById(R.id.textTitle);
        }
    }
}
