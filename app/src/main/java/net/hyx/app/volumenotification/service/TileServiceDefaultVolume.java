/*
 * Copyright 2017 https://github.com/seht
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.hyx.app.volumenotification.service;

import android.annotation.TargetApi;
import android.media.AudioManager;
import android.os.Build;

import net.hyx.app.volumenotification.controller.NotificationServiceController;

@TargetApi(Build.VERSION_CODES.N)
public class TileServiceDefaultVolume extends VolumeTileService {

    private static final int STREAM_TYPE = AudioManager.STREAM_SYSTEM;

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        updateTile(STREAM_TYPE);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile(STREAM_TYPE);
    }

    @Override
    public void onClick() {
        super.onClick();
        adjustVolume(STREAM_TYPE);
    }

}
