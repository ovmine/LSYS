package com.example.administrator.lsys_camera.recycler.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.administrator.lsys_camera.R;
import com.example.administrator.lsys_camera.recycler.list.ItemListCollage;

import java.util.List;

public class RecyclerAdapterCollage extends RecyclerView.Adapter<RecyclerAdapterCollage.ViewHolder> {

    Context context;

    private List<ItemListCollage> imaList;
    private int itemLayout;

    //아이템 클릭 콜백 등록
    private ItemClick itemClick;

    public interface ItemClick {
        public void onClick(int position);
    }

    public void setItemClick(ItemClick itemClick) {
        this.itemClick = itemClick;
    }

    public RecyclerAdapterCollage(Context context, List<ItemListCollage> img, int itemLayout) {
        this.context = context;
        this.imaList = img;
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
        ItemListCollage item = imaList.get(position);

        viewHolder.img.setBackgroundResource(item.getImage());

        Glide.with(context).load(imaList.get(position).getImage())
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
        return imaList.size();
    }

    // 뷰 재활용을 위한 ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView img;

        public ViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.img);
        }
    }
}
