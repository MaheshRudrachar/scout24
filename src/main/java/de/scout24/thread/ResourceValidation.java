package de.scout24.thread;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class ResourceValidation implements Callable<Map<String, Integer>> {

    private List<String> resourceList;
    private Map<String, Integer> resourceValidationMap = new HashMap<>();

    public ResourceValidation(List<String> resourceList) {
        this.resourceList = resourceList;
    }

    @Override
    public Map<String, Integer> call() {
        for (String resource: resourceList) {
            int respCode;
            try {
                respCode = getResponseCode(resource);
                resourceValidationMap.put(resource, respCode);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resourceValidationMap;
    }

    public static int getResponseCode(String urlString) throws MalformedURLException, IOException {
        URL u = new URL(urlString);
        if (u.openConnection() instanceof HttpURLConnection) {
            HttpURLConnection huc = (HttpURLConnection) u.openConnection();
            /*
             * set request method head and follow redirects to false
             */
            huc.setRequestMethod("HEAD");
            HttpURLConnection.setFollowRedirects(false);

            huc.connect();
            return huc.getResponseCode();
        }
        return 0;
    }

}
