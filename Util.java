//package StellarisRandomizer;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class Util {

    //FIX FOR MODDED EDITING
    public static Queue<File[]> getTextFilesFromTarget(LinkedList<File> targets){
        Queue<File[]> targetFiles = new LinkedList<File[]>();
        Queue<File> nestedFiles = new LinkedList<File>();
        File currFile, childFile;
        String[] childrenStrings;

        for (File file: targets){
            //BFS on each child
            nestedFiles.add(file);
            while(nestedFiles.peek() != null){
                currFile = nestedFiles.remove();
                System.out.println(currFile.getName());
                childrenStrings = currFile.list();
                for (String child: childrenStrings){
                    childFile = new File(currFile, child);
                    if(childFile.isDirectory()){
                        nestedFiles.add(childFile);
                        //System.err.println("Pushed file " + child + " to the search queue");
                    }
                    //check to make sure the file is a .txt before searching it.
                    else{
                        if(Util.isTXTFile(child)){
                            //if the child is a text file, check it and randomize token appearences
                            File overrideFileName = new File(currFile, child);
                            File[] from_to = new File[2];
                            from_to[0] = overrideFileName;
                            from_to[1] = new File(buildOverwriteFilePath(overrideFileName.getAbsolutePath()));
                            //FIX FOR MODDED EDITING
                            targetFiles.add(from_to);
                        }
                    }
                }
            }
        }
        return targetFiles;
    }

    public static void recursiveDelete(File parentFile){
        if(!parentFile.isDirectory()){
            parentFile.delete();
            return;
        }
        String[] childrenStrings = parentFile.list();
        File childFile;
        for(int i = 0; i < childrenStrings.length; i++){
            childFile = new File(parentFile, childrenStrings[i]);
            recursiveDelete(childFile);
        }
        parentFile.delete();
    } 

    //initializes the search parameters based on the settings text file
    //NEED TO FIX -- values in defines... pops, jobs, weapon percentages, energy from jobs, housing, jobs provided, districts, diplo weight
    public static void readSettings(LinkedList<File> targets, HashMap<String, LinkedList<String>> tokenMap){
        if(targets == null && tokenMap == null){
            return;
        }

        Scanner reader;
        try{
            reader = new Scanner(new File("./settings.txt")); 
        }
        catch(Exception E){
            return;
        }
        //if the settings text file does not exist, make it and print helpful info
        String unparsedIn = reader.nextLine();
        int start = unparsedIn.indexOf("{");
        String[] parsedIn;

        while(start == -1){
            unparsedIn = reader.nextLine();
            start = unparsedIn.indexOf("{");
        }

        unparsedIn = reader.nextLine();
        while(!unparsedIn.trim().equals("}")){
            if(targets != null){
                parsedIn = unparsedIn.split(",");
                //parsedIn is now a list of the file paths we need to target
                for(int i = 0; i < parsedIn.length; i++){
                    targets.add(new File(parsedIn[i].trim()));
                }
            }
            unparsedIn = reader.nextLine();
        }

        //now we need to get the tokens...
        if(tokenMap == null){
            reader.close();
            return;
        }
        unparsedIn = reader.nextLine();
        start = unparsedIn.indexOf("{");
        while(start == -1){
            unparsedIn = reader.nextLine();
            start = unparsedIn.indexOf("{");
        }

        unparsedIn = reader.nextLine();
        while(!unparsedIn.trim().equals("}")){
            parsedIn = unparsedIn.split(",");
            //parsedIn is now a list of the file paths we need to target
            LinkedList<String> tags = new LinkedList<String>();
            
            for(int i = 1; i < parsedIn.length; i++){
                tags.add(parsedIn[i].trim());
            }
            tokenMap.put(parsedIn[0].trim(), tags);
            unparsedIn = reader.nextLine();
        }
        
        reader.close();
    }

    //FIX FOR MODDED EDITING
    public static String buildOverwriteFilePath(String parentPathString){
        //assumes that the last occurence of "Stellaris" in the path denotes the Stellaris directory...
        String[] parsedStrings = parentPathString.split("Stellaris");
        return ".\\overwrite" + parsedStrings[parsedStrings.length-1];
    }

    public static boolean isTXTFile(String fileName){
        int extensionIndex = fileName.lastIndexOf(".");
        String extension = fileName.substring(extensionIndex+1);
        if(extension.equals("txt")){
            return true;
        }
        return false;
    }
}