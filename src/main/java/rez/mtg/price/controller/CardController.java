package rez.mtg.price.controller;

import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import rez.mtg.price.helper.ScryfallHelper;

import java.io.IOException;

@Controller
@RequestMapping(path = "/cards")
public
class CardController {

    @Autowired
    ScryfallHelper scryfallHelper;

    @GetMapping(path = "/")
    public @ResponseBody
    String testEndPoint() {
        return "Test end point success.";
    }

    @GetMapping(path = "/download")
    public @ResponseBody
    String downloadScryFall() {
        JSONArray data = null;
        String fileLocation = null;
        try {
            fileLocation = scryfallHelper.downloadDailyBulkData();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(fileLocation!=null)
        {

        }
        return fileLocation;
    }
}
