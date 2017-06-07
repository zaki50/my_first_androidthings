/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.androidthings.myproject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.contrib.driver.bmx280.Bmx280;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;
import com.google.android.things.pio.Gpio;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * Skeleton of the main Android Things activity. Implement your device's logic
 * in this class.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 */
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Handler handler;
    private Gpio led;

    private Bmx280 sensor;
    AlphanumericDisplay display;
    private boolean ledOn;

    private boolean done;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        handler = new Handler(getMainLooper());

        try {
            sensor = RainbowHat.openSensor();
            display = RainbowHat.openDisplay();
            sensor.setMode(Bmx280.MODE_NORMAL);
            sensor.setTemperatureOversampling(Bmx280.OVERSAMPLING_1X);
            display.setEnabled(true);

            led = RainbowHat.openLedGreen();
            ledOn = false;
            runAndReschedule();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // とりあえず5秒後に自動的に終了
        handler.postDelayed(this::finish, TimeUnit.SECONDS.toMillis(5));
    }

    private void runAndReschedule() {
        if (done) {
            return;
        }

        try {
            display.display(sensor.readTemperature());

            led.setValue(ledOn);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ledOn = !ledOn;

        handler.postDelayed(this::runAndReschedule, 300);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        done = true;

        try {
            led.setValue(false);
            led.close();
            led = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            display.clear();
            display.close();
            display = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            sensor.close();
            sensor = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
