/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.bluenode.Functions;

import kostiskag.unitynetwork.bluenode.BlueNode.lvl3BlueNode;
import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            lvl3BlueNode.die();
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
        
        lvl3BlueNode.network = Boolean.parseBoolean(UseNetwork);
        lvl3BlueNode.Taddr = UnityTracker; 
        lvl3BlueNode.Hostname = Hostname;
        lvl3BlueNode.Tport = Integer.parseInt(UnityTrackerAuthPort);        
        lvl3BlueNode.UseList = Boolean.parseBoolean(UseList);
        lvl3BlueNode.authport = Integer.parseInt(AuthPort);
        lvl3BlueNode.startport = Integer.parseInt(udpstart);
        lvl3BlueNode.endport = Integer.parseInt(udpend);
        lvl3BlueNode.hostnameEntries = Integer.parseInt(RedNodeLimit);
        lvl3BlueNode.gui = Boolean.parseBoolean(UseGUI);
        lvl3BlueNode.soutTraffic = Boolean.parseBoolean(ConsoleTraffic);
        lvl3BlueNode.autologin =  Boolean.parseBoolean(AutoLogin);
        lvl3BlueNode.log = Boolean.parseBoolean(Log);
        
        System.out.println("");
        System.out.println("UnityTracker is "+lvl3BlueNode.Taddr);        
        System.out.println("UnityTrackerAuthPort is "+lvl3BlueNode.Tport);        
        System.out.println("Hostname is "+lvl3BlueNode.Hostname);
        System.out.println("AuthPort is "+lvl3BlueNode.authport);
        System.out.println("udpstart is "+lvl3BlueNode.startport);
        System.out.println("udpend is "+lvl3BlueNode.endport);
        System.out.println("RedNodeLimit is "+lvl3BlueNode.hostnameEntries);
        System.out.println("UseGUI is "+lvl3BlueNode.gui);
        System.out.println("ConsoleTraffic is "+lvl3BlueNode.soutTraffic);
        System.out.println("AutoLogin is "+lvl3BlueNode.autologin);        
        System.out.println("Log is "+lvl3BlueNode.log);   
        System.out.println("");                
    }   

    public static void ParseList(File userlist) throws IOException {
        if (userlist == null || lvl3BlueNode.accounts == null){
            System.err.println("Rarselist error called with non init accounts or file error");
            lvl3BlueNode.die();
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
                    lvl3BlueNode.accounts.insert(validline[0], validline[1], validline[2], validline[3]);
                    System.out.println(validline[0]+ validline[1]+ validline[2]+ validline[3]);
                }
            }            
            System.out.println("userlist loaded");
            
        } catch (FileNotFoundException ex) {
            System.err.println("read userlist error the file by this far should have been valid. this may be a bug");
            lvl3BlueNode.die();
        }
    }
}