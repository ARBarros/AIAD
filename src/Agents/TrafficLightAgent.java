package Agents;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import trasmapi.genAPI.exceptions.UnimplementedMethod;
import trasmapi.sumo.Sumo;
import trasmapi.sumo.SumoTrafficLight;
import trasmapi.sumo.SumoTrafficLightProgram;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andre on 06/12/2016.
 */
public class TrafficLightAgent extends Agent {

    private String id;
    SumoTrafficLight trafficLight;
    private int tickCounter;
    List<SumoTrafficLightProgram.Phase> tlPhases;
    ArrayList<Integer> phaseDuration = new ArrayList<Integer>();
    private int currentPhase;

    public TrafficLightAgent(String id, SumoTrafficLight trafficLight){
        super();
        this.id = id;

        this.trafficLight = trafficLight;

        tlPhases = trafficLight.getProgram().getPhases();
        System.out.println(tlPhases);
        for(SumoTrafficLightProgram.Phase phase : tlPhases ){
            phaseDuration.add(phase.getDuration() /1000 );
        }

        currentPhase=0;
        System.out.println(phaseDuration);
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

    public ArrayList<String> getLanes(){
        return trafficLight.getControlledLanes();
    }

    public void update(boolean nextPhase){
       // nextPhase=false;
        //System.out.println(id);
        //System.out.println(id);
        //System.out.println(tickCounter);
        /*
        if(id.equals("A1")){
            System.out.println(id);
            System.out.println(tickCounter);
        }
        */
        if(tickCounter == 0){
            String state = tlPhases.get(0).getState();
            trafficLight.setState(state);
            tickCounter++;
        }else if(nextPhase){
            tickCounter = 0; //reset counter
            System.out.println("ENTRA AQUI");
            if(currentPhase+1 >= tlPhases.size()){
                if(id.equals("B1")){
                    System.out.println("FASES ATUAL +1" + currentPhase);
                }
                updatePhasesList(currentPhase);
                updatePhaseDuration(currentPhase);
            }else{
                if(id.equals("B1")){
                    System.out.println("FASES ATUAL " + currentPhase);
                }
                updatePhasesList(currentPhase+1);
                updatePhaseDuration(currentPhase+1);
            }

            currentPhase =0;
            String state = tlPhases.get(0).getState();
            if(id.equals("B1")){
                System.out.println("FASES " + tlPhases);
                System.out.println(state);
            }

            trafficLight.setState(state);
            tickCounter++;
        }else{
            int durationTotal = 0;
            for(int i=0; i < phaseDuration.size(); i++){
                durationTotal += phaseDuration.get(i);
                if(tickCounter < durationTotal){
                    //System.out.println(i);
                    currentPhase = i;
                    break;
                }


            }

            if(tickCounter >= durationTotal){
                System.out.println("reset counter");
                tickCounter =0;
                String state = tlPhases.get(0).getState();
                trafficLight.setState(state);
            }else{
                tickCounter++;
                //System.out.println("phase " + currentPhase);
                String state = tlPhases.get(currentPhase).getState();
                //System.out.println(state);
                trafficLight.setState(state);
            }
        }
    }

    public void updatePhasesList(int index){
        ArrayList<SumoTrafficLightProgram.Phase> finalArray = new ArrayList<SumoTrafficLightProgram.Phase>();

        ArrayList<SumoTrafficLightProgram.Phase> before = new ArrayList<SumoTrafficLightProgram.Phase>(tlPhases.subList(0, index));
        if(id.equals("B1")){
            System.out.println(tlPhases.get(index));
            System.out.println("BEFORE " + before);
            System.out.println(index);
            System.out.println(tlPhases.size());
        }


        finalArray.add(tlPhases.get(index));

        if(index+1 != tlPhases.size()){
            ArrayList<SumoTrafficLightProgram.Phase> after = new ArrayList<SumoTrafficLightProgram.Phase>(tlPhases.subList(index+1, tlPhases.size()));

            finalArray.addAll(after);

            //

            if(id.equals("B1")){
                System.out.println("AFTER " + after);
            }

        }



        finalArray.addAll(before);
        //System.out.println("CENAS " + finalArray);
        this.tlPhases = finalArray;
    }

    public void updatePhaseDuration(int index){
        ArrayList<Integer> finalArray = new ArrayList<Integer>();

        ArrayList<Integer> before = new ArrayList<Integer>(phaseDuration.subList(0, index));

        finalArray.add(phaseDuration.get(index));

        if(index+1 != phaseDuration.size()){
            ArrayList<Integer> after = new ArrayList<Integer>(phaseDuration.subList(index, phaseDuration.size()-1   ));

            finalArray.addAll(after);
        }
        finalArray.addAll(before);
        this.phaseDuration = finalArray;
    }

    @Override
    public void takeDown(){

    }
}
