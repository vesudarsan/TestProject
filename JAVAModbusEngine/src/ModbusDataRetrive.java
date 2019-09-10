import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.re.easymodbus.modbusclient.ModbusClient;

public class ModbusDataRetrive {


	// The following class variables are used for each interface configuration
	public String protocol,interfaceName,linkType,interfaces,curDir,targetIP;
	public int dataset,gatewayID,period,timeout,timeWaitToRequest,port,slaveId;
	public boolean reconnect = false;
	public int totalNoOfInterfaces;	
	public String modbusConfigFileName,modbusRegisterMapFileName,myDataset;

	List<Integer> modbusInterfaceList = new ArrayList<Integer>();
	JSONArray jsonAIGlobalObject;//2dl change name later
	ModbusClient modbusClient;
	




	//Constructor
	
	public ModbusDataRetrive(int dataset)
	{
		this.modbusConfigFileName = "ModbusConfig.json";
		this.modbusRegisterMapFileName = "RegisterDeviceMap.json";
		this.myDataset = Integer.toString(dataset);		
		System.out.println("ModbusDataRetrive class Initiated for " + "dataset: " + dataset);		
	}


	
	//This method will converts all input bytes into little endian order(Least Significant Byte to Most Significant Byte)
	public int [] toLittleEndian(int [] value) 
	{
		final int length = value.length;
		int [] result = new int[length];
		for(int i = 0; i < length; i++) {
			result[length - i - 1] = value[i];
		}
		return result;
	}
	
	//This method will converts all input bytes into little endian order(Most Significant Byte to Least Significant Byte)
	public int [] toBigEndian(int [] value) 
	{
		final int length = value.length;
		int [] result = new int[length];
		for(int i = 0; i < length; i++) {
			result[i] = value[i];
		}
		return result;
	}


	//This method converts all input bytes to a integer value
	public int bytesToInteger(int [] value)
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
	public int signedConversion(int [] value, String signed)
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
	public float bytesToFloat(int [] buffer)
	{
		return(Float.intBitsToFloat( buffer[3] ^ buffer[2]<<8 ^ buffer[1]<<16 ^ buffer[0]<<24));	
	}	

