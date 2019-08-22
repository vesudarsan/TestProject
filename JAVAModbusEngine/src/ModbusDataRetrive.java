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
		
		public static List<Integer> modbusDatastructList(JSONArray jsonArray){
			int modRegister,nxtModRegister=0,noOfReg=0;
			String endiansize;
			int twoBytes=2,oneByte=1,diffValue=0;//2dl
		    boolean firstAddressAdded = true;			
			List<Integer> modbusInterfaceList = new ArrayList<Integer>();
//			System.out.println("jsonArray.size()" + jsonArray.size() );//2dl
			int funcCode=0;
			
			try
			{
				for (int i=0; i<jsonArray.size(); i++)
				{						
									
					//do the following, if is the last index in Json array
					if (i == jsonArray.size()-1)
					{
						// add no of modbus registes present till this block
						modbusInterfaceList.add(noOfReg);//2dl need to check this code again
						
						// add last index at modbus address break
						modbusInterfaceList.add(i);
						break;
					}
				 
				   JSONObject jsonObj = (JSONObject) jsonArray.get(i);
				   JSONObject jsonNxtObj = (JSONObject) jsonArray.get(i+1);	
					
					
					modRegister = (int)(long) jsonObj.get("modRegister");
					nxtModRegister = (int)(long) jsonNxtObj.get("modRegister");
					endiansize = (String)jsonObj.get("endianSize");
					funcCode = (int)(long)jsonObj.get("funcCode");
					
					//Add start address of the modbus request
					if (firstAddressAdded) 
					{						
						// add first index of the modbus address block
						modbusInterfaceList.add(i);
						
						// add function code
						modbusInterfaceList.add(funcCode);
						
						//add start address of the modbus request
						modbusInterfaceList.add(modRegister);
						firstAddressAdded=false;
					}			
							
					
					if ((endiansize.equals("B32")) || (endiansize.equals("L32")))
					{
						diffValue = twoBytes;
						noOfReg += 2;
					}
					else if ((endiansize.equals("B16")) || (endiansize.equals("L16")))
					{
						diffValue = oneByte;
						noOfReg += 1;
					}
					//if there is a difference, then address break is found
					if ( nxtModRegister - modRegister != diffValue)
					{
										
						// add no of modbus registes present till this block
						modbusInterfaceList.add(noOfReg);
						
						// add last index at modbus address break
						modbusInterfaceList.add(i);
						
						// rest this variable for the next modbus request block
						noOfReg = 0;
						
						// reset the variable to select start address for next block
						firstAddressAdded = true;
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
			
//			int funcCode=0;
			
			List<Integer> modbusInterfaceList = new ArrayList<Integer>();
			
			JSONParser jsonParser = new JSONParser();
			
			try
			{
				Object obj = jsonParser.parse(new FileReader(myConnection.modbusRegisterMapFileName));
				JSONObject jsonObject = (JSONObject) obj;
				JSONObject jsonchildObject = (JSONObject) jsonObject.get("dataset1");
				//JSONObject jsonchil1dObject = (JSONObject) jsonchildObject.get("deviceInfo");
				JSONArray jsonArray = (JSONArray) jsonchildObject.get("AItagList");		
				
				
				
				modbusInterfaceList = modbusDatastructList(jsonArray);
				System.out.println("FirstIdx, " + "FuncCode" + "ModbuStartAddr, " + "NoofRegs, " + "LastIdx" );
				for(int i=0;i<modbusInterfaceList.size();i+=5){					
					System.out.print( modbusInterfaceList.get(i));
				    System.out.print(", " + modbusInterfaceList.get(i+1));
				    System.out.print(", " + modbusInterfaceList.get(i+2));
				    System.out.print(", " + modbusInterfaceList.get(i+3));
				    System.out.print(", " + modbusInterfaceList.get(i+4));
				    System.out.println();
				} 
				

			}
			catch(FileNotFoundException e)
			{
				e.printStackTrace();
			}		
			
			
			
			return modbusInterfaceList;//2dl check later
		}

}
