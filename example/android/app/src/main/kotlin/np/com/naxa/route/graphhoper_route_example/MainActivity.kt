package np.com.naxa.route.graphhoper_route_example

import android.os.Bundle
import android.util.Log
import androidx.annotation.NonNull;
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugins.GeneratedPluginRegistrant
import np.com.naxa.route.graphhoper_route.downloader.CalculatePathTask

class MainActivity : FlutterActivity() {
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine);
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        CalculatePathTask(object : CalculatePathTask.OnCalculateTaskTaskListener {
//            override fun onFailed(message: String?) {
//                Log.i("Nishon", message)
//            }
//
//            override fun onPathCalculated(path: String?) {
//                Log.i("Nishon", path)
//            }
//        }).execute(listOf(27.7172, 85.3240, 28.2096, 83.9856))
//    }
}
