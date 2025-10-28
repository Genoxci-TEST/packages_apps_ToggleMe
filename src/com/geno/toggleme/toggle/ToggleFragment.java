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
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Log;

import com.android.settingslib.widget.SettingsBasePreferenceFragment;

import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.geno.toggleme.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToggleFragment extends SettingsBasePreferenceFragment {

    private static final String TAG = "ToggleFragment";
    
    // Handler instances
    private final Map<String, ToggleHandler> mHandlers = new HashMap<>();

    private static class ToggleInfo {
        int order;
        SwitchPreferenceCompat preference;

        ToggleInfo(int order, SwitchPreferenceCompat preference) {
            this.order = order;
            this.preference = preference;
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Initialize handlers
        mHandlers.put("rro", new RROToggleHandler());
        mHandlers.put("settings_secure", new SettingsSecureToggleHandler());
        mHandlers.put("write", new WriteToggleHandler());

        Context context = getContext();
        if (context == null) {
            Log.e(TAG, "Context is null");
            return;
        }

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);
        setPreferenceScreen(screen);

        PreferenceCategory currentCategory = null;
        List<ToggleInfo> currentToggles = new ArrayList<>();

        try {
            XmlResourceParser parser = context.getResources().getXml(R.xml.toggles_list);
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();
                    
                    if ("category".equals(tagName)) {
                        // Add sorted toggles from previous category
                        if (currentCategory != null && !currentToggles.isEmpty()) {
                            addSortedToggles(currentCategory, currentToggles);
                            currentToggles.clear();
                        }

                        int titleResId = parser.getAttributeResourceValue(null, "title", 0);
                        String titleString = parser.getAttributeValue(null, "title");
                        currentCategory = new PreferenceCategory(context);
                        
                        if (titleResId != 0) {
                            try {
                                currentCategory.setTitle(context.getString(titleResId));
                            } catch (Resources.NotFoundException e) {
                                Log.w(TAG, "Resource not found for category title: " + titleResId);
                            }
                        } else if (titleString != null) {
                            currentCategory.setTitle(titleString);
                        }
                        screen.addPreference(currentCategory);
                        
                    } else if ("toggle".equals(tagName)) {
                        int titleResId = parser.getAttributeResourceValue(null, "title", 0);
                        int summaryResId = parser.getAttributeResourceValue(null, "summary", 0);
                        String type = parser.getAttributeValue(null, "type");
                        int enabledResId = parser.getAttributeResourceValue(null, "enabled", 0);
                        int order = parser.getAttributeIntValue(null, "order", 0);
                        
                        // RRO-specific attributes
                        String packageName = parser.getAttributeValue(null, "package");
                        
                        // Settings.Secure-specific attributes
                        int targetResId = parser.getAttributeResourceValue(null, "target", 0);
                        int enableValueResId = parser.getAttributeResourceValue(null, "enable_value", 0);
                        int disableValueResId = parser.getAttributeResourceValue(null, "disable_value", 0);
                        
                        // Write-specific attributes
                        int pathResId = parser.getAttributeResourceValue(null, "path", 0);

                        boolean enabled = true;
                        if (enabledResId != 0) {
                            enabled = getResources().getBoolean(enabledResId);
                        }

                        if (enabled && type != null) {
                            String toggleKey = null;
                            String enableValue = null;
                            String disableValue = null;
                            
                            // Determine the key based on toggle type
                            if ("rro".equals(type)) {
                                toggleKey = packageName;
                            } else if ("settings_secure".equals(type) && targetResId != 0) {
                                toggleKey = context.getString(targetResId);
                                if (enableValueResId != 0) {
                                    enableValue = context.getString(enableValueResId);
                                }
                                if (disableValueResId != 0) {
                                    disableValue = context.getString(disableValueResId);
                                }
                            } else if ("write".equals(type) && pathResId != 0) {  
                                toggleKey = context.getString(pathResId);
                                if (enableValueResId != 0) {
                                    enableValue = context.getString(enableValueResId);
                                }
                                if (disableValueResId != 0) {
                                    disableValue = context.getString(disableValueResId);
                                }
                            }

                            if (toggleKey != null) {
                                SwitchPreferenceCompat toggle = createTogglePreference(
                                        context, titleResId, summaryResId, type, toggleKey, 
                                        enableValue, disableValue);
                                if (toggle != null) {
                                    currentToggles.add(new ToggleInfo(order, toggle));
                                }
                            }
                        }
                    }
                }
                eventType = parser.next();
            }

            // Add sorted toggles from last category
            if (currentCategory != null && !currentToggles.isEmpty()) {
                addSortedToggles(currentCategory, currentToggles);
            }

        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "Error parsing XML", e);
        }
    }

    private void addSortedToggles(PreferenceCategory category, List<ToggleInfo> toggles) {
        Collections.sort(toggles, new Comparator<ToggleInfo>() {
            @Override
            public int compare(ToggleInfo t1, ToggleInfo t2) {
                return Integer.compare(t1.order, t2.order);
            }
        });

        for (ToggleInfo toggleInfo : toggles) {
            category.addPreference(toggleInfo.preference);
        }
    }

    private SwitchPreferenceCompat createTogglePreference(Context context, int titleResId, 
            int summaryResId, String type, String key, String enableValue, String disableValue) {
        ToggleHandler handler = mHandlers.get(type);
        if (handler == null) {
            Log.e(TAG, "Unknown toggle type: " + type);
            return null;
        }

        SwitchPreferenceCompat toggle = new SwitchPreferenceCompat(context);
        
        // Set title and summary
        try {
            toggle.setTitle(context.getString(titleResId));
            if (summaryResId != 0) {
                toggle.setSummary(context.getString(summaryResId));
            }
        } catch (Resources.NotFoundException e) {
            Log.w(TAG, "Resource not found for title or summary: " + titleResId + " / " + summaryResId);
        }

        toggle.setKey(key);

        // Store custom values if provided (for settings_secure)
        if (enableValue != null) {
            toggle.getExtras().putString("enable_value", enableValue);
        }
        if (disableValue != null) {
            toggle.getExtras().putString("disable_value", disableValue);
        }

        // Check availability and set initial state
        if (!handler.isAvailable(context, key)) {
            toggle.setEnabled(false);
            Log.w(TAG, "Toggle not available: " + key);
        } else {
            Bundle extras = toggle.getExtras();
            boolean currentState = handler.getState(context, key, extras);
            toggle.setChecked(currentState);
        }

        // Set change listener
        toggle.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean enabled = (boolean) newValue;
            Bundle extras = ((SwitchPreferenceCompat) preference).getExtras();
            return handler.setState(context, key, enabled, extras);
        });

        return toggle;
    }
}
