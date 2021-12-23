
import 'dart:async';

import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';



class HelixTimex {


  static const MethodChannel _channel = MethodChannel('helix_timex');
  static const EventChannel _deviceFoundStream = EventChannel('helix_timex_device_found_stream');
  static const EventChannel _scanningStateStream =  EventChannel('helix_timex_device_scanning_state');
  static const EventChannel _connectionStateStream =  EventChannel('helix_timex_device_connection_state');

  static const EventChannel _heartRateStream =  EventChannel('helix_timex_heartRate');
  static const EventChannel _spo2Stream =  EventChannel('helix_timex_spo2');
  static const EventChannel _bloodPressureStream =  EventChannel('helix_timex_bloodPressure');


  Stream get getScanningStateStream => _scanningStateStream.receiveBroadcastStream();
  Stream<ConnectionStateModal> get getConnectionStateStream => _connectionStateStream.receiveBroadcastStream().map((element) => ConnectionStateModal.fromString(element));

  Stream<DeviceData> get deviecFoundStream => _deviceFoundStream.receiveBroadcastStream().map((element) => DeviceData.fromJson(element));



  Stream get getHeartRateStream => _heartRateStream.receiveBroadcastStream();
  Stream get getSpo2Stream => _spo2Stream.receiveBroadcastStream();
  Stream get getBloodPressureStream => _bloodPressureStream.receiveBroadcastStream();

  // listenToData(){
  //   _detectDataStream.receiveBroadcastStream().listen((event) {
  //
  //     print('MyData'+event.toString());
  //     print('My'+event['spo2'].toString());
  //     print('My'+OximeterData.fromJson( Map<String, dynamic>.from(event)).toString());
  //     print('My'+OximeterData.fromJson(Map<String, dynamic>.from(event)).heartRate.toString());
  //   });
  // }



  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }



  void startScanDevice() async{
    await _channel.invokeMethod('startScanDevice');
  }



  void measureHeartRate() async{
    await _channel.invokeMethod('measureHeartRate');
  }


  void measureBloodPressure() async{
    await _channel.invokeMethod('measureBloodPressure');
  }


  void measureSpo2() async{
    await _channel.invokeMethod('measureSpo2');
  }

  void measureDynamicRate() async{
    await _channel.invokeMethod('measureDynamicRate');
  }





  void connect({
    required String macAddress,
    required String deviceName,
  }) async{
    try{
      await _channel.invokeMethod('connect',[macAddress,deviceName]);


    }
    catch(e) {
      print(e);
    }
  }

  void disConnect() async{
    try{
      await _channel.invokeMethod('disConnect',);
    }
    catch(e) {
      print(e);
    }
  }





}



class DeviceData {
  String? macAddress;
  String? deviceName;

  DeviceData(
      {
        this.macAddress,
        this.deviceName,

      });

  factory DeviceData.fromJson(json) => DeviceData(
    macAddress: (json['macAddress']??'') as String,
    deviceName: (json['deviceName']??'') as String,

  );


}

enum TimexConnectionState{
  connected,
  connecting,
  disconnected,
}

class ConnectionStateModal {
  TimexConnectionState? connectionState;

  ConnectionStateModal(
      {
        this.connectionState,

      });

  factory ConnectionStateModal.fromString(string) => ConnectionStateModal(
    connectionState:

    string==null?
    TimexConnectionState.disconnected
        :
    string==2?
    TimexConnectionState.connected:
    string==1?
    TimexConnectionState.connecting:
    string==0?
    TimexConnectionState.disconnected:


        TimexConnectionState.disconnected,

  );


}
