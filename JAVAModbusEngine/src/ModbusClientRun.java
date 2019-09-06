
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
		
		
		Task.init();

		int coreCount = Runtime.getRuntime().availableProcessors();		
		ScheduledExecutorService service = Executors.newScheduledThreadPool(coreCount);
		service.scheduleAtFixedRate(new Task(), 0, 1, TimeUnit.SECONDS);
			
	}
	
	static class Task implements Runnable
	{
		static ModbusDataRetrive object = new ModbusDataRetrive("dataset1");
		
		static public void init() throws IOException, JSONException, ParseException
		{
			
			if (!object.init())
			{
				System.out.println("Cannot start :-(");
				return;
			}
			
		}
					
		public void run() 
		{
			try {
				object.run();
			} catch (IOException e) {				
				e.printStackTrace();
			}	
			
			
		}
	}
}


