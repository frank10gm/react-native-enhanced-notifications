package it.enhancers.firebase.messaging;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.facebook.react.HeadlessJsTaskService;
import com.google.firebase.messaging.RemoteMessage;

import it.enhancers.firebase.app.ReactNativeEnhancedNotificationsApp;
import it.enhancers.firebase.common.ReactNativeEnhancedNotificationsEventEmitter;
import it.enhancers.firebase.common.SharedUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ReactNativeEnhancedNotificationsMessagingReceiver extends BroadcastReceiver {
  private static final String TAG = "RNFirebaseMsgReceiver";
  static HashMap<String, RemoteMessage> notifications = new HashMap<>();

  private Class getMainActivityClass(Context context) {
    String packageName = context.getPackageName();
    Intent launchIntent = context
      .getPackageManager()
      .getLaunchIntentForPackage(packageName);

    try {
      return Class.forName(launchIntent.getComponent().getClassName());
    } catch (ClassNotFoundException e) {
      Log.e(TAG, "Failed to get main activity class", e);
      return null;
    }
  }

  public void createNotification(String title,
                                 String body,
                                 String image,
                                 Context context,
                                 RemoteMessage remoteMessage
  ) {
    final int NOTIFY_ID = 1002;

    String name = "MKT Channel";
    String id = "mkt-channel";
    String description = "MKT Cloud notification channel";

    Intent intent;
    PendingIntent pendingIntent;
    NotificationCompat.Builder builder;
    Bitmap remotePicture = null;
    NotificationCompat.BigPictureStyle notiStyle = new NotificationCompat.BigPictureStyle();

    try {
      remotePicture = BitmapFactory.decodeStream((InputStream) new URL(image).getContent());
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (remotePicture != null)
      notiStyle.bigPicture(remotePicture);

    NotificationManagerCompat notifManager = NotificationManagerCompat.from(context);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      int importance = NotificationManager.IMPORTANCE_HIGH;
      NotificationChannel mChannel = notifManager.getNotificationChannel(id);
      if (mChannel == null) {
        mChannel = new NotificationChannel(id, name, importance);
        mChannel.setDescription(description);
        mChannel.enableVibration(true);
        mChannel.setLightColor(Color.GREEN);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        notifManager.createNotificationChannel(mChannel);
      }

      builder = new NotificationCompat.Builder(context, id);

      intent = new Intent(context, getMainActivityClass(context));
      intent.putExtra("message", remoteMessage);
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
      pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


      builder.setContentTitle(title)  // required
        .setSmallIcon(context
          .getResources()
          .getIdentifier("ic_notification", "drawable", context.getPackageName())
        ) // required
        .setContentText(body)  // required
        .setDefaults(Notification.DEFAULT_ALL)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .setTicker(title)
        .setStyle(remotePicture == null ? null : notiStyle)
        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
    } else {

      builder = new NotificationCompat.Builder(context, id);

      intent = new Intent(context, getMainActivityClass(context));
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
      intent.putExtra("message", remoteMessage);
      pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

      builder.setContentTitle(title)                           // required
        .setSmallIcon(context
          .getResources()
          .getIdentifier("ic_notification", "drawable", context.getPackageName())
        ) // required
        .setContentText(body)  // required
        .setDefaults(Notification.DEFAULT_ALL)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .setTicker(title)
        .setStyle(remotePicture == null ? null : notiStyle)
        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
        .setPriority(Notification.PRIORITY_HIGH);
    }

    Notification notification = builder.build();
    int oneTimeID = (int) SystemClock.uptimeMillis();
    notifManager.notify(oneTimeID, notification);
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d(TAG, "broadcast received for message");
    Context actualContext;
    if (ReactNativeEnhancedNotificationsApp.getApplicationContext() == null) {
      ReactNativeEnhancedNotificationsApp.setApplicationContext(context.getApplicationContext());
      actualContext = context.getApplicationContext();
    } else {
      actualContext = ReactNativeEnhancedNotificationsApp.getApplicationContext();
    }
    RemoteMessage remoteMessage = new RemoteMessage(intent.getExtras());
    ReactNativeEnhancedNotificationsEventEmitter emitter = ReactNativeEnhancedNotificationsEventEmitter.getSharedInstance();
    @NonNull Map<String, String> messageMkt = remoteMessage.getData();

    // Add a RemoteMessage if the message contains a notification payload
    if (remoteMessage.getNotification() != null) {
      notifications.put(remoteMessage.getMessageId(), remoteMessage);
      ReactNativeEnhancedNotificationsMessagingStoreHelper.getInstance()
        .getMessagingStore()
        .storeFirebaseMessage(remoteMessage);
    }

    //  |-> ---------------------
    //      App in Foreground
    //   ------------------------
    if (SharedUtils.isAppInForeground(context)) {
      emitter.sendEvent(
        ReactNativeEnhancedNotificationsMessagingSerializer.remoteMessageToEvent(remoteMessage, false));
      return;
    }

    // SFMC
    if ("SFMC".equalsIgnoreCase(messageMkt.get("_sid"))) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        createNotification(messageMkt.get("title"), messageMkt.get("alert"), messageMkt.get("_mediaUrl"), actualContext, remoteMessage);
      }
      return;
    }

    //  |-> ---------------------
    //    App in Background/Quit
    //   ------------------------

    try {
      Intent backgroundIntent =
        new Intent(context, ReactNativeEnhancedNotificationsMessagingHeadlessService.class);
      backgroundIntent.putExtra("message", remoteMessage);
      ComponentName name = context.startService(backgroundIntent);
      if (name != null) {
        HeadlessJsTaskService.acquireWakeLockNow(context);
      }
    } catch (IllegalStateException ex) {
      // By default, data only messages are "default" priority and cannot trigger Headless tasks
      Log.e(TAG, "Background messages only work if the message priority is set to 'high'", ex);
    }
  }
}
