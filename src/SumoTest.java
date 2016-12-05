import trasmapi.genAPI.TraSMAPI;
import trasmapi.genAPI.exceptions.TimeoutException;
import trasmapi.sumo.Sumo;
import trasmapi.sumo.SumoConfig;
import trasmapi.genAPI.exceptions.UnimplementedMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andre on 04/12/2016.
 */
public class SumoTest {
    public static void main(String[] args) throws IOException, UnimplementedMethod, TimeoutException {
        TraSMAPI api = new TraSMAPI();

        //Create SUMO
        Sumo sumo = new Sumo("guisim");
        List<String> params = new ArrayList<String>();
        params.add("-c=src\\T1Map\\map.sumo.cfg");
        sumo.addParameters(params);
        sumo.addConnections("localhost", 8820);

        //Add Sumo to TraSMAPI
        api.addSimulator(sumo);

        //Launch and Connect all the simulators added
        api.launch();

        api.connect();

    }
}
