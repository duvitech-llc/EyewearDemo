package com.six15.eyeweardemo;

import java.util.HashMap;

/**
 * Created by George on 9/19/2015.
 */
public class Six15GattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String SIX15_DATA_RX = "0000fff4-0000-1000-8000-00805f9b34fb";
    public static String SIX15_BLE_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";

    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Generic Access Profile");
        attributes.put("00001801-0000-1000-8000-00805f9b34fb", "Generic Attribute Profile");
        attributes.put("0000fff0-0000-1000-8000-00805f9b34fb", "Six-15 Comm Service");

        attributes.put("0000fff3-0000-1000-8000-00805f9b34fb", "Six-15 RX Characteristic");
        attributes.put("0000fff4-0000-1000-8000-00805f9b34fb", "Six-15 TX Characteristic");

        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }

}
