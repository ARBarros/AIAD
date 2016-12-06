package Main;

import Agents.AgentManager;
import jade.BootProfileImpl;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;
import trasmapi.sumo.Sumo;

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

    public static void main(String args[]){
        if(jade_gui){
            List<String> params = new ArrayList<String>();
            params.add("-gui");
            profile = new BootProfileImpl(params.toArray(new String[0]));
        } else
            profile = new ProfileImpl();

        Runtime rt = Runtime.instance();

        mainContainer = rt.createMainContainer(profile);

        manager = new AgentManager(sumo, mainContainer);


    }
}
