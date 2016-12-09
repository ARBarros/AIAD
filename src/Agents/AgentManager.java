package Agents;

import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import trasmapi.genAPI.TrafficLight;
import trasmapi.genAPI.exceptions.UnimplementedMethod;
import trasmapi.sumo.Sumo;
import trasmapi.sumo.SumoTrafficLight;
import trasmapi.sumo.SumoVehicle;
import trasmapi.sumo.SumoCom;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.HashMap;

import static trasmapi.sumo.SumoCom.arrivedVehicles;
import static trasmapi.sumo.SumoCom.vehicles;

/**
 * Created by Andre on 06/12/2016.
 */
public class AgentManager {
    ArrayList<String> vehiclesId;
    ArrayList<String> trafficLightsId;
    ArrayList<TrafficLightAgent> tlAgents = new ArrayList<TrafficLightAgent>();
    ArrayList<DriverAgent> driverAgents = new ArrayList<DriverAgent>();
    ArrayList<SumoTrafficLight> trafficLights = new ArrayList<SumoTrafficLight>();
    Sumo sumo;
    ContainerController mainContainer;
    ArrayList<SumoVehicle> vehicles = new ArrayList<SumoVehicle>();
    ArrayList<String> arrivedVehicles = new ArrayList<String>();


    public AgentManager(Sumo sumo, ContainerController mainContainer){

        this.mainContainer = mainContainer;
        vehiclesId = null;
        trafficLightsId = SumoTrafficLight.getIdList();
        SumoCom.createAllRoutes();

    }

    public void updateDrivers() throws UnimplementedMethod {
        if(vehiclesId == null) {
            vehiclesId = SumoCom.getAllVehiclesIds();
            for (String id : vehiclesId) {
                System.out.println("crl");
                try {
                    DriverAgent toAdd = new DriverAgent(id);

                    mainContainer.acceptNewAgent("Driver-" + id, toAdd);
                    driverAgents.add(toAdd);

                    vehicles.add(new SumoVehicle(id));
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
                        vehicles.add(new SumoVehicle(id));
                        //System.out.println("Criou agente novo " + id);
                    } catch (StaleProxyException e) {
                        e.printStackTrace();
                    }
                }

                for(SumoVehicle v : vehicles){
                    if (SumoCom.arrivedVehicles.contains(v.id)) {
                        vehicles.remove(v);
                        vehiclesId.remove(v.id);
                    }
                }
            }
        }

        for(String v : SumoCom.getArrivedVehicles()){
            arrivedVehicles.add(v);
        }
    }

    public void startTrafficLights(){

        for(String id : trafficLightsId){
            try {
                SumoTrafficLight temp = new SumoTrafficLight(id);
                trafficLights.add(temp);
                TrafficLightAgent tempAgent = new TrafficLightAgent(id, temp);
                mainContainer.acceptNewAgent("TF- " + id, tempAgent);
                tlAgents.add(tempAgent);

                System.out.println("Novo Semaforo " + id);
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
        System.out.println(trafficLights);
    }

    public void updateTrafficLights() throws UnimplementedMethod {

        HashMap<String, Integer> stoppedVehicles = getNumVehiclesStoppedPerEdgeID();
        //System.out.println(stoppedVehicles);

        for(TrafficLightAgent tl: tlAgents){
            ArrayList<String> lanes = tl.getLanes();
            int vehicleCount = 0;
            for(String lane : lanes){
                //System.out.println(stoppedVehicles);
                lane = lane.replace("_0", "");
                //System.out.println(lane);

                if(!stoppedVehicles.isEmpty()){
                    try{
                        vehicleCount = stoppedVehicles.get(lane);
                        //System.out.println(vehicleCount);
                        if(vehicleCount >= 3){
                            tl.update(true);
                            break;
                        }
                    }catch (NullPointerException e){
                        vehicleCount = 0;
                    }


                }

            }

                tl.update(false);


        }

    }

    public  HashMap<String, Integer> getNumVehiclesStoppedPerEdgeID() throws UnimplementedMethod {

        HashMap<String, Integer> vehPerEdge = new HashMap<String, Integer>();

        //System.out.println("veiculos " + vehicles);

        for(SumoVehicle v: vehicles){
            //System.out.println("ARRIVED " + v.edgeId);
            //System.out.println("ARRIVED VEHICLES " + arrivedVehicles);
            //System.out.println(v.id);
            if(!arrivedVehicles.contains(v.id)){
                String edge = v.getEdgeId();
                System.out.println("EDGE " + edge);


                if(!vehPerEdge.containsKey(edge) && v.speed < 0.3)
                    vehPerEdge.put(edge, 1);
                else if(v.speed < 0.3)
                    vehPerEdge.put(edge, vehPerEdge.get(edge)+1);
            }

        }

        return vehPerEdge;
    }

    public int countStoppedCars(TrafficLightAgent tl) throws UnimplementedMethod {

        int count = 0;
        for(DriverAgent driver : driverAgents){
            System.out.println("CICLO 1");
            for(String laneId : tl.getLanes()){
                System.out.println("TL " + tl.getLanes() + "DRIVER " + driver.getLaneId());
                if(laneId == driver.getLaneId() && driver.getSpeed() == 0) {
                    System.out.println("mais um");
                    count++;
                }

                if(count >= 3){
                    return count;
                }
            }
        }
        return count;
    }

}
