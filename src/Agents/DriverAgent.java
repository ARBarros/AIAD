package Agents;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import trasmapi.sumo.SumoVehicle;

import java.util.Random;


public class DriverAgent extends Agent {

    private SumoVehicle agentVehicle;
    public static Random rand;

    public DriverAgent(){
        super();
    }

    @Override
    protected void setup(){
        DFAgentDescription ad = new DFAgentDescription();
        ad.setName(getAID()); //agentID
        System.out.println("AID: "+ad.getName());

        ServiceDescription sd = new ServiceDescription();
        sd.setName(getName()); //nome do agente
        System.out.println("Nome: "+sd.getName());

        sd.setType("Driver");
        System.out.println("Tipo: "+sd.getType()+"\n\n\n");
    }

    @Override
    protected void takeDown(){

    }
}
