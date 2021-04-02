package com.zkxl.intelligentlock;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


/**
 * 创建作者       : albus
 * 创建时间       : 2020/3/17
 * Fuction(类描述):
 */
public class FindBandRecycleViewAdapter extends RecyclerView.Adapter<FindBandRecycleViewAdapter.ViewHolder> {
    private Context context;
    private ArrayList<BluetoothDevice> listItem;
    private OnItemClick onitemClick;

    public FindBandRecycleViewAdapter(Context context, ArrayList<BluetoothDevice> normalDevices) {
        this.context = context;
        this.listItem = normalDevices;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder viewHolder = null;
        View view = LayoutInflater.from(context).inflate(R.layout.band_adress_item, parent, false);
        viewHolder = new ViewHolder(view);
        viewHolder.tv = view.findViewById(R.id.adress);
        viewHolder.name = view.findViewById(R.id.name);
        viewHolder.line = view.findViewById(R.id.top_line);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        if (listItem.get(position).getColor()== Color.RED){
//            holder.tv.setTextColor(Color.RED);
//        }else {
//            holder.tv.setTextColor(Color.BLACK);
//        }
        holder.tv.setText(listItem.get(position).getAddress());
        holder.name.setText(listItem.get(position).getName());
        if (position!=0){
            holder.line.setVisibility(View.GONE);
        }else {
            holder.line.setVisibility(View.VISIBLE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothDevice bluetoothDevice = listItem.get(position);
                onitemClick.onItemClick(bluetoothDevice);
            }
        });
    }


    @Override
    public int getItemCount() {
        if (listItem == null) {
            return 0;
        }
        return listItem.size();
    }

    public void setList(ArrayList<BluetoothDevice> listItem) {
        this.listItem = listItem;
    }

    public void addData(ArrayList<BluetoothDevice> normalDevices) {
        listItem.addAll(normalDevices);
        notifyDataSetChanged();
//        notifyDataSetChanged();
//        notifyItemChanged(listItem.size());
//        notifyItemRangeInserted();
//        Log.e("yanchuang",listItem.get(listItem.size()-1).getMsg());
    }

    public void clearLog() {
        listItem.clear();
        notifyDataSetChanged();
    }

    public ArrayList<BluetoothDevice> getList() {
        return listItem;
    }


    public interface OnItemClick {
        void onItemClick(BluetoothDevice adress);
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onitemClick = onItemClick;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv;
        private TextView name;
        private TextView line;

        public ViewHolder(View view) {
            super(view);
        }
    }
}
