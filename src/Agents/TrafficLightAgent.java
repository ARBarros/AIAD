package Agents;

import Behaviours.IntersectionBehaviour;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import trasmapi.genAPI.TrafficLight;
import trasmapi.genAPI.exceptions.UnimplementedMethod;
import trasmapi.sumo.Sumo;
import trasmapi.sumo.SumoLane;
import trasmapi.sumo.SumoTrafficLight;
import trasmapi.sumo.SumoTrafficLightProgram;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * Created by Andre on 06/12/2016.
 */
public class TrafficLightAgent extends Agent {

    String id;
    SumoTrafficLight trafficLight;
    private int tickCounter;
    List<SumoTrafficLightProgram.Phase> tlPhases;
    ArrayList<Integer> phaseDuration = new ArrayList<Integer>();
    private int currentPhase;
    AgentManager manager;
    private String type;
    private Sumo sumo;
    private HashSet<String> neighbours = new HashSet<>();
    private int intersections;
    private int states;
    private int actions;
    private double LISTEN_TO_NEIGHBOURS_EMERGENCIES_PROB;
    private int emergencyIndex = -1;
    private ArrayList<String> controlledLanes;
    double stoppedSpeed = 0.3;
    boolean stop;


    public HashSet<String> getNeighbours() {
        return neighbours;
    }

    public void stop(boolean b){
        this.stop = b;
    }

    public TrafficLightAgent(String id, SumoTrafficLight trafficLight, AgentManager manager, String type){
        super();
        this.id = id;
        this.type = type;


        this.trafficLight = trafficLight;

        tlPhases = trafficLight.getProgram().getPhases();
        //System.out.println(tlPhases);
        for(SumoTrafficLightProgram.Phase phase : tlPhases ){
            phaseDuration.add(phase.getDuration() /1000 );
        }

        controlledLanes = trafficLight.getControlledLanes();
        System.out.println("CONTROLLED " + controlledLanes);

        for(int i=0; i < controlledLanes.size(); i++){
            //System.out.println("Cenas " + controlledLanes.listIterator(i).next().split("to")[0]);
            neighbours.add(controlledLanes.listIterator(i).next().split("to")[0]);
        }

        currentPhase=0;
        System.out.println(phaseDuration);
        this.manager = manager;
    }



    @Override
    public void setup(){
        DFAgentDescription ad = new DFAgentDescription();
        ad.setName(getAID()); //agentID
        System.out.println("AID: "+ad.getName());

        ServiceDescription sd = new ServiceDescription();
        sd.setName(getName()); //nome do agente
        System.out.println("Nome: "+sd.getName());

        sd.setType("Traffic Light");
        System.out.println("Tipo: "+sd.getType()+"\n\n\n");

        //System.out.println("CARALHO MEU");

        if(type.equals("intersection")){
            //IntersectionBehaviour behaviour = new IntersectionBehaviour(this, manager);
            //this.addBehaviour(behaviour);
        }
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
            System.out.println(id);
            String state = tlPhases.get(0).getState();
            trafficLight.setState(state);
            tickCounter++;
        }else if(nextPhase){
            tickCounter = 0; //reset counter
            //System.out.println("ENTRA AQUI");
            if(currentPhase+1 >= tlPhases.size()){

                updatePhasesList(currentPhase);
                updatePhaseDuration(currentPhase);
            }else{
                //System.out.println("FODASSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEE");

                updatePhasesList(currentPhase+1);
                updatePhaseDuration(currentPhase+1);
            }

            currentPhase =0;
            String state = tlPhases.get(0).getState();


            trafficLight.setState(state);
            tickCounter++;
        }else{
            //System.out.println("TEM DE ENTRAR AQUI");
            int durationTotal = 0;
            for(int i=0; i < phaseDuration.size(); i++){
                durationTotal += phaseDuration.get(i);
                //System.out.println(i);
                if(tickCounter < durationTotal){

                    //System.out.println(i);
                    currentPhase = i;
                    break;
                }


            }
            System.out.println("a1");
            if(tickCounter >= durationTotal){
                //System.out.println("reset counter");
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
            //System.out.println("a2");
        }
    }



    public void updatePhasesList(int index){
        ArrayList<SumoTrafficLightProgram.Phase> finalArray = new ArrayList<SumoTrafficLightProgram.Phase>();

        ArrayList<SumoTrafficLightProgram.Phase> before = new ArrayList<SumoTrafficLightProgram.Phase>(tlPhases.subList(0, index));



        finalArray.add(tlPhases.get(index));

        if(index+1 != tlPhases.size()){
            ArrayList<SumoTrafficLightProgram.Phase> after = new ArrayList<SumoTrafficLightProgram.Phase>(tlPhases.subList(index+1, tlPhases.size()));

            finalArray.addAll(after);

            //



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



    public Boolean hasAdjacentTL(int x, int y){
        String[] temp = id.split("/");
        int col = Integer.parseInt(temp[0]);
        int lin = Integer.parseInt(temp[1]);

        String comp = Integer.toString(col + x) + "/" + Integer.toString(lin+y);

        return neighbours.contains(comp);
    }






    public int getStoppedVehicles(){
        int ecx = 0;
        int ret = 0;


        if(hasAdjacentTL(1, 0)){
            SumoLane lane = new SumoLane(trafficLight.getControlledLanes().listIterator(ecx).next());
            ret+= lane.getNumVehiclesStopped(stoppedSpeed);
            ecx++;
        }
        if(hasAdjacentTL(0, 1)){
            SumoLane lane = new SumoLane(trafficLight.getControlledLanes().listIterator(ecx).next());
            ret+= lane.getNumVehiclesStopped(stoppedSpeed);
            ecx++;
        }
        if(hasAdjacentTL(-1, 0)){
            SumoLane lane = new SumoLane(trafficLight.getControlledLanes().listIterator(ecx).next());
            ret+= lane.getNumVehiclesStopped(stoppedSpeed);
            ecx++;
        }
        if(hasAdjacentTL(0, -1)){
            SumoLane lane = new SumoLane(trafficLight.getControlledLanes().listIterator(ecx).next());
            ret+= lane.getNumVehiclesStopped(stoppedSpeed);
            ecx++;
        }
        return ret;
    }
}
