package rez.mtg.price.controller;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import rez.mtg.price.exception.ResourceNotFoundException;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;

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

    @PostMapping(path = "/update")
    public @ResponseBody
    String updatePriceForTodayTest(@RequestParam(value = "file", required = false) String file) {
        StopWatch sw = new StopWatch();
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

            sw.start();
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
                            if (priceArrayList.size() >= 1000) {
                                sw.split();
                                saved += priceArrayList.size();
                                logger.info("Saved a total of {} in {}.",
                                            saved,
                                            sw.toSplitString());
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
            saved += priceArrayList.size();
            logger.info("going to save {} price",
                        priceArrayList.size());
            priceRepository.saveAll(priceArrayList);
            priceArrayList.clear();
        }
        long end = System.currentTimeMillis();
        sw.stop();
        logger.info("Done updating the prices.  We went through {} cards and saved {} cards in {}.",
                    count,
                    saved,
                    sw.toString());
        return ("Done updating the prices.  We went through " + count + " cards and saved " + saved + " cards.");
    }

    @GetMapping(path = "/card/{cardId}/date/{date}")
    public @ResponseBody
    HashMap<String, Object> getCard(@PathVariable("cardId") String cardId, @PathVariable("date") String date) {
        logger.info("finding price for cardId: {} on date {}", cardId, date);
        Card card = cardRepository
                .findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("could not find card with id: " + cardId));
        Set<Price> prices = priceRepository.findAllByCardId(card.getId());
        HashMap<String, Object> map = new HashMap<>();
        Map<Date, List<Price>> priceMap = prices.stream().filter(p -> p.getDate().toString().equalsIgnoreCase(date)).collect(groupingBy(Price::getDate));
        return getStringObjectHashMap(card,
                                      map,
                                      priceMap);
    }

    @GetMapping(path = "/card/{cardId}")
    public @ResponseBody
    HashMap<String, Object> getCard(@PathVariable("cardId") String cardId) {
        logger.info("finding price for cardId: {}", cardId);
        Card card = cardRepository
                .findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("could not find card with id: " + cardId));
        Set<Price> prices = priceRepository.findAllByCardId(card.getId());
        HashMap<String, Object> map = new HashMap<>();
        Map<Date, List<Price>> priceMap = prices.stream().collect(groupingBy(Price::getDate));
        return getStringObjectHashMap(card,
                                      map,
                                      priceMap);
    }

    private
    HashMap<String, Object> getStringObjectHashMap(Card card, HashMap<String, Object> map,
                                                   Map<Date, List<Price>> priceMap) {
        Map<String, Object> tempPriceMap = new HashMap<>();
        for (Date key : priceMap.keySet()) {
            Price price = priceMap.get(key).get(0);
            HashMap<String, Object> temp = new HashMap<>();
            if (price.getUsd() != null) {
                temp.put("usd",
                         price.getUsd());
            }
            if (price.getUsd_foil() != null) {
                temp.put("usd_foil",
                         price.getUsd_foil());
            }
            if (price.getTix() != null) {
                temp.put("tix",
                         price.getTix());
            }
            if (price.getEur() != null) {
                temp.put("eur",
                         price.getEur());
            }
            if (temp.size() > 0) {
                tempPriceMap.put(key.toString(),
                        temp);
            }
        }
        map.put("card",
                card);
        map.put("price",
                tempPriceMap);
        return map;
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
