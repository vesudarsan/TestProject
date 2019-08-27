import de.re.easymodbus.server.*;
import de.re.easymodbus.exceptions.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class ModbusServerRun {

	public static void main(String[] args) throws IOException 
	
	{
		System.out.println("Hello Modbus Server Initiated");
		ModbusServer modbusServer = new ModbusServer();
		modbusServer.setPort(502);
		Map <Integer, Integer> reigisterMap = new HashMap<>();
		reigisterMap.put(5, 5);
		reigisterMap.put(4, 4);
		reigisterMap.put(3, 3);
		reigisterMap.put(2, 2);
		reigisterMap.put(1, 1);

//		Set<Integer> keys = reigisterMap.keySet();

		
	
		for (int i=1; i<=120;i++)
		{
			reigisterMap.put(i, i);// load dummy values
			modbusServer.holdingRegisters[i] = (int) reigisterMap.get(i);
			modbusServer.inputRegisters[i] = (int) reigisterMap.get(i);
		}
		
		try
		{
			modbusServer.Listen();			
		}
		
		
		//catch(java.io.IOException | Exceptions.ModbusException e)
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("44444444444444444");
		}
		finally
		{
			
		}
		//encoding float value for first modbus register
		modbusServer.inputRegisters[1] = 0xC3;
		modbusServer.inputRegisters[2] = 0xd9;
		modbusServer.inputRegisters[3] = 0x8f;
		modbusServer.inputRegisters[4] = 0xcd;
	}

}
