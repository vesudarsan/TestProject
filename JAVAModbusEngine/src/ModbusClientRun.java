//import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
//import java.util.Set;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import de.re.easymodbus.modbusclient.ModbusClient;




public class ModbusClientRun {
	

	// The following class variables are used for each interface configuration
	public String protocol,interfaceName,targetIP,linkType,interfaces,curDir;
	public int dataset,gatewayID,port,period,timeout,timeWaitToRequest;
	public boolean reconnect = false;
	
	public int totalNoOfInterfaces;	
	public String modbusConfigFileName,modbusRegisterMapFileName;
	
	// The following class variables are used for modbus Register configurations
	public int startAddr,noOfReg;
	
	
	
	//Constructor
	public ModbusClientRun()
	{
		//this.targetIP = "127.0.0.1";//2d check later
		//this.port = 502;//2d check later
		this.modbusConfigFileName = "ModbusConfig.json";
		this.modbusRegisterMapFileName = "RegisterDeviceMap.json";
		//this.startAddr =0;//2d check later
		//this.noOfReg =10;//2d check later
		System.out.println("Modbus Client Initiated");//2dl check later
		System.out.println("I am in ModbusClientRun Constructor ");	//2dl check later	
	}
	
	

	

	public static void main(String[] args) throws IOException,JSONException, ParseException
	{
		int funcCode=3;
		List<Integer> modbusInterfaceList = new ArrayList<Integer>();
		
		
		ModbusClientRun myConnection = new ModbusClientRun();	
        
		if  (ModbusDataRetrive.readModbusConfigFile(myConnection) == false)
		{
			System.out.println("ModbusConfig.json File not found");
			System.out.println("Gateway Interfaces will not launch");
			return;
		}
		
		
//		if (ModbusDataRetrive.readModbusRegisters(myConnection) == false)//2dl check later
//		{
//			System.out.println("RegisterDeviceMap.json File not found");
//			System.out.println(" Interfaces will not launch");
//			return;
//		}
		
		modbusInterfaceList = ModbusDataRetrive.readModbusRegisters( myConnection);
		
		ModbusClient modbusClient = new ModbusClient();
		modbusClient.Connect(myConnection.targetIP,myConnection.port);
        modbusClient.setUnitIdentifier((byte)1);

        
        try
        {
        	 do
             {
             	for (int i=0;i<modbusInterfaceList.size()-1;i+=5)
             	{
             		funcCode = modbusInterfaceList.get(i+1);
             		myConnection.startAddr = modbusInterfaceList.get(i+2);
             		myConnection.noOfReg   = modbusInterfaceList.get(i+3);//2dl add comments later
             		
             		if (funcCode == 3)// Read Holding Registers
             		{
             			int[] responseHoldingRegs = modbusClient.ReadHoldingRegisters(myConnection.startAddr, myConnection.noOfReg);
             			
             			for (int j=0; j<responseHoldingRegs.length; j++)
             			{
             				System.out.println(responseHoldingRegs[j]);
             			}
             			
                 		System.out.println("responseHoldingRegs.length " + responseHoldingRegs.length);
                 		System.out.println("---------------------------------");
             			
             		}
             		else if (funcCode == 4)// Read Input Registers
             		{
             			int[] responseInputRegs = modbusClient.ReadInputRegisters(myConnection.startAddr, myConnection.noOfReg);
             			
             			for (int j=0; j<responseInputRegs.length; j++)
             			{
             				System.out.println(responseInputRegs[j]);
             			}
             			
                 		System.out.println("responseInputRegs.length " + responseInputRegs.length);
                 		System.out.println("---------------------------------");
             		}
             	}
             	
             } while(false);        	
        	
        	
        }
        catch (Exception e)
        {
        	
        }
        finally
        {
        	
        }
       
        
        
            
        
        //while (true)
//        do
//        {
//        	try
//        	{
//        		int[] responseHoldingRegs = modbusClient.ReadHoldingRegisters(myConnection.startAddr, myConnection.noOfReg);
//        		Thread.sleep(500);
//        		
////        		for (int i=0; i<responseHoldingRegs.length; i++)
////        		{        			
////        			System.out.println(responseHoldingRegs[i]);
////        		}
////        		System.out.println("--------------------");
////        		System.out.println("responseHoldingRegs.length " + responseHoldingRegs.length);
//        		
//        	}
//        	catch (Exception e)
//        	{
//        		
//        	}
//        	finally
//        	{
//        		
//        	}
//        }
//        	while(false);
        }





	private static List<Integer> readModbusRegisters(ModbusClientRun myConnection) {
		// TODO Auto-generated method stub
		return null;
	}
        
        

	}


