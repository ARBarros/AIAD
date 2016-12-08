package Agents;

import Statistics.LightsStatistics;
import Statistics.SpeedStatistics;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import trasmapi.sumo.Sumo;
import trasmapi.sumo.SumoTrafficLight;
import trasmapi.sumo.SumoVehicle;
import trasmapi.sumo.SumoCom;

import java.util.ArrayList;

import static trasmapi.sumo.SumoCom.vehicles;

/**
 * Created by Andre on 06/12/2016.
 */
public class AgentManager {
    ArrayList<String> vehiclesId;
    ArrayList<String> trafficLightsId;
    Sumo sumo;
    ContainerController mainContainer;
    LightsStatistics lightsStatistics;
    SpeedStatistics speedStatistics;


    public AgentManager(Sumo sumo, ContainerController mainContainer){

        this.mainContainer = mainContainer;
        vehiclesId = null;
        trafficLightsId = SumoTrafficLight.getIdList();
        lightsStatistics = new LightsStatistics(sumo);
        speedStatistics = new SpeedStatistics(sumo);
        lightsStatistics = new LightsStatistics(sumo);

        new Thread(speedStatistics).start();
        new Thread(lightsStatistics).start();
    }

    public void updateDrivers(){
        if(vehiclesId == null) {
            vehiclesId = SumoCom.getAllVehiclesIds();
            for (String id : vehiclesId) {
                System.out.println("crl");
                try {
                    mainContainer.acceptNewAgent("Driver-" + id, new DriverAgent(id));
                    System.out.println("Created Agent " + id);
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }

            }
        }else{
            for(String id : SumoCom.getAllVehiclesIds()){
                if(!vehiclesId.contains(id)){
                    try {
                        mainContainer.acceptNewAgent("Driver-" + id, new DriverAgent(id));
                        vehiclesId.add(id);
                        System.out.println("Criou agente novo " + id);
                    } catch (StaleProxyException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void startTrafficLights(){

        for(String id : trafficLightsId){
            try {
                mainContainer.acceptNewAgent("TF- " + id, new TrafficLightAgent(id));
                System.out.println("Novo Semaforo " + id);
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }


}
