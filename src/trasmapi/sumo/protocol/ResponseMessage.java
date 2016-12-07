
package trasmapi.sumo.protocol;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import trasmapi.genAPI.exceptions.WrongCommand;

public class ResponseMessage {
	
	private int length;
	public StatusResponse status;
	public ArrayList<Command> commands;

	public ResponseMessage() {
		
	}

	public ResponseMessage(DataInputStream in)  {

		commands = new ArrayList<Command>();

		System.out.println();
		//System.out.println(in.readInt());
		try {
			length = in.readInt();
		} catch (IOException e) {
			System.out.println("cenas321");
			e.printStackTrace();
		}
		System.out.println("RESPONSE MSG");
		byte[] buffer = new byte[length-Integer.SIZE/8];
		try {
			in.readFully(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Buffer buf = new Buffer(buffer);

		if(length > 0){


			while(!buf.end()){

				try {
					status = new StatusResponse(buf);
				} catch (IOException e) {
					e.printStackTrace();
				}

				if(status.getIdentifier() == Constants.CMD_SIMSTEP2){

					int numCommands = buf.readInt();
					if(numCommands > 0)
						for(int i=0; i< numCommands ;i++)
							commands.add(new Command(buf));
					break;
				}
				else{
					if(!buf.end())
						commands.add(new Command(buf));
				}
			}
		}
	}

	public Content validate(byte cmdGetVehicleVariable,byte responseGetVehicleVariable, byte idList, byte typeStringlist) throws WrongCommand {

		if( cmdGetVehicleVariable != status.getIdentifier())
			throw new WrongCommand("COMMAND_ANSW check: Was expecting " + Integer.toString(cmdGetVehicleVariable & 0xFF, 16) +
					" and got " +  Integer.toString(status.getIdentifier() & 0xFF, 16));

		return commands.get(0).validate(responseGetVehicleVariable, idList, typeStringlist);
	}

	public void print() {
		System.out.println("-- Response Message --");
		status.print("\t");
		System.out.println("  -- Number of Commands: "+ commands.size());
		for(Command c:commands)
			c.print("\t");
	}


}
