import 'dart:async';

import 'package:flutter/services.dart';
import 'dart:io' show Platform;

class GraphhoperRoute {
  static const MethodChannel _channel = const MethodChannel('graphhoper_route');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    if (Platform.isAndroid) {

      return version;
    }
    return "Not Supported";
  }
}
