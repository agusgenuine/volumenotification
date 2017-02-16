/*
 * Copyright (C) 2017 Seht (R) Hyx Ltd.
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

package net.hyx.app.volumenotification.factory;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import net.hyx.app.volumenotification.R;
import net.hyx.app.volumenotification.entity.ButtonsItem;
import net.hyx.app.volumenotification.model.ButtonsModel;
import net.hyx.app.volumenotification.model.SettingsModel;
import net.hyx.app.volumenotification.receiver.SetVolumeReceiver;

import java.util.List;

public class NotificationFactory {

    public static final String EXTRA_ITEM_ID = "item_id";

    private static final int STREAM_BLUETOOTH = 6;
    private static final int[] STREAM_TYPES = {
            AudioManager.STREAM_MUSIC,
            AudioManager.STREAM_VOICE_CALL,
            AudioManager.STREAM_RING,
            AudioManager.STREAM_ALARM,
            AudioManager.STREAM_NOTIFICATION,
            AudioManager.STREAM_SYSTEM,
            STREAM_BLUETOOTH
    };
    private static boolean _mute = false;
    private static boolean _silent = false;
    private static String _package;
    private final Context context;
    private final NotificationManager manager;
    private final AudioManager audio;
    private final SettingsModel settings;
    private final ButtonsModel model;
    private List<ButtonsItem> items;

    private NotificationFactory(Context context) {
        this.context = context;
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        settings = new SettingsModel(context);
        model = new ButtonsModel(context);
        items = model.getButtonList();
        _package = context.getPackageName();
    }

    public static NotificationFactory newInstance(Context context) {
        return new NotificationFactory(context);
    }

    public void setVolume(int id) {
        int index = id - 1;
        int type = STREAM_TYPES[index];
        int direction = AudioManager.ADJUST_SAME;

        if (type == AudioManager.STREAM_MUSIC && settings.getToggleMute()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                direction = AudioManager.ADJUST_TOGGLE_MUTE;
            } else {
                _mute = !_mute;
                audio.setStreamMute(type, _mute);
            }
        } else if (type == AudioManager.STREAM_RING && settings.getToggleSilent()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                direction = AudioManager.ADJUST_TOGGLE_MUTE;
            } else {
                _silent = !_silent;
                audio.setStreamMute(type, _silent);
            }
        }
        audio.adjustStreamVolume(type, direction, AudioManager.FLAG_SHOW_UI);
    }

    public void create() {
        cancel();
        if (settings.getEnabled()) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setOngoing(true)
                    .setPriority(getPriority())
                    .setVisibility(getVisibility())
                    .setCustomContentView(getCustomContentView())
                    .setSmallIcon((settings.getHideStatus()) ? android.R.color.transparent : R.drawable.ic_launcher);
            manager.notify(1, builder.build());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            for (int pos = 0; pos < items.size(); pos++) {
                ButtonsItem item = items.get(pos);
                TileService.requestListeningState(context, new ComponentName("net.hyx.app.volumenotification.service", "TileService" + item.id));
            }
        }
    }

    public void cancel() {
        manager.cancelAll();
    }

    @TargetApi(Build.VERSION_CODES.N)
    public void updateTile(Tile tile, int id) {
        ButtonsItem item = model.getParseButtonItem(id);
        if (item != null) {
            tile.setIcon(Icon.createWithResource(context, model.getButtonIconDrawable(item.icon)));
            tile.setLabel(item.label);
            if (item.status > 0) {
                tile.setState(Tile.STATE_ACTIVE);
            } else {
                tile.setState(Tile.STATE_INACTIVE);
            }
            tile.updateTile();
        }
    }

    private int getPriority() {
        if (settings.getTopPriority()) {
            return NotificationCompat.PRIORITY_MAX;
        }
        return NotificationCompat.PRIORITY_MIN;
    }

    private int getVisibility() {
        if (settings.getHideLocked()) {
            return NotificationCompat.VISIBILITY_SECRET;
        }
        return NotificationCompat.VISIBILITY_PUBLIC;
    }

    private RemoteViews getCustomContentView() {

        RemoteViews view = new RemoteViews(_package, R.layout.view_layout_notification);

        int theme = settings.resources.getIdentifier("style_" + settings.getTheme(), "style", _package);
        int backgroundColor;
        int iconColor;

        if (theme != 0) {
            TypedArray attrs = context.obtainStyledAttributes(theme, R.styleable.styleable);
            backgroundColor = attrs.getColor(R.styleable.styleable_background_color, 0);
            iconColor = attrs.getColor(R.styleable.styleable_icon_color, 0);
            attrs.recycle();
        } else {
            backgroundColor = settings.getColor(settings.getCustomThemeBackgroundColor());
            iconColor = settings.getColor(settings.getCustomThemeIconColor());
        }
        if (settings.getTranslucent()) {
            backgroundColor = android.R.color.transparent;
        }
        view.setInt(R.id.layout_background, "setBackgroundColor", backgroundColor);
        view.removeAllViews(R.id.layout_buttons);

        for (int pos = 0; pos < items.size(); pos++) {
            ButtonsItem item = model.getParseButtonItem(items.get(pos));
            if (item.status < 1) {
                continue;
            }
            int btnId = settings.resources.getIdentifier("btn_id_" + item.id, "id", _package);
            RemoteViews btn = new RemoteViews(_package, settings.resources.getIdentifier("view_btn_id_" + item.id, "layout", _package));
            Intent intent = new Intent(context, SetVolumeReceiver.class).putExtra(EXTRA_ITEM_ID, item.id);
            PendingIntent event = PendingIntent.getBroadcast(context, item.id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            btn.setOnClickPendingIntent(btnId, event);
            btn.setInt(btnId, "setImageResource", model.getButtonIconDrawable(item.icon));
            btn.setInt(btnId, "setColorFilter", iconColor);
            view.addView(R.id.layout_buttons, btn);
        }

        return view;
    }

}