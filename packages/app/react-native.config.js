module.exports = {
  dependency: {
    platforms: {
      android: {
        packageImportPath: 'import it.enhancers.firebase.app.ReactNativeEnhancedNotificationsAppPackage;',
      },
      ios: {
        scriptPhases: [
          {
            name: '[RNEN] Core Configuration',
            path: './ios_config.sh',
            execution_position: 'after_compile',
            input_files: ['$(BUILT_PRODUCTS_DIR)/$(INFOPLIST_PATH)'],
          },
        ],
      },
    },
  },
};
