import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ModbusDataRetrive {


	static JSONArray jsonAIGlobalObject;


	//the following method will load interface configuration data
	public static boolean readModbusConfigFile (ModbusClientRun myConnection) throws IOException,JSONException, ParseException
	{
		JSONParser jsonParser = new JSONParser();
		try
		{
			Object obj = jsonParser.parse(new FileReader(myConnection.modbusConfigFileName));
			JSONObject jsonObject = (JSONObject) obj;	

			myConnection.totalNoOfInterfaces = (int)(long)jsonObject.get("totalDatasets");


			JSONObject jsonChildObject = (JSONObject) jsonObject.get("dataset1");
			myConnection.protocol = (String)jsonChildObject.get("protocol");
			myConnection.interfaceName = (String)jsonChildObject.get("interfaceName");
			myConnection.targetIP = (String)jsonChildObject.get("targetIP");
			myConnection.linkType = (String)jsonChildObject.get("linkType");
			myConnection.interfaces = (String)jsonChildObject.get("interfaces");


			myConnection.dataset = (int)(long)jsonChildObject.get("dataSet");
			myConnection.gatewayID = (int)(long)jsonChildObject.get("gatewayID");
			myConnection.port = (int)(long)jsonChildObject.get("port");	
			myConnection.period = (int)(long)jsonChildObject.get("period");	
			myConnection.timeout = (int)(long)jsonChildObject.get("timeout");	
			myConnection.timeWaitToRequest = (int)(long)jsonChildObject.get("timeWaitToRequest");	
			myConnection.reconnect = (boolean)jsonChildObject.get("reconnect");	


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
	// FirstIdx, FuncCode, ModbuStartAddr, NoofRegs, LastIdx
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


	public static List<Integer> readModbusRegisters(ModbusClientRun myConnection)throws IOException,JSONException, ParseException
	{		
		List<Integer> modbusInterfaceList = new ArrayList<Integer>();
		JSONParser jsonParser = new JSONParser();

		try
		{
			Object obj = jsonParser.parse(new FileReader(myConnection.modbusRegisterMapFileName));
			JSONObject jsonObject = (JSONObject) obj;
			JSONObject jsonchildObject = (JSONObject) jsonObject.get("dataset1");
			//JSONObject jsonchil1dObject = (JSONObject) jsonchildObject.get("deviceInfo");
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
		}		



		return modbusInterfaceList;//2dl check later
	}

}
