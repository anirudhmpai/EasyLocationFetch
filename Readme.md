# EasyLocationFetch

[![](https://jitpack.io/v/anirudhmpai/EasyLocationFetch.svg)](https://jitpack.io/#anirudhmpai/EasyLocationFetch)

# Setup

### Step 1. Add it in your root build.gradle at the end of repositories:
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
### Step 2. Add this to gradle
	android {

	    ...
	    compileOptions {
		sourceCompatibility JavaVersion.VERSION_1_8
		targetCompatibility JavaVersion.VERSION_1_8
	    }
	}
	dependencies {
	        implementation 'com.github.anirudhmpai:EasyLocationFetch:1.0.2'
	}

# Implementation :

### Method 1. this uses googles api alongside internal providers(works better in all scenarios)

```
GeoLocationModel geoLocationModel = new EasyLocationFetch(context,GoogleApiKey).getLocationData();
```
### Method 2. uses builtin providers
	
```
GeoLocationModel geoLocationModel = new EasyLocationFetch(context).getLocationData();
```
# Different data items you can call from above model
	geoLocationModel.getAddress()
	geoLocationModel.getCity()
	geoLocationModel.getLattitude()
	geoLocationModel.getLongitude()
