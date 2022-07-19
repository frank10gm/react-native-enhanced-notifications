package it.enhancers.firebase.app;

/*
 * Copyright (c) 2016-present Invertase Limited & Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this library except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.google.firebase.FirebaseApp;
import it.enhancers.firebase.common.RCTConvertFirebase;
import it.enhancers.firebase.common.ReactNativeEnhancedNotificationsEvent;
import it.enhancers.firebase.common.ReactNativeEnhancedNotificationsEventEmitter;
import it.enhancers.firebase.common.ReactNativeEnhancedNotificationsJSON;
import it.enhancers.firebase.common.ReactNativeEnhancedNotificationsMeta;
import it.enhancers.firebase.common.ReactNativeEnhancedNotificationsModule;
import it.enhancers.firebase.common.ReactNativeEnhancedNotificationsPreferences;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReactNativeEnhancedNotificationsAppModule extends ReactNativeEnhancedNotificationsModule {
  private static final String TAG = "App";

  ReactNativeEnhancedNotificationsAppModule(ReactApplicationContext reactContext) {
    super(reactContext, TAG);
  }

  @Override
  public void initialize() {
    super.initialize();
    ReactNativeEnhancedNotificationsEventEmitter.getSharedInstance().attachReactContext(getContext());
  }

  @ReactMethod
  public void initializeApp(ReadableMap options, ReadableMap appConfig, Promise promise) {
    FirebaseApp firebaseApp =
        RCTConvertFirebase.readableMapToFirebaseApp(options, appConfig, getContext());

    WritableMap firebaseAppMap = RCTConvertFirebase.firebaseAppToWritableMap(firebaseApp);
    promise.resolve(firebaseAppMap);
  }

  @ReactMethod
  public void setAutomaticDataCollectionEnabled(String appName, Boolean enabled) {
    FirebaseApp firebaseApp = FirebaseApp.getInstance(appName);
    firebaseApp.setDataCollectionDefaultEnabled(enabled);
  }

  @ReactMethod
  public void deleteApp(String appName, Promise promise) {
    FirebaseApp firebaseApp = FirebaseApp.getInstance(appName);

    if (firebaseApp != null) {
      firebaseApp.delete();
    }

    promise.resolve(null);
  }

  @ReactMethod
  public void eventsNotifyReady(Boolean ready) {
    ReactNativeEnhancedNotificationsEventEmitter emitter = ReactNativeEnhancedNotificationsEventEmitter.getSharedInstance();
    emitter.notifyJsReady(ready);
  }

  @ReactMethod
  public void eventsGetListeners(Promise promise) {
    ReactNativeEnhancedNotificationsEventEmitter emitter = ReactNativeEnhancedNotificationsEventEmitter.getSharedInstance();
    promise.resolve(emitter.getListenersMap());
  }

  @ReactMethod
  public void eventsPing(String eventName, ReadableMap eventBody, Promise promise) {
    ReactNativeEnhancedNotificationsEventEmitter emitter = ReactNativeEnhancedNotificationsEventEmitter.getSharedInstance();
    emitter.sendEvent(
        new ReactNativeEnhancedNotificationsEvent(
            eventName, RCTConvertFirebase.readableMapToWritableMap(eventBody)));
    promise.resolve(RCTConvertFirebase.readableMapToWritableMap(eventBody));
  }

  @ReactMethod
  public void eventsAddListener(String eventName) {
    ReactNativeEnhancedNotificationsEventEmitter emitter = ReactNativeEnhancedNotificationsEventEmitter.getSharedInstance();
    emitter.addListener(eventName);
  }

  @ReactMethod
  public void eventsRemoveListener(String eventName, Boolean all) {
    ReactNativeEnhancedNotificationsEventEmitter emitter = ReactNativeEnhancedNotificationsEventEmitter.getSharedInstance();
    emitter.removeListener(eventName, all);
  }

  @ReactMethod
  public void addListener(String eventName) {
    // Keep: Required for RN built in Event Emitter Calls.
  }

  @ReactMethod
  public void removeListeners(Integer count) {
    // Keep: Required for RN built in Event Emitter Calls.
  }

  /** ------------------ META ------------------ */
  @ReactMethod
  public void metaGetAll(Promise promise) {
    promise.resolve(ReactNativeEnhancedNotificationsMeta.getSharedInstance().getAll());
  }

  /** ------------------ JSON ------------------ */
  @ReactMethod
  public void jsonGetAll(Promise promise) {
    promise.resolve(ReactNativeEnhancedNotificationsJSON.getSharedInstance().getAll());
  }

  /** ------------------ PREFERENCES ------------------ */
  @ReactMethod
  public void preferencesSetBool(String key, boolean value, Promise promise) {
    ReactNativeEnhancedNotificationsPreferences.getSharedInstance().setBooleanValue(key, value);
    promise.resolve(null);
  }

  @ReactMethod
  public void preferencesSetString(String key, String value, Promise promise) {
    ReactNativeEnhancedNotificationsPreferences.getSharedInstance().setStringValue(key, value);
    promise.resolve(null);
  }

  @ReactMethod
  public void preferencesGetAll(Promise promise) {
    promise.resolve(ReactNativeEnhancedNotificationsPreferences.getSharedInstance().getAll());
  }

  @ReactMethod
  public void preferencesClearAll(Promise promise) {
    ReactNativeEnhancedNotificationsPreferences.getSharedInstance().clearAll();
    promise.resolve(null);
  }

  @Override
  public Map<String, Object> getConstants() {
    Map<String, Object> constants = new HashMap<>();
    List<Map<String, Object>> appsList = new ArrayList<>();
    List<FirebaseApp> firebaseApps = FirebaseApp.getApps(getReactApplicationContext());

    for (FirebaseApp app : firebaseApps) {
      appsList.add(RCTConvertFirebase.firebaseAppToMap(app));
    }

    constants.put("NATIVE_FIREBASE_APPS", appsList);

    constants.put("FIREBASE_RAW_JSON", ReactNativeEnhancedNotificationsJSON.getSharedInstance().getRawJSON());

    return constants;
  }
}
