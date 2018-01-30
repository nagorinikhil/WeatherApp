package com.inclass.weatherapp.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inclass.weatherapp.Adapter.SavedCityListAdapter;
import com.inclass.weatherapp.POJO.CityPOJO;
import com.inclass.weatherapp.R;
import com.inclass.weatherapp.SharedPreference.MySharedPreferences;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements SavedCityListAdapter.Favorite{

    EditText editTextCityName, editTextCountryName;
    AlertDialog alertDialog;
    OkHttpClient client;
    String locationKey;
    LinearLayout linearLayoutCurrentCity, linearLayoutCurrentCityDetails;
    DatabaseReference myRef;
    ArrayList<CityPOJO> savedCityArrayList;
    ProgressDialog progressDialog;
    MySharedPreferences sharedPreferences;
    String cityName, countryName, weatherIcon;
    TextView textViewCityName, textViewCityWeather, textViewCityTemperature, textViewCityTime, textViewSavedCity;
    ImageView imageViewWeather;
    RecyclerView recyclerViewSavedCity;
    SavedCityListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myRef = FirebaseDatabase.getInstance().getReference("City");
        client = new OkHttpClient();
        savedCityArrayList = new ArrayList<>();

        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);


        editTextCityName = (EditText) findViewById(R.id.editTextCityName);
        editTextCountryName = (EditText) findViewById(R.id.editTextCountryName);
        linearLayoutCurrentCity = (LinearLayout) findViewById(R.id.linearLayoutCurrentCity);
        linearLayoutCurrentCityDetails = (LinearLayout) findViewById(R.id.linearLayoutCurrentCityDetails);
        textViewCityName = (TextView) findViewById(R.id.textViewCityName);
        textViewCityWeather = (TextView) findViewById(R.id.textViewCityWeather);
        textViewCityTemperature = (TextView) findViewById(R.id.textViewCityTemperature);
        textViewCityTime = (TextView) findViewById(R.id.textViewCityTime);
        textViewSavedCity = (TextView)findViewById(R.id.textViewSavedCity);
        imageViewWeather = (ImageView) findViewById(R.id.imageViewWeather);
        recyclerViewSavedCity = (RecyclerView)findViewById(R.id.recyclerViewSavedCity);

        adapter = new SavedCityListAdapter(this,R.layout.saved_city_list,savedCityArrayList,this);
        recyclerViewSavedCity.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSavedCity.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        progressDialog = new ProgressDialog(this);
        sharedPreferences = new MySharedPreferences(this);

        if (sharedPreferences.getUserPrefsTemp().equals("")) {
            sharedPreferences.setUserPrefsTemp("C");
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        findViewById(R.id.buttonSetCurrentCity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                final View inflaterView = inflater.inflate(R.layout.alert_dialog_layout, null);

                builder.setTitle("Enter City Details")
                        .setView(inflaterView);

                final EditText editTextCity = (EditText) inflaterView.findViewById(R.id.editTextCity);
                final EditText editTextCountry = (EditText) inflaterView.findViewById(R.id.editTextCountry);

                builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (editTextCity.length() > 0 && editTextCountry.length() > 0) {
                            //Toast.makeText(MainActivity.this, editTextCity.getText(), Toast.LENGTH_SHORT).show();
                            cityName = editTextCity.getText().toString();
                            countryName = editTextCountry.getText().toString();
                            if (isConnected()) {
                                //progressDialog.show();
                                dialogInterface.cancel();
                                getKey();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Set both values", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });

                builder.show();
            }
        });

        findViewById(R.id.buttonSearchCity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editTextCityName.length() != 0 && editTextCountryName.length() != 0) {
                    Intent i = new Intent(MainActivity.this, CityWeatherActivity.class);
                    i.putExtra("City", editTextCityName.getText().toString());
                    i.putExtra("Country", editTextCountryName.getText().toString());
                    startActivity(i);
                } else {
                    Toast.makeText(MainActivity.this, "Enter both values", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    protected void onStart() {

        super.onStart();

        editTextCountryName.setText("");
        editTextCityName.setText("");

        if(sharedPreferences.getUserPrefsCityKey()!=""){
            progressDialog.show();
            getCurrentWeather();
        }

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                savedCityArrayList.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    CityPOJO city = child.getValue(CityPOJO.class);
                    savedCityArrayList.add(city);
                }
                if(savedCityArrayList.size()>0){
                    textViewSavedCity.setText("Saved Cities");
                    Collections.sort(savedCityArrayList, new Comparator<CityPOJO>() {
                        @Override
                        public int compare(CityPOJO cityPOJO, CityPOJO t1) {
                            if(cityPOJO.isFavorite())
                                return -1;
                            else
                                return 1;
                        }
                    });
                }else{
                    textViewSavedCity.setText("There are no cities to display \n Search the city from the search box and save");
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_settings){
            Intent i = new Intent(MainActivity.this,Preference.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isConnected() {
        ConnectivityManager cM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cM.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected() == true) {
            return true;
        }
        return false;
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
                //Toast.makeText(MainActivity.this, "City not found", Toast.LENGTH_SHORT).show();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String temp = response.body().string();
                if (temp.length()>2) {
                    try {
                        JSONArray jsonArray = new JSONArray(temp);
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        locationKey = jsonObject.getString("Key");
                        sharedPreferences.setUserPrefsCityKey(locationKey);
                        sharedPreferences.setUserPrefsCityName(cityName);
                        sharedPreferences.setUserPrefsCountryName(countryName);
                        getCurrentWeather();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "City not found", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    public void getCurrentWeather() {

        Request request = new Request.Builder()
                .url(getString(R.string.current_forecast, sharedPreferences.getUserPrefsCityKey(), getString(R.string.api_key)))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String temp = response.body().string();
                if (temp.length()>2) {
                    try {
                        JSONArray jsonArray = new JSONArray(temp);
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        final String time = jsonObject.getString("LocalObservationDateTime");
                        final String weatherText = jsonObject.getString("WeatherText");
                        weatherIcon = jsonObject.getString("WeatherIcon");
                        if (weatherIcon.length() < 2) {
                            weatherIcon = "0" + weatherIcon;
                        }
                        final String temperature;
                        if (sharedPreferences.getUserPrefsTemp().equals("C")) {
                            temperature = jsonObject.getJSONObject("Temperature").getJSONObject("Metric").getString("Value");
                        } else {
                            temperature = jsonObject.getJSONObject("Temperature").getJSONObject("Imperial").getString("Value");
                        }

                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                linearLayoutCurrentCity.setVisibility(View.GONE);
                                linearLayoutCurrentCityDetails.setVisibility(View.VISIBLE);
                                textViewCityName.setText(sharedPreferences.getUserPrefsCityName() + "," + sharedPreferences.getUserPrefsCountryName());
                                textViewCityTemperature.setText("Temperature : "+temperature +"\u00b0 " +sharedPreferences.getUserPrefsTemp());
                                textViewCityWeather.setText(weatherText);
                                textViewCityTime.setText("Updated "+ changeUpdatedTime(time));
                                Picasso.with(MainActivity.this)
                                        .load(getString(R.string.weather_icon, weatherIcon))
                                        .into(imageViewWeather);
                                progressDialog.dismiss();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                        }
                    });
                }

            }
        });
    }

    @Override
    public void changeFavorite(int pos) {
        if(savedCityArrayList.get(pos).isFavorite()){
            myRef.child(savedCityArrayList.get(pos).getCityKey()).child("favorite").setValue(false);
        }else {
            myRef.child(savedCityArrayList.get(pos).getCityKey()).child("favorite").setValue(true);
        }
    }

    @Override
    public void deleteCity(int pos) {
        myRef.child(savedCityArrayList.get(pos).getCityKey()).removeValue();
    }

    public String changeUpdatedTime(String stime){
        SimpleDateFormat iformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        SimpleDateFormat oformat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");


        PrettyTime p = new PrettyTime();
        try {
            return p.format(oformat.parse(oformat.format(iformat.parse(stime))));
        } catch (ParseException e) {
            e.printStackTrace();
            return  "";
        }
    }
}
