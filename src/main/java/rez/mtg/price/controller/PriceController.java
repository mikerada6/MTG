package rez.mtg.price.controller;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import rez.mtg.price.helper.JSONHelper;
import rez.mtg.price.helper.ScryfallHelper;
import rez.mtg.price.magic.Card;
import rez.mtg.price.magic.Price;
import rez.mtg.price.repository.CardRepository;
import rez.mtg.price.repository.PriceRepository;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

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

    @Value("${mtg.datapath}")
    private String downloadLocation;

    @GetMapping(path = "/version")
    public @ResponseBody
    String updatePriceForTodayTest()
    {
        return "Version: 1";
    }


    @PostMapping(path = "/update")
    public @ResponseBody
    String updatePriceForTodayTest(@RequestParam(value = "file", required = false) String file) {
        logger.info("test");
        int count = 0;
        int saved = 0;
        SimpleDateFormat sdf1 = new SimpleDateFormat("MM-dd-yyyy");
        Date date = new Date(Calendar.getInstance().getTime().getTime());
        JsonParser jsonParser = null;
        ArrayList<Price> priceArrayList = new ArrayList<Price>();
        try {
            if (file == null) {
                logger.info("no input file going to download the latest data");
                file = scryfallHelper.downloadDailyBulkData();
            } else {
                String year = StringUtils.leftPad(file.substring(0,
                                                                 4),
                                                  4);
                String month = StringUtils.leftPad(file.substring(4,
                                                                  6),
                                                   2);
                String day = StringUtils.leftPad(file.substring(6,
                                                                8),
                                                 2);
                date = new java.sql.Date(sdf1.parse(month + "-" + day + "-" + year).getTime());
                file = downloadLocation + file;
                logger.info("input file using {}.",
                            file);
            }
            jsonParser = scryfallHelper.openDownloadedJson(file);
        } catch (ParseException e) {
            e.printStackTrace();
            logger.error("ParseException {}.",
                         e);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("IOException {}.",
                         e);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            logger.error("ParseException {}.",
                         e);
        }
        try {

            while (!jsonParser.isClosed()) {
                count++;
                JsonToken jsonToken = jsonParser.nextToken();
                if (JsonToken.START_OBJECT.equals(jsonToken)) {
                    HashMap<String, Object> map = getObject(jsonParser);
                    if (map.containsKey("lang") && map.get("lang").equals("en")) {
                        Card card = cardRepository.findById(map.get("id").toString()).orElse(null);
                        if (card == null) {
                            logger.info("Card ID:{} | {} was not in the database.  So we are creating one.",
                                        map.get("id").toString(),
                                        map.get("name").toString());
                            card = new Card();
                            card.setName(map.get("name").toString());
                            card.setId(map.get("id").toString());
                            cardRepository.save(card);
                        }
                        if (map.containsKey("prices")) {
                            HashMap<String, Object> prices = (HashMap<String, Object>) map.get("prices");
                            Price price = new Price();
                            price.setCard(card);
                            price.setDate(date);
                            if (prices.containsKey("usd")) {
                                if (prices.get("usd") != null) {
                                    double value = Double.parseDouble(prices.get("usd").toString());
                                    price.setUsd(value);
                                } else price.setUsd(null);
                            }
                            if (prices.containsKey("usd_foil")) {
                                if (prices.get("usd_foil") != null) {
                                    double value = Double.parseDouble(prices.get("usd_foil").toString());
                                    price.setUsd_foil(value);
                                } else price.setUsd_foil(null);
                            }
                            if (prices.containsKey("tix")) {
                                if (prices.get("tix") != null) {
                                    double value = Double.parseDouble(prices.get("tix").toString());
                                    price.setTix(value);
                                } else price.setTix(null);
                            }
                            if (prices.containsKey("eur")) {
                                if (prices.get("eur") != null) {
                                    double value = Double.parseDouble(prices.get("eur").toString());
                                    price.setEur(value);
                                } else price.setEur(null);
                            }
                            priceArrayList.add(price);
                            if (priceArrayList.size() >= 500) {
                                logger.info("going to save {} price",
                                            priceArrayList.size());
                                saved += priceArrayList.size();
                                priceRepository.saveAll(priceArrayList);
                                priceArrayList.clear();
                            }
                        }

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("IOException {}.",
                         e);
        }
        if (priceArrayList.size() > 0) {
            count += priceArrayList.size();
            logger.info("going to save {} price",
                        priceArrayList.size());
            priceRepository.saveAll(priceArrayList);
            priceArrayList.clear();
        }
        logger.info("Done updating the prices.  We went through {} cards and saved {} cards.",
                    count,
                    saved);
        return ("Done updating the prices.  We went through " + count + " cards and saved " + saved + " cards.");
    }

    private
    HashMap<String, Object> getObject(JsonParser jsonParser) {
        HashMap<String, Object> map = new HashMap<>();
        try {
            JsonToken jsonToken = jsonParser.nextToken();
            while (!JsonToken.END_OBJECT.equals(jsonToken)) {
                if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                    String fieldName = jsonParser.getCurrentName();
                    jsonToken = jsonParser.nextToken();
                    if (JsonToken.START_ARRAY.equals(jsonToken)) {
                        jsonToken = jsonParser.nextToken();
                        ArrayList<Object> list = new ArrayList<>();
                        while (!JsonToken.END_ARRAY.equals(jsonToken)) {
                            list.add(jsonParser.getValueAsString());
                            jsonToken = jsonParser.nextToken();
                        }
                        map.put(fieldName,
                                list);
                    } else if (JsonToken.START_OBJECT.equals(jsonToken)) {
                        HashMap<String, Object> temp = getObject(jsonParser);
                        map.put(fieldName,
                                temp);
                    } else {
                        String value = jsonParser.getValueAsString();
                        map.put(fieldName,
                                value);
                    }
                } else {
                    jsonToken = jsonParser.nextToken();
                }
            }
        } catch (IOException e) {
            logger.error("IOException {}.",
                         e);
            e.printStackTrace();
        }
        return map;
    }

}
