package blueserve;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 *
 * @author Ramis
 */
public class BlueServe {

    public static void main(String[] args) throws IOException {
        if(args.length == 0)
        {
            Process runtime = Runtime.getRuntime().exec("cmd /c start cmd /c java -jar blueServe.jar 2");
            System.exit(0);
        }
        Thread waitThread = new Thread(new WaitThread());
        waitThread.start();
    }

    private static class WaitThread implements Runnable {

        @Override
        public void run() {
            waitForConnection();
        }

        private void waitForConnection() {
            LocalDevice local = null;

            StreamConnectionNotifier notifier;
            StreamConnection connection = null;

            try {
                local = LocalDevice.getLocalDevice();
                local.setDiscoverable(DiscoveryAgent.GIAC);

                UUID uuid = new UUID(80087355);
                String url = "btspp://localhost:" + uuid.toString() + ";name=RemoteBluetooth";
                notifier = (StreamConnectionNotifier) Connector.open(url);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            // waiting for connection
            while (true) {
                try {
                    System.out.println("waiting for connection...");
                    connection = notifier.acceptAndOpen();

                    Thread processThread = new Thread(new ProcessConnectionThread(connection));
                    processThread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

        private class ProcessConnectionThread implements Runnable {

            private StreamConnection mConnection;
            Automator p;

            public ProcessConnectionThread(StreamConnection connection) throws AWTException {
                mConnection = connection;
                p = new Automator();
            }

            @Override
            public void run() {
                try {
                    DataInputStream inputStream = mConnection.openDataInputStream();
                    System.out.println("waiting for input");
                    String receive = "";
                    int find = 0;
                    while (true) {
                        int g = inputStream.read();
                        if (find++ > 0) {
                            if (find == 1 && (char) g == '|') {
                                receive += (char) g;
                            continue;
                            }else if ((char) g != '|') {
                               receive += (char) g;
                            continue;
                            }
                            else{
                            processCommand(receive);
                            find = 0;
                            receive = "";
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            private void processCommand(String command) {
                try {
                    System.out.println(command);
                    String[] data = command.split(":");
                    if(data[2].equals("DOWN"))
                        if(data[0].equals("MOUSE"))
                          p.MouseDown(data[1]);
                        else
                            p.KeyDown(data[1]);
                    else if(data[2].equals("UP"))
                        if(data[0].equals("MOUSE"))
                            p.MouseUp(data[1]);
                        else
                            p.KeyUp(data[1]);
                    else{
                        p.MouseMove(data[1], (int) (Double.parseDouble(data[2]) / 15));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        class Automator {
            Robot robot;
            public Automator() throws AWTException {
                this.robot = new Robot();
             }
                private void KeyUp(String action){
                try{
                    robot.keyRelease(GetKeyCode(action));
                }
                catch(Exception e){
                    System.out.println("these key to be implemented");
                }
            }
            public int GetKeyCode(String action) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException{
                for (Field Field : Class.forName(KeyEvent.class.getCanonicalName()).getFields()) {
                    if (Field.getName().equals("VK_"+action)) {
                        return (int) Field.get(Field);
                    }
                }
                return -1;
            }
            private void KeyDown(String action){
                try{
                    robot.keyPress(GetKeyCode(action));
                }
                catch(Exception e){
                    System.out.println("these key to be implemented");
                }
            }
            Point p;
            private void MouseDown(String action){
                if(action.equals("RIGHTCLK"))
                                    robot.mousePress(InputEvent.BUTTON3_MASK);
                
                else if(action.equals("LEFTCLK"))
                                    robot.mousePress(InputEvent.BUTTON1_MASK);
                
            }
            private void MouseUp(String action)
            {
                if(action.equals("RIGHTCLK"))
                    robot.mouseRelease(InputEvent.BUTTON3_MASK);
                else if(action.equals("LEFTCLK"))
                    robot.mouseRelease(InputEvent.BUTTON1_MASK);
            }
            public void MouseMove(String action, int Speed){
                                p = MouseInfo.getPointerInfo().getLocation();
                if(action.equals("DOWN"))
                    robot.mouseMove(p.x, p.y+Speed);
                else if(action.equals("LEFT"))
                    robot.mouseMove(p.x-Speed, p.y);
                else if(action.equals("RIGHT"))
                    robot.mouseMove(p.x+Speed, p.y);
                else if(action.equals("UP"))
                    robot.mouseMove(p.x, p.y-Speed);
                else if(action.equals("UPRIGHT"))
                    robot.mouseMove(p.x+Speed, p.y-Speed);
                else if(action.equals("UPLEFT"))
                    robot.mouseMove(p.x-Speed, p.y-Speed);
                else if(action.equals("DOWNRIGHT"))
                    robot.mouseMove(p.x+Speed, p.y+Speed);
                else if(action.equals("DOWNLEFT"))
                    robot.mouseMove(p.x-Speed, p.y+Speed);
            
        }
    }
}
}