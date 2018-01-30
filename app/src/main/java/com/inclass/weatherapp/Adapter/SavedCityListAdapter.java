/*
* Homework 7
* MyListAdapter.java
* Nikhil Nagori, Hozefa Haveliwala
* */

package com.inclass.weatherapp.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.inclass.weatherapp.POJO.CityPOJO;
import com.inclass.weatherapp.R;
import com.inclass.weatherapp.SharedPreference.MySharedPreferences;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;

/**
 * Created by Admin on 03-03-2017.
 */

public class SavedCityListAdapter extends RecyclerView.Adapter<SavedCityListAdapter.ViewHolder> {

    static Context context;
    int resource;
    static List<CityPOJO> cityPOJOList;
    static Favorite sMB;
    MySharedPreferences sharedPreferences;
    PrettyTime prettyTime;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewRecycleTemperature, textViewRecycleTime,textViewRecycleCityName;
        ImageView imageViewRecycleFavorite;

        public ViewHolder(View view) {
            super(view);
            textViewRecycleTemperature = (TextView) view.findViewById(R.id.textViewRecyclerTemperature);
            textViewRecycleTime = (TextView) view.findViewById(R.id.textViewRecycleTime);
            textViewRecycleCityName = (TextView) view.findViewById(R.id.textViewRecyclerCityName);
            imageViewRecycleFavorite = (ImageView) view.findViewById(R.id.imageViewRecycleFavorite);

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    getClickedItem(getAdapterPosition());
                    return false;
                }
            });
        }
    }

    public SavedCityListAdapter(Context context, int resource, List<CityPOJO> cityPOJOList, Favorite sMB) {

        this.context = context;
        this.resource = resource;
        this.cityPOJOList = cityPOJOList;
        this.sMB = sMB;
        sharedPreferences = new MySharedPreferences(context);
        prettyTime = new PrettyTime();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(resource, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final CityPOJO city = cityPOJOList.get(position);
        holder.textViewRecycleCityName.setText(city.getCityName()+", "+city.getCountryName());
        if(sharedPreferences.getUserPrefsTemp().equals("F")){
            holder.textViewRecycleTemperature.setText("Temperature : "+ CtoFarheneit(city.getTemperature()) +"° " +sharedPreferences.getUserPrefsTemp());
        }else {
            holder.textViewRecycleTemperature.setText("Temperature : "+ city.getTemperature() +"° " +sharedPreferences.getUserPrefsTemp());
        }

        holder.textViewRecycleTime.setText("Last updated :"+ prettyTime.format(city.getTime()));

        if(city.isFavorite()){
            holder.imageViewRecycleFavorite.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            holder.imageViewRecycleFavorite.setImageResource(android.R.drawable.btn_star_big_off);
        }

        holder.imageViewRecycleFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context, podcast.getTitle(), Toast.LENGTH_SHORT).show();
                sMB.changeFavorite(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return cityPOJOList.size();
    }

    private static void getClickedItem(int pos) {
        sMB.deleteCity(pos);
    }

    static public interface Favorite {
        void changeFavorite(int pos);
        void deleteCity(int pos);
    }

    public String CtoFarheneit(String temp){
        Double t = Double.parseDouble(temp);
        Long t1 = Math.round(((t*1.8)+32));
        return String.valueOf(t1);
    }
}

