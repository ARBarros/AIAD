package Agents;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * Created by Andre on 06/12/2016.
 */
public class TrafficLightAgent extends Agent {

    private String id;

    public TrafficLightAgent(String id){
        super();
        this.id = id;
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
    }

    @Override
    public void takeDown(){

    }
}
