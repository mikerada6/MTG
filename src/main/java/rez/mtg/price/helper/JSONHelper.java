package rez.mtg.price.helper;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import rez.mtg.price.controller.CardController;

@Component
public
class JSONHelper {

    private static final Logger logger = LoggerFactory.getLogger(JSONHelper.class);
    private final CloseableHttpClient httpClient;

    public JSONHelper() {
        httpClient = HttpClients.createDefault();
    }

    public String getRequest(String url) {
//		logger.info("Getting info from {}.", url);
        String result = null;

        HttpGet request = new HttpGet(url);

        try (CloseableHttpResponse response = httpClient.execute(request)) {

            // Get HttpResponse Status

            HttpEntity entity = response.getEntity();

            if (entity != null) {
                // return it as a String
                result = EntityUtils.toString(entity);
            }

        } catch (ClientProtocolException e) {
            logger.error("ClientProtocolException {}", e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("IOException {}", e);
            e.printStackTrace();
        }
        logger.trace("Got result {}: ", result);
        return result;
    }

    public JSONArray JsonFromFile(String file) {
        return null;
    }

    public static JSONObject getJsonObject(String jsonString) throws JSONException {
        return new JSONObject(jsonString);
    }

}
