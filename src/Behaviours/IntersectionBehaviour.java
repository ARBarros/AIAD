package Behaviours;

import Agents.DriverAgent;
import Agents.TrafficLightAgent;
import Agents.AgentManager;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.wrapper.StaleProxyException;
import trasmapi.genAPI.exceptions.UnimplementedMethod;
import trasmapi.sumo.SumoCom;
import trasmapi.sumo.SumoVehicle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

/**
 * Created by Andre on 09/12/2016.
 */
public class IntersectionBehaviour extends CyclicBehaviour {

    ArrayList<SumoVehicle> vehicles = new ArrayList<SumoVehicle>();
    TrafficLightAgent tl;
    AgentManager manager;
    private ArrayList<String> vehiclesId;
    Semaphore semaphore;

    public IntersectionBehaviour(TrafficLightAgent tl, AgentManager manager){
        System.out.println("BEHAVIOUR CRIADO");
        this.tl = tl;
        this.manager = manager;
        semaphore = new Semaphore(1);
    }


    @Override
    public void action() {
        System.out.println("ENTRA BEHAVIOUR");
        updateInfo();


        HashMap<String, Integer> stoppedVehicles = null;
        try {
            stoppedVehicles = getNumVehiclesStoppedPerEdgeID();
        } catch (UnimplementedMethod unimplementedMethod) {
            unimplementedMethod.printStackTrace();
        }
        //System.out.println(stoppedVehicles);


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
            System.out.println("PASTEISSSSSSSSS");
            tl.update(false);



    }

    public void updateInfo(){


        if(vehiclesId == null) {
            vehiclesId = SumoCom.getAllVehiclesIds();
            for (String id : vehiclesId) {
                //System.out.println("crl");

                    //DriverAgent toAdd = new DriverAgent(id);

                    //mainContainer.acceptNewAgent("Driver-" + id, toAdd).start();
                    //driverAgents.add(toAdd);

                    vehicles.add(new SumoVehicle(id));
                    //System.out.println("Created Agent " + id);


            }
        }else{
            for(String id : SumoCom.getAllVehiclesIds()){
                if(!vehiclesId.contains(id)){

                        //mainContainer.acceptNewAgent("Driver-" + id, new DriverAgent(id));
                        vehiclesId.add(id);
                        vehicles.add(new SumoVehicle(id));
                        //System.out.println("Criou agente novo " + id);

                }

                for(SumoVehicle v : vehicles){
                    if (SumoCom.arrivedVehicles.contains(v.id)) {
                        vehicles.remove(v);
                        vehiclesId.remove(v.id);
                    }
                }
            }
        }
/*
        for(String v : SumoCom.getArrivedVehicles()){
            //System.out.println("A TRAP DO CONTADOR");
            try {
                semaphore.acquire();
                System.out.println("acquire 1");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            arrivedVehicles.add(v);

            semaphore.release();
            System.out.println("release 1");

            //System.out.println(arrivedVehicles);
            System.out.println(SumoCom.getArrivedVehicles());
        }
        System.out.println(SumoCom.getArrivedVehicles());

        //System.out.println("A GRANDE PUTA QUE TE PARIU " + SumoCom.getArrivedVehicles());
        */
    }

    public HashMap<String, Integer> getNumVehiclesStoppedPerEdgeID() throws UnimplementedMethod {


        HashMap<String, Integer> vehPerEdge = new HashMap<String, Integer>();



        //System.out.println("veiculos " + vehicles);

        for(Iterator<SumoVehicle> it = vehicles.iterator(); it.hasNext();){

            SumoVehicle v = it.next();


            //System.out.println("ARRIVED " + v.edgeId);
            //System.out.println("ARRIVED VEHICLES " + arrivedVehicles);
            //System.out.println(v.id);
            //semaphore.release();
            try {
                semaphore.acquire();
                System.out.println("acquire 2");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //System.out.println("AQUI PUTA " + arrivedVehicles + " " + v.id);
            System.out.println(v.id + " " + manager.arrivedVehicles);
            if(!manager.arrivedVehicles.contains(v.id)){
                System.out.println("entrararrar");

                String edge = v.getEdgeId();
                //System.out.println("EDGE " + edge);


                if(!vehPerEdge.containsKey(edge) && v.speed < 0.3)
                    vehPerEdge.put(edge, 1);
                else if(v.speed < 0.3)
                    vehPerEdge.put(edge, vehPerEdge.get(edge)+1);
            }
            semaphore.release();
            System.out.println("release 2");

        }






        return vehPerEdge;

    }


}
