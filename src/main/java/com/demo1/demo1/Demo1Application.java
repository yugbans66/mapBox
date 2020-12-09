package com.demo1.demo1;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SpringBootApplication
@RestController
public class Demo1Application {
    static OkHttpClient client = new OkHttpClient();

    static String fetch(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
    private static String getUrl(String poi){
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.mapbox.com/geocoding/v5/mapbox.places/"+poi+".json?autocomplete=true&bbox=27.97448019822218%2C40.68160256806826%2C29.9279778356659%2C41.60751896412282&access_token=pk.eyJ1IjoieXVnYmFuczY2IiwiYSI6ImNraWVvZjNzdTEwZmcycXJzeHZubGs3bDMifQ.80onLHMFoC68PxvlDYZcUg&types=poi&limit=10").newBuilder();
        return urlBuilder.build().toString();
    }
    private static Collection<JSONObject> filterPoi(JSONArray poi) throws JSONException {
        Collection<JSONObject> result = new ArrayList<JSONObject>();
        for (int i = 0 ; i < poi.length(); i++) {
            JSONObject feature = poi.getJSONObject(i);
            JSONObject resultObject = new JSONObject();
            String[] categories = feature.getJSONObject("properties").getString("category").split(",");
            String region=feature.getJSONArray("context").getJSONObject(0).getString("text");
            JSONArray categoryArray = new JSONArray();
            for(String a:categories){
                categoryArray.put(a);
            }
            resultObject.put("name",feature.getString("text"));
            resultObject.put("categories",categoryArray);
            resultObject.put("region",region);
            result.add(resultObject);
        }
        return result;
    }
    private static Collection<JSONObject> random10(Collection<JSONObject> coll){
        ArrayList<Integer> ans = new ArrayList<>();
        List<JSONObject> colList = List.copyOf(coll);
        int random = (int) (Math.random() * 30);
        for (int i = 0; i < 10; i++) {
            while (ans.contains(random)) {
                random = (int) (Math.random() * 30);
            }
            ans.add(random);
        }
        JSONObject[] finalArr=colList.toArray(new JSONObject[0]);
        Collection<JSONObject> finalCollection= new ArrayList<JSONObject>();
        Integer randNumArr[] = new Integer[10];
        randNumArr = ans.toArray(randNumArr);
        for(Integer i: randNumArr) {
            finalCollection.add(finalArr[i]);
        }
        return finalCollection;
    }
    public static void main(String[] args) {
        String[] pois = {"palace","museum","tourism"};
        SpringApplication.run(Demo1Application.class, args);
    }


//    @GetMapping("/itinerary")
    @RequestMapping(value = "/itinerary", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String getItinerary(){
        String resp="";
        try {
            JSONObject palacesJson = new JSONObject(fetch(getUrl("palace")));
            JSONObject touristSpotsJson = new JSONObject(fetch(getUrl("tourism")));
            JSONObject museumsJson = new JSONObject(fetch(getUrl("museum")));
            JSONArray palaces = palacesJson.getJSONArray("features");
            JSONArray touristSpots = touristSpotsJson.getJSONArray("features");
            JSONArray museums = museumsJson.getJSONArray("features");
            Collection<JSONObject> palaceCollection = filterPoi(palaces);
            Collection<JSONObject> touristCollection = filterPoi(touristSpots);
            Collection<JSONObject> museumCollection = filterPoi(museums);
            Iterable<JSONObject> combinedIterables = Iterables.unmodifiableIterable(
                    Iterables.concat(palaceCollection,touristCollection,museumCollection));
            Collection<JSONObject> finalCollection = Lists.newArrayList(combinedIterables);
            finalCollection=random10(finalCollection);
            return finalCollection.toString();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return resp;
    }
}
