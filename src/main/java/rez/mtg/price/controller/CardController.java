package rez.mtg.price.controller;

import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import rez.mtg.price.helper.ScryfallHelper;
import rez.mtg.price.repository.CardRepository;

import java.io.IOException;

@Controller
@RequestMapping(path = "/cards")
public
class CardController {

    private static final Logger logger = LoggerFactory.getLogger(CardController.class);

    @Autowired
    ScryfallHelper scryfallHelper;

    @Autowired
    private CardRepository cardRepository;

    @Value("${mtg.datapath}")
    private String downloadLocation;

    @GetMapping(path = "/")
    public @ResponseBody
    String testEndPoint() {
        logger.info("testEndPoint");
        return "Test end point success with logs.";
    }

    @GetMapping(path = "/count")
    public @ResponseBody
    long cardCount() {
        logger.info("cardCount");
        return cardRepository.count();
    }

    @GetMapping(path = "/download")
    public @ResponseBody
    String downloadScryFall() {
        logger.info("download");
        JSONArray data = null;
        String fileLocation = null;
        try {
            fileLocation = scryfallHelper.downloadDailyBulkData();
        } catch (ParseException e) {
            e.printStackTrace();
            logger.error("ParseException {}", e);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("IOException {}", e);
        }
        if(fileLocation!=null)
        {

        }
        return fileLocation;
    }
}

