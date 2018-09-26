/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier.mode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jenotifier.config.JENotifierConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gf
 */
public class Service {
    private static final Logger logger = LogManager.getLogger(Service.class);

    private JENotifierConfig _config;
    private List<Long> _notiID;
//    private JEVisDataSource _ds;

    public Service(JENotifierConfig config) {
        _config = config;
//        connectDatabase();
    }

    //    private void connectDatabase() {
//        try {
//            _ds = new JEVisDataSourceSQL(_config.getDBHost(), _config.getDBPort(), _config.getDBSchema(), _config.getDBUser(), _config.getDBPassword());
//            _ds.connect(_config.getJEVisUserName(), _config.getJEVisUserPassword());
//        } catch (JEVisException ex) {
//            Logger.getLogger(Single.class.getName()).log(Level.ERROR, null, ex);
//        }
//    }
    public void start() {
        try {
            int port = _config.getServicePort();
            ServerSocket server = new ServerSocket(port);
            while (true) {
                Socket socket = server.accept();
                new Thread(new ServiceTask(socket)).start();
            }
        } catch (IOException ex) {
            logger.fatal(ex);
        }
    }

    class ServiceTask implements Runnable {

        private Socket socket;

        public ServiceTask(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            handleSocket();
        }

        private void handleSocket() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String temp;
                int index;

                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                writer(writer, "---------the instruct of the service---------\nend: eof;\nbegin: start;\n---------------------------------------------");
                while ((temp = reader.readLine()) != null) {//
                    if ((index = temp.indexOf("start")) != -1) {//temp.equals("start")
                        writer(writer, "---------import the ids of the notifications, split by \";\"; Or send all notifications, enter \"sendAll\"; Or enter \"eid\" to return---------");
                        while ((temp = reader.readLine()) != null) {//
                            if ((index = temp.indexOf("sendAll")) != -1) {//
                                SingleAll all = new SingleAll(_config, writer);
                                all.start();
                                break;
                            } else {
                                _notiID = IDsHandler(temp);
                                if (_notiID != null && !_notiID.isEmpty()) {
                                    _config.changeNotificationIDs(_notiID);
//                                    logger.info("fgh" + _config.getNotificationIDs().size());
                                    Single s = new Single(_config, writer);
                                    s.start();
                                    writer(writer, "If you wants to send more notifications,enter \"start\" and import ids again.");
                                    break;
                                } else if ((index = temp.indexOf("eid")) != -1) {//
                                    break;
                                } else {
                                    writer(writer, "please imort the right ids.Or enter \"eid\" to stop import ids.");
                                }
                            }
//                            _notiID = IDsHandler(temp);
//                            for (long lg : _notiID) {
//                                writer.write(lg + "\n");
//                                writer.flush();
//                            }
                        }

                    } else if ((index = temp.indexOf("eof")) != -1) {//
                        break;
                    }
                }
                writer(writer, "-------End the Service of JENotifier.--------");
                writer.close();
                reader.close();
                socket.close();
            } catch (IOException ex) {
                logger.fatal(ex);
            }
//            catch (InterruptedException ex) {
//                logger.fatal(ex);
//            }

        }

        /**
         * @param writer
         * @param str
         */
        private void writer(PrintWriter writer, String str) {
            writer.println(str);
            writer.flush();
        }

        /**
         * @param str
         * @return
         */
        private List<Long> IDsHandler(String str) {
            List<Long> ids = new ArrayList<>();
            StringBuilder id = new StringBuilder();
            for (char c : str.toCharArray()) {
                if (Character.isDigit(c)) {
                    id = id.append(c);
                } else {
                    if (!id.toString().equals("")) {
                        ids.add(Long.parseLong(id.toString()));
                        id = new StringBuilder();
                    }
                }
            }
            if (!id.toString().equals("")) {
                ids.add(Long.parseLong(id.toString()));
            }
            return ids;
        }
    }
}
