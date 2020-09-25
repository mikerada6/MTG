package rez.mtg.price.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@ManagedResource(description = "Alive Controller")
@Controller
@RequestMapping("/magic")
public class AliveController {

    private static boolean _alive = true;

    @Value("${mtg.version}")
    private String version;

    private static final Logger logger = LoggerFactory.getLogger(AliveController.class);


    /**
     * Endpoint to check if the application is "alive". For use by load-balancer？
     *
     * @return true if alive
     */
    @RequestMapping(value = "/alive", method = RequestMethod.GET)
    public @ResponseBody
    Boolean alive() {
        logger.info("alive");
        return _alive;
    }

    /**
     * @return the alive
     */
    @ManagedAttribute(description = "The Alive Attribute")
    public boolean isAlive() {
        return _alive;
    }

    /**
     * @param alive
     *            the alive to set
     */
    @ManagedAttribute(description = "The Alive Attribute")
    public static void setAlive(boolean alive) {
        _alive = alive;
    }

    @GetMapping(path = "/version")
    public @ResponseBody
    String updatePriceForTodayTest() {
        logger.info("version");
        String ans = "Version: " + version;

        return ans;
    }
}
