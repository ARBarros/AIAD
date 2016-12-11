package Behaviours;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import trasmapi.sumo.SumoTrafficLight;

import java.security.acl.Acl;
import java.util.ArrayList;

/**
 * Created by Andre on 10/12/2016.
 */
public class LearningBehaviour extends Behaviour{
    ArrayList<String> trafficLightsId = SumoTrafficLight.getIdList();
    String tlId;

    @Override
    public void action() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent("ESTA MERDA ESTÁ TODA A ARDER JEBUS");

        for(String tl : trafficLightsId){
            if(!tl.equals(tlId)){
                msg.addReceiver(new AID("TF-"+tl , AID.ISLOCALNAME));
            }

        }



    }

    @Override
    public boolean done() {
        return false;
    }
}
