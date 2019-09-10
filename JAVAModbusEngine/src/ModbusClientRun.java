
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.simple.parser.ParseException;





public class ModbusClientRun 
{
	
	public static void main(String[] args) throws IOException, JSONException, ParseException
	{
		final int noOfDevices;

		noOfDevices = ModbusDataRetrive.readNoDevices("ModbusConfig.json");
		
		ModbusDataRetrive deviceObjects[] = new ModbusDataRetrive[noOfDevices+1];
		
		for (int i=1;i<=noOfDevices;i++)
		{
			deviceObjects[i] = new ModbusDataRetrive(i);
			if (!deviceObjects[i].init())
			{
				System.out.println("Cannot start device interface for dataset: " + i);
				//2dl need to add code for not starting devices
			}						
			
		}
	
		
				
		Runnable runnable = () ->
		{
			
			try {
				for (int i=1;i<=noOfDevices;i++)
				{
					deviceObjects[i].run();
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};

		int coreCount = Runtime.getRuntime().availableProcessors();		
		ScheduledExecutorService service = Executors.newScheduledThreadPool(coreCount);
		service.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.SECONDS);
			
	}
	

}
