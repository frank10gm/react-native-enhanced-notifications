package it.enhancers.firebase.messaging;

import android.content.Intent;
import android.os.Bundle;
import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.google.firebase.messaging.RemoteMessage;
import it.enhancers.firebase.common.ReactNativeEnhancedNotificationsJSON;
import javax.annotation.Nullable;

public class ReactNativeEnhancedNotificationsMessagingHeadlessService extends HeadlessJsTaskService {
  private static final long TIMEOUT_DEFAULT = 60000;
  private static final String TIMEOUT_JSON_KEY = "messaging_android_headless_task_timeout";
  private static final String TASK_KEY = "ReactNativeEnhancedNotificationsMessagingHeadlessTask";

  @Override
  protected @Nullable HeadlessJsTaskConfig getTaskConfig(Intent intent) {
    Bundle extras = intent.getExtras();
    if (extras == null) return null;
    RemoteMessage remoteMessage = intent.getParcelableExtra("message");

    return new HeadlessJsTaskConfig(
        TASK_KEY,
        ReactNativeEnhancedNotificationsMessagingSerializer.remoteMessageToWritableMap(remoteMessage),
        ReactNativeEnhancedNotificationsJSON.getSharedInstance().getLongValue(TIMEOUT_JSON_KEY, TIMEOUT_DEFAULT),
        // Prevents race condition where the user opens the app at the same time as a notification
        // is delivered, causing a crash.
        true);
  }
}
