package de.blinkt.openvpn;


import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.StringReader;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.StatusListener;
import de.blinkt.openvpn.listeners.OpenVpnConnectionNetstatListener;
import de.blinkt.openvpn.listeners.OpenVpnConnectionStateListener;
import de.blinkt.openvpn.listeners.OpenVpnListener;
import de.blinkt.openvpn.listeners.OpenVpnLogListener;


public class OpenVpnApi {
    public enum Notifier { CONNECTION_STATE, NETSTAT, LOG }

    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
//    private final StatusListener statusListener = new StatusListener();

    @Nullable
    private static OpenVpnConnectionStateListener _stateListener;
    @Nullable
    private static OpenVpnConnectionNetstatListener _netstatListener;
    @Nullable
    private static OpenVpnLogListener _logListener;


    public static void notify(String[] args, Notifier notifierType) {
        switch (notifierType) {
            case CONNECTION_STATE:
                if (_stateListener != null) {
                    mainHandler.post(() -> { _stateListener.onEvent(args[0]); });
                }
                break;
            case NETSTAT:
                if (_netstatListener != null) {
                    mainHandler.post(() -> { _netstatListener.onEvent(args[0], args[1]); });
                }
                break;
            case LOG:
                if (_logListener != null) {
                    mainHandler.post(() -> { _logListener.onEvent(args[0]); });
                }
                break;
        }
    }

    public static <T extends  OpenVpnListener> void setListener(T listener) {
        if (listener instanceof OpenVpnConnectionStateListener) {
            _stateListener = (OpenVpnConnectionStateListener) listener;
        } else if (listener instanceof OpenVpnConnectionNetstatListener) {
            _netstatListener = (OpenVpnConnectionNetstatListener) listener;
        } else if (listener instanceof OpenVpnLogListener) {
            _logListener = (OpenVpnLogListener) listener;
        }
    }


    public static VpnProfile createVpnProfile(Context context, String config, String name, String username, String password, String module) throws RemoteException {
        try {
            ConfigParser cp = new ConfigParser();
            cp.parseConfig(new StringReader(config));
            VpnProfile vp = cp.convertProfile();
            vp.mName = name;
            vp.mNameModule = module;
            if (vp.checkProfile(context) != de.blinkt.openvpn.R.string.no_error_found) {
                throw new RemoteException(context.getString(vp.checkProfile(context)));
            }
            vp.mProfileCreator = context.getPackageName();
            vp.mUsername = username;
            vp.mPassword = password;
            ProfileManager.setTemporaryProfile(context, vp);
            return vp;
        } catch (IOException | ConfigParser.ConfigParseError e) {
            throw new RemoteException(e.getMessage());
        }

    }

    public void initialize(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannels(context);
//        statusListener.init(context);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannels(Context ctx) {
        NotificationManager mNotificationManager =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        // Background message
        CharSequence name = ctx.getString(R.string.channel_name_background);
        NotificationChannel mChannel = new NotificationChannel(OpenVPNService.NOTIFICATION_CHANNEL_BG_ID,
                name, NotificationManager.IMPORTANCE_HIGH);

        mChannel.setDescription(ctx.getString(R.string.channel_description_background));
        mChannel.enableLights(false);

        mChannel.setLightColor(Color.DKGRAY);
        mNotificationManager.createNotificationChannel(mChannel);

        // Connection status change messages
        name = ctx.getString(R.string.channel_name_status);
        mChannel = new NotificationChannel(OpenVPNService.NOTIFICATION_CHANNEL_NEWSTATUS_ID,
                name, NotificationManager.IMPORTANCE_HIGH);

        mChannel.setDescription(ctx.getString(R.string.channel_description_status));
        mChannel.enableLights(true);

        mChannel.setLightColor(Color.BLUE);
        mNotificationManager.createNotificationChannel(mChannel);

        // Urgent requests, e.g. two factor auth
        name = ctx.getString(R.string.channel_name_userreq);
        mChannel = new NotificationChannel(OpenVPNService.NOTIFICATION_CHANNEL_USERREQ_ID,
                name, NotificationManager.IMPORTANCE_HIGH);
        mChannel.setDescription(ctx.getString(R.string.channel_description_userreq));
        mChannel.enableVibration(true);
        mChannel.setLightColor(Color.CYAN);
        mNotificationManager.createNotificationChannel(mChannel);
    }

}
