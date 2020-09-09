package rez.mtg.price.helper;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.rmi.server.ExportException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public
class ScryfallHelper {

    private static final Logger logger = LoggerFactory.getLogger(ScryfallHelper.class);
    @Autowired
    private JSONHelper jsonHelper;
    @Value("${mtg.datapath}")
    private String downloadLocation;

    public
    String downloadDailyBulkData() throws ParseException, IOException {
        String url = "https://api.scryfall.com/bulk-data";
        String defaultCardsLocation = null;

        String result = jsonHelper.getRequest(url);
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(result);
        JSONObject object = (JSONObject) obj;
        String updateTime = "";
        if (object.containsKey("data")) {
            JSONArray data = (JSONArray) object.get("data");
            for (int i = 0; i < data.size(); i++) {
                JSONObject datum = (JSONObject) data.get(i);
                if (datum.get("name").equals("Default Cards")) {
                    defaultCardsLocation = (String) datum.get("download_uri");
                    updateTime = (String) datum.get("updated_at");
                    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyLLdd'_'kkmm");
                    ZonedDateTime zonedDateTime = ZonedDateTime.parse(updateTime);
                    updateTime = zonedDateTime.format(format);
                    break;
                }
            }
        }


        String file = downloadLocation + updateTime + ".json";

        URL website = new URL(defaultCardsLocation);
        try (InputStream in = website.openStream()) {
            logger.info("Starting to download data from {}.",
                        defaultCardsLocation);
            Files.copy(in,
                       Paths.get(file),
                       StandardCopyOption.REPLACE_EXISTING);
            logger.info("Data downloaded to {}.", file);
        } catch (ExportException e) {
            return null;
        }
        return file;
    }

    public
    JsonParser openDownloadedJson(String filePath) throws ParseException, IOException {
        Object obj = null;
        JsonFactory jsonfactory = new JsonFactory();
        try (FileReader reader = new FileReader(filePath)) {
            //Read JSON file
            logger.info("Going to open file {}",
                        filePath);

            File jsonFile = new File(filePath);
            JsonParser jsonParser = jsonfactory.createParser(jsonFile);
            logger.info("File is loaded into the factory");
            return jsonParser;

        } catch (FileNotFoundException e) {
            logger.error("FileNotFoundException {}",
                         e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("IOException {}",
                         e);
            e.printStackTrace();
        }
        logger.error("We were not able to open the file {}.",
                     filePath);
        return null;
    }
}
