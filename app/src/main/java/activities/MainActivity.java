package activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.example.weatherapp.R;
import com.example.weatherapp.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

import network.Network;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// Made by: Daniel Agraz Vallejo. May 1st 2023.

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    // Setting variables for later use
    String currentLocation;

    String latitude;
    String longitude;
    String url;

    String minTemp;
    String currentTemp;
    String maxTemp;

    private final String TAG = "AGRAZ DEBUGGING";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();

        // Improve the readiness of these variables in CurrentLocation
        latitude = intent.getStringExtra("LATITUDE");
        longitude = intent.getStringExtra("LONGITUDE");
        currentLocation = latitude + "," + longitude;
        Log.v(TAG, currentLocation);

        final OkHttpClient client = new OkHttpClient();

        // This was the naughty one. I had to double checked that the URL that we were sending the request was about right
        final Request request = new Request.Builder()
                .url(Network.openWeatherAPI + "forecast.json?key=" + Network.openWeatherAPIKey
                        + "&q=" + currentLocation + "&days=1&aqi=no&alerts=no")
                .build();

        // To make sure that the Call to the API was correct
        url = Network.openWeatherAPI + "forecast.json?key=" + Network.openWeatherAPIKey + "&q=" + currentLocation + "&days=1&aqi=no&alerts=no";
        Log.d(TAG, url);

        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    Response response = client.newCall(request).execute();
                    if(!response.isSuccessful()){
                        return null;
                    }
                    return response.body().string();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String s){
                super.onPostExecute(s);

                if(s != null) {
                    JSONObject jsonResponse = null;
                    try{
                        jsonResponse = new JSONObject(s);

                        // Getting the JSON Object according to an specific key
                        JSONObject locationObject = jsonResponse.getJSONObject("location");
                        JSONObject currentObject = jsonResponse.getJSONObject("current");
                        JSONObject forecastObject = jsonResponse.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(0);
                        JSONObject dayObject = forecastObject.getJSONObject("day");

                        // Getting the String from the icon key that has a CDN to an image
                        String iconObject = currentObject.getJSONObject("condition").getString("icon");

                        // Employing the icon key that has a URL to a PNG image related to the current weather
                        Glide.with(MainActivity.this)
                                .load("https:"+iconObject)
                                .into(binding.weatherImage);

                        // Using placeholders to substitute the value from the JSONObject
                        minTemp = getString(R.string.min_placeholder, dayObject.getString("mintemp_c"), getString(R.string.celcius));
                        currentTemp = getString(R.string.current_placeholder, currentObject.getString("temp_c"), getString(R.string.celcius));
                        maxTemp = getString(R.string.max_placeholder, dayObject.getString("maxtemp_c"), getString(R.string.celcius));

                        // Setting the TextViews according to it's value
                        binding.locationText.setText(locationObject.getString("name"));
                        binding.minimumText.setText(minTemp);
                        binding.currentText.setText(currentTemp);
                        binding.maximumText.setText(maxTemp);
                    } catch (JSONException e){
                        throw new RuntimeException(e);
                    }
                }
            }
        };

        asyncTask.execute();
    }
}

// Made by: Daniel Agraz Vallejo