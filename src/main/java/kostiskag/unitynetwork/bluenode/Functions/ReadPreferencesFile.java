package kostiskag.unitynetwork.bluenode.Functions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Properties;
import kostiskag.unitynetwork.bluenode.App;

/**
 *
 * @author kostis
 */
public class ReadPreferencesFile {
    
    public static void ParseFile(InputStream file) {
        
        Properties cfg = new java.util.Properties();
        try {        
            cfg.load(file);
        } catch (IOException ex) {
            System.err.println("read preferences load file error the file by this far should have been good! this may be an error");
            App.die();
        }
        
        String UseNetwork = cfg.getProperty("network");
        String UnityTracker = cfg.getProperty("UnityTracker");
        String UnityTrackerAuthPort = cfg.getProperty("UnityTrackerAuthPort");        
        String UseList = cfg.getProperty("uselist");
        String Hostname = cfg.getProperty("Hostname");
        String AuthPort = cfg.getProperty("AuthPort");
        String udpstart = cfg.getProperty("udpstart");
        String udpend = cfg.getProperty("udpend");
        String RedNodeLimit = cfg.getProperty("RedNodeLimit");
        String UseGUI = cfg.getProperty("UseGUI");
        String ConsoleTraffic = cfg.getProperty("ConsoleTraffic");
        String AutoLogin = cfg.getProperty("AutoLogin");
        String Log = cfg.getProperty("log");
        
        App.network = Boolean.parseBoolean(UseNetwork);
        App.Taddr = UnityTracker; 
        App.Hostname = Hostname;
        App.Tport = Integer.parseInt(UnityTrackerAuthPort);        
        App.UseList = Boolean.parseBoolean(UseList);
        App.authport = Integer.parseInt(AuthPort);
        App.startport = Integer.parseInt(udpstart);
        App.endport = Integer.parseInt(udpend);
        App.hostnameEntries = Integer.parseInt(RedNodeLimit);
        App.gui = Boolean.parseBoolean(UseGUI);
        App.soutTraffic = Boolean.parseBoolean(ConsoleTraffic);
        App.autologin =  Boolean.parseBoolean(AutoLogin);
        App.log = Boolean.parseBoolean(Log);
        
        System.out.println("");
        System.out.println("UnityTracker is "+App.Taddr);        
        System.out.println("UnityTrackerAuthPort is "+App.Tport);        
        System.out.println("Hostname is "+App.Hostname);
        System.out.println("AuthPort is "+App.authport);
        System.out.println("udpstart is "+App.startport);
        System.out.println("udpend is "+App.endport);
        System.out.println("RedNodeLimit is "+App.hostnameEntries);
        System.out.println("UseGUI is "+App.gui);
        System.out.println("ConsoleTraffic is "+App.soutTraffic);
        System.out.println("AutoLogin is "+App.autologin);        
        System.out.println("Log is "+App.log);   
        System.out.println("");                
    }   

    public static void ParseList(File userlist) throws IOException {
        if (userlist == null || App.accounts == null){
            System.err.println("Rarselist error called with non init accounts or file error");
            App.die();
        }
            
        try {
            
            BufferedReader br = new BufferedReader(new FileReader(userlist));            
                    
            LinkedList<String> list = new LinkedList<String>();
            
            while(br.ready()){            
                list.add(br.readLine());
            }            
            
            int size = list.size();
            String line;
            String[] validline;
            for (int i=0; i<size; i++){
                line = list.poll();
                if (!line.startsWith("#")){
                    validline = line.split("\\s+");
                    App.accounts.insert(validline[0], validline[1], validline[2], validline[3]);
                    System.out.println(validline[0]+ validline[1]+ validline[2]+ validline[3]);
                }
            }            
            System.out.println("userlist loaded");
            
        } catch (FileNotFoundException ex) {
            System.err.println("read userlist error the file by this far should have been valid. this may be a bug");
            App.die();
        }
    }
}