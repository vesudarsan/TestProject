
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
		
//		Runnable runnable = () ->{
//			try {
//				object.run();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		};
		
		//ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

//		service.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.SECONDS);
	//	service.execute();
		
		ExecutorService service = Executors.newFixedThreadPool(10);
		for (int i=0;i<10;i++)
		{
			service.execute(new Task());
		}
			


	}
	
	static class Task implements Runnable
	{
		
		
					
		public void run()
		{
			do 
			{
				try 
				{
					object.run();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}while(true);
			
			
			
			
		}
	}
}


