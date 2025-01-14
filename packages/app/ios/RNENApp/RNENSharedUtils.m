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

#import "RNENSharedUtils.h"
#import "RNENJSON.h"
#import "RNENMeta.h"
#import "RNENPreferences.h"
#import "RNENRCTEventEmitter.h"

#pragma mark -
#pragma mark Constants

NSString *const DEFAULT_HON_APP_DISPLAY_NAME = @"[DEFAULT]";
NSString *const DEFAULT_HON_APP_NAME = @"__FIRAPP_DEFAULT";

@implementation RNENSharedUtils
static NSString *const RNENErrorDomain = @"RNENErrorDomain";

#pragma mark -
#pragma mark Methods

+ (NSString *)getAppJavaScriptName:(NSString *)appDisplayName {
  if ([appDisplayName isEqualToString:DEFAULT_HON_APP_NAME]) {
    return DEFAULT_HON_APP_DISPLAY_NAME;
  }
  return appDisplayName;
}

+ (NSDictionary *)firAppToDictionary:(FIRApp *)firApp {
  FIROptions *firOptions = [firApp options];
  NSMutableDictionary *firAppDictionary = [NSMutableDictionary new];
  NSMutableDictionary *firAppOptions = [NSMutableDictionary new];
  NSMutableDictionary *firAppConfig = [NSMutableDictionary new];

  NSString *name = [firApp name];
  if ([name isEqualToString:DEFAULT_HON_APP_NAME]) {
    name = DEFAULT_HON_APP_DISPLAY_NAME;
  }

  firAppConfig[@"name"] = name;
  firAppConfig[@"automaticDataCollectionEnabled"] = @([firApp isDataCollectionDefaultEnabled]);

  firAppOptions[@"apiKey"] = firOptions.APIKey;
  firAppOptions[@"appId"] = firOptions.googleAppID;
  firAppOptions[@"projectId"] = firOptions.projectID;
  firAppOptions[@"databaseURL"] = firOptions.databaseURL;
  firAppOptions[@"gaTrackingId"] = firOptions.trackingID;
  firAppOptions[@"storageBucket"] = firOptions.storageBucket;
  firAppOptions[@"messagingSenderId"] = firOptions.GCMSenderID;
  // missing from android sdk - ios only:
  firAppOptions[@"clientId"] = firOptions.clientID;
  firAppOptions[@"androidClientID"] = firOptions.androidClientID;
  firAppOptions[@"deepLinkUrlScheme"] = firOptions.deepLinkURLScheme;

  firAppDictionary[@"options"] = firAppOptions;
  firAppDictionary[@"appConfig"] = firAppConfig;

  return firAppDictionary;
}

+ (void)rejectPromiseWithExceptionDict:(RCTPromiseRejectBlock)reject
                             exception:(NSException *)exception {
  NSMutableDictionary *userInfo = [NSMutableDictionary dictionary];

  [userInfo setValue:@(YES) forKey:@"fatal"];
  [userInfo setValue:@"unknown" forKey:@"code"];
  [userInfo setValue:exception.reason forKey:@"message"];
  [userInfo setValue:exception.name forKey:@"nativeErrorCode"];
  [userInfo setValue:exception.reason forKey:@"nativeErrorMessage"];

  NSError *error = [NSError errorWithDomain:RNENErrorDomain code:666 userInfo:userInfo];

  reject(exception.name, exception.reason, error);
}

+ (void)rejectPromiseWithNSError:(RCTPromiseRejectBlock)reject error:(NSError *)error {
  NSMutableDictionary *userInfo = [NSMutableDictionary dictionary];

  [userInfo setValue:@(NO) forKey:@"fatal"];
  [userInfo setValue:@"unknown" forKey:@"code"];
  [userInfo setValue:error.localizedDescription forKey:@"message"];
  [userInfo setValue:@(error.code) forKey:@"nativeErrorCode"];
  [userInfo setValue:error.localizedDescription forKey:@"nativeErrorMessage"];

  NSError *newErrorWithUserInfo = [NSError errorWithDomain:RNENErrorDomain
                                                      code:666
                                                  userInfo:userInfo];
  reject(@"unknown", error.localizedDescription, newErrorWithUserInfo);
}

+ (void)rejectPromiseWithUserInfo:(RCTPromiseRejectBlock)reject
                         userInfo:(NSMutableDictionary *)userInfo {
  NSError *error = [NSError errorWithDomain:RNENErrorDomain code:666 userInfo:userInfo];
  reject(userInfo[@"code"], userInfo[@"message"], error);
}

// for easier v5 migration
+ (void)sendJSEventForApp:(FIRApp *)app name:(NSString *)name body:(NSDictionary *)body {
  NSMutableDictionary *newBody = [body mutableCopy];
  newBody[@"appName"] = [self getAppJavaScriptName:app.name];
  [[RNENRCTEventEmitter shared] sendEventWithName:name body:newBody];
}

+ (NSString *)getISO8601String:(NSDate *)date {
  static NSDateFormatter *formatter = nil;

  if (!formatter) {
    formatter = [[NSDateFormatter alloc] init];
    [formatter setLocale:[NSLocale localeWithLocaleIdentifier:@"en_US_POSIX"]];
    formatter.timeZone = [NSTimeZone timeZoneWithAbbreviation:@"UTC"];
    [formatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss"];
  }

  NSString *iso8601String = [formatter stringFromDate:date];

  return [iso8601String stringByAppendingString:@"Z"];
}

+ (BOOL)configContains:(NSString *)key {
  return [[RNENPreferences shared] contains:key] || [[RNENJSON shared] contains:key] ||
         [RNENMeta contains:key];
}

+ (BOOL)getConfigBooleanValue:(NSString *)tag key:(NSString *)key defaultValue:(BOOL)defaultValue {
  BOOL enabled;

  if ([[RNENPreferences shared] contains:key]) {
    enabled = [[RNENPreferences shared] getBooleanValue:key defaultValue:defaultValue];
    DLog(@"%@ %@ via "
         @"RNENPreferences: %d",
         tag, key, enabled);
  } else if ([[RNENJSON shared] contains:key]) {
    enabled = [[RNENJSON shared] getBooleanValue:key defaultValue:defaultValue];
    DLog(@"%@ %@ via "
         @"RNENJSON: %d",
         tag, key, enabled);
  } else {
    // Note that if we're here, and the key is not set on the app's bundle, our final default is the
    // one passed in
    enabled = [RNENMeta getBooleanValue:key defaultValue:defaultValue];
    DLog(@"%@ %@ via "
         @"RNENMeta: %d",
         tag, key, enabled);
  }

  DLog(@"%@ %@ final value: %d", tag, key, enabled);

  return enabled;
}

@end
