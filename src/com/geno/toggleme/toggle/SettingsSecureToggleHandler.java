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

package com.geno.extras.toggle;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

/**
 * Generic handler for Settings.Secure toggles
 * Supports custom enable/disable values via preference extras
 */
public class SettingsSecureToggleHandler implements ToggleHandler {
    private static final String TAG = "SettingsSecureHandler";

    @Override
    public boolean getState(Context context, String key, Bundle extras) {
        try {
            // Get custom values from extras if provided
            String enableValue = extras != null ? extras.getString("enable_value") : null;
            String disableValue = extras != null ? extras.getString("disable_value") : null;
            
            // Default to checking if value != 0
            if (enableValue == null && disableValue == null) {
                int value = Settings.Secure.getInt(
                        context.getContentResolver(),
                        key,
                        0
                );
                Log.d(TAG, "Get state for " + key + ": " + value);
                return value != 0;
            }
            
            // Use custom values
            String currentValue = Settings.Secure.getString(
                    context.getContentResolver(),
                    key
            );
            
            Log.d(TAG, "Get state for " + key + ": " + currentValue + 
                    " (enable=" + enableValue + ", disable=" + disableValue + ")");
            
            if (currentValue == null) {
                return false;
            }
            
            // Check against enable value if provided
            if (enableValue != null) {
                return currentValue.equals(enableValue);
            }
            
            // Otherwise check if it's NOT the disable value
            if (disableValue != null) {
                return !currentValue.equals(disableValue);
            }
            
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Failed to get Settings.Secure value for " + key, e);
            return false;
        }
    }

    @Override
    public boolean setState(Context context, String key, boolean enabled, Bundle extras) {
        try {
            String enableValue = extras != null ? extras.getString("enable_value") : null;
            String disableValue = extras != null ? extras.getString("disable_value") : null;
            
            // Use custom values if provided, otherwise default to 1/0
            String valueToSet;
            if (enabled) {
                valueToSet = enableValue != null ? enableValue : "1";
            } else {
                valueToSet = disableValue != null ? disableValue : "0";
            }
            
            Log.d(TAG, "Setting " + key + " to " + valueToSet + 
                    " (enabled=" + enabled + ")");
            
            boolean success = Settings.Secure.putString(
                    context.getContentResolver(),
                    key,
                    valueToSet
            );
            
            if (success) {
                // Verify the write
                String verified = Settings.Secure.getString(
                        context.getContentResolver(),
                        key
                );
                Log.d(TAG, "Verified value for " + key + ": " + verified);
            }
            
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Failed to set Settings.Secure value for " + key, e);
            return false;
        }
    }

    @Override
    public boolean isAvailable(Context context, String key) {
        // Settings.Secure keys are always available, but we can check if readable
        try {
            Settings.Secure.getString(context.getContentResolver(), key);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Settings.Secure key not available: " + key, e);
            return false;
        }
    }
}
