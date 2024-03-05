//package StellarisRandomizer;

import java.io.File;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.HashMap;

public class Randomizer{

    private static Scanner reader = null;
    private static PrintStream writer = null;
    private static HashMap<String,LinkedList<String>> tokenMap = null;
    private static LinkedList<File> targets = null;

    public static void main(String args[]){
        //read the settings...
        targets = new LinkedList<File>();
        tokenMap = new HashMap<String, LinkedList<String>>();
        
        Util.readSettings(targets, tokenMap);
        File destFile = new File("./overwrite");
        if(destFile.exists()){
            //if it exists, delete it all
            Util.recursiveDelete(destFile);
        }
        try{
            destFile.mkdir();
        }
        catch(Exception E){
            System.exit(-1);
        }

        //we need to recursively search through each file in the targets list
        @SuppressWarnings("unchecked")
        List<File[]> queue = (List<File[]>)Util.getTextFilesFromTarget(targets);
        LinkedList<File[]> targetGameFiles = new LinkedList<File[]>(queue);

        for (File[] from_to: targetGameFiles){
            
            reader = null;
            writer = null;
            try{
                from_to[1].getParentFile().mkdirs();
                from_to[1].createNewFile();
                reader = new Scanner(from_to[0]);
                writer = new PrintStream(from_to[1]);
            }
            catch(Exception E){
                System.exit(-2);
            }
            //we now have the parent file to read from, and the new file to write to....
            buildNewGameFile();
        }
    }

    private static void buildNewGameFile(){

        //read and write exactly as we find, unless the line contains a token...
        String line;
        String newLine;
        String[] parsedLine;
        while(reader.hasNextLine()){
            line = reader.nextLine();
            newLine = line;
            for(String token:tokenMap.keySet()){
                if(line.contains(token)){
                    //if the line contains a token...
                    newLine = "";
                    parsedLine = line.split(token);
                    try{
                        //preserve the whitespace...
                        newLine += parsedLine[0] + token;
                        //the first instance of the token should be followed by a equals and a number ONLY
                        parsedLine = parsedLine[1].split("=");
                        //preserve whitespace before the equals...
                        newLine += parsedLine[0] + "= ";
                        //take the number that comes after the =...
                        int closeIndex = parsedLine[1].indexOf("}");
                        if(closeIndex != -1){
                            //if there is a } in the same line as the number, remove it and add it back later
                            parsedLine[1] = parsedLine[1].substring(0, closeIndex);
                        }
                        float val = Float.parseFloat(parsedLine[1].trim());
                        //adjust the value to be within +/- 2 times the original value
                        float adj = (float)(Math.random() * 3 - 1);
                        LinkedList<String> tagList = tokenMap.get(token);
                        if(tagList.contains("importantValue")){
                            adj = (float)(Math.random() * 4.33f + 0.67f);
                        }
                        else if(tagList.contains("importantValueSmall")){
                            adj = (float)(Math.random() * 2.33f + 0.67f);
                        }
                        val *= adj;
                        newLine += String.format("%.3f", val);
                        if(closeIndex != -1){
                            newLine += " }";
                        }
                        System.out.println("Printed line: " + newLine.trim());
                        break;
                    }
                    catch(Exception E){
                        //System.err.println("Error: " + line);
                        System.err.println(E);
                        newLine = line;
                        break;
                    }
                }
            }
            writer.println(newLine);
        }

        reader.close();
        writer.close();
    }
}