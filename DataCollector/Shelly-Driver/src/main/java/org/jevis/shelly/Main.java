package org.jevis.shelly;

public class Main {


    public static void main(String[] args) throws Exception {


        Configuration configuration = new Configuration("C:/Users/fs/Documents/test.conf");


        ShellyCloudClient shellyCloudClient = new ShellyCloudClient(configuration);
    }
}
