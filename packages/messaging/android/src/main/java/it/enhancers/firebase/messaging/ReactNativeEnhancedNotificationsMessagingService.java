package it.enhancers.firebase.messaging;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import it.enhancers.firebase.common.ReactNativeEnhancedNotificationsEventEmitter;

public class ReactNativeEnhancedNotificationsMessagingService extends FirebaseMessagingService {
  @Override
  public void onSendError(String messageId, Exception sendError) {
    ReactNativeEnhancedNotificationsEventEmitter emitter = ReactNativeEnhancedNotificationsEventEmitter.getSharedInstance();
    emitter.sendEvent(
        ReactNativeEnhancedNotificationsMessagingSerializer.messageSendErrorToEvent(messageId, sendError));
  }

  @Override
  public void onDeletedMessages() {
    ReactNativeEnhancedNotificationsEventEmitter emitter = ReactNativeEnhancedNotificationsEventEmitter.getSharedInstance();
    emitter.sendEvent(ReactNativeEnhancedNotificationsMessagingSerializer.messagesDeletedToEvent());
  }

  @Override
  public void onMessageSent(String messageId) {
    ReactNativeEnhancedNotificationsEventEmitter emitter = ReactNativeEnhancedNotificationsEventEmitter.getSharedInstance();
    emitter.sendEvent(ReactNativeEnhancedNotificationsMessagingSerializer.messageSentToEvent(messageId));
  }

  @Override
  public void onNewToken(String token) {
    ReactNativeEnhancedNotificationsEventEmitter emitter = ReactNativeEnhancedNotificationsEventEmitter.getSharedInstance();
    emitter.sendEvent(ReactNativeEnhancedNotificationsMessagingSerializer.newTokenToTokenEvent(token));
  }

  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {
    // noop - handled in receiver
  }
}
