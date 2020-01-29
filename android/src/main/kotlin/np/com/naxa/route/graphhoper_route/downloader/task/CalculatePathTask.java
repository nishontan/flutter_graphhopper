package np.com.naxa.route.graphhoper_route.downloader.task;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.util.shapes.GHPoint3D;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class CalculatePathTask extends AsyncTask<List<Double>, Void, AsyncTaskResult<String>> {
    @SafeVarargs
    @Override
    protected final AsyncTaskResult<String> doInBackground(List<Double>... lists) {
        try {
            GraphHopper tmpHopp = new GraphHopper().forMobile();
            tmpHopp.load(new File(getMapFolder(), "nepal").getAbsolutePath() + "-gh");
            GHRequest request = new GHRequest(lists[0].get(0), lists[0].get(1), lists[0].get(2), lists[0].get(3));
            GHResponse route = tmpHopp.route(request);
            String path = mapGHResponseToJSON(route);

            return new AsyncTaskResult<String>(path);
        } catch (Exception anyError) {
            return new AsyncTaskResult<String>(anyError);
        }
    }


    private String mapGHResponseToJSON(GHResponse ghRsp) throws JSONException {


        JSONObject json = new JSONObject();
        JSONArray paths = new JSONArray();

//        json.put("hints", "");
//        json.put("info", "");
        json.put("paths", paths);

        JSONObject jsonPath = new JSONObject();
        for (PathWrapper ar : ghRsp.getAll()) {
            jsonPath.put("distance", ar.getDistance());
            jsonPath.put("time", ar.getTime());

            JSONArray points = new JSONArray();
            for (GHPoint3D ghPoint3D : ar.getPoints()) {
                JSONArray coordinates = new JSONArray();
                coordinates.put(ghPoint3D.getLon());
                coordinates.put(ghPoint3D.getLat());
                points.put(coordinates);
            }
            JSONObject jsonPoint = new JSONObject();
            jsonPoint.put("type", "LineString");
            jsonPoint.put("coordinates", points);

            jsonPath.put("points", jsonPoint);
            jsonPath.put("bbox", ar.calcBBox2D().toGeoJson());

            paths.put(jsonPath);
        }

        return json.toString();
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

    @Override
    protected void onPostExecute(AsyncTaskResult<String> result) {
        if (result.getError() != null) {
            listener.onFailed(result.getError().getMessage());
            result.getError().printStackTrace();
        } else if (isCancelled()) {
            listener.onFailed("canceled");
        } else {
            listener.onPathCalculated(result.getResult());
        }
    }

    private final OnCalculateTaskTaskListener listener;

    public CalculatePathTask(OnCalculateTaskTaskListener listener) {
        this.listener = listener;
    }

    public interface OnCalculateTaskTaskListener {
        void onPathCalculated(String path);

        void onFailed(String message);
    }


}
