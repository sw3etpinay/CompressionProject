/*
 * Huffman.java
 *
 * Created on May 21, 2007, 1:01 PM
 */

package compression;


import java.util.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jin Chang & pbladek
 */
public class Huffman
{  
    public static final int CHARMAX = 128;
    public static final byte CHARBITS = 7;
    public static final short CHARBITMAX = 128;
    private HuffmanTree theTree = new HuffmanTree() ;
    private HuffmanChar charCount;
    private HuffmanData data;
    private byte[] byteArray;
    private String[] codeArray;
    private String file;
    private Map mapCharCount;
    private Map sortedMap;
    private SortedMap<Character, String> keyMap;
    private SortedMap<String, Character> codeMap;
    protected ArrayList<HuffmanChar> charCountArray;
    TreeSet <HuffTree> huffTree = new TreeSet<HuffTree>();
    StringBuffer code = new StringBuffer();
    Hashtable <Character,String>huffEncodeTable = new 
        Hashtable <Character,String>();
    ArrayList<String> values = new ArrayList();
    ArrayList<Byte> dataEncoded =  new ArrayList();
    String dataCode = "";
    char[] readChar; 
    byte[] saveDataArray;
    
    
    /**
     * Creates a new instance of Main
     */
    public Huffman() 
    {
  
        codeArray = new String[1000];
        byteArray = new byte[CHARBITMAX];
        mapCharCount = new HashMap<>();
        sortedMap = new LinkedHashMap<>();
        charCountArray = new ArrayList<HuffmanChar>();
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
//        args[0] = "src/compression/11.txt";
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
                for(int cnt = 0;cnt < sentence.length();cnt++)
                {
                    char key = sentence.charAt(cnt);
                    if(mapCharCount.containsKey(key))
                    {
                        int value = (int) mapCharCount.get(key);
                        value += 1;
                        mapCharCount.put(key,value);
                    }
                    else
                    {
                        mapCharCount.put(key,1);
                    }
                }
            }
            readChar = null;
        }
        catch(FileNotFoundException e)
        {
            System.exit(0);
        }
        catch(IOException e)
        {
            
        }
        addCharAndCount();
        addHuffArray(sortedMap);
        readTree();
        createCode(huffTree.first());
        values = readSetFile(fileName);
        writeEncodedFile(values, fileName);
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
     * @param values
     * @param fileName file input
     */ 
    public void writeEncodedFile(ArrayList<String> values, String fileName)          
    {
        writeKeyFile(fileName);
        try
        {
            PrintWriter write = new PrintWriter(new FileOutputStream(
                        file)); 
            for (String value : values) 
                write.print(value);
            write.close();
            
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        writeSecondFile(fileName);  
    }
    private ArrayList readSetFile(String fileName)
    {
        ArrayList<String> values = new ArrayList();
        String line = "";
        try
        {
            BufferedReader buff = new BufferedReader(new FileReader(fileName));
            while((line = buff.readLine()) != null)
            {
                for(int i = 0;i < line.length();i++)
                {
                    char key = line.charAt(i);
                    if (huffEncodeTable.containsKey(key))
                        values.add(huffEncodeTable.get(line.charAt(i)));                   
                } 
            }
            
        }
        catch(FileNotFoundException ex)
        {
            
        } catch (IOException ex) 
        {
            ex.printStackTrace();
        }
        return values;
    }
    private void readTree()
    {
        Hashtable <Character,Integer>frequencyData = 
                new Hashtable<Character,Integer>();
        frequencyData.putAll(sortedMap);
        Enumeration <Character>enumerator = frequencyData.keys();
        while(enumerator.hasMoreElements())
        {
            Character nextKey = enumerator.nextElement();
            huffTree.add(new HuffLeaf(nextKey,frequencyData.get(nextKey)));
        }
        while (huffTree.size() > 1)
        {
            theTree.add(huffTree);          
        }
    }
    /**
     * 
     * @param huffTree 
     */
    private void createCode(HuffTree huffTree)
    {
        if(huffTree instanceof HuffNode)
        {
           HuffNode node = (HuffNode)huffTree;
           HuffTree left = node.getLeft();
           HuffTree right = node.getRight();
           code.append("0");
           createCode(left);
           code.deleteCharAt(code.length() - 1);
           code.append("1");
           createCode(right);
           code.deleteCharAt(code.length() - 1);
        }
        else
        {
            HuffLeaf leaf = (HuffLeaf)huffTree;
            huffEncodeTable.put((char)(leaf.getValue()),code.toString());
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
     * writeSecondFile
     * @param fileName the name of the file to write to
     */
    public void writeSecondFile(String fileName)
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
        file += ".cod";
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
               byteArray[i] ++; 
              
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

//        for(int i = 0; i < byteArray.length; i++)
//        {
//            byte element = byteArray[i];
//            if (element > 0)
//            {
//                char key = (char)i;
//                int count = element;
//                mapCharCount.put(key,count);
//                le++;
//            }
//        }
//        byteArray = null;
        sortedMap.putAll(sortMap(mapCharCount));
//        mapCharCount.clear();
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
            public int compare(final Map.Entry<Character, Integer> element1,
                   final Map.Entry<Character, Integer> element2)
            {
                return element1.getValue().compareTo(element2.getValue());
            }
        }); 
        //add sorted list to new map
        for(Map.Entry<Character, Integer> entry : sortedList)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        } 
        return sortedMap;
    }  
    /**
     * adds the map keys and values to the arrayList
     * @param map the sorted map
     */
    private void addHuffArray(Map map)
    {
        Iterator iterateKey = map.keySet().iterator();
        Iterator iterateValues = map.values().iterator();
        while(iterateKey.hasNext() && iterateValues.hasNext())
        {
            char key = (char) iterateKey.next();
            int value = (int) iterateValues.next();
            charCount = new HuffmanChar(key, value);
            charCountArray.add(charCount);
        }
//        map.clear();       
    }
}
