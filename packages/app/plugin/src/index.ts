import { ConfigPlugin, withPlugins, createRunOncePlugin } from '@expo/config-plugins';

import {
  withApplyGoogleServicesPlugin,
  withBuildscriptDependency,
  withCopyAndroidGoogleServices,
} from './android';
import { withFirebaseAppDelegate, withIosGoogleServicesFile } from './ios';

/**
 * A config plugin for configuring `@react-native-enhanced-notifications/app`
 */
const withRnFirebaseApp: ConfigPlugin = config => {
  return withPlugins(config, [
    // iOS
    withFirebaseAppDelegate,
    withIosGoogleServicesFile,

    // Android
    withBuildscriptDependency,
    withApplyGoogleServicesPlugin,
    withCopyAndroidGoogleServices,
  ]);
};

const pak = require('@react-native-enhanced-notifications/app/package.json');
export default createRunOncePlugin(withRnFirebaseApp, pak.name, pak.version);
