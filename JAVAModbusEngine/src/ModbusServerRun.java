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
		//-435.12344 big endian
//		modbusServer.inputRegisters[1] = 0xC3;
//		modbusServer.inputRegisters[2] = 0xd9;
//		modbusServer.inputRegisters[3] = 0x8f;
//		modbusServer.inputRegisters[4] = 0xcd;
		//-435.12344 little endian
//		modbusServer.inputRegisters[1] = 0xcd;
//		modbusServer.inputRegisters[2] = 0x8f;
//		modbusServer.inputRegisters[3] = 0xd9;
//		modbusServer.inputRegisters[4] = 0xc3;
		
		
		
		//negative value -18
//		modbusServer.inputRegisters[1] = 0xFF;
//		modbusServer.inputRegisters[2] = 0xEC;
		
//		//negative value -58
//		modbusServer.inputRegisters[1] = 0xFF;
//		modbusServer.inputRegisters[2] = 0xFF;		
//		modbusServer.inputRegisters[3] = 0xFF;
//		modbusServer.inputRegisters[4] = 0xC4;			
		
		//1234 value big endian
		modbusServer.inputRegisters[1] = 0x00;
		modbusServer.inputRegisters[2] = 0x00;
		modbusServer.inputRegisters[3] = 0x04;
		modbusServer.inputRegisters[4] = 0xd2;		
//		
		
//		//5678 value big endian
//		modbusServer.inputRegisters[5] = 0x00;
//		modbusServer.inputRegisters[6] = 0x00;
//		modbusServer.inputRegisters[7] = 0x16;
//		modbusServer.inputRegisters[8] = 0x2E;	
		
		//8765 value little endian
		modbusServer.inputRegisters[5] = 0x2e;
		modbusServer.inputRegisters[6] = 0x16;
		modbusServer.inputRegisters[7] = 0x00;
		modbusServer.inputRegisters[8] = 0x00;
		
		
		//9101112 value
		modbusServer.inputRegisters[9] = 0x00;
		modbusServer.inputRegisters[10] = 0x8a;
		modbusServer.inputRegisters[11] = 0xdf;
		modbusServer.inputRegisters[12] = 0x38;		
		
		
		
	}

}
