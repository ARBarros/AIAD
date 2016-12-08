package Statistics;

import trasmapi.genAPI.TrafficLight;
import trasmapi.genAPI.exceptions.UnimplementedMethod;
import trasmapi.sumo.Sumo;
import trasmapi.sumo.SumoCom;
import trasmapi.sumo.SumoTrafficLight;
import trasmapi.sumo.SumoVehicle;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by m_bot on 07/12/2016.
 */
public class SpeedStatistics implements Runnable{

    Sumo sumo;
    private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public SpeedStatistics(Sumo sumo) {
        this.sumo = sumo;
    }

    @Override
    public void run() {
        Boolean run = true;
        ArrayList<String> vehicles = new ArrayList<String>();
        double time = 0.0;
        double average = 0.0;
        double tempTime = 0.0;

        Calendar cal = Calendar.getInstance();
        String temp = sdf.format(cal.getTime());
        temp = temp.substring(0, 10);
        temp = temp.replaceAll("/", ".");

        String name = "statistics_speed:" + ".txt";
        FileWriter file;

        try {

            while(run){

                file = new FileWriter(name, true);
                vehicles = SumoCom.getAllVehiclesIds();
                time = 0.0;

                for ( int i = 0; i < vehicles.size(); i++){
                    SumoVehicle ve = new SumoVehicle(vehicles.get(i));
                    tempTime = ve.getSpeed();
                    if ( tempTime != -1 ) {
                        file.write("Vehicle id: " + vehicles.get(i) + ", speed: " + tempTime);
                        file.write("\r\n");
                        System.err.println("Vehicle id: " + vehicles.get(i) + ", speed: " + tempTime);

                    }
                }
                file.write("\r\n");
                file.write("\r\n");
                file.write("\r\n");
                file.close();
                Thread.sleep(5000);
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

}