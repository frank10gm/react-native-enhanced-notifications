/*
 * Copyright (c) 2016-present Enhancers Limited & Contributors
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

describe('config', function () {
  describe('meta', function () {
    it('should read Info.plist/AndroidManifest.xml meta data', async function () {
      const metaData = await NativeModules.RNENAppModule.metaGetAll();
      metaData.rnfirebase_meta_testing_string.should.equal('abc');
      metaData.rnfirebase_meta_testing_boolean_false.should.equal(false);
      metaData.rnfirebase_meta_testing_boolean_true.should.equal(true);
    });
  });

  describe('json', function () {
    it('should read firebase.json data', async function () {
      const jsonData = await NativeModules.RNENAppModule.jsonGetAll();
      jsonData.rnfirebase_json_testing_string.should.equal('abc');
      jsonData.rnfirebase_json_testing_boolean_false.should.equal(false);
      jsonData.rnfirebase_json_testing_boolean_true.should.equal(true);
    });
  });

  describe('prefs', function () {
    beforeEach(async function () {
      await NativeModules.RNENAppModule.preferencesClearAll();
    });

    // NOTE: "preferencesClearAll" clears Firestore settings. Set DB as emulator again.
    after(async function () {
      await firebase
        .firestore()
        .settings({ host: 'localhost:8080', ssl: false, persistence: true });
    });

    it('should set bool values', async function () {
      const prefsBefore = await NativeModules.RNENAppModule.preferencesGetAll();
      should.equal(prefsBefore.enhancers_oss, undefined);
      await NativeModules.RNENAppModule.preferencesSetBool('enhancers_oss', true);
      const prefsAfter = await NativeModules.RNENAppModule.preferencesGetAll();
      prefsAfter.enhancers_oss.should.equal(true);
    });

    it('should set string values', async function () {
      const prefsBefore = await NativeModules.RNENAppModule.preferencesGetAll();
      should.equal(prefsBefore.enhancers_oss, undefined);
      await NativeModules.RNENAppModule.preferencesSetString('enhancers_oss', 'enhancers.io');
      const prefsAfter = await NativeModules.RNENAppModule.preferencesGetAll();
      prefsAfter.enhancers_oss.should.equal('enhancers.io');
    });

    it('should clear all values', async function () {
      await NativeModules.RNENAppModule.preferencesSetString('enhancers_oss', 'enhancers.io');
      const prefsBefore = await NativeModules.RNENAppModule.preferencesGetAll();
      prefsBefore.enhancers_oss.should.equal('enhancers.io');

      await NativeModules.RNENAppModule.preferencesClearAll();
      const prefsAfter = await NativeModules.RNENAppModule.preferencesGetAll();
      should.equal(prefsAfter.enhancers_oss, undefined);
    });
  });
});
