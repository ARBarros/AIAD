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
        for(/*String id : SumoCom.getAllVehiclesIds()*/ int i=0; i < 10 ; i++){

            try {
                mainContainer.acceptNewAgent("Driver-" + i, new DriverAgent(Integer.toString(i)));
                System.out.println("Created Agent " + i);
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }

        }
    }

}
