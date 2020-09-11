package rez.mtg.price.controller;

import org.apache.commons.lang3.time.StopWatch;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import rez.mtg.price.exception.ResourceNotFoundException;
import rez.mtg.price.helper.ScryfallHelper;
import rez.mtg.price.magic.Card;
import rez.mtg.price.repository.CardRepository;

import java.io.IOException;
import java.util.List;

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
    List<Card> getAllCards() {
        logger.info("getting all cards");
        StopWatch sw = new StopWatch();
        sw.start();
        List<Card> cards = cardRepository.findAll();
        sw.stop();
        logger.info("Got all cards in {}.", sw.toString());
        return cards;
    }

    @GetMapping(path = "/test")
    public @ResponseBody
    void testEndPoint() {
        logger.info("testing");
        StopWatch sw = new StopWatch();
        sw.start();
        for(int i=0; i<100;i++)
        {
            try {
                Thread.sleep(100);
                sw.split();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        sw.stop();
        logger.info("Got all cards in {}.", sw.toString());
        logger.info("Got all cards in {}.", sw.toSplitString());
    }

    @GetMapping(path = "/{cardId}")
    public @ResponseBody
    Card getCard(@PathVariable("cardId")
                                 String cardId) {
        logger.info("cardId");
        return cardRepository.findById(cardId).orElseThrow(() -> new ResourceNotFoundException("could not find card with id: " + cardId));
    }

    @DeleteMapping(path = "/{cardId}")
    public void deleteCard(@PathVariable("cardId")
                                   String cardId) {
        logger.info("delete cardId");
        cardRepository.deleteById(cardId);
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

