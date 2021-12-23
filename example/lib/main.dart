

import 'package:flutter/material.dart';
import 'package:flutter_bluetooth_serial/flutter_bluetooth_serial.dart';
import 'package:helix_timex/helix_timex.dart';
import 'dart:async';
import 'package:permission_handler/permission_handler.dart';

import 'location_service.dart';


void main() {
  runApp(const MyApp());
}



class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  bool isScanning = false;
  TimexConnectionState conState = TimexConnectionState.disconnected;

  HelixTimex timex=HelixTimex();


  String heartRate='';
  String spo2='';
  String sis='';
  String dis='';



  @override
  void initState() {
    super.initState();
    initPlatformState();


  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    timex.getScanningStateStream.listen((event) {

      print('My Scanning State '+ event.toString());

      isScanning=event;
      setState(() {

      });
    });


    timex.getConnectionStateStream.listen((event) {

      print('My Connection State'+event.connectionState.toString());
      conState=event.connectionState!;
      setState(() {

      });
    });






    timex.getHeartRateStream.listen((event) {

      print('My Heart Rate'+event.toString());
      heartRate=event.toString();
      setState(() {

      });
    });

    timex.getSpo2Stream.listen((event) {

      print('My SpO2'+event.toString());
      spo2=event.toString();
      setState(() {

      });
    });

    timex.getBloodPressureStream.listen((event) {

      print('My Blood Pressure'+event.toString());
      sis=event['sbp'].toString();
      dis=event['dbp'].toString();
      setState(() {

      });
    });




  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [

              Text(conState==TimexConnectionState.connected? 'Connected':
              conState==TimexConnectionState.connecting? 'Connecting': 'Disconnected',
                style: TextStyle(
                  color: conState==TimexConnectionState.connected? Colors.green:Colors.red,

                ),),


              StreamBuilder<DeviceData>(
                  stream: timex.deviecFoundStream,
                  builder: (context, snapshot) {
                    return snapshot.data==null?
                    const Text('No Device Found')
                        :Column(
                      children: [
                        Text((snapshot.data!.deviceName??'0').toString()),
                        Text((snapshot.data!.macAddress??'0').toString()),
                        TextButton(onPressed: (){
                          timex.connect(macAddress: snapshot.data!.macAddress??'', deviceName: snapshot.data!.deviceName??'');
                        }, child: const Text('Connect')),


                        TextButton(onPressed: (){
                          timex.disConnect(
                          );
                        }, child: const Text('DisConnect')),


                        Text('Heart Rate: '+heartRate.toString()),
                        Text('Spo2: '+spo2.toString()),
                        Text('Sis BP: '+sis.toString()),
                        Text('Dis BP: '+dis.toString()),


                      ],
                    );
                  }
              ),
              const SizedBox(height: 10,),
              // StreamBuilder<timexmeterData>(
              //     stream: timex.detectedDataStream,
              //     builder: (context, snapshot) {
              //       return snapshot.data==null?
              //       const Text('NO Data Yet')
              //           :Column(
              //         children: [
              //           Text((snapshot.data!.spo2??'0').toString()),
              //           Text((snapshot.data!.heartRate??'0').toString()),
              //           Text((snapshot.data!.hrv??'0').toString()),
              //           Text((snapshot.data!.perfusionIndex!.toStringAsFixed(2))),
              //
              //
              //         ],
              //       );
              //     }
              // ),
              isScanning?
              const CircularProgressIndicator()
                  :TextButton(
                onPressed: () async{
                  startScan();

                },
                child: const Text('Start Scan'),
              ),

              // TextButton(
              //   onPressed: () async{
              //
              //     timex.connect(macAddress: 'FA:B6:4B:25:15:38',deviceName: 'djjd');
              //   },
              //   child: Text('Connect'),
              // ),
            ],
          ),
        ),
      ),
    );
  }



  void startScan() async{

    bool locationEnable=await LocationService().enableGPS();
    await FlutterBluetoothSerial.instance.requestEnable();
    await Permission.location.request();

    if(locationEnable){
      bool bluetoothEnable=(await FlutterBluetoothSerial.instance.isEnabled)??false;

      if(bluetoothEnable){
        if(await Permission.location.isGranted){
          timex.startScanDevice();
        }
        else{
          alertToast(context, 'Location Permission is required to use this feature');
        }

      }
      else{
        alertToast(context, 'Please enable bluetooth to use this feature');
      }

    }
    else{
      alertToast(context, 'Please enable location to use this feature');
    }
  }

}






