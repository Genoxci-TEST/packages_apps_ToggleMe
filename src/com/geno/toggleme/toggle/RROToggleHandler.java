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
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

/**
 * Generic handler for RRO (Runtime Resource Overlay) toggles
 */
public class RROToggleHandler implements ToggleHandler {
    private static final String TAG = "RROToggleHandler";
    private final IOverlayManager mOverlayManager;

    public RROToggleHandler() {
        mOverlayManager = IOverlayManager.Stub.asInterface(
                ServiceManager.getService(Context.OVERLAY_SERVICE));
    }

    @Override
    public boolean getState(Context context, String packageName, Bundle extras) {
        try {
            OverlayInfo info = mOverlayManager.getOverlayInfo(packageName, 0);
            return info != null && info.isEnabled();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get overlay info for " + packageName, e);
            return false;
        }
    }

    @Override
    public boolean setState(Context context, String packageName, boolean enabled, Bundle extras) {
        try {
            boolean success = mOverlayManager.setEnabled(packageName, enabled, 0);
            if (!success) {
                Log.e(TAG, "Failed to set RRO state for " + packageName);
            }
            return success;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to set overlay state for " + packageName, e);
            return false;
        }
    }

    @Override
    public boolean isAvailable(Context context, String packageName) {
        try {
            OverlayInfo info = mOverlayManager.getOverlayInfo(packageName, 0);
            return info != null;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to check overlay availability for " + packageName, e);
            return false;
        }
    }
}
