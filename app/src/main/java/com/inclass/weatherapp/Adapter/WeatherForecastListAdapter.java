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

import com.inclass.weatherapp.POJO.WeatherForecast;
import com.inclass.weatherapp.R;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Admin on 03-03-2017.
 */

public class WeatherForecastListAdapter extends RecyclerView.Adapter<WeatherForecastListAdapter.ViewHolder> {

    static Context context;
    int resource;
    static List<WeatherForecast> weatherForecasts;
    static WeatherDay weatherDay;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView_forecastRecycler;
        ImageView imageView_forecastRecycler;

        public ViewHolder(View view) {
            super(view);
            textView_forecastRecycler = (TextView) view.findViewById(R.id.textView_forecastRecycler);
            imageView_forecastRecycler = (ImageView)view.findViewById(R.id.imageView_forecastRecycler);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getClickedItem(getAdapterPosition());
                }
            });
        }
    }

    public WeatherForecastListAdapter(Context context, int resource, List<WeatherForecast> weatherForecasts, WeatherDay weatherDay) {

        this.context = context;
        this.resource = resource;
        this.weatherForecasts = weatherForecasts;
        this.weatherDay = weatherDay;
        //Log.d("In Recycler View",weatherForecasts.get(0).getMaxTemp());
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(resource, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final WeatherForecast weather = weatherForecasts.get(position);
        holder.textView_forecastRecycler.setText(changeDateFormat(weather.getDate()));

        Picasso.with(context)
                .load(context.getString(R.string.weather_icon, weather.getDayIcon()))
                .into(holder.imageView_forecastRecycler);

    }

    @Override
    public int getItemCount() {
        return weatherForecasts.size();
    }

    private static void getClickedItem(int pos) {
        weatherDay.setDayDetails(pos);
    }

    static public interface WeatherDay {
        void setDayDetails(int pos);
    }

    public String changeDateFormat(String date){
        SimpleDateFormat iformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        SimpleDateFormat oformat = new SimpleDateFormat("dd MMM''yy");
        try {
            return oformat.format(iformat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }
}

