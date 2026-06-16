# helix_timex

A Flutter plugin to integrate with Helix Timex smart wearable devices (compatible with CRREPA Bluetooth protocols) on **Android**.

## Features

- **Device Discovery**: Scan and identify nearby Helix smartwatches.
- **Connection Management**: Connect to and disconnect from Helix watches using MAC address and device name.
- **Biometric Monitoring**:
  - Read Heart Rate data and trigger manual measurements.
  - Read SpO2 (Blood Oxygen) levels and trigger manual measurements.
  - Read Blood Pressure (Systolic and Diastolic) and trigger manual measurements.
  - Enable dynamic heart rate monitoring.
- **Real-time Streams**: Listen to scanning state, connection state, and discovered device details.

---

## Platform Support

| Android | iOS |
| :---: | :---: |
| ✔ (SDK 21+) | N/A |

---

## Installation

Add `helix_timex` as a dependency in your `pubspec.yaml` file:

```yaml
dependencies:
  helix_timex:
    path: /path/to/helix_timex # Local or git path
```

Run `flutter pub get` to fetch the package.

### Android Setup

1. Make sure you have Bluetooth and Location permissions configured in your `android/app/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<!-- For Android 12+ -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
```

2. If you are obfuscating your application with ProGuard, append these rules to your `android/app/proguard-rules.pro`:

```proguard
-keep class com.crrepa.ble.** { *; }
-keep class com.devnation.helix_timex.** { *; }
```

---

## Usage Example

### Initialize and Scan

```dart
import 'package:helix_timex/helix_timex.dart';

final HelixTimex timex = HelixTimex();

// Listen to scanning state
timex.getScanningStateStream.listen((isScanning) {
  print("Scanning state: $isScanning");
});

// Listen to discovered devices
timex.deviecFoundStream.listen((device) {
  print("Found device: ${device.deviceName} - ${device.macAddress}");
});

// Start scanning
timex.startScanDevice();
```

### Connect to Device

```dart
timex.connect(
  macAddress: "00:11:22:33:44:55",
  deviceName: "HXW01",
);

// Listen to connection state
timex.getConnectionStateStream.listen((state) {
  print("Connection state: ${state.connectionState}");
});
```

### Measure Health Parameters

```dart
// Listen to Heart Rate
timex.getHeartRateStream.listen((rate) {
  print("Heart Rate: $rate BPM");
});
timex.measureHeartRate();

// Listen to SpO2
timex.getSpo2Stream.listen((spo2) {
  print("SpO2 level: $spo2%");
});
timex.measureSpo2();

// Listen to Blood Pressure
timex.getBloodPressureStream.listen((bpData) {
  print("Systolic: ${bpData['sbp']}, Diastolic: ${bpData['dbp']}");
});
timex.measureBloodPressure();
```
