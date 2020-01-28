package np.com.naxa.route.graphhoper_route.downloader;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.util.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
            path = mapGHResponseToJSON(route);
        } catch (Exception e) {
            listener.onFailed(e.getMessage());
            e.printStackTrace();
        }

        return path;
    }

    private String mapGHResponseToJSON(GHResponse route) throws JSONException {

        boolean enableElevation = true;
        boolean calcPoints = true;
        boolean pointsEncoded = false;

        JSONObject json = new JSONObject();
        json.put("hints", route.getHints());
        JSONArray array = new JSONArray();
        json.put("paths", array);
        for (PathWrapper ar : route.getAll()) {
            JSONObject jsonPath = new JSONObject();
            jsonPath.put("distance", Helper.round(ar.getDistance(), 3));
            jsonPath.put("weight", Helper.round6(ar.getRouteWeight()));
            jsonPath.put("time", ar.getTime());
            jsonPath.put("transfers", ar.getNumChanges());
            if (!ar.getDescription().isEmpty()) {
                jsonPath.put("description", ar.getDescription());
            }
            jsonPath.put("points_encoded", pointsEncoded);
            if (ar.getPoints().getSize() >= 2) {
                jsonPath.put("bbox", ar.calcBBox2D());
            }
            jsonPath.put("points", ar.getPoints().toLineString(enableElevation));

            jsonPath.put("instructions", ar.getInstructions());
            jsonPath.put("legs", ar.getLegs());
            jsonPath.put("details", ar.getPathDetails());
            jsonPath.put("ascend", ar.getAscend());
            jsonPath.put("descend", ar.getDescend());

            array.put(jsonPath);
        }
        return json.toString();
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
