/**
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

#import <Firebase/Firebase.h>
#import <React/RCTUtils.h>

#import "RNENAppModule.h"
#import "RNENJSON.h"
#import "RNENMeta.h"
#import "RNENPreferences.h"
#import "RNENRCTEventEmitter.h"
#import "RNENSharedUtils.h"
#import "RNENVersion.h"

#if __has_include(<FirebaseCore/FIRAppInternal.h>)
#import <FirebaseCore/FIRAppInternal.h>
#define REGISTER_LIB
#endif

@implementation RNENAppModule

#pragma mark -
#pragma mark Module Setup

RCT_EXPORT_MODULE();

- (dispatch_queue_t)methodQueue {
  return dispatch_get_main_queue();
}

- (void)setBridge:(RCTBridge *)bridge {
  [RNENRCTEventEmitter shared].bridge = bridge;
}

- (RCTBridge *)bridge {
  return [RNENRCTEventEmitter shared].bridge;
}

- (id)init {
  if (self = [super init]) {
#ifdef REGISTER_LIB
    static dispatch_once_t once;
    dispatch_once(&once, ^{
      [FIRApp registerLibrary:@"react-native-enhanced-notifications" withVersion:[RNENVersionString copy]];
    });
#endif
    if ([[RNENJSON shared] contains:@"app_log_level"]) {
      NSString *logLevel = [[RNENJSON shared] getStringValue:@"app_log_level" defaultValue:@"info"];
      [self setLogLevel:logLevel];
    }
  }

  return self;
}

- (void)invalidate {
  [[RNENRCTEventEmitter shared] invalidate];
}

#pragma mark -
#pragma mark META Methods

RCT_EXPORT_METHOD(metaGetAll
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
  resolve([RNENMeta getAll]);
}

#pragma mark -
#pragma mark JSON Methods

RCT_EXPORT_METHOD(jsonGetAll
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
  resolve([[RNENJSON shared] getAll]);
}

#pragma mark -
#pragma mark Preference Methods

RCT_EXPORT_METHOD(preferencesSetBool
                  : (NSString *)key boolValue
                  : (BOOL)boolValue resolver
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
  [[RNENPreferences shared] setBooleanValue:key boolValue:boolValue];
  resolve([NSNull null]);
}

RCT_EXPORT_METHOD(preferencesSetString
                  : (NSString *)key stringValue
                  : (NSString *)stringValue resolver
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
  [[RNENPreferences shared] setStringValue:key stringValue:stringValue];
  resolve([NSNull null]);
}

RCT_EXPORT_METHOD(preferencesGetAll
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
  resolve([[RNENPreferences shared] getAll]);
}

RCT_EXPORT_METHOD(preferencesClearAll
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
  [[RNENPreferences shared] clearAll];
  resolve([NSNull null]);
}

#pragma mark -
#pragma mark Event Methods

RCT_EXPORT_METHOD(eventsNotifyReady : (BOOL)ready) {
  [[RNENRCTEventEmitter shared] notifyJsReady:ready];
}

RCT_EXPORT_METHOD(eventsGetListeners
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
  resolve([[RNENRCTEventEmitter shared] getListenersDictionary]);
}

RCT_EXPORT_METHOD(eventsPing
                  : (NSString *)eventName eventBody
                  : (NSDictionary *)eventBody resolver
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
  [[RNENRCTEventEmitter shared] sendEventWithName:eventName body:eventBody];
  resolve(eventBody);
}

RCT_EXPORT_METHOD(eventsAddListener : (NSString *)eventName) {
  [[RNENRCTEventEmitter shared] addListener:eventName];
}

RCT_EXPORT_METHOD(eventsRemoveListener : (NSString *)eventName all : (BOOL)all) {
  [[RNENRCTEventEmitter shared] removeListeners:eventName all:all];
}

#pragma mark -
#pragma mark Events Unused

RCT_EXPORT_METHOD(addListener : (NSString *)eventName) {
  // Keep: Required for RN built in Event Emitter Calls.
}

RCT_EXPORT_METHOD(removeListeners : (NSInteger)count) {
  // Keep: Required for RN built in Event Emitter Calls.
}

#pragma mark -
#pragma mark Firebase App Methods

RCT_EXPORT_METHOD(initializeApp
                  : (FIROptions *)firOptions appConfig
                  : (NSDictionary *)appConfig resolver
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
  RCTUnsafeExecuteOnMainQueueSync(^{
    FIRApp *firApp;
    NSString *appName = [appConfig valueForKey:@"name"];

    @try {
      if (!appName || [appName isEqualToString:DEFAULT_HON_APP_DISPLAY_NAME]) {
        [FIRApp configureWithOptions:firOptions];
        firApp = [FIRApp defaultApp];
      } else {
        [FIRApp configureWithName:appName options:firOptions];
        firApp = [FIRApp appNamed:appName];
      }
    } @catch (NSException *exception) {
      return [RNENSharedUtils rejectPromiseWithExceptionDict:reject exception:exception];
    }

    firApp.dataCollectionDefaultEnabled =
        (BOOL)[appConfig valueForKey:@"automaticDataCollectionEnabled"];

    resolve([RNENSharedUtils firAppToDictionary:firApp]);
  });
}

RCT_EXPORT_METHOD(setLogLevel : (NSString *)logLevel) {
  int level = FIRLoggerLevelError;
  if ([logLevel isEqualToString:@"verbose"]) {
    level = FIRLoggerLevelDebug;
  } else if ([logLevel isEqualToString:@"debug"]) {
    level = FIRLoggerLevelDebug;
  } else if ([logLevel isEqualToString:@"info"]) {
    level = FIRLoggerLevelInfo;
  } else if ([logLevel isEqualToString:@"warn"]) {
    level = FIRLoggerLevelWarning;
  }
  DLog(@"RNENSetLogLevel: setting level to %d from %@.", level, logLevel);
  [[FIRConfiguration sharedInstance] setLoggerLevel:level];
}

RCT_EXPORT_METHOD(setAutomaticDataCollectionEnabled : (FIRApp *)firApp enabled : (BOOL)enabled) {
  if (firApp) {
    firApp.dataCollectionDefaultEnabled = enabled;
  }
}

RCT_EXPORT_METHOD(deleteApp
                  : (FIRApp *)firApp resolver
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
  if (!firApp) {
    return resolve([NSNull null]);
  }

  [firApp deleteApp:^(BOOL success) {
    if (success) {
      resolve([NSNull null]);
    } else {
      // try again once more
      [firApp deleteApp:^(BOOL success2) {
        if (success2) {
          resolve([NSNull null]);
        } else {
          // TODO js error builder
          reject(@"app/delete-app-failed", @"Failed to delete the specified app.", nil);
        }
      }];
    }
  }];
}

- (NSDictionary *)constantsToExport {
  NSDictionary *firApps = [FIRApp allApps];
  NSMutableArray *appsArray = [NSMutableArray new];
  NSMutableDictionary *constants = [NSMutableDictionary new];

  for (id key in firApps) {
    [appsArray addObject:[RNENSharedUtils firAppToDictionary:firApps[key]]];
  }

  constants[@"NATIVE_FIREBASE_APPS"] = appsArray;

  constants[@"FIREBASE_RAW_JSON"] = [[RNENJSON shared] getRawJSON];

  return constants;
}

+ (BOOL)requiresMainQueueSetup {
  return YES;
}
@end
