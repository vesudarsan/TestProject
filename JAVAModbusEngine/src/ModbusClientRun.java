//import java.io.File;
import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Collections;
import java.util.List;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import de.re.easymodbus.modbusclient.ModbusClient;


public class ModbusClientRun 
{


	// The following class variables are used for each interface configuration
	public String protocol,interfaceName,targetIP,linkType,interfaces,curDir;
	public int dataset,gatewayID,port,period,timeout,timeWaitToRequest,slaveId;
	public boolean reconnect = false;

	public int totalNoOfInterfaces;	
	public String modbusConfigFileName,modbusRegisterMapFileName;

	// The following class variables are used for modbus Register configurations
	public int startAddr,noOfReg;

	//Constructor
	public ModbusClientRun()
	{
		this.modbusConfigFileName = "ModbusConfig.json";
		this.modbusRegisterMapFileName = "RegisterDeviceMap.json";
		System.out.println("Modbus Client Initiated");		
	}

	//This method will converts all input bytes into little endian order(Least Significant Byte to Most Significant Byte)
	public static int [] toLittleEndian(int [] value) 
	{
		final int length = value.length;
		int [] result = new int[length];
		for(int i = 0; i < length; i++) {
			result[length - i - 1] = value[i];
		}
		return result;
	}
	
	//This method will converts all input bytes into little endian order(Most Significant Byte to Least Significant Byte)
	public static int [] toBigEndian(int [] value) 
	{
		final int length = value.length;
		int [] result = new int[length];
		for(int i = 0; i < length; i++) {
			result[i] = value[i];
		}
		return result;
	}


	//This method converts all input bytes to a integer value
	public static int bytesToInteger(int [] value)
	{
		int result=0;
		
		for (int i=0; i<value.length;i++)
		{
			result |=  value[i]; // or operation			
			if (i != value.length-1)
			{
				result <<= 8;
			}
		}	
		
		return result;

	}
	
	//This method does two's complement and one's complement conversion for the input bytes
	public static int signedConversion(int [] value, String signed)
	{
		int result=0;		
	
		if ((value[0] & 0x80) !=0)
		{
			result = bytesToInteger(value);	
			// Two's compliment logic
			if (value.length == 2)
			{
				result ^=0xFFFF;
			}
			else if (value.length == 4)
			{
				result ^=0xFFFFFFFF;
			}	
			
			if (signed.equals("signed-2"))
			{
				result -=1;					
			}
			else if (signed.equals("signed-1"))
			{
				//do nothing
			}
			result *=-1;			
		}
		else
		{
			result = bytesToInteger(value);	
		}			
		return result;		
	}

	
	//This method converts input bytes to four byte float value
	public static float bytesToFloat(int [] buffer)
	{
		return(Float.intBitsToFloat( buffer[3] ^ buffer[2]<<8 ^ buffer[1]<<16 ^ buffer[0]<<24));	
	}	

	//This method extracts the modbus register bytes from the response and does the required transformation as required
	// Output of this method is Tagid attached with engineering value.	
	public static void decodeModbus_FC3_FC4(int [] rxBuff,int firstIdx, int lastIdx)
	{

		String endiansize,encoding,tagId;
		int bytelen=0,arryIndx=0;
		int [] bytes;
		float engineeringValue=0,scaling=0;



		for (int i=firstIdx; i<=lastIdx; i++)
		{
			JSONObject jsonObj = (JSONObject) ModbusDataRetrive.jsonAIGlobalObject.get(i);


			endiansize = (String)jsonObj.get("endianSize");			
			encoding = (String)jsonObj.get("encoding");			
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
			//extract exact bytes of the modbus registers
			bytes = Arrays.copyOfRange(rxBuff, arryIndx, arryIndx+bytelen);	
			//update arryIndx for next modbus register
			arryIndx += bytelen;


			// Endian code check & conversion
			if ((endiansize.equals("B32")) || (endiansize.equals("B16")))
			{
				bytes = toBigEndian(bytes);
			}
			else if ((endiansize.equals("L32")) || (endiansize.equals("L16")))
			{
				bytes = toLittleEndian(bytes);

			}

			if (encoding.equals("floating"))
			{
				engineeringValue = bytesToFloat(bytes);
			}
			else if (encoding.equals("signed-1"))
			{
				engineeringValue = signedConversion(bytes,"signed-1");
				engineeringValue *=scaling;				
			}
			else if (encoding.equals("signed-2"))
			{				
				engineeringValue = signedConversion(bytes,"signed-2");
				engineeringValue *=scaling;

			}
			else if (encoding.equals("unsigned"))
			{
				engineeringValue = bytesToInteger(bytes);
				engineeringValue *=scaling;
			}		

			System.out.println(tagId + ": " +engineeringValue);//2dl


		}

		System.out.println("------------One Modbus Transaction Done-----");//2dl remove traces later

	}


	public static void main(String[] args) throws IOException,JSONException, ParseException
	{
		int funcCode=3,firstIndex=0,lastIndex=0;
		List<Integer> modbusInterfaceList = new ArrayList<Integer>();


		ModbusClientRun myConnection = new ModbusClientRun();	

		if  (ModbusDataRetrive.readModbusConfigFile(myConnection) == false)
		{
			System.out.println("ModbusConfig.json File not found");
			System.out.println("Gateway Interfaces will not launch");
			return;
		}


		modbusInterfaceList = ModbusDataRetrive.readModbusRegisters( myConnection);//2dl first read modbus registers file and load datastructure

		ModbusClient modbusClient = new ModbusClient();
		modbusClient.Connect(myConnection.targetIP,myConnection.port);
		modbusClient.setUnitIdentifier((byte)myConnection.slaveId);
	


		try
		{
			do
			{
				for (int i=0;i<modbusInterfaceList.size()-1;i+=5)
				{
					firstIndex = modbusInterfaceList.get(i);
					funcCode = modbusInterfaceList.get(i+1);
					myConnection.startAddr = modbusInterfaceList.get(i+2);
					myConnection.noOfReg   = modbusInterfaceList.get(i+3)*2;//2dl add comments later             		
					lastIndex  = modbusInterfaceList.get(i+4);

					if (funcCode == 3)// Read Holding Registers
					{	
						int[] responseHoldingRegs = modbusClient.ReadHoldingRegisters(myConnection.startAddr-1, myConnection.noOfReg);           			
						decodeModbus_FC3_FC4(responseHoldingRegs,firstIndex,lastIndex);		
						
					}
					else if (funcCode == 4)// Read Input Registers
					{	
						int[] responseInputRegs = modbusClient.ReadInputRegisters(myConnection.startAddr-1, myConnection.noOfReg);//2dl check start address again

						decodeModbus_FC3_FC4(responseInputRegs,firstIndex,lastIndex);
						

					}
				}
				//2dl add period as delay
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


