package com.mauritues.brume.bmtraffic;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mauritues.brume.bmtraffic.model.LocationRecording;

import java.util.List;

public class CustomListAdapter extends RecyclerView.Adapter<CustomListAdapter.ViewHolder> {

    //to store the animal images
    private final List<LocationRecording> recordHistories;


    public CustomListAdapter(List<LocationRecording> locationRecordings) {
        this.recordHistories = locationRecordings;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listview_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.nameTextField.setSelected(true);
        holder.infoTextField.setSelected(true);
        holder.nameTextField.setText(recordHistories.get(position).getSourceLocationAddress());
        holder.infoTextField.setText(recordHistories.get(position).getDestinationLocationAddress());
    }

    @Override
    public int getItemCount() {
        return recordHistories.size();
    }

    /*public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.listview_row, null, true);

        //this code gets references to objects in the listview_row.xml file
        TextView nameTextField = rowView.findViewById(R.id.nameTextViewID);
        TextView infoTextField = rowView.findViewById(R.id.infoTextViewID);
        //ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView1ID);

        //this code sets the values of the objects to values from the arrays
//        imageView.setImageResource(imageIDarray[position]);

        return rowView;
    }*/

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextField;
        TextView infoTextField;

        ViewHolder(View v) {
            super(v);
            nameTextField = v.findViewById(R.id.nameTextViewID);
            infoTextField = v.findViewById(R.id.infoTextViewID);
        }
    }


}
