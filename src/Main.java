import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

import net.wimpi.modbus.ModbusCoupler; 
import net.wimpi.modbus.io.ModbusSerialTransaction;
import net.wimpi.modbus.msg.ModbusRequest;
import net.wimpi.modbus.msg.ReadInputRegistersRequest;
import net.wimpi.modbus.msg.ReadInputRegistersResponse;
import net.wimpi.modbus.msg.WriteCoilRequest;
import net.wimpi.modbus.net.SerialConnection; 
import net.wimpi.modbus.util.SerialParameters;

 
public class Main {

	/**
	 * @param args
	 * @throws PortInUseException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws PortInUseException, IOException {
		
		Enumeration e =CommPortIdentifier.getPortIdentifiers();
		CommPortIdentifier portId;
		while (e.hasMoreElements()) {
            portId = (CommPortIdentifier) e.nextElement();
            if(portId.getPortType()==CommPortIdentifier.PORT_SERIAL){
            	SerialPort port = (SerialPort) portId.open("klej", 2000);
            	OutputStream is = port.getOutputStream();
            	is.write(2);
            	port.close();
            }
            System.out.println(portId.isCurrentlyOwned());
            System.out.println(portId.getName());
        }
		SerialConnection con = null; // the connection
		
		try {
			/* The important instances of the classes mentioned before */
			ModbusSerialTransaction trans = null; // the transaction
			ModbusRequest req = null; // the request
			ReadInputRegistersResponse res = null; // the response

			/* Variables for storing the parameters */ 
			String portname = "COM1"; // the name of the serial port to be used
			int unitid = 2; // the unit identifier we will be talking to
			int ref = 100; // the reference, where to start reading from
			int count = 10; // the count of IR's to read
			int repeat = 3; // a loop for repeating the transaction
			// 1. Setup the parameters
			/*if (args.length < 4) {
				System.exit(1);
			} else {
				try {
					portname = args[0];
					unitid = Integer.parseInt(args[1]);
					ref = Integer.parseInt(args[2]);
					count = Integer.parseInt(args[3]);
					if (args.length == 5) {
						repeat = Integer.parseInt(args[4]);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					System.exit(1);
				}
			}*/
			// 2. Set master identifier
			// ModbusCoupler.
			ModbusCoupler.getReference().setUnitID(0);

			// 3. Setup serial parameters
			SerialParameters params = new SerialParameters();
			params.setPortName(portname);
			params.setBaudRate(9600);
			params.setDatabits(8);			
			params.setParity(0);
			params.setStopbits(1);
			params.setEncoding("RTU");
			params.setReceiveTimeout(500);
			params.setEcho(false);
			// 4. Open the connection
			con = new SerialConnection(params);
			//System.out.println(con.getSerialPort());
			System.out.println(con.isOpen());
			con.open();
			
			// 5. Prepare a request
			req = new ReadInputRegistersRequest(ref, count);
			//req = new WriteCoilRequest(1,true);
			req.setUnitID(unitid);
			req.setHeadless();

			// 6. Prepare a transaction
			trans = new ModbusSerialTransaction(con);
			trans.setRequest(req);
			trans.setCheckingValidity(true);
			
			//trans.setTransDelayMS(500);
			// 7. Execute the transaction repeat times
			int k = 0;
			do {
				trans.execute();
				res = (ReadInputRegistersResponse) trans.getResponse();
				System.out.println(res.getWordCount());
				for (int n = 0; n < res.getWordCount(); n++) {
					System.out.println("Word " + n + "="
							+ res.getRegisterValue(n));
				}
				k++;
			} while (k < repeat);

			// 8. Close the connection
			con.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally{
			con.close();
		}
	}

}