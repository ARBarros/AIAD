package Agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
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
import java.util.Iterator;
import java.util.concurrent.Semaphore;

import static trasmapi.sumo.SumoCom.arrivedVehicles;
import static trasmapi.sumo.SumoCom.vehicles;

/**
 * Created by Andre on 06/12/2016.
 */
public class AgentManager extends Agent {
    private ArrayList<String> vehiclesId;
    private ArrayList<String> trafficLightsId;
    private ArrayList<TrafficLightAgent> tlAgents = new ArrayList<TrafficLightAgent>();
    private ArrayList<DriverAgent> driverAgents = new ArrayList<DriverAgent>();
    private ArrayList<SumoTrafficLight> trafficLights = new ArrayList<SumoTrafficLight>();
    private Sumo sumo;
    private ContainerController mainContainer;
    private ArrayList<SumoVehicle> vehicles = new ArrayList<SumoVehicle>();
    public ArrayList<String> arrivedVehicles = new ArrayList<String>();
    private String type;
    private Semaphore semaphore;


    public AgentManager(Sumo sumo, ContainerController mainContainer, String type){

        this.mainContainer = mainContainer;
        vehiclesId = null;
        trafficLightsId = SumoTrafficLight.getIdList();
        System.out.println("TF»RAFFIC LIGHTS " + trafficLightsId);
        SumoCom.createAllRoutes();
        this.type = type;
        semaphore = new Semaphore(1);

    }

    @Override
    public void setup(){
        DFAgentDescription ad = new DFAgentDescription();
        ad.setName(getAID()); //agentID
        System.out.println("AID: "+ad.getName());

        ServiceDescription sd = new ServiceDescription();
        sd.setName(getName()); //nome do agente
        System.out.println("Nome: "+sd.getName());

        sd.setType("Driver");
        System.out.println("Tipo: "+sd.getType()+"\n\n\n");

        ad.addServices(sd);

        try {
            DFService.register(this, ad);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        super.setup();
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        super.takeDown();
    }

    public void killAgents(){
        for(TrafficLightAgent agent: tlAgents){
            agent.stop(true);
        }
    }

    public void updateDrivers() throws UnimplementedMethod {
        if(vehiclesId == null) {
            vehiclesId = SumoCom.getAllVehiclesIds();
            for (String id : vehiclesId) {
                //System.out.println("crl");
                try {
                    DriverAgent toAdd = new DriverAgent(id);

                    mainContainer.acceptNewAgent("Driver-" + id, toAdd).start();
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
            //System.out.println("A TRAP DO CONTADOR");
            arrivedVehicles.add(v);
            //System.out.println(arrivedVehicles);
            //System.out.println(SumoCom.getArrivedVehicles());
        }
        //System.out.println("A GRANDE PUTA QUE TE PARIU " + SumoCom.getArrivedVehicles());
        //System.out.println(arrivedVehicles);

    }

    public void startTrafficLights(String type){

        //SumoCom.createAllRoutes();

        for(String id : trafficLightsId){
            try {

                SumoTrafficLight temp = new SumoTrafficLight(id);
                trafficLights.add(temp);
                TrafficLightAgent tempAgent;
                if(type.equals("learning")){
                    tempAgent = new InteligentTrafficLight(id, temp, this, type);
                }else{
                    tempAgent = new TrafficLightAgent(id, temp, this, type);
                }

                mainContainer.acceptNewAgent("TF- " + id, tempAgent).start();
                tlAgents.add(tempAgent);

                System.out.println("Novo Semaforo " + id);
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
        System.out.println(trafficLights);
    }

    public void updateTrafficLightsBasic(){
        for(TrafficLightAgent tl: tlAgents){
            tl.update(false);
        }
    }

    public void updateTrafficLightsInt() throws UnimplementedMethod {

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
                        if(vehicleCount >= 5){
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

    public HashMap<String, Integer> getNumVehiclesStoppedPerEdgeID() throws UnimplementedMethod {

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HashMap<String, Integer> vehPerEdge = new HashMap<String, Integer>();



        //System.out.println("veiculos " + vehicles);

        for(Iterator<SumoVehicle> it = vehicles.iterator(); it.hasNext();){

            SumoVehicle v = it.next();


            //System.out.println("ARRIVED " + v.edgeId);
            //System.out.println("ARRIVED VEHICLES " + arrivedVehicles);
            //System.out.println(v.id);
            System.out.println("AQUI PUTA " + arrivedVehicles + " " + v.id);
            if(!arrivedVehicles.contains(v.id)){
                String edge = v.getEdgeId();
                System.out.println("EDGE " + edge);


                if(!vehPerEdge.containsKey(edge) && v.speed < 0.3)
                    vehPerEdge.put(edge, 1);
                else if(v.speed < 0.3)
                    vehPerEdge.put(edge, vehPerEdge.get(edge)+1);
            }

        }

        semaphore.release();

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