	//This method extracts the modbus register bytes from the response and does the required transformation as required
	// Output of this method is Tagid attached with engineering value.	
	public void decodeModbus_FC3_FC4(int [] rxBuff,int firstIdx, int lastIdx)	
	{

		String endiansize,encoding,tagId;
		int bytelen=0,arryIndx=0;
		int [] bytes;
		float engineeringValue=0,scaling=0;



		for (int i=firstIdx; i<=lastIdx; i++)
		{
			JSONObject jsonObj = (JSONObject) jsonAIGlobalObject.get(i);


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

		System.out.println("---One Modbus Transaction Done for DS: " + dataset);//2dl remove traces later

	}
	//the following method will read ModbusConfig.json file and identify number of interfaces
	public static int readNoDevices (String modbusConfigFileName) 
	{
		int count =0;
		JSONParser jsonParser = new JSONParser();
		try {
			Object obj = jsonParser.parse(new FileReader(modbusConfigFileName));
			JSONObject jsonObject = (JSONObject) obj;	
			count = (int)(long)jsonObject.get("totalDatasets");			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("modbusConfigFileName file not found");
			
			e.printStackTrace();
			return count;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return count;
		
	}

	//the following method will load interface configuration data
	public boolean readModbusConfigFile () throws IOException,JSONException, ParseException
	{
		JSONParser jsonParser = new JSONParser();
		try
		{
			Object obj = jsonParser.parse(new FileReader(modbusConfigFileName));
			JSONObject jsonObject = (JSONObject) obj;	

			totalNoOfInterfaces = (int)(long)jsonObject.get("totalDatasets");

			JSONObject jsonChildObject = (JSONObject) jsonObject.get(myDataset);
			protocol = (String)jsonChildObject.get("protocol");
			interfaceName = (String)jsonChildObject.get("interfaceName");
			targetIP = (String)jsonChildObject.get("targetIP");
			linkType = (String)jsonChildObject.get("linkType");
			interfaces = (String)jsonChildObject.get("interfaces");


			dataset = (int)(long)jsonChildObject.get("dataSet");
			gatewayID = (int)(long)jsonChildObject.get("gatewayID");
			port = (int)(long)jsonChildObject.get("port");	
			period = (int)(long)jsonChildObject.get("period");	
			timeout = (int)(long)jsonChildObject.get("timeout");	
			timeWaitToRequest = (int)(long)jsonChildObject.get("timeWaitToRequest");	
			reconnect = (boolean)jsonChildObject.get("reconnect");	


		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();			
			return false;//
		}

		return true;//2dl check later
	}


	// This method will iterate over the json array schema and extract the following information
	// if there is modbus address break found, then the list will be updated with the following info for each Modbus request
	// First Index of the array, Function Code, ModbuStartAddr, No of Registers, Last Index of the array
	public static List<Integer> modbusDatastructList(JSONArray jsonArray){
		int modRegister,nxtModRegister=0,noOfReg=0;
		String endiansize;
		int twoBytes=2,oneByte=1,diffValue=0;//2dl
		boolean startAddressNotAdded = true;			
		List<Integer> modbusInterfaceList = new ArrayList<Integer>();
		int funcCode=0;

		try
		{ 
			for (int i=0; i<=jsonArray.size()-1; i++)
			{
				// execute for all entries except for the last entry in the json scheme
				if (i != jsonArray.size()-1)
				{
					JSONObject jsonObj = (JSONObject) jsonArray.get(i);
					JSONObject jsonNxtObj = (JSONObject) jsonArray.get(i+1);	

					// extract the modbus address for the present line and next line
					modRegister = (int)(long) jsonObj.get("modRegister");
					nxtModRegister = (int)(long) jsonNxtObj.get("modRegister");
					endiansize = (String)jsonObj.get("endianSize");
					funcCode = (int)(long)jsonObj.get("funcCode");

					//Add index no of the json arrary and start address of the modbus request in the list
					if (startAddressNotAdded) 
					{						
						// add first index of the JSON SChema
						modbusInterfaceList.add(i);

						// add function code
						modbusInterfaceList.add(funcCode);

						//add start address of the modbus request
						modbusInterfaceList.add(modRegister);
						startAddressNotAdded=false;
					}			

					// find the register size 
					if ((endiansize.equals("B32")) || (endiansize.equals("L32")))
					{
						diffValue = twoBytes;
						noOfReg += twoBytes;
					}
					else if ((endiansize.equals("B16")) || (endiansize.equals("L16")))
					{
						diffValue = oneByte;
						noOfReg += oneByte;
					}
					//if there is a difference, then address break is found
					if ( nxtModRegister - modRegister != diffValue)
					{

						// add no of modbus registers with out break in the list
						modbusInterfaceList.add(noOfReg);

						// add last index of the JSON array SChema, when address break is found
						modbusInterfaceList.add(i);

						// rest this variable for the next modbus request block
						noOfReg = 0;

						// reset the following boolean variable, for the next modbus request information
						startAddressNotAdded = true;
					} 
				}
				// the following code is only for last line of the JSON schema register map
				else if (i == jsonArray.size()-1)
				{
					JSONObject jsonObj = (JSONObject) jsonArray.get(i);	   
					modRegister = (int)(long) jsonObj.get("modRegister");
					funcCode = (int)(long)jsonObj.get("funcCode");
					endiansize = (String)jsonObj.get("endianSize");

					if ((endiansize.equals("B32")) || (endiansize.equals("L32")))
					{
						diffValue = twoBytes;
						noOfReg += twoBytes;
					}
					else if ((endiansize.equals("B16")) || (endiansize.equals("L16")))
					{
						diffValue = oneByte;
						noOfReg += oneByte;
					}

					//Add start address of the modbus request
					if (startAddressNotAdded) 
					{						
						// add first index of the JSON SChema
						modbusInterfaceList.add(i);

						// add function code
						modbusInterfaceList.add(funcCode);

						//add start address of the modbus request
						modbusInterfaceList.add(modRegister);
						startAddressNotAdded=false;

						// add no of modbus registes present till this block
						modbusInterfaceList.add(noOfReg);
						// rest this variable for the next modbus request block
						noOfReg = 0;

						// add last index of the JSON SChema, when address break is found
						modbusInterfaceList.add(i);
					}
					else
					{
						// add no of modbus registes present till this block
						modbusInterfaceList.add(noOfReg);
						// rest this variable for the next modbus request block
						noOfReg = 0;

						// add last index of the JSON SChema, when address break is found
						modbusInterfaceList.add(i);
					}

				}				   

			}

		}
		catch(Exception e)//2dl need to check for default exception later
		{
			e.printStackTrace();
		}


		return modbusInterfaceList;

	}


	public boolean readModbusRegisters()throws IOException,JSONException, ParseException
	{	
		JSONParser jsonParser = new JSONParser();
		boolean result = true;

		try
		{
			Object obj = jsonParser.parse(new FileReader(modbusRegisterMapFileName));
			JSONObject jsonObject = (JSONObject) obj;
			JSONObject jsonchildObject = (JSONObject) jsonObject.get(myDataset);
			JSONObject jsonchil1dObject = (JSONObject) jsonchildObject.get("deviceInfo");
			slaveId = (int)(long)jsonchil1dObject.get("slaveId");
			JSONArray jsonArray = (JSONArray) jsonchildObject.get("AItagList");		


			jsonAIGlobalObject = jsonArray; // this is will be used for decoding signals //2dl add traces
			modbusInterfaceList = modbusDatastructList(jsonArray);
			// the following code will be used for debuging purpose. so commented 
			//				System.out.println("FirstIdx, " + "FuncCode, " + "ModbuStartAddr, " + "NoofRegs, " + "LastIdx" );
			//				for(int i=0;i<modbusInterfaceList.size();i+=5){					
			//					System.out.print( modbusInterfaceList.get(i));
			//				    System.out.print(", " + modbusInterfaceList.get(i+1));
			//				    System.out.print(", " + modbusInterfaceList.get(i+2));
			//				    System.out.print(", " + modbusInterfaceList.get(i+3));
			//				    System.out.print(", " + modbusInterfaceList.get(i+4));
			//				    System.out.println();
			//				} 
			//				

		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
			result = false;			
		}		


		return result;
	}

	public boolean init() throws IOException, JSONException, ParseException
	{
		
	   if  (readModbusConfigFile() == false)
		{
			System.out.println("ModbusConfig.json file not found");
			System.out.println("Gateway Interfaces will not launch");
			return false;
		}
		
		//2dl first read modbus registers file and load data structure	
		if (readModbusRegisters()== false)
		{
			System.out.println("RegisterDeviceMap.json file not found");
			System.out.println("Gateway Interfaces will not launch");
			return false;
		}
		
		modbusClient = modbusDeviceConnection();


	return true;
		
	}
	
	//2dl here use single ton design pattern
	public ModbusClient modbusDeviceConnection() throws UnknownHostException, IOException
	{
		ModbusClient modbusClient = new ModbusClient();//2dl need to check this code
		modbusClient.Connect(targetIP,port);
		modbusClient.setUnitIdentifier((byte)slaveId);	
		return (modbusClient);
		
	}
	
	public void run() throws UnknownHostException, IOException
	{
		int firstIndex=0,funcCode=0,startAddr=0,noOfReg=0,lastIndex=0;	
			
		try
		{
			
				for (int i=0;i<modbusInterfaceList.size()-1;i+=5)
				{
					firstIndex = modbusInterfaceList.get(i);
					funcCode = modbusInterfaceList.get(i+1);
					startAddr = modbusInterfaceList.get(i+2);
					noOfReg   = modbusInterfaceList.get(i+3)*2;//2dl add comments later             		
					lastIndex  = modbusInterfaceList.get(i+4);

					if (funcCode == 3)// Read Holding Registers
					{	
						int[] responseHoldingRegs = modbusClient.ReadHoldingRegisters(startAddr-1, noOfReg);           			
						decodeModbus_FC3_FC4(responseHoldingRegs,firstIndex,lastIndex);		
						
					}
					else if (funcCode == 4)// Read Input Registers
					{	
						int[] responseInputRegs = modbusClient.ReadInputRegisters(startAddr-1, noOfReg);//2dl check start address again
						decodeModbus_FC3_FC4(responseInputRegs,firstIndex,lastIndex);						

					}
				}

		}
		catch (Exception e)
		{

		}
		finally
		{
			

		}		
		
	}
	
}
