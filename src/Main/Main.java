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
import trasmapi.sumo.SumoTrafficLight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andre on 05/12/2016.
 */
public class Main {

    private static boolean jade_gui = true;
    private static ProfileImpl profile;
    private static ContainerController mainContainer;
    private static AgentManager manager;
    private static Sumo sumo;

    public static void main(String args[]) throws UnimplementedMethod, IOException, TimeoutException {
        if(jade_gui){
            List<String> params = new ArrayList<String>();
            params.add("-gui");
            profile = new BootProfileImpl(params.toArray(new String[0]));
        } else
            profile = new ProfileImpl();

        Runtime rt = Runtime.instance();

        mainContainer = rt.createMainContainer(profile);

        System.out.println("cenas");



        //Create SUMO
        Sumo sumo = new Sumo("guisim");
        List<String> params = new ArrayList<String>();
        params.add("-c=src\\T1Map\\sumo.cfg");
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



        manager = new AgentManager(sumo, mainContainer);
        manager.startTrafficLights();
        //System.out.println(SumoTrafficLight.getIdList());

        System.out.println("chega");
        while(true) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(!api.simulationStep(0))
                break;

            manager.updateDrivers();
        }

    }
}



