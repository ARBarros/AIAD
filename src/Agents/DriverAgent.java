package Agents;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import trasmapi.genAPI.Route;
import trasmapi.genAPI.exceptions.UnimplementedMethod;
import trasmapi.sumo.SumoCom;
import trasmapi.sumo.SumoVehicle;

import java.util.Random;


public class DriverAgent extends Agent {

    private SumoVehicle agentVehicle;
    public static Random rand;
    private String id;
    private double speed;

    public DriverAgent(String id){
        super();


        this.id = id;


        //this.agentVehicle = SumoCom.getVehicleById(rand.nextInt(SumoCom.vehicleTypesIDs.size()));
    }

    @Override
    protected void setup(){
        System.out.println("CARALHO");
        DFAgentDescription ad = new DFAgentDescription();
        ad.setName(getAID()); //agentID
        System.out.println("AID: "+ad.getName());

        ServiceDescription sd = new ServiceDescription();
        sd.setName(getName()); //nome do agente
        System.out.println("Nome: "+sd.getName());

        sd.setType("Driver");
        System.out.println("Tipo: "+sd.getType()+"\n\n\n");

        this.agentVehicle = SumoCom.getVehicleById(Integer.parseInt(id));
    }

    public double getSpeed(){
        if(speed != -1){
            speed = agentVehicle.getSpeed();
        }
        return speed;
    }

    public SumoVehicle getVehicle(){
        return agentVehicle;
    }

    public Route getRoute() throws UnimplementedMethod {
        return agentVehicle.getRoute();
    }

    public String getLaneId() throws UnimplementedMethod {
        //System.out.println(agentVehicle);
        return agentVehicle.getLaneId();
    }



    @Override
    protected void takeDown(){

    }
}
