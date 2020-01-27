import 'dart:async';

import 'package:flutter/services.dart';
import 'dart:io' show Platform;
import 'dart:io' as io;

class GraphhoperRoute {
  static const MethodChannel _channel = const MethodChannel('graphhoper_route');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    if (Platform.isAndroid) {
      return version;
    }
    return "Not Supported";
  }

  static Future<String> getRouteAsLatLng({List<double> points,route}) async {
    assert(points != null);
    assert(points.length % 2 == 0);
    return await _channel
        .invokeMethod("getRouteAsLatLng", <String, dynamic>{'points': points});
  }
}
