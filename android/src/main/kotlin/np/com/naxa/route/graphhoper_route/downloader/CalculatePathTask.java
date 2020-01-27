package np.com.naxa.route.graphhoper_route.downloader;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;

import java.io.File;
import java.util.List;

public class CalculatePathTask extends AsyncTask<List<Double>, Void, String> {

    private final OnCalculateTaskTaskListener listener;

    public CalculatePathTask(OnCalculateTaskTaskListener listener) {
        this.listener = listener;
    }

    public interface OnCalculateTaskTaskListener {
        void onPathCalculated(String path);

        void onFailed(String message);
    }


    private void log(String str) {
        Log.i("GH", str);
    }

    private void log(String str, Throwable t) {
        Log.i("GH", str, t);
    }

    private void logUser(String str) {
        log(str);
    }

    @SafeVarargs
    @Override
    protected final String doInBackground(List<Double>... lists) {
        String path = null;
        try {
            GraphHopper tmpHopp = new GraphHopper().forMobile();
            tmpHopp.load(new File(getMapFolder(), "nepal").getAbsolutePath() + "-gh");
            log("found graph " + tmpHopp.getGraphHopperStorage().toString() + ", nodes:" + tmpHopp.getGraphHopperStorage().getNodes());
            GHRequest request = new GHRequest(lists[0].get(0), lists[0].get(1), lists[0].get(2), lists[0].get(3));
            GHResponse route = tmpHopp.route(request);
            path = route.toString();
        } catch (Exception e) {
            listener.onFailed(e.getMessage());
            e.printStackTrace();
        }

        return path;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        listener.onPathCalculated(s);
    }

    private File getMapFolder() {

        File mapsFolder;
        String mapPath = "";
        boolean greaterOrEqKitkat = Build.VERSION.SDK_INT >= 19;
        if (greaterOrEqKitkat) {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                throw new RuntimeException("GraphHopper is not usable without an external storage!");
            }
            mapsFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    mapPath);
        } else
            mapsFolder = new File(Environment.getExternalStorageDirectory(), mapPath);

        return mapsFolder;
    }
}
