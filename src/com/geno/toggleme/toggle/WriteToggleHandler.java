/*
 * Copyright (C) 2025 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.geno.toggleme.toggle;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Generic handler for file-based toggles using echo-style writes
 * Supports custom enable/disable values via preference extras
 */
public class WriteToggleHandler implements ToggleHandler {
    private static final String TAG = "WriteToggleHandler";
    private static final String PREF_PREFIX = "write_toggle_state_";

    @Override
    public boolean getState(Context context, String path, Bundle extras) {
        if (path == null || path.isEmpty()) {
            Log.e(TAG, "Path is null or empty");
            return false;
        }

        // Since we can't reliably read the current state from most sysfs nodes,
        // we track the state in SharedPreferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String prefKey = PREF_PREFIX + path;
        boolean state = prefs.getBoolean(prefKey, false);
        
        Log.d(TAG, "Get state for " + path + ": " + state);
        return state;
    }

    @Override
    public boolean setState(Context context, String path, boolean enabled, Bundle extras) {
        if (path == null || path.isEmpty()) {
            Log.e(TAG, "Path is null or empty");
            return false;
        }

        String enableValue = extras != null ? extras.getString("enable_value") : null;
        String disableValue = extras != null ? extras.getString("disable_value") : null;

        // Default to 1/0 if custom values not provided
        String valueToWrite;
        if (enabled) {
            valueToWrite = enableValue != null ? enableValue : "1";
        } else {
            valueToWrite = disableValue != null ? disableValue : "0";
        }

        Log.d(TAG, "Writing \"" + valueToWrite + "\" to " + path + 
                " (enabled=" + enabled + ")");

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));
            writer.write(valueToWrite);
            writer.close();

            // Save state to SharedPreferences
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String prefKey = PREF_PREFIX + path;
            prefs.edit().putBoolean(prefKey, enabled).apply();

            Log.d(TAG, "Successfully wrote to " + path);
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Failed to write to " + path + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isAvailable(Context context, String path) {
        if (path == null || path.isEmpty()) {
            Log.e(TAG, "Path is null or empty");
            return false;
        }

        try {
            File file = new File(path);
            boolean exists = file.exists();
            boolean canWrite = file.canWrite();
            
            Log.d(TAG, "Checking availability for " + path + 
                    ": exists=" + exists + ", canWrite=" + canWrite);
            
            return exists && canWrite;
        } catch (Exception e) {
            Log.e(TAG, "Error checking availability for " + path + ": " + e.getMessage());
            return false;
        }
    }
}
