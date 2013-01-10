Android DirectoryChooser
========================

A simple directory chooser you can integrate into your Android app.

This version of the library has no additional dependencies, but requires Android
v11+ to work. There is, however, [a v11-brach][2] that supports down to v7 using
ActionBarSherlock.

Based on the DirectoryChooser from the excellent
[AntennaPod App](https://github.com/danieloeh/AntennaPod) by danieloeh.

![DirectoryChooser Sample Screenshot][1]

Dependencies
------------

 * Roboguice (but should work with any other JSR-330 complient container)

Roadmap
-------

 * 1.0: Publish to Maven Central
 * 2.0: Asynchronous directory chooser

Usage
-----

For a full example see the `sample` app in the
[repository](https://github.com/passy/Android-DirectoryChooser/tree/master/sample).

### Manifest

You need to declare the `DirectoryChooserActivity` and request the
`android.permission.WRITE_EXTERNAL_STORAGE` permission.

```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
...
<application>
    <activity android:name="net.rdrei.android.dirchooser.DirectoryChooserActivity" />
</application>
```

### Activity

To choose a directory, start the activity from your app logic:

```java
final Intent chooserIntent = new Intent(this, DirectoryChooserActivity.class);

// Optional: Allow users to create a new directory with a fixed name.
chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_NEW_DIR_NAME,
                       "DirChooserSample");

// REQUEST_DIRECTORY is a constant integer to identify the request, e.g. 0
startActivityForResult(chooserIntent, REQUEST_DIRECTORY);
```

Handle the result in your `onActivityResult` method:

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == REQUEST_DIRECTORY) {
        if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
            handleDirectoryChoice(data
                .getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR));
        } else {
            // Nothing selected
        }
    }
}
```

License
-------

```text
Copyright 2013 Pascal Hartig

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

 [1]: https://raw.github.com/passy/Android-DirectoryChooser/master/media/screenshot_phone.png
 [2]: https://github.com/passy/Android-DirectoryChooser/tree/pre-v11
