package np.com.naxa.route.graphhoper_route.downloader;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
            e.printStackTrace();
        }

        return path;
    }


    /**
     * This includes the required attribution for OpenStreetMap.
     * Do not hesitate to you mention us and link us in your about page
     * https://support.graphhopper.com/support/search/solutions?term=attribution
     */
    private final List<String> COPYRIGHTS = Arrays.asList("GraphHopper", "OpenStreetMap contributors");

    private ObjectNode jsonResponsePutInfo(ObjectNode json, float took) {
        final ObjectNode info = json.putObject("info");
        info.putPOJO("copyrights", COPYRIGHTS);
        info.put("took", Math.round(took * 1000));
        return json;
    }


    private String mapGHResponseToJSON(GHResponse ghRsp) throws JSONException {

        JSONObject json = new JSONObject();
        JSONArray paths = new JSONArray();

        json.put("hints", "");
        json.put("info", "");
        json.put("paths", paths);

        JSONObject jsonPath = new JSONObject();
        for (PathWrapper ar : ghRsp.getAll()) {
            jsonPath.put("distance", ar.getDistance());
            jsonPath.put("time", ar.getTime());

            ArrayList<ArrayList<Double>> points = new ArrayList<>();
            for (GHPoint3D ghPoint3D : ar.getPoints()) {
                ArrayList<Double> coordinates = new ArrayList<>();
                coordinates.add(ghPoint3D.getLon());
                coordinates.add(ghPoint3D.getLat());
                points.add(coordinates);
            }
            JSONObject jsonPoint = new JSONObject();
            jsonPoint.put("type", "LineString");
            jsonPoint.put("coordinates", points);

            jsonPath.put("points", jsonPoint);

            paths.put(jsonPath);
        }

        return json.toString();
    }

    private String mapGHResponseToJSON(GHResponse ghRsp, float took) throws JsonProcessingException {

        ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.putPOJO("hints", ghRsp.getHints().toMap());
        jsonResponsePutInfo(json, took);
        ArrayNode jsonPathList = json.putArray("paths");


        boolean enableElevation = true;

        for (PathWrapper ar : ghRsp.getAll()) {
            ObjectNode jsonPath = jsonPathList.addObject();

            jsonPath.put("distance", Helper.round(ar.getDistance(), 3));
            jsonPath.put("weight", Helper.round6(ar.getRouteWeight()));
            jsonPath.put("time", ar.getTime());
            jsonPath.put("transfers", ar.getNumChanges());
            jsonPath.putPOJO("instructions", ar.getInstructions());
            jsonPath.putPOJO("legs", ar.getLegs());
            jsonPath.putPOJO("details", ar.getPathDetails());
            jsonPath.put("ascend", ar.getAscend());
            jsonPath.put("descend", ar.getDescend());


            if (!ar.getDescription().isEmpty()) {
                jsonPath.putPOJO("description", ar.getDescription());
            }
            jsonPath.putPOJO("snapped_waypoints", ar.getWaypoints().toLineString(enableElevation));
            if (ar.getFare() != null) {
                jsonPath.put("fare", NumberFormat.getCurrencyInstance(Locale.ROOT).format(ar.getFare()));
            }

//            jsonPath.putPOJO("points", ar.getPoints().toLineString(enableElevation));


        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
        ObjectNode node = mapper.getNodeFactory().objectNode();
        node.put("field1", "Maël Hörz");

        return toJsonString(json);
    }

    private String toJsonString(Object obj) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonGenerationException e) {
            throw new ClientException("Unable to generate json", e);
        } catch (JsonMappingException e) {
            throw new ClientException("Unable to map json", e);
        } catch (IOException e) {
            throw new ClientException("IO error", e);
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (s == null) {
            listener.onFailed(e.getMessage());
        } else {
            listener.onPathCalculated(s);
        }
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
