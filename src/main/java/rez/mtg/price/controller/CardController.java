package rez.mtg.price.controller;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import org.apache.commons.lang3.time.StopWatch;
import org.json.JSONException;
import org.json.JSONObject;
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
import rez.mtg.price.helper.JSONHelper;
import rez.mtg.price.helper.ScryfallHelper;
import rez.mtg.price.magic.Card;
import rez.mtg.price.repository.CardRepository;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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

    @Autowired
    private JSONHelper jsonHelper;

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

    @GetMapping(path = "/set/{setName}/downloadImages")
    public @ResponseBody
    List<Card> downloadSetImages(@PathVariable("setName")
                                         String setName) {
        logger.info("getting all cards for set {}.", setName);
        StopWatch sw = new StopWatch();
        sw.start();
        List<Card> cards = cardRepository.findAllBySet(setName);
        sw.stop();
        logger.info("Took {} to get {} cards from set {}.", sw.toString(), cards.size(), setName);
        sw.reset();
        sw.start();
        for(Card card: cards)
        {
            downloadCard(card);
            try {
                Thread.sleep(75);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        sw.stop();
        logger.info("downloaded all cards in {}", sw.toString());
        return cards;
    }

    private void downloadCard(Card card)
    {
        logger.info("Card URI: {}.", card.getURI());
        String jsonString = jsonHelper.getRequest(card.getURI());
        JSONObject json = null;
        try {
            json = new JSONObject(jsonString);
            String largeImageURI = ((JSONObject) json.get("image_uris")).get("large").toString();
            byte[] image = getImageFromURL(largeImageURI);
            saveImageToFile(downloadLocation+"/cards/", card.getId(),image);
        } catch (JSONException | IOException e) {
            logger.error("JSONException {}", e);
            e.printStackTrace();
        }

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

    private byte[] getImageFromURL(String s) throws IOException {
        URL url = new URL(s);
        InputStream in = new BufferedInputStream(url.openStream());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n = 0;
        while (-1 != (n = in.read(buf))) {
            out.write(buf,
                      0,
                      n);
        }
        out.close();
        in.close();
        return out.toByteArray();
    }

    /**
     * @param folder
     * @param file
     * @param image
     * @throws IOException
     */
    private void saveImageToFile(String folder, String file, byte[] image) throws IOException {
        logger.info("Folder: " + folder);
        logger.info("\tfile: " + file);
        String PATH = "";
        String directoryName = PATH.concat(folder);
        String fileName = file + ".jpg";

        File directory = new File(directoryName);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        logger.info("Saving file at {}.", folder);
        FileOutputStream fos =
                new FileOutputStream(folder + "/" + fileName);
        logger.info(folder + "/" + fileName);
        fos.write(image);
        fos.close();
    }
}

