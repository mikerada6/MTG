package rez.mtg.price.controller;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import rez.mtg.price.helper.JSONHelper;
import rez.mtg.price.helper.ScryfallHelper;
import rez.mtg.price.magic.Card;
import rez.mtg.price.magic.Price;
import rez.mtg.price.repository.CardRepository;
import rez.mtg.price.repository.PriceRepository;

import java.io.IOException;
import java.util.ArrayList;

@Controller
@RequestMapping(path = "/price")
public
class PriceController {

    private static final Logger logger = LoggerFactory.getLogger(PriceController.class);
    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private JSONHelper jsonHelper;

    @Autowired
    private ScryfallHelper scryfallHelper;

    @PostMapping(path = "/today")
    public @ResponseBody
    void updatePriceForToday() {
        logger.info("updatePriceForToday");
        JsonParser jsonParser = null;
        ArrayList<Price> priceArrayList = new ArrayList<Price>();
        try {
            String file = scryfallHelper.downloadDailyBulkData();
            jsonParser = scryfallHelper.openDownloadedJson(file);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int count = 0;
        int saved = 0;
        try {
            Price price = null;
            Card card = null;
            while (!jsonParser.isClosed()) {

                JsonToken jsonToken = jsonParser.nextToken();

                if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                    String fieldName = jsonParser.getCurrentName();

                    jsonToken = jsonParser.nextToken();
                    if ("object".equals(fieldName)) {
                        count++;
                        //first check to see if the price is null
                        if (price != null) {
                            priceArrayList.add(price);
                            price = null;
                            card = null;
                            if (priceArrayList.size() >= 500) {
                                logger.info("going to save {} price",
                                            priceArrayList.size());
                                saved += priceArrayList.size();
                                priceRepository.saveAll(priceArrayList);
                                priceArrayList.clear();
                            }
                        }
                    }
                    if ("id".equals(fieldName)) {
                        String id = jsonParser.getValueAsString();
                        logger.trace("Id: {}.",
                                     id);
                        card = cardRepository.findById(id).orElse(null);
                        if (card == null) {
                            logger.trace("We could not find card with id {} in the database.",
                                         id);
                        } else {
                            price = new Price();
                            price.setCard(card);
                        }
                    } else if (price != null && "usd".equals(fieldName)) {
                        double usd = jsonParser.getValueAsDouble();
                        price.setUsd(usd);
                        logger.trace("usd: {}.",
                                     usd);
                    } else if (price != null && "tix".equals(fieldName)) {
                        double tix = jsonParser.getValueAsDouble();
                        price.setTix(tix);
                        logger.trace("tix: {}.",
                                     tix);
                    } else if (price != null && "eur".equals(fieldName)) {
                        double eur = jsonParser.getValueAsDouble();
                        price.setEur(eur);
                        logger.trace("eur: {}.",
                                     eur);
                    } else if (price != null && "usd_foil".equals(fieldName)) {
                        double usd_foil = jsonParser.getValueAsDouble();
                        price.setUsd_foil(usd_foil);
                        logger.trace("usd_foil: {}.",
                                     usd_foil);
                    }
                }
            }
            if (price != null) {
                priceArrayList.add(price);
            }
            if (priceArrayList.size() > 0) {
                logger.info("going to save {} price",
                            priceArrayList.size());
                priceRepository.saveAll(priceArrayList);
                priceArrayList.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Done updating the prices.  We went through {} cards and saved {} cards.",
                    count,
                    saved);
    }
}
