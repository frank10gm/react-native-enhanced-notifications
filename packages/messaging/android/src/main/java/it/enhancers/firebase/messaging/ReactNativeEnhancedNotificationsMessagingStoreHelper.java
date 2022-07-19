package it.enhancers.firebase.messaging;

public class ReactNativeEnhancedNotificationsMessagingStoreHelper {

  private ReactNativeEnhancedNotificationsMessagingStore messagingStore;

  private ReactNativeEnhancedNotificationsMessagingStoreHelper() {
    messagingStore = new ReactNativeEnhancedNotificationsMessagingStoreImpl();
  }

  private static ReactNativeEnhancedNotificationsMessagingStoreHelper _instance;

  public static ReactNativeEnhancedNotificationsMessagingStoreHelper getInstance() {
    if (_instance == null) {
      _instance = new ReactNativeEnhancedNotificationsMessagingStoreHelper();
    }
    return _instance;
  }

  public ReactNativeEnhancedNotificationsMessagingStore getMessagingStore() {
    return messagingStore;
  }
}
