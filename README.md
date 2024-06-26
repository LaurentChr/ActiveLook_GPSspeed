# ActiveLook® SPEECH

Description : What is said around you is translated & displayed in your ActiveLook® A/R glasses

<p align="center"> <img src="./ActiveLook GPSspeed 1024.png"/ </p>

	The application can be found on GooglePlay :
	https://play.google.com/store/apps/details?id=com.GPSspeed 

    
### License

```
Licensed under the Apache License, Version 2.0 (the “License”);
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an “AS IS” BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

### Requirements

You will need the following:
- A pair of ActiveLook® glasses
- Android Studio
- An android device with BLE

Known supported Devices :
- ENGO® : Cycling & Running action glasses (http://engoeyewear.com/)
- Julbo EVAD® : Premium smart glasses providing live data for intense sporting experiences (https://www.julbo.com/en_gb/evad-1)
- Cosmo Connected : GPS & cycling (https://cosmoconnected.com/fr/produits-velo-trottinette/cosmo-vision)

### File to create and add at the root : .env

First, you need to add a file called '.env' at the source of the project. This file will contain only 2 lines :
```
ACTIVELOOK_SDK_TOKEN = ""
ACTIVELOOK_CFG_PASSWORD = 0xDEADBEEF
```

### Main files to modify

The name of the app is defined in the strings.xml file.

* app\src\main\res\layout\content_scrolling.xml
* app\src\main\res\values\strings.xml
* app\src\main\java\com\speech\demo\MainActivity.java

In order to get the best performances, the ActiveLookSDK directory should be the latest release from : https://github.com/ActiveLook/android-sdk

### detailled description of this Android application

This "ActiveLook GPSspeed" application connects to Activelook® smart glasses to display, live, and right in your field of vision, the GPS data and their variations shapes.

This application is dedicated to any activities during which you need to keep track of your GPS data and variations easily right in your eyes without moving your head, especially sailing on a boat, or riding  bicycle, or walking in the countryside or in the mountains.

It also displays your mobile notifications at the top of the screen during a few seconds ... as on your smartphone. 

This "ActiveLook® GPSspeed" application connects to any Activelook® augmented reality glasses to display, live, and right in your field of vision, the key information you need to keep you always informed. The application will first pair via BTLE with your Activelook® smart glasses.
