import java.io.File;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;

public class randomizer{

    private static Scanner reader = null;
    private static PrintStream writer = null;
    private static Map<String,LinkedList<String>> tokenMap = null;
    private static LinkedList<File> targets = null;

    public static void main(String args[]){
        //read the settings...
        readSettings();
        //we need to recursively search through each file in the targets list
        Stack<File> nestedFiles;
        File currFile;
        File childFile;
        String[] childrenStrings;
        String destPath;
        File destFile = new File("./overwrite");
        if(destFile.exists()){
            //if it exists, delete it all
            recursiveDelete(destFile);
        }
        try{
            destFile.mkdir();
        }
        catch(Exception E){
            System.exit(-1);
        }

        for (File file: targets){
            //DFS on each child
            nestedFiles = new Stack<File>();
            nestedFiles.push(file);
            while(!nestedFiles.empty()){
                currFile = nestedFiles.pop();
                System.out.println(currFile.getName());
                childrenStrings = currFile.list();
                for (String child: childrenStrings){
                    childFile = new File(currFile, child);
                    if(childFile.isDirectory()){
                        nestedFiles.push(childFile);
                        System.out.println("Pushed file " + child + " to the search queue");
                    }
                    //check to make sure the file is a .txt before searching it.
                    else{
                        if(isTXTFile(child)){
                            //if the child is a text file, check it and randomize token appearences
                            File overrideFileName = new File(currFile, child);
                            destPath = buildOverwriteFilePath(overrideFileName.getAbsolutePath());
                            destFile = new File(destPath);
                            reader = null;
                            writer = null;
                            try{
                                destFile.getParentFile().mkdirs();
                                destFile.createNewFile();
                                reader = new Scanner(childFile);
                                writer = new PrintStream(destFile);
                            }
                            catch(Exception E){
                                System.exit(-2);
                            }
                            //we now have the parent file to read from, and the new file to write to....
                            buildNewGameFile();

                        }
                    }
                }
            }
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

    private static String buildOverwriteFilePath(String parentPathString){
        //assumes that the last occurence of "Stellaris" in the path denotes the Stellaris directory...
        String[] parsedStrings = parentPathString.split("Stellaris");
        return ".\\overwrite" + parsedStrings[parsedStrings.length-1];
    }

    private static void recursiveDelete(File parentFile){
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

    private static boolean isTXTFile(String fileName){
        int extensionIndex = fileName.lastIndexOf(".");
        String extension = fileName.substring(extensionIndex+1);
        if(extension.equals("txt")){
            return true;
        }
        return false;
    }

    //initializes the search parameters based on the settings text file
    //NEED TO FIX -- values in defines... 
    private static void readSettings(){
        try{
            reader = new Scanner(new File("./settings.txt"));
            tokenMap = new HashMap<String, LinkedList<String>>();
        }
        catch(Exception E){}
        //if the settings text file does not exist, make it and print helpful info
        String unparsedIn = reader.nextLine();
        int start = unparsedIn.indexOf("{");
        String[] parsedIn;
        targets = new LinkedList<File>();

        while(start == -1){
            unparsedIn = reader.nextLine();
            start = unparsedIn.indexOf("{");
        }

        unparsedIn = reader.nextLine();
        while(!unparsedIn.trim().equals("}")){
            
            parsedIn = unparsedIn.split(",");
            //parsedIn is now a list of the file paths we need to target
            for(int i = 0; i < parsedIn.length; i++){
                targets.add(new File(parsedIn[i].trim()));
            }
            unparsedIn = reader.nextLine();
        }

        //now we need to get the tokens...
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
}