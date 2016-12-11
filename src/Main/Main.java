package Main;

import Agents.AgentManager;
import jade.BootProfileImpl;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;
import trasmapi.genAPI.TraSMAPI;
import trasmapi.genAPI.exceptions.TimeoutException;
import trasmapi.genAPI.exceptions.UnimplementedMethod;
import trasmapi.sumo.Sumo;
import trasmapi.sumo.SumoCom;
import trasmapi.sumo.SumoTrafficLight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;

/**
 * Created by Andre on 05/12/2016.
 */
public class Main {

    private static boolean jade_gui = true;
    private static ProfileImpl profile;
    private static ContainerController mainContainer;
    private static AgentManager manager;
    private static Sumo sumo;
    private static String type;
    private static boolean stopMain= false;

    public static void main(String args[]) throws UnimplementedMethod, IOException, TimeoutException {
        if(args[0].equals("basic")){
           type = "basic";
        }else if(args[0].equals("intersection")){
            type = "intersection";
        }else if(args[0].equals("learning")){
            type = "learning";
        }else{
            System.out.println("Bad argument passed, exiting program");
            return;
        }


        if(jade_gui){
            List<String> params = new ArrayList<String>();
            params.add("-gui");
            profile = new BootProfileImpl(params.toArray(new String[0]));
        } else
            profile = new ProfileImpl();

        Runtime rt = Runtime.instance();

        mainContainer = rt.createMainContainer(profile);

        //System.out.println("cenas");



        //Create SUMO
        Sumo sumo = new Sumo("guisim");
        List<String> params = new ArrayList<String>();
        if(type.equals("learning")){
            params.add("-c=src\\T2Map\\file.sumocfg");
        }else{
            params.add("-c=src\\T1Map\\sumo.cfg");
        }

        sumo.addParameters(params);
        sumo.addConnections("localhost", 8820); //6942

        TraSMAPI api = new TraSMAPI();
        //Add Sumo to TraSMAPI
        api.addSimulator(sumo);

        //Launch and Connect all the simulators added

        api.launch();
        System.out.println("coisas");
        api.connect();
        System.out.println("a chamar");

        api.start();



        manager = new AgentManager(sumo, mainContainer, type);
        manager.startTrafficLights(type);
        //System.out.println(SumoTrafficLight.getIdList());
        if(type.equals("learning")) {

            TimerTask timertask = new TimerTask() {
                @Override
                public void run() {
                    if (equalLists(SumoCom.getAllVehiclesIds(), manager.arrivedVehicles)) {

                        try {
                            stopMain = true;
                            if (manager != null)
                                manager.killAgents();
                            Thread.sleep(300);
                            if (api != null)
                                api.close();

                        } catch (UnimplementedMethod | IOException | InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            };
        }

        //System.out.println("chega");
        while(true) {

            if(!api.simulationStep(0))
                break;


            //System.out.println("entr111ar");
            manager.updateDrivers();

            if(type.equals("basic")){
                manager.updateTrafficLightsBasic();
            }else if(type.equals("intersection")){
                manager.updateTrafficLightsInt();
            }



        }

    }


    public static boolean equalLists(List<String> a, List<String> b){
        // Check for sizes and nulls
        if ((a.size() != b.size()) || (a == null && b!= null) || (a != null && b== null)){
            return false;
        }

        if (a == null && b == null) return true;

        // Sort and compare the two lists
        Collections.sort(a);
        Collections.sort(b);
        return a.equals(b);
    }


}



