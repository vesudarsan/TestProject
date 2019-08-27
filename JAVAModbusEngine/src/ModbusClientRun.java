//import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
//import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;
import org.json.simple.JSONObject;
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
		System.out.println("Modbus Client Initiated");//2dl check later
		System.out.println("I am in ModbusClientRun Constructor ");	//2dl check later	
	}
	
	//add comments
	public static int [] toLittleEndian(int [] value) 
	{
	    final int length = value.length;
	    int [] result = new int[length];
	    for(int i = 0; i < length; i++) {
	    	result[length - i - 1] = value[i];
	    }
	    return result;
	}
	
	
	//add comments
	public static int bytesToInteger(int [] value)
	{
		StringBuilder strNum = new StringBuilder();

		for (int num : value) 
		{
		     strNum.append((num));
			//strNum.append((Integer.toHexString(num)));	
		     System.out.println(num);//2dl
		}
		return (Integer.parseInt(strNum.toString()));
	

	}
	
	
	//add comments
	public static float bytesToFloat(int [] buffer)
	{
		StringBuilder strNum = new StringBuilder();

		for (int num : buffer) 
		{
		     strNum.append((num));			
		     System.out.println(num);//2dl
		}
		
		return(Float.intBitsToFloat( buffer[3] ^ buffer[2]<<8 ^ buffer[1]<<16 ^ buffer[0]<<24));
		


	}	
	


	public static void decodeModbusFC4(int [] rxBuff,int firstIdx, int lastIdx)
	{
		
		String endiansize,encoding,tagId;
		int bytelen=0,arryIndx=0;
		int [] bytes;
		float engineeringValue,scaling;
		
//		System.out.println("firstIdx" + firstIdx);//2dl check later
//		System.out.println("lastIdx" + lastIdx); // 2dl check later
		
		


		for (int i=firstIdx; i<=lastIdx; i++)
		{
			JSONObject jsonObj = (JSONObject) ModbusDataRetrive.jsonAIGlobalObject.get(i);
			//System.out.println(jsonObj);//2dl
			

			endiansize = (String)jsonObj.get("endianSize");
			encoding = (String)jsonObj.get("encoding");// 2dl add code later
			tagId = (String)jsonObj.get("tagId");
			scaling = Float.valueOf((String)jsonObj.get("scaling"));

			
			if ((endiansize.equals("B32")) || (endiansize.equals("L32")))
			{
				bytelen =4;				
			}
			else if ((endiansize.equals("B16")) || (endiansize.equals("L16")))
			{
				bytelen =2;
			}
	
			bytes = Arrays.copyOfRange(rxBuff, arryIndx, arryIndx+bytelen);	
			//update arryIndx for next modbus register
			arryIndx += bytelen;
			
			
			//code for Endian conversion
			if ((endiansize.equals("B32")) || (endiansize.equals("B16")))
			{
							
			}
			else if ((endiansize.equals("L32")) || (endiansize.equals("L16")))
			{
				bytes = toLittleEndian(bytes);
				
			}
			
	
			engineeringValue = bytesToFloat(bytes);
			

			
//			engineeringValue *=scaling;
			System.out.println(tagId + ": " +engineeringValue);

			
		}

		System.out.println("-------------------------------------------");
		
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
		
		modbusInterfaceList = ModbusDataRetrive.readModbusRegisters( myConnection);//2dl first read modbus registers file and load datastructure
		
		ModbusClient modbusClient = new ModbusClient();
		modbusClient.Connect(myConnection.targetIP,myConnection.port);
        modbusClient.setUnitIdentifier((byte)1);//2dl read from file

        
        try
        {
        	 do
             {
             	for (int i=0;i<modbusInterfaceList.size()-1;i+=5)
             	{
             		int firstIndex = modbusInterfaceList.get(i);
             		funcCode = modbusInterfaceList.get(i+1);
             		myConnection.startAddr = modbusInterfaceList.get(i+2);
             		myConnection.noOfReg   = modbusInterfaceList.get(i+3)*2;//2dl add comments later             		
             	    int lastIndex  = modbusInterfaceList.get(i+4);
             		
             		if (funcCode == 3)// Read Holding Registers
             		{
             			int[] responseHoldingRegs = modbusClient.ReadHoldingRegisters(myConnection.startAddr, myConnection.noOfReg);
             			
             			System.out.println("---------------------------------");
             			
             		}
             		else if (funcCode == 4)// Read Input Registers
             		{
             			int[] responseInputRegs = modbusClient.ReadInputRegisters(myConnection.startAddr-1, myConnection.noOfReg);//2dl check start address again
             			
             		
             			decodeModbusFC4(responseInputRegs,firstIndex,lastIndex);
             			
             			
//             			for (int j=0; j<responseInputRegs.length; j++)
//             			{
//             				System.out.println(responseInputRegs[j]);
//             			}
//             			
//                 		System.out.println("responseInputRegs.length " + responseInputRegs.length);
//                 		System.out.println("---------------------------------");
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
       
        
              
        

        }


        

	}


