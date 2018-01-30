package com.inclass.weatherapp.Activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.inclass.weatherapp.R;
import com.inclass.weatherapp.SharedPreference.MySharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Nikhil on 07/04/2017.
 */

public class Preference extends PreferenceActivity {

    MySharedPreferences mySharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new MyPreferenceFragment())
                .commit();
        mySharedPreferences = new MySharedPreferences(this);
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);

        p.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals("Temperature_pref")){
                    Log.d("PrefrenceActivity",sharedPreferences.getString(key,""));
                    mySharedPreferences.setUserPrefsTemp(sharedPreferences.getString(key,""));
                }
            }
        });
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        MySharedPreferences mySharedPreferences;
        String cityName, countryName;
        ProgressDialog progressDialog;
        OkHttpClient client;

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            mySharedPreferences = new MySharedPreferences(getActivity());
            client = new OkHttpClient();
            progressDialog = new ProgressDialog(getActivity());

            ListPreference listPreference = (ListPreference) findPreference("Temperature_pref");
            if(mySharedPreferences.getUserPrefsCityKey()!=""){
                if(mySharedPreferences.getUserPrefsTemp().equals("C"))
                    listPreference.setValueIndex(0);
                else listPreference.setValueIndex(1);
            }


            android.preference.Preference currentCityPref = findPreference("currentCity");

            currentCityPref.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(android.preference.Preference preference) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    String title, setButtonText;


                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    final View inflaterView = inflater.inflate(R.layout.dialog_pref_layout, null);
                    if(mySharedPreferences.getUserPrefsCityKey()!=""){
                        title = "Update city details";
                        setButtonText = "Change";
                        cityName = mySharedPreferences.getUserPrefsCityName();
                        countryName = mySharedPreferences.getUserPrefsCountryName();
                    } else {
                        title = "Set city details";
                        setButtonText = "Set";
                    }
                    builder.setTitle(title)
                            .setView(inflaterView)
                            .setCancelable(false);

                    final EditText editTextDiagPrefCityName = (EditText) inflaterView.findViewById(R.id.editTextDiagPrefCityName);
                    final EditText editTextDiagPrefCountryName = (EditText) inflaterView.findViewById(R.id.editTextDiagPrefCountryName);
                    editTextDiagPrefCityName.setText(cityName);
                    editTextDiagPrefCountryName.setText(countryName);

                    builder.setPositiveButton(setButtonText, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (editTextDiagPrefCityName.length() > 0 && editTextDiagPrefCountryName.length() > 0) {

                                cityName = editTextDiagPrefCityName.getText().toString();
                                countryName = editTextDiagPrefCountryName.getText().toString();
                                getKey();

                            } else {
                                Toast.makeText(getActivity(), "Set both values", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    }).show();

                    return true;
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
                    //Toast.makeText(MainActivity.this, "City not found", Toast.LENGTH_SHORT).show();
                    getActivity().runOnUiThread(new Runnable() {
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
                            //locationKey = jsonObject.getString("Key");
                            mySharedPreferences.setUserPrefsCityKey(jsonObject.getString("Key"));
                            mySharedPreferences.setUserPrefsCityName(cityName);
                            mySharedPreferences.setUserPrefsCountryName(countryName);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), "Current City Set", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                Toast.makeText(getActivity(), "City not found", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }

    }


}
