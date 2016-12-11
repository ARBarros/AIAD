package Agents;

import Behaviours.RequestBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import trasmapi.sumo.SumoLane;
import trasmapi.sumo.SumoTrafficLight;

import java.util.Random;

/**
 * Created by Andre on 10/12/2016.
 */
public class InteligentTrafficLight extends TrafficLightAgent {

    private int extMessages = 0;
    private int extStopped = 0;

    private static final long serialVersionUID = 1L;


    private int greenTime1, greenTime2, yellowTime;

    //private Learning qLearn;

    private int action;


    // learning rate = 1 because it's deterministic model
    private double learningRate = 1;

    // discount future rewards is low so it can consider more recent rewards
    private double discountFutureRewards = 0.3;

    //softmax temperature
    private double softmax = 0.5;

    //[State][Action] table
    private double[][] qTable;

    // best possible Quality
    private double bestQ;

    // minimum time of traffic light
    private final int minTime = 20;

    // steps between traffic light time
    private final int interval = 5;

    //semaphore time must be between 20 and 60, with 5 secs intervals
    private int states = 9;

    //actions needed
    private int actions = 9;

    public InteligentTrafficLight(String id, SumoTrafficLight trafficLight, AgentManager manager, String type) {
        super(id, trafficLight, manager, type);

        greenTime1 = 30;
        greenTime2 = 30;
        yellowTime = 2;

        // assumindo que se manteve no in�cio
        action = 0;
        initializeLearning();
    }

    public void setup() {
        addBehaviour(new RequestBehaviour());
        Thread thread = new Thread(new Runnable() {
            public void run() {
                executeSemaphore();
            }
        });
        thread.start();
    }

    public void initializeLearning() {
        bestQ = 0;
        qTable = new double[states][];

        for(int i = 0; i < states;i++)
            qTable[i] = new double[actions];

        // initialize with maximum value, so it can learn in the first step
        for(int i = 0; i < states;i++)
            for(int j = 0; j < actions;j++)
                qTable[i][j] = 1;
    }

    public void update(int outState, int action, int stoppedCars) {
        //reinforce with the number of stopped cars
        int reinforce = 100-stoppedCars;

        int state = (outState-minTime)/interval;
        System.out.println(qTable);

        // gets current Q from table
        double currentQ = qTable[state][action];

        // Q(s,a) = Q(s,a) + alpha * ( reinf + beta * ( Q(s',a') - Q(s,a)))
        currentQ = currentQ + learningRate * ( reinforce + discountFutureRewards * (bestQ - currentQ));
        qTable[state][action] = currentQ;

        // if a new best is found, update it
        if(currentQ > bestQ)
            bestQ = currentQ;
    }


    public int getAction(int state) {
        // denominator for soft max temperature
        double denominator = 0.0;
        double choice = Math.random();

        int bestAction = getBestAction(state);
        double numerator = Math.exp(bestAction/softmax);


        for(int i = 0; i < states;i++)
            for(int j = 0; j < actions;j++)
                denominator +=  Math.exp(qTable[i][j]/softmax);

        // if the random number is less than the probability, return best action
        if(choice < (numerator/denominator))
            return bestAction;
        else // if not, return a random action between 0 and 8
            return new Random().nextInt(actions);
    }

    private int getBestAction(int state) {
        //same as before, between 20 and 60, 8 states, 5 step
        int state_temp = (state-minTime)/interval;
        int best = 0;
        for(int i = 0; i < actions; i++)
            if(qTable[state_temp][i] > bestQ )
                best = i;


        return best;
    }

    public String generateState(boolean position, boolean yellow) {
        String Str = "";
        int column = Integer.parseInt(id.split("/")[0]);
        int line = Integer.parseInt(id.split("/")[1]);
        String upper = Integer.toString(column) + "/" + Integer.toString(line + 1);
        String righter = Integer.toString(column + 1) + "/" + Integer.toString(line);
        String below = Integer.toString(column) + "/" + Integer.toString(line - 1);
        String lefter = Integer.toString(column - 1) + "/" + Integer.toString(line);
        if (position) {
            if (getNeighbours().contains(upper)) {
                if (!yellow)
                    Str += "Gg";
                else
                    Str += "yy";
            }
            if (getNeighbours().contains(righter)) {
                Str += "rr";
            }
            if (getNeighbours().contains(below)) {
                if (!yellow)
                    Str += "gG";
                else
                    Str += "yy";

            }
            if (getNeighbours().contains(lefter)) {
                Str += "rr";
            }
        } else {
            if (getNeighbours().contains(upper)) {
                Str += "rr";

            }
            if (getNeighbours().contains(righter)) {
                if (!yellow)
                    Str += "Gg";
                else
                    Str += "yy";
            }
            if (getNeighbours().contains(below)) {

                Str += "rr";

            }
            if (getNeighbours().contains(lefter)) {
                if (!yellow)
                    Str += "gG";
                else
                    Str += "yy";
            }
        }

        return Str.toString();
    }

