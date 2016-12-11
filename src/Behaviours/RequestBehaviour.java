package Behaviours;

import Agents.InteligentTrafficLight;
import Agents.TrafficLightAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Created by Andre on 10/12/2016.
 */
public class RequestBehaviour extends CyclicBehaviour{
    ACLMessage msg;
    @Override
    public void action() {
        msg = myAgent.receive();
        if (msg != null) {
            String message = msg.getContent();
             if (message.startsWith("Learn")){
                //System.out.println("recebeu Learn");
                ACLMessage response = msg.createReply();
                response.setContent("NumVehicles "+ ((TrafficLightAgent) myAgent).getStoppedVehicles());
                response.setPerformative(ACLMessage.INFORM);
                myAgent.send(response);
            }
            else if (message.startsWith("NumVehicles")){
                //System.out.println("recebeu Resposta do Learn" + message);

                int exStop = Integer.parseInt(message.split(" ")[1]);
                ((InteligentTrafficLight) myAgent).addOthersStopped(exStop);

            }

        }
    }
}
