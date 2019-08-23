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
import android.service.quicksettings.TileService;

import net.hyx.app.volumenotification.controller.NotificationServiceController;
import net.hyx.app.volumenotification.controller.TileServiceController;
import net.hyx.app.volumenotification.model.AudioManagerModel;
import net.hyx.app.volumenotification.model.VolumeControlModel;

@TargetApi(Build.VERSION_CODES.N)
abstract public class VolumeTileService extends TileService {

    protected void updateTile(int streamType) {
        TileServiceController tileServiceController = TileServiceController.newInstance(getApplicationContext());
        tileServiceController.updateTile(getQsTile(), streamType);
    }

    protected void adjustVolume(int streamType) {
        AudioManagerModel audioManagerModel = new AudioManagerModel(getApplicationContext());
        audioManagerModel.adjustVolume(streamType);
    }

}