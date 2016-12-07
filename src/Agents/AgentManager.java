package Agents;

import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import trasmapi.sumo.Sumo;
import trasmapi.sumo.SumoVehicle;
import trasmapi.sumo.SumoCom;

import java.util.ArrayList;

/**
 * Created by Andre on 06/12/2016.
 */
public class AgentManager {
    ArrayList<SumoVehicle> vehicles;
    Sumo sumo;

    public AgentManager(Sumo sumo, ContainerController mainContainer){

        //inicialização dos agentes dos veiculos
        System.out.println("CARALHOOOOOO");
        System.out.println(SumoCom.vehicles);
        System.out.println("cenas");
        for(String id : SumoCom.getAllVehiclesIds()){
            System.out.println("crl");
            try {
                mainContainer.acceptNewAgent("Driver-" + id, new DriverAgent(id));
                System.out.println("Created Agent " + id);
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }

        }
    }

}
