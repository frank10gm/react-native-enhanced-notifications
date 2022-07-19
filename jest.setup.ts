import * as ReactNative from 'react-native';

jest.doMock('react-native', () => {
  return Object.setPrototypeOf(
    {
      Platform: {
        OS: 'android',
        select: () => {},
      },
      NativeModules: {
        ...ReactNative.NativeModules,
        RNENAnalyticsModule: {
          logEvent: jest.fn(),
        },
        RNENAppModule: {
          NATIVE_FIREBASE_APPS: [
            {
              appConfig: {
                name: '[DEFAULT]',
              },
              options: {},
            },

            {
              appConfig: {
                name: 'secondaryFromNative',
              },
              options: {},
            },
          ],
          FIREBASE_RAW_JSON: '{}',
          addListener: jest.fn(),
          eventsAddListener: jest.fn(),
          eventsNotifyReady: jest.fn(),
          removeListeners: jest.fn(),
        },
        RNENAuthModule: {
          APP_LANGUAGE: {
            '[DEFAULT]': 'en-US',
          },
          APP_USER: {
            '[DEFAULT]': 'jestUser',
          },
          addAuthStateListener: jest.fn(),
          addIdTokenListener: jest.fn(),
          useEmulator: jest.fn(),
        },
        RNENCrashlyticsModule: {},
        RNENDatabaseModule: {
          on: jest.fn(),
          useEmulator: jest.fn(),
        },
        RNENFirestoreModule: {
          settings: jest.fn(),
          documentSet: jest.fn(),
        },
        RNENMessagingModule: {
          onMessage: jest.fn(),
        },
        RNENPerfModule: {},
        RNENStorageModule: {
          useEmulator: jest.fn(),
        },
      },
    },
    ReactNative,
  );
});
