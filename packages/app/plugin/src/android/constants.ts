const appPackageJson = require('@react-native-enhanced-notifications/app/package.json');

export const DEFAULT_TARGET_PATH = 'app/google-services.json';

export const googleServicesClassPath = 'com.google.gms:google-services';
export const googleServicesPlugin = 'com.google.gms.google-services';
export const googleServicesVersion = appPackageJson.sdkVersions.android.gmsGoogleServicesGradle;
