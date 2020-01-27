import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:graphhoper_route/graphhoper_route.dart';

void main() {
  const MethodChannel channel = MethodChannel('graphhoper_route');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await GraphhoperRoute.platformVersion, '42');
  });
}
