package it.enhancers.firebase.messaging;

import com.facebook.react.bridge.WritableMap;
import com.google.firebase.messaging.RemoteMessage;

public interface ReactNativeEnhancedNotificationsMessagingStore {
  void storeFirebaseMessage(RemoteMessage remoteMessage);

  @Deprecated
  RemoteMessage getFirebaseMessage(String remoteMessageId);

  WritableMap getFirebaseMessageMap(String remoteMessageId);

  void clearFirebaseMessage(String remoteMessageId);
}
