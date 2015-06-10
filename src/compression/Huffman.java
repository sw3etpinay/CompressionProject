/*
 * Huffman.java
 *
 * Created on May 21, 2007, 1:01 PM
 */

package compression;


import java.util.*;
import java.io.*;

/**
 *
 * @author Jin Chang & pbladek
 */
public class Huffman
{  
    public static final int CHARMAX = 128;
    public static final byte CHARBITS = 7;
    public static final short CHARBITMAX = 128;
    //private HuffmanTree<Character> theTree;
    private byte[] byteArray;
    private String file;
    private Map mapCharCount;
    private Map sortedMap;
    HuffmanChar[] charCountArray;
    char[] readChar; 
    byte[] saveDataArray;
    
    
    /**
     * Creates a new instance of Main
     */
    public Huffman() 
    {
        byteArray = new byte[CHARBITMAX];
        mapCharCount = new HashMap<>();
        sortedMap = new LinkedHashMap<>();
    }
    
    /**
     * main
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
//----------------------------------------------------
// used for debugging encoding
//----------------------------------------------------
//        args = new String[1];
//        args[0] = "alice.txt";
//----------------------------------------------------
// used for debugging encoding
//----------------------------------------------------
//        args = new String[2];
//        args[0] = "-d";
//        args[1] = "alice.txt";  
//----------------------------------------------------        
        boolean decode = true;
        String textFileName = "";
        if(args.length > 0)
        {
            if(args[0].substring(0,2).toLowerCase().equals("-d"))
            {
                decode = true;
                if(args.length > 1)
                    textFileName = args[1];
            }
            else
                textFileName = args[0];
        }
        Huffman coder = new Huffman();
        if(decode)
            coder.encode(textFileName);
        else
            coder.decode(textFileName);
    }

    /*
     * encode
     * @param fileName the file to encode
     */
    public void encode(String fileName)
    {
        String text = "";
        try
        {
            BufferedReader readMe = new BufferedReader
                    (new FileReader(fileName));
            while((text = readMe.readLine()) != null)
            {
                String sentence = text + "\n";
                readChar = sentence.toCharArray();
                for(int i = 0; i < readChar.length; i++)
                {
                    char c = readChar[i];
                    countChars((byte)c);
                }
            }   
        }
        catch(FileNotFoundException e)
        {
            System.exit(0);
        }
        catch(IOException e)
        {
            
        }
        addCharAndCount();
//        theTree = new HuffmanTree(sortedMap);
        writeEncodedFile(byteArray, fileName);
        writeKeyFile(fileName);
    } 
 
    /*
     * decode
     * @param inFileName the file to decode
     */   
    public void decode(String inFileName)
    { 
        String text = "";
        try
        {
            BufferedReader readMe = new BufferedReader
                    (new FileReader(inFileName));
            while((text = readMe.readLine()) != null)
            {
                String sentence = text + "\n";
                readChar = sentence.toCharArray();
                for(int i = 0; i < readChar.length; i++)
                {
                    char c = readChar[i];
                    countChars((byte)c);
                }
            }   
        }
        catch(FileNotFoundException e)
        {
            System.exit(0);
        }
        catch(IOException e)
        {
            
        }
    }
      
    /**
     * writeEncodedFile
     * @param bytes bytes for file
     * @param fileName file input
     * @throws FileNotFoundException if file cannot be found
     */ 
    public void writeEncodedFile(byte[] bytes, String fileName)
            
    {
        writeKeyFile(fileName);
        try
        {
            PrintWriter write = new PrintWriter(new FileOutputStream(
                        file));  
            for(int i = 0; i < bytes.length; i++)
            {
                write.println(bytes[i]);
            }
            write.close();
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
   
    /**
     * writeKeyFile
     * @param fileName the name of the file to write to
     */
    public void writeKeyFile(String fileName)
    {
        String separate = File.separator;
        String editFileName;
        // Remove the path upto the filename.
        int lastSeparatorIndex = fileName.lastIndexOf(separate);
        if (lastSeparatorIndex == -1) 
        {
            editFileName = fileName;
        } 
        else 
        {
            editFileName = fileName.substring(lastSeparatorIndex + 1);
        }
        // Remove the extension.
        int extensionIndex = editFileName.lastIndexOf(".");
        file = editFileName.substring(0, extensionIndex);
        file += ".huf";
    }
    /**
     * count the number characters
     * @param character the character
     */
    private void countChars(byte character)
    {
        for(int i = 0; i < byteArray.length; i++)
        {
            if(i == character)
            {
               byteArray[i] += 1; 
               break;
            }
        }
    }
    /**
     * adds the Characters and occurences into a map then sorts that map and
     * recreates a new sorted map.
     */
    private void addCharAndCount()
    {
        for(int i = 0; i < byteArray.length; i++)
        {
            byte element = byteArray[i];
            if(element > 0)
            {
                char key = (char)i;
                int count = element;
                mapCharCount.put(key,count);
            }
        }
        sortedMap.putAll(sortMap(mapCharCount));
    }
    /**
     * Sorts the Map from lowest to highest and returns the sorted map.
     * @param <Character> ASCII characters
     * @param <Integer> character occurences
     * @param unsortedmap the unsorted map
     * @return sortedMap 
     */
    public static <Character, Integer extends Comparable< ? super Integer>>
            Map<Character, Integer>
    sortMap(final Map <Character, Integer> unsortedmap)
    {
        List<Map.Entry<Character, Integer>> sortedList =
            new ArrayList<>(unsortedmap.size());  
        Map<Character, Integer> sortedMap = new LinkedHashMap
                <Character, Integer>();
        sortedList.addAll(unsortedmap.entrySet());

        Collections.sort(sortedList,
                         new Comparator<Map.Entry<Character, Integer>>()
        {
            @Override
            public int compare(
                   final Map.Entry<Character, Integer> element1,
                   final Map.Entry<Character, Integer> element2)
            {
                return element1.getValue().compareTo(element2.getValue());
            }
        }); 
        //add sorted list to new map
        for (Map.Entry<Character, Integer> entry : sortedList)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        } 
        return sortedMap;
    }  
}
