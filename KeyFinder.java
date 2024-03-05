//package StellarisRandomizer;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;

public class KeyFinder{
    
    //function to find possible keys in the files
    static LinkedList<File> targets = null;
    static HashMap<String,LinkedList<String>> tokenMap = null;
    static TreeSet<String> found = null;
    public static void main(String[] args){
        
        targets = new LinkedList<File>();
        tokenMap = new HashMap<String,LinkedList<String>>();

        Util.readSettings(targets, tokenMap);
        if(targets.isEmpty()){
            return;
        }

        File fileOut = new File("./PossibleKeys.txt");
        PrintStream out = null;
        Scanner in = null;
        try{
            if(fileOut.exists()){
                fileOut.delete();
            }
            fileOut.createNewFile();
            out = new PrintStream(fileOut);
        }
        catch(Exception E){}
        
        @SuppressWarnings("unchecked")
        List<File[]> fileQueue = (List<File[]>)Util.getTextFilesFromTarget(targets);
        String lineString, parsedLineString[];
        found = new TreeSet<String>();
        //read through each target file, print out things that might be keys
        for(File[] from_to: fileQueue){
            try{
                in = new Scanner(from_to[0]);
            }
            catch(Exception E){}
            //out.println(from_to[0].getName());
            while(in.hasNext()){
                lineString = in.nextLine();
                //split by equals...
                parsedLineString = lineString.trim().split("=");
                if(parsedLineString.length != 2){
                    continue;
                }
                if(parsedLineString[0].split(" ").length != 1){
                    continue;
                }
                try{
                    Float.parseFloat(parsedLineString[1]);
                }
                catch(Exception E){
                    continue;
                }
                if(found.contains(parsedLineString[0])){
                    continue;
                }
                if(tokenMap.keySet().contains(parsedLineString[0].trim())){
                    continue;
                }
                //out.print("\t");
                out.println(parsedLineString[0]);
                found.add(parsedLineString[0]);
            }
        }
    }
}