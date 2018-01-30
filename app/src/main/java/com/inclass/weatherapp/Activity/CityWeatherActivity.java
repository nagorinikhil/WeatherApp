package com.inclass.weatherapp.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inclass.weatherapp.Adapter.WeatherForecastListAdapter;
import com.inclass.weatherapp.POJO.CityPOJO;
import com.inclass.weatherapp.POJO.WeatherForecast;
import com.inclass.weatherapp.R;
import com.inclass.weatherapp.SharedPreference.MySharedPreferences;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CityWeatherActivity extends AppCompatActivity implements WeatherForecastListAdapter.WeatherDay {

    OkHttpClient client;
    String locationKey, cityName, countryName;
    String headline, headlineLink;
    ArrayList<WeatherForecast> weatherForecastArrayList;
    CityPOJO city;
    DatabaseReference myRef;
    ArrayList<String> databaseKeys;
    TextView textViewHeader, textViewHeadline, textViewForecastDate, textViewTemperature, textViewDayWeather, textViewNightWeather;
    ImageView imageViewDay, imageViewNight;
    Button buttonMoreDetails, buttonExtendedForecast;
    MySharedPreferences sharedPreferences;
    int globalPosition = 0;
    ProgressDialog progressDialog;
    RecyclerView recyclerView;
    WeatherForecastListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_weather);

        myRef = FirebaseDatabase.getInstance().getReference("City");
        client = new OkHttpClient();
        weatherForecastArrayList = new ArrayList<>();
        databaseKeys = new ArrayList<>();
        sharedPreferences = new MySharedPreferences(CityWeatherActivity.this);
        progressDialog = new ProgressDialog(this);

        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        textViewHeader = (TextView) findViewById(R.id.textViewHeader);
        textViewHeadline = (TextView) findViewById(R.id.textViewHeadline);
        textViewForecastDate = (TextView) findViewById(R.id.textViewForecastDate);
        textViewTemperature = (TextView) findViewById(R.id.textViewTemperature);
        textViewDayWeather = (TextView) findViewById(R.id.textViewDayWeather);
        textViewNightWeather = (TextView) findViewById(R.id.textViewNightWeather);
        imageViewDay = (ImageView) findViewById(R.id.imageViewDay);
        imageViewNight = (ImageView) findViewById(R.id.imageViewNight);
        buttonMoreDetails = (Button) findViewById(R.id.buttonMoreDetails);
        buttonExtendedForecast = (Button) findViewById(R.id.buttonExtendedForecast);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_weatherForecast);


        cityName = getIntent().getStringExtra("City");
        countryName = getIntent().getStringExtra("Country");


        textViewHeader.setText("Daily forecast for " + cityName + ", " + countryName);
        getKey();

        buttonMoreDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (weatherForecastArrayList.size() > 0) {
                    Intent i = new Intent();
                    i.setAction(android.content.Intent.ACTION_VIEW);
                    i.setData(Uri.parse(weatherForecastArrayList.get(globalPosition).getMobileLink()));
                    startActivity(i);
                }

            }
        });

        buttonExtendedForecast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (weatherForecastArrayList.size() > 0) {
                    Intent i = new Intent();
                    i.setAction(android.content.Intent.ACTION_VIEW);
                    i.setData(Uri.parse(headlineLink));
                    startActivity(i);
                }
            }
        });

    }

    public void getKey() {
        progressDialog.setCancelable(false);
        progressDialog.show();
        Request request = new Request.Builder()
                .url(getString(R.string.location_api, countryName, getString(R.string.api_key), cityName))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //Toast.makeText(CityWeatherActivity.this, "City not found", Toast.LENGTH_SHORT).show();
                CityWeatherActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String temp = response.body().string();
                if (temp.length() > 2) {
                    Log.d("Temp = ", temp);
                    try {
                        JSONArray jsonArray = new JSONArray(temp);
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        locationKey = jsonObject.getString("Key");

                        get5DayForecast();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    CityWeatherActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(CityWeatherActivity.this, "City not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
            }
        });
    }

    public void get5DayForecast() {

        Request request = new Request.Builder()
                .url(getString(R.string.weather_city, locationKey, getString(R.string.api_key)))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                CityWeatherActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String temp = response.body().string();
                if (temp.length() > 2) {
                    try {
                        JSONObject jsonObject = new JSONObject(temp);
                        headline = jsonObject.getJSONObject("Headline").getString("Text");
                        headlineLink = jsonObject.getJSONObject("Headline").getString("MobileLink");
                        JSONArray jsonArray = jsonObject.getJSONArray("DailyForecasts");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            WeatherForecast weatherForecast = new WeatherForecast();
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            weatherForecast.setDate(jsonObject1.getString("Date"));
                            weatherForecast.setMinTemp(jsonObject1.getJSONObject("Temperature").getJSONObject("Minimum").getString("Value"));
                            weatherForecast.setMaxTemp(jsonObject1.getJSONObject("Temperature").getJSONObject("Maximum").getString("Value"));
                            weatherForecast.setDayIcon(jsonObject1.getJSONObject("Day").getString("Icon"));
                            if (weatherForecast.getDayIcon().length() < 2)
                                weatherForecast.setDayIcon("0" + weatherForecast.getDayIcon());
                            weatherForecast.setNightIcon(jsonObject1.getJSONObject("Night").getString("Icon"));
                            if (weatherForecast.getNightIcon().length() < 2)
                                weatherForecast.setNightIcon("0" + weatherForecast.getDayIcon());
                            weatherForecast.setDayForecast(jsonObject1.getJSONObject("Day").getString("IconPhrase"));
                            weatherForecast.setNightForecast(jsonObject1.getJSONObject("Night").getString("IconPhrase"));
                            weatherForecast.setMobileLink(jsonObject1.getString("MobileLink"));
                            weatherForecastArrayList.add(weatherForecast);
                        }
                        CityWeatherActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                adapter = new WeatherForecastListAdapter(CityWeatherActivity.this, R.layout.forecast_list_item, weatherForecastArrayList, CityWeatherActivity.this);
                                adapter.notifyDataSetChanged();
                                recyclerView.setLayoutManager(new LinearLayoutManager(CityWeatherActivity.this, LinearLayoutManager.HORIZONTAL, false));
                                recyclerView.setAdapter(adapter);
                                setWeather(0);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    CityWeatherActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(CityWeatherActivity.this, "Weather not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_weather_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            city = new CityPOJO();
            city.setCityKey(locationKey);
            city.setCityName(cityName);
            city.setCountryName(countryName);
            city.setTime(new Date());
            city.setTemperature(FtoCelcius(weatherForecastArrayList.get(0).getMaxTemp()));
            if (databaseKeys.contains(locationKey)) {
                myRef.child(locationKey).child("temperature").setValue(city.getTemperature());
                myRef.child(locationKey).child("time").setValue(city.getTime());
                Toast.makeText(this, "City Updated", Toast.LENGTH_SHORT).show();
            } else {
                city.setFavorite(false);
                myRef.child(locationKey).setValue(city);
                Toast.makeText(this, "City Saved", Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.action_settings) {
            Intent i = new Intent(CityWeatherActivity.this,Preference.class);
            startActivity(i);

        } else if (id == R.id.action_currentCity) {

            if (sharedPreferences.getUserPrefsCityKey() != "") {
                sharedPreferences.setUserPrefsCityKey(locationKey);
                sharedPreferences.setUserPrefsCityName(cityName);
                sharedPreferences.setUserPrefsCountryName(countryName);
                Toast.makeText(this, "City Updated", Toast.LENGTH_SHORT).show();
            } else {
                sharedPreferences.setUserPrefsCityKey(locationKey);
                sharedPreferences.setUserPrefsCityName(cityName);
                sharedPreferences.setUserPrefsCountryName(countryName);
                Toast.makeText(this, "City Saved", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (weatherForecastArrayList.size() != 0) {
            setWeather(globalPosition);
        }

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    databaseKeys.add(child.toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public String FtoCelcius(String temp) {
        Double t = Double.parseDouble(temp);
        Long t1 = Math.round((((t - 32) * 5) / 9));

        return String.valueOf(t1);
    }

    public void setWeather(int position) {
        adapter.notifyDataSetChanged();
        globalPosition = position;
        WeatherForecast weather = weatherForecastArrayList.get(position);
        textViewHeadline.setText(headline);
        if (sharedPreferences.getUserPrefsTemp().equals("C")) {
            textViewTemperature.setText("Temperature : " + FtoCelcius(weather.getMaxTemp()) + "°"+sharedPreferences.getUserPrefsTemp()+" / " + FtoCelcius(weather.getMinTemp()) + "°" + sharedPreferences.getUserPrefsTemp());
        } else {
            textViewTemperature.setText("Temperature : " + weather.getMaxTemp() + "°"+sharedPreferences.getUserPrefsTemp()+" / "+  weather.getMinTemp() + "°" + sharedPreferences.getUserPrefsTemp());
        }

        textViewForecastDate.setText("Forecast on " + changeDateFormat(weather.getDate()));
        textViewDayWeather.setText(weather.getDayForecast());
        textViewNightWeather.setText(weather.getNightForecast());

        Picasso.with(CityWeatherActivity.this)
                .load(getString(R.string.weather_icon, weather.getDayIcon()))
                .into(imageViewDay);

        Picasso.with(CityWeatherActivity.this)
                .load(getString(R.string.weather_icon, weather.getNightIcon()))
                .into(imageViewNight);
    }

    @Override
    public void setDayDetails(int pos) {
        setWeather(pos);
    }

    public String changeDateFormat(String date) {
        SimpleDateFormat iformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        SimpleDateFormat oformat = new SimpleDateFormat("MMMM dd,yyyy");


        try {
            return oformat.format(iformat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }
}
