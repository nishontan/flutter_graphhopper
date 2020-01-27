package np.com.naxa.route.graphhoper_route

import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import np.com.naxa.route.graphhoper_route.downloader.CalculatePathTask


/** GraphhoperRoutePlugin */
class GraphhoperRoutePlugin : FlutterPlugin, MethodCallHandler {


    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        val channel = MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "graphhoper_route")
        channel.setMethodCallHandler(GraphhoperRoutePlugin());
    }

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "graphhoper_route")
            channel.setMethodCallHandler(GraphhoperRoutePlugin())
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when {
            call.method == "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
            call.method == "getRouteAsLatLng" -> getRouteAsLatLng(call, result)
            else -> result.notImplemented()
        }
    }

    private fun getRouteAsLatLng(call: MethodCall, result: Result) {
        val points: List<Double>? = call.argument<List<Double>>("points")
        assert(points != null)
        assert(points?.size == 4)

        CalculatePathTask(object : CalculatePathTask.OnCalculateTaskTaskListener {
            override fun onFailed(message: String?) {
                result.error("", message, "")
            }

            override fun onPathCalculated(path: String?) {
                result.success(path)
            }
        }).execute(listOf(27.7172, 85.3240, 28.2096, 83.9856))


    }


    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    }
}
