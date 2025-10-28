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
import android.os.Bundle;

/**
 * Interface for handling different types of toggle logic
 */
public interface ToggleHandler {
    /**
     * Get the current state of the toggle
     * @param context Application context
     * @param key The key/identifier for this toggle (package name, settings key, etc.)
     * @param extras Additional parameters (e.g., custom enable/disable values)
     * @return true if enabled, false if disabled
     */
    boolean getState(Context context, String key, Bundle extras);

    /**
     * Set the state of the toggle
     * @param context Application context
     * @param key The key/identifier for this toggle
     * @param enabled The new state
     * @param extras Additional parameters (e.g., custom enable/disable values)
     * @return true if successful, false otherwise
     */
    boolean setState(Context context, String key, boolean enabled, Bundle extras);

    /**
     * Check if the toggle is available/functional
     * @param context Application context
     * @param key The key/identifier for this toggle
     * @return true if the toggle can be used, false otherwise
     */
    boolean isAvailable(Context context, String key);
}
