require 'json'
package = JSON.parse(File.read(File.join(__dir__, 'package.json')))
appPackage = JSON.parse(File.read(File.join('..', 'app', 'package.json')))

coreVersionDetected = appPackage['version']
coreVersionRequired = package['peerDependencies'][appPackage['name']]
firebase_sdk_version = appPackage['sdkVersions']['ios']['firebase']
if coreVersionDetected != coreVersionRequired
  Pod::UI.warn "NPM package '#{package['name']}' depends on '#{appPackage['name']}' v#{coreVersionRequired} but found v#{coreVersionDetected}, this might cause build issues or runtime crashes."
end

Pod::Spec.new do |s|
  s.name                = "RNEN_Template_"
  s.version             = package["version"]
  s.description         = package["description"]
  s.summary             = <<-DESC
                            A well tested feature rich Firebase implementation for React Native, supporting iOS & Android.
                          DESC
  s.homepage            = "http://enhancers.it/oss/react-native-enhanced-notifications"
  s.license             = package['license']
  s.authors             = "Invertase Limited"
  s.source              = { :git => "https://github.com/frank10gm/react-native-enhanced-notifications.git", :tag => "v#{s.version}" }
  s.social_media_url    = 'http://twitter.com/enhancers'
  s.ios.deployment_target = "10.0"
  s.source_files        = 'ios/**/*.{h,m}'

  # React Native dependencies
  s.dependency          'React-Core'
  s.dependency          'RNENApp'

  if defined?($FirebaseSDKVersion)
    Pod::UI.puts "#{s.name}: Using user specified Firebase SDK version '#{$FirebaseSDKVersion}'"
    firebase_sdk_version = $FirebaseSDKVersion
  end

  # Firebase dependencies
  s.dependency          'Firebase/_Template_', firebase_sdk_version

  if defined?($RNFirebaseAsStaticFramework)
    Pod::UI.puts "#{s.name}: Using overridden static_framework value of '#{$RNFirebaseAsStaticFramework}'"
    s.static_framework = $RNFirebaseAsStaticFramework
  else
    s.static_framework = false
  end
end
