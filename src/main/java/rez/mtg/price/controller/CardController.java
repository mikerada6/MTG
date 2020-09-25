package rez.mtg.price.controller;

import nu.pattern.OpenCV;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import rez.mtg.price.exception.ResourceNotFoundException;
import rez.mtg.price.helper.JSONHelper;
import rez.mtg.price.helper.ScryfallHelper;
import rez.mtg.price.magic.Card;
import rez.mtg.price.model.Temp;
import rez.mtg.price.repository.CardRepository;
import rez.mtg.price.repository.TempRepository;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgproc.Imgproc.INTER_AREA;
import static org.opencv.imgproc.Imgproc.resize;


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
    private TempRepository tempRepository;

    @Autowired
    private JSONHelper jsonHelper;

    @Value("${mtg.datapath}")
    private String downloadLocation;

    public static
    double byteArrayToDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }

    @GetMapping(path = "/")
    public @ResponseBody
    List<Card> getAllCards() {
        logger.info("getting all cards");
        StopWatch sw = new StopWatch();
        sw.start();
        List<Card> cards = cardRepository.findAll();
        sw.stop();
        logger.info("Got all cards in {}.",
                    sw.toString());
        return cards;
    }

    public
    Mat getCardImage(@PathVariable("id") String id) {
        OpenCV.loadLocally();
        String path = downloadLocation + "cards/" + "" + id + ".jpg";
        Mat img = imread(path);
        return img;
    }

    @GetMapping(path = "/dummy")
    public @ResponseBody String dummy()
    {
        String setName = "iko";
        logger.info("Going to load all the images from set {}",
                    setName);
        List<Card> cards = cardRepository.findAllBySet(setName);
        logger.info("{} cards identify as in the set.",
                    cards.size());
        int count =0;
        for (Card card : cards) {
            try {

                double[] array = getArrayOfImage(getCardImage(card.getId()));
                Temp temp = Temp.builder().cardId(card.getId()).set(card.getSet()).imageArray(array).build();
                tempRepository.save(temp);
                logger.info("double array is now {} elements", ++count);
            } catch (Exception e) {
                logger.error("Could not load card {} because of {}.",
                             card.getId(),
                             e);
            }
        }
        return "DONE";
    }

    @GetMapping(path = "/identify/set/{setName}")
    public @ResponseBody
    String identifyCardGivenSet(@PathVariable("setName") String setName, @RequestPart byte[] _unidentifiedCard) {
        long time = System.currentTimeMillis();
        OpenCV.loadLocally();
        try {
            saveImageToFile(downloadLocation + "identify",
                            "temp",
                            _unidentifiedCard);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String fileLocation = downloadLocation + "identify/temp.jpg";
        Mat unidentifiedCard = imread(fileLocation);
        double[] unidentifiedCardArray = getArrayOfImage(unidentifiedCard);
        logger.info("Going to load all the images from set {}",
                    setName);
        List<Card> cards = cardRepository.findAllBySet(setName);
        logger.info("{} cards identify as in the set.",
                    cards.size());
        ArrayList<Mat> mats = new ArrayList<>();
        ArrayList<double[]> doubleArrays = new ArrayList<>();
        for (Card card : cards) {
            try {
                doubleArrays.add(getArrayOfImage(getCardImage(card.getId())));
                logger.info("doube array is now {} elemets", doubleArrays.size());
            } catch (Exception e) {
                logger.error("Could not load card {} becaues of {}.",
                             card.getId(),
                             e);
            }
        }
        logger.info("We have loaded {} mats.",
                    mats.size());
        double max = Integer.MIN_VALUE;
        Card bestCard = null;
        for (double[] mat : doubleArrays) {
            double corr = new PearsonsCorrelation().correlation(unidentifiedCardArray,
                                                                mat);
            logger.info("corr: {}.",
                        corr);
        }

        return "done";
    }

    @GetMapping(path = "/set/{setName}/downloadImages")
    public @ResponseBody
    List<Card> downloadSetImages(@PathVariable("setName") String setName) {
        logger.info("getting all cards for set {}.",
                    setName);
        StopWatch sw = new StopWatch();
        sw.start();
        List<Card> cards = cardRepository.findAllBySet(setName);
        sw.stop();
        logger.info("Took {} to get {} cards from set {}.",
                    sw.toString(),
                    cards.size(),
                    setName);
        sw.reset();
        sw.start();
        for (Card card : cards) {
            downloadCard(card);
            try {
                Thread.sleep(75);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        sw.stop();
        logger.info("downloaded all cards in {}",
                    sw.toString());
        return cards;
    }

    private
    void downloadCard(Card card) {
        logger.info("Card URI: {}.",
                    card.getURI());
        String jsonString = jsonHelper.getRequest(card.getURI());
        JSONObject json = null;
        try {
            json = new JSONObject(jsonString);
            String largeImageURI = ((JSONObject) json.get("image_uris")).get("large").toString();
            byte[] image = getImageFromURL(largeImageURI);
            saveImageToFile(downloadLocation + "/cards/",
                            card.getId(),
                            image);
        } catch (JSONException | IOException e) {
            logger.error("JSONException {}",
                         e);
            e.printStackTrace();
        }

    }

    @GetMapping(path = "/test")
    public @ResponseBody
    void testEndPoint() {
        logger.info("testing");
        StopWatch sw = new StopWatch();
        sw.start();
        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(100);
                sw.split();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        sw.stop();
        logger.info("Got all cards in {}.",
                    sw.toString());
        logger.info("Got all cards in {}.",
                    sw.toSplitString());
    }

    @GetMapping(path = "/{cardId}")
    public @ResponseBody
    Card getCard(@PathVariable("cardId") String cardId) {
        logger.info("cardId");
        return cardRepository
                .findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("could not find card with id: " + cardId));
    }

    @DeleteMapping(path = "/{cardId}")
    public
    void deleteCard(@PathVariable("cardId") String cardId) {
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
            logger.error("ParseException {}",
                         e);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("IOException {}",
                         e);
        }
        if (fileLocation != null) {

        }
        return fileLocation;
    }

    private
    byte[] getImageFromURL(String s) throws IOException {
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
    private
    void saveImageToFile(String folder, String file, byte[] image) throws IOException {
        logger.info("Folder: " + folder);
        logger.info("\tfile: " + file);
        String PATH = "";
        String directoryName = PATH.concat(folder);
        String fileName = file + ".jpg";

        File directory = new File(directoryName);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        logger.info("Saving file at {}.",
                    folder);
        FileOutputStream fos = new FileOutputStream(folder + "/" + fileName);
        logger.info(folder + "/" + fileName);
        fos.write(image);
        fos.close();
    }

    private
    double[] getArrayOfImage(Mat src) {
        OpenCV.loadLocally();
        Mat resizeimage = new Mat();
        Size scaleSize = new Size(250,
                                  250);
        resize(src,
               resizeimage,
               scaleSize,
               0,
               0,
               INTER_AREA);
        String temp = resizeimage.dump();
        String[] split = temp.split(";");
        StringBuilder sb = new StringBuilder();
        for (String t : split) {
            sb.append(t + ",");
        }
        temp = sb.toString();
        temp = temp
                .replace("[",
                         "")
                .replace("]",
                         "")
                .replace(" ",
                         "");
        return Arrays.stream(temp.split(",")).mapToDouble(Double::parseDouble).toArray();
    }
}

