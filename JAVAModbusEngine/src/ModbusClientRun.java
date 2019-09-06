
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.simple.parser.ParseException;






public class ModbusClientRun 
{
	static ModbusDataRetrive object = new ModbusDataRetrive();



	public static void main(String[] args) throws IOException, JSONException, ParseException
	{
		
		
		if (!object.init())
		{
			System.out.println("Cannot start :-(");
			return;
		}
		

		int coreCount = Runtime.getRuntime().availableProcessors();		
		ScheduledExecutorService service = Executors.newScheduledThreadPool(coreCount);
		service.scheduleAtFixedRate(new Task(), 0, 3, TimeUnit.SECONDS);

			
	}
	
	static class Task implements Runnable
	{
					
		public void run()
		{
			try {
				object.run();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
			
		}
	}
}


