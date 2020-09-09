package rez.mtg.price.controller;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
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
import java.util.Calendar;

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
    String updateBulk() {
        JSONArray arrayData = null;
        ArrayList<Price> priceArrayList = new ArrayList<Price>();
        try {
            String file = scryfallHelper.downloadDailyBulkData();
            arrayData = scryfallHelper.openDownloadedJson(file);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (arrayData != null) {
            for (int i = 0; i < arrayData.size(); i++) {
                JSONObject datum = ((JSONObject) arrayData.get(i));
                JSONObject prices = ((JSONObject) datum.get("prices"));
                String cardId = datum.containsKey("id") ? datum.get("id").toString() : null;
                double usd = -1;
                double tix = -1;
                double eur = -1;
                double usd_foil = -1;
                //TODO make this better
                try {
                    usd = prices.get("usd") == null ? null : Double.parseDouble((String) prices.get("usd"));
                } catch (Exception e) {

                }
                try {
                    tix = prices.get("tix") == null ? null : Double.parseDouble((String) prices.get("tix"));
                } catch (Exception e) {

                }
                try {
                    eur = prices.get("eur") == null ? null : Double.parseDouble((String) prices.get("eur"));
                } catch (Exception e) {

                }
                try {
                    usd_foil = prices.get("usd_foil") ==
                               null ? null : Double.parseDouble((String) prices.get("usd_foil"));
                } catch (Exception e) {

                }

                Price price = new Price();
                Card card = cardRepository.findById(cardId)
                                          .orElse(null);
                if (card != null) {
                    price.setCard(card);
                    price.setDate(new java.sql.Date(Calendar.getInstance().getTime().getTime()));
                    price.setUsd(usd == -1.0 ? null : usd);
                    price.setUsd_foil(usd_foil == -1.0 ? null : usd_foil);
                    price.setTix(tix == -1.0 ? null : tix);
                    price.setEur(eur == -1.0 ? null : eur);
                    if (usd + usd_foil + tix + eur > -4) {
                        //only save a price if its not all null
                        priceArrayList.add(price);
                        if (priceArrayList.size() == 500) {
                            logger.info("Saving some price data");
                            priceRepository.saveAll(priceArrayList);
                            priceArrayList.clear();
                        }
                    }
                }
            }
            priceRepository.saveAll(priceArrayList);
        }
        logger.info("Done price update");
        return "done";
    }
}
