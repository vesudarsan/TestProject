
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
		
		ModbusDataRetrive object = new ModbusDataRetrive();
		if (!object.init())
		{
			System.out.println("Cannot start :-(");
			return;
		}
		
		Runnable runnable = () ->{
			try {
				object.run();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};
		
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.SECONDS);


	}
}