    public void executeSemaphore() {
        boolean position = true, yellow = false;
        boolean updateQtable = true;
        SumoTrafficLight semaphore = new SumoTrafficLight(id);
        int stopped = 0;
        int i = 0;
        while (!stop) {
            int laneCounter = 0;
            // states of all semaphores
            if (getNeighbours().size() == 4 && position) {
                if (!yellow)
                    semaphore.setState("rrrGGgrrrgGG");
                else
                    semaphore.setState("rrryyyrrryyy");
            } else if (getNeighbours().size() == 4 && !position) {
                if (!yellow)
                    semaphore.setState("GGgrrrGGgrrr");
                else
                    semaphore.setState("yyyrrryyyrrr");
            } else if (getNeighbours().size() == 3) { //cruzamentos com 3 semáforos

                semaphore.setState(generateState(position, yellow));

            }
            try {
                Thread.sleep(2);
                i++;
                //se já tiver passado todos os estados (green1-yellow-green2-yellow)
                if (i > greenTime2 + yellowTime * 2 + greenTime1) {
                    i = 0;
                    laneCounter = 0;
                    position = true;
                    yellow = false;
                    SumoLane lane;
                    if (hasAdjacentTL(0, 1)) {
                        lane = new SumoLane(semaphore.getControlledLanes().listIterator(laneCounter).next());
                        stopped += lane.getNumVehiclesStopped(stoppedSpeed);
                        laneCounter++;
                    }
                    if (hasAdjacentTL(1, 0)) {
                        laneCounter++;
                    }
                    if (hasAdjacentTL(0, -1)) {
                        lane = new SumoLane(semaphore.getControlledLanes().listIterator(laneCounter).next());
                        stopped += lane.getNumVehiclesStopped(stoppedSpeed);

                    }
                    learnProposal();
                    System.out.println(action);
                    update(greenTime2, action,(int)( stopped*0.5+ extStopped*0.5));

                    extMessages = 0;
                    extStopped = 0;
                    action = getAction(greenTime2);
                    updateGreenTime(action);

                    updateQtable = true;
                    stopped = 0;// reset stoppped vehicles
                } else if (i > greenTime2 + greenTime1 + yellowTime) { //se já tiver passado por green1-yellow-green2
                    yellow = true;

                } else if (i > greenTime1 + yellowTime) {
                    position = false;

                    yellow = false;
                } else if (i > greenTime1) {

                    yellow = true;

                    // updates stopped vehicles
                    if (updateQtable) {
                        laneCounter = 0;
                        SumoLane lane;
                        if (hasAdjacentTL(0, 1)) {
                            laneCounter++;
                        }
                        if (hasAdjacentTL(1, 0)) {
                            lane = new SumoLane(semaphore.getControlledLanes().listIterator(laneCounter).next());
                            stopped += lane.getNumVehiclesStopped(stoppedSpeed);
                            laneCounter++;
                        }
                        if (hasAdjacentTL(0, -1)) {
                            laneCounter++;
                        }
                        if (hasAdjacentTL(-1, 0)) {
                            lane = new SumoLane(semaphore.getControlledLanes().listIterator(laneCounter).next());
                            stopped += lane.getNumVehiclesStopped(stoppedSpeed);

                        }

                        updateQtable = false;
                    }
                }

                // System.out.println("Greentime 1: " + greenTime1);
                // System.out.println("Greentime 2: " + greenTime2);

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // for(int i = 0; i < )

        }
    }

    private void updateGreenTime(int action) {
        int timeChange = 5;
        int time1Mult = 0, time2Mult = 0;// multipler to increase/decrease time

        switch (action) {
            case 4:
                time1Mult = 1;
                time2Mult = 1;
                break;
            case 1:

                time1Mult = 1;
                time2Mult = 0;
                break;
            case 2:

                time1Mult = 1;
                time2Mult = -1;
                break;
            case 3:

                time1Mult = 0;
                time2Mult = 1;
                break;
            case 0:

                time1Mult = 0;
                time2Mult = 0;
                break;
            case 5:

                time1Mult = 0;
                time2Mult = -1;
                break;
            case 6:

                time1Mult = -1;
                time2Mult = 1;
                break;
            case 7:

                time1Mult = -1;
                time2Mult = 0;
                break;
            case 8:

                time1Mult = -1;
                time2Mult = -1;
                break;
            default:
                break;
        }

        greenTime1 += timeChange * time1Mult;
        greenTime2 += timeChange * time2Mult;

        if (greenTime1 < 20)
            greenTime1 = 20;
        if (greenTime2 < 20)
            greenTime2 = 20;
        if (greenTime1 > 60)
            greenTime1 = 60;
        if (greenTime2 > 60)
            greenTime2 = 60;

    }

    private void learnProposal() {


        ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
        msg.setContent("Learn");
        for (String adjacent : getNeighbours() ){
            msg.addReceiver(new AID("Semaphore-"+adjacent, AID.ISLOCALNAME));
            send(msg);
        }
    }


    public void addOthersStopped(int stopped){
        if(extMessages < getNeighbours().size()){
            extStopped += stopped;
        }
        extMessages++;
    }

    public int getExternalMessagesReceived() {
        return extMessages;
    }

    public void setExternalMessagesReceived(int externalMessagesReceived) {
        this.extMessages = externalMessagesReceived;
    }

    public int getExternalStopped() {
        return extStopped;
    }
    public void setExternalStopped(int externalStopped) {
        this.extStopped = externalStopped;
    }


}
