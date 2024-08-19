package com.example.adas_project;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;

import android.util.Log;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;

import android.Manifest;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    AbstractMQTTHelper mqttHelper;
    ImageView mImageView;
    TextView textView;
    Spinner spino;
    String[] courses = {"image 0", "image 1", "image 2", "image 3", "image 4", "image 5", "image 6", "image 7", "image 8", "image 9"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitializeMQTT();
        InitializeSpinner();
        textView = findViewById(R.id.textView);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        String selectedItem = courses[position];
        Log.d("MainActivity", "Selected item: " + selectedItem);

        InputStream imageStream;

        switch (selectedItem) {
            case "image 0":
                mImageView = findViewById(R.id.imageView2);
                mImageView.setImageResource(R.drawable.img0);
                imageStream = getResources().openRawResource(R.raw.img0);
                buttonClick(imageStream);
                break;
            case "image 1":
                mImageView = findViewById(R.id.imageView2);
                mImageView.setImageResource(R.drawable.img1);
                imageStream = getResources().openRawResource(R.raw.img1);
                buttonClick(imageStream);
                break;
            case "image 2":
                mImageView = findViewById(R.id.imageView2);
                mImageView.setImageResource(R.drawable.img2);
                imageStream = getResources().openRawResource(R.raw.img2);
                buttonClick(imageStream);
                break;
            case "image 3":
                mImageView = findViewById(R.id.imageView2);
                mImageView.setImageResource(R.drawable.img3);
                imageStream = getResources().openRawResource(R.raw.img3);
                buttonClick(imageStream);
                break;
            case "image 4":
                mImageView = findViewById(R.id.imageView2);
                mImageView.setImageResource(R.drawable.img4);
                imageStream = getResources().openRawResource(R.raw.img4);
                buttonClick(imageStream);
                break;
            case "image 5":
                mImageView = findViewById(R.id.imageView2);
                mImageView.setImageResource(R.drawable.img5);
                imageStream = getResources().openRawResource(R.raw.img5);
                buttonClick(imageStream);
                break;
            case "image 6":
                mImageView = findViewById(R.id.imageView2);
                mImageView.setImageResource(R.drawable.img6);
                imageStream = getResources().openRawResource(R.raw.img6);
                buttonClick(imageStream);
                break;
            case "image 7":
                mImageView = findViewById(R.id.imageView2);
                mImageView.setImageResource(R.drawable.img7);
                imageStream = getResources().openRawResource(R.raw.img7);
                buttonClick(imageStream);
                break;
            case "image 8":
                mImageView = findViewById(R.id.imageView2);
                mImageView.setImageResource(R.drawable.img8);
                imageStream = getResources().openRawResource(R.raw.img8);
                buttonClick(imageStream);
                break;
            case "image 9":
                mImageView = findViewById(R.id.imageView2);
                mImageView.setImageResource(R.drawable.img9);
                imageStream = getResources().openRawResource(R.raw.img9);
                buttonClick(imageStream);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Handle no item selected
    }

    private void buttonClick(InputStream huuhaa) {
        Button button = findViewById(R.id.button);
        button.setOnClickListener(v -> convertAndSend(huuhaa));
    }


    private void convertAndSend(InputStream imageStream){
        try {
            byte[] imageBytes = convertStreamToByteArray(imageStream);
            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            JSONObject jsonMessage = new JSONObject();
            jsonMessage.put("image", encodedImage); // Set the encoded image
            String topic = "adas/image"; // Topic to which you want to send the message
            String message = jsonMessage.toString();

            mqttHelper.sendMessage(topic, message.getBytes()); // Convert message to byte array and send
            Log.d("MainActivity", "Image message sent successfully");

        } catch (JSONException | MqttException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void InitializeMQTT() {
        mqttHelper = new AbstractMQTTHelper(
                "tcp://broker.hivemq.com:1883", // URI of the MQTT broker
                "1234", // Client name
                new String[]{"adas/"}) { // Topics to subscribe to
            @Override
            public void onMessage(String topic, String message) {
                try {
                    JSONObject jsonObject = new JSONObject(message);

                    // Extract the keys object
                    JSONObject keysObject = jsonObject.getJSONObject("keys");

                    // Initialize StringBuilder for coordinates
                    StringBuilder coordinatesBuilder = new StringBuilder();

                    // Iterate over keys
                    Iterator<String> keys = keysObject.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        JSONArray coordinatesArray = keysObject.getJSONArray(key);
                        coordinatesBuilder.append("Class: ").append(key).append(", Coordinates: ").append(coordinatesArray).append("\n");
                    }
                    String coordinates = coordinatesBuilder.toString().trim();

                    // Extract the base64 encoded image string
                    String base64Image = jsonObject.getString("image");

                    // Decode base64 string to byte array
                    byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);

                    // Convert byte array to Bitmap
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    // Update the ImageView to show the decoded bitmap
                    mImageView.post(() -> mImageView.setImageBitmap(decodedBitmap));
                    //textView = findViewById(R.id.textView);

                    // Update the TextView with the keys and coordinates
                    textView.post(() -> textView.setText( coordinates));

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private byte[] convertStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return byteArrayOutputStream.toByteArray();
    }


    private void InitializeSpinner() {
        spino = findViewById(R.id.spinner);
        spino.setOnItemSelectedListener(this);

        ArrayAdapter<String> ad = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                courses);

        ad.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        spino.setAdapter(ad);
    }
}






