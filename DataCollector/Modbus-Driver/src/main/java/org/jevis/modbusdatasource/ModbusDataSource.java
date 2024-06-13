package org.jevis.modbusdatasource;

import com.intelligt.modbus.jlibmodbus.Modbus;
import com.intelligt.modbus.jlibmodbus.data.ModbusHoldingRegisters;
import com.intelligt.modbus.jlibmodbus.exception.*;
import com.intelligt.modbus.jlibmodbus.master.ModbusMaster;
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory;
import com.intelligt.modbus.jlibmodbus.msg.request.ReadHoldingRegistersRequest;
import com.intelligt.modbus.jlibmodbus.msg.response.ReadHoldingRegistersResponse;
import com.intelligt.modbus.jlibmodbus.tcp.TcpParameters;
import com.intelligt.modbus.jlibmodbus.utils.DataUtils;
import com.intelligt.modbus.jlibmodbus.utils.FrameEvent;
import com.intelligt.modbus.jlibmodbus.utils.FrameEventListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.driver.DataSource;
import org.jevis.commons.driver.Importer;
import org.jevis.commons.driver.ImporterFactory;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModbusDataSource implements DataSource {
    private static final Logger logger = LogManager.getLogger(ModbusDataSource.class);
    private final List<JEVisObject> channels = new ArrayList<>();
    private Importer importer;
    private Integer connectionTimeout;
    private Integer readTimeout;
    private String server;
    private Integer port;
    //10.230.240.248:502
    //register 32
    //register holdread 03

    @Override
    public void run() {
        TcpParameters tcpParameters = new TcpParameters();
        //listening on localhost
        try {
            tcpParameters.setHost(InetAddress.getByName(server));

            tcpParameters.setPort(port);
            tcpParameters.setKeepAlive(true);
            tcpParameters.setConnectionTimeout(connectionTimeout);

            ModbusMaster master = ModbusMasterFactory.createModbusMasterTCP(tcpParameters);

            master.setResponseTimeout(readTimeout);

            FrameEventListener listener = new FrameEventListener() {
                @Override
                public void frameSentEvent(FrameEvent event) {
                    logger.info("frame sent {}", DataUtils.toAscii(event.getBytes()));
                }

                @Override
                public void frameReceivedEvent(FrameEvent event) {
                    logger.info("frame recv {}", DataUtils.toAscii(event.getBytes()));
                }
            };

            master.addListener(listener);

            Modbus.setAutoIncrementTransactionId(true);

            master.connect();

            //prepare request
            ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest();
            request.setServerAddress(Modbus.TCP_DEFAULT_ID);
            request.setStartAddress(0);
            request.setQuantity(10);
            ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) request.getResponse();

            master.processRequest(request);
            ModbusHoldingRegisters registers = response.getHoldingRegisters();
            for (int r : registers) {
                logger.info(r);
            }
            //get float
            logger.info("PI is approximately equal to {}", registers.getFloat64At(0));

            master.disconnect();

        } catch (IllegalDataAddressException e) {
            logger.error(e);
        } catch (IllegalDataValueException e) {
            logger.error(e);
        } catch (ModbusIOException e) {
            logger.error(e);
        } catch (ModbusNumberException e) {
            logger.error(e);
        } catch (ModbusProtocolException e) {
            logger.error(e);
        } catch (UnknownHostException e) {
            logger.error(e);
        } catch (Exception e) {
            logger.error(e);
        }

        for (JEVisObject channel : channels) {
            runChannel(channel);
        }
    }

    private void runChannel(JEVisObject channel) {

    }

    @Override
    public void initialize(JEVisObject modbusDataSource) {
        initializeAttributes(modbusDataSource);
        initializeChannelObjects(modbusDataSource);

        importer = ImporterFactory.getImporter(modbusDataSource);
        importer.initialize(modbusDataSource);
    }

    private void initializeChannelObjects(JEVisObject modbusDataSource) {

    }

    @Override
    public List<InputStream> sendSampleRequest(JEVisObject channel) throws Exception {
        return Collections.emptyList();
    }

    @Override
    public void parse(List<InputStream> input) {

    }

    @Override
    public void importResult() {

    }


    private void initializeAttributes(JEVisObject modbusDataSource) {
        try {

            JEVisType readTimeoutType = modbusDataSource.getJEVisClass().getType(DataCollectorTypes.DataSource.DataServer.READ_TIMEOUT);
            JEVisType connectionTimeoutType = modbusDataSource.getJEVisClass().getType(DataCollectorTypes.DataSource.DataServer.CONNECTION_TIMEOUT);
            JEVisType serverType = modbusDataSource.getJEVisClass().getType(DataCollectorTypes.DataSource.DataServer.HOST);
            JEVisType portType = modbusDataSource.getJEVisClass().getType(DataCollectorTypes.DataSource.DataServer.PORT);

            connectionTimeout = DatabaseHelper.getObjectAsInteger(modbusDataSource, connectionTimeoutType);
            readTimeout = DatabaseHelper.getObjectAsInteger(modbusDataSource, readTimeoutType);
            server = DatabaseHelper.getObjectAsString(modbusDataSource, serverType);
            port = DatabaseHelper.getObjectAsInteger(modbusDataSource, portType);

        } catch (Exception e) {
            logger.error(e);
        }
    }
}
