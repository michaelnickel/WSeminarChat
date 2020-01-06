package de.michey.wsem.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static void logServer(String message) {
        System.out.println("[Server] " + message);
    }

    public static void logClient(String message) {
        System.out.println("[Client] " + message);
    }

    public static String now() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

}
