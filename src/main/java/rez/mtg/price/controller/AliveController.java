package rez.mtg.price.controller;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@ManagedResource(description = "Alive Controller")
@Controller
@RequestMapping("/magic")
public class AliveController {

    private static boolean _alive = true;

    /**
     * Endpoint to check if the application is "alive". For use by load-balancer？
     *
     * @return true if alive
     */
    @RequestMapping(value = "/alive", method = RequestMethod.GET)
    public @ResponseBody
    Boolean alive() {
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
}
