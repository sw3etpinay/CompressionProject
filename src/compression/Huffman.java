/*
 * Huffman.java
 *
 * Created on May 21, 2007, 1:01 PM
 */

package compression;


import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.Map.*;
import java.util.logging.*;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;

/**
 *
 * @author Jin Chang & pbladek
 */
public class Huffman
{  
    public static final int CHARMAX = 128;
    public static final byte CHARBITS = 7;
    public static final short CHARBITMAX = 128;
    public static final int STRINGMAX = 1000;
    private HuffmanTree theTree = new HuffmanTree() ;
    private HuffmanChar charCount;
    private HuffmanData data;
    private byte[] byteArray;
    private String[] codeArray;
    private String hufFile;
    private String codFile;
    private String txtFile;
    private String compression;
    private Map mapCharCount;
    private Map sortedMap;
    String StringdataEncoded;
    String textEntered;
    private SortedMap<Character, String> keyMap;
    private SortedMap<String, Character> codeMap;
    TreeSet <HuffTree> huffTree = new TreeSet<>();
    StringBuffer code = new StringBuffer();
    Hashtable <String,Byte>encodingBitMap;
    ArrayList<String> values = new ArrayList();
    ArrayList<String> sentences = new ArrayList();
    
    ArrayList<Byte> dataEncoded =  new ArrayList();
    ArrayList<HuffmanData> charCounter =  new ArrayList();
    String dataCode = "";
    char[] readChar; 
    byte[] saveDataArray;
    HuffmanData[] charCountArray;
    DecimalFormat decimalFormatter;
    
    /**
     * Creates a new instance of Main
     */
    public Huffman() 
    {
        this.encodingBitMap = new Hashtable<>();
        codeArray = new String[STRINGMAX];
        byteArray = new byte[CHARBITMAX];
        mapCharCount = new HashMap<>();
        sortedMap = new LinkedHashMap<>();
        decimalFormatter = new DecimalFormat("#.00");
    }
    
    /**
     * main
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException
    {     
        boolean decode = false;
        String textFileName = "";
        Huffman coder = new Huffman();
        if(args.length == 0)
        {
            textFileName = JOptionPane.showInputDialog("Please "
                                + "enter the file you wish to compress: ");
            if(!coder.isFileFound(textFileName))
            {
                textFileName = JOptionPane.showInputDialog("Please "
                                + "enter the file you wish to compress: ");
            }
        }
        else
        {
            if(args[0].substring(0,2).toLowerCase().equals("-d"))
            {
                decode = true;
                if(args.length > 1)
                {   
                    textFileName = args[1];
                    if(!coder.isFileFound(textFileName))
                    {
                       textFileName = JOptionPane.showInputDialog("Please "
                                + "enter the file you wish to decompress: ");
                    }
                }
                else
                {
                    textFileName = JOptionPane.showInputDialog("Please enter "
                            + "the file you wish to decompress: ");
                    if(!coder.isFileFound(textFileName))
                    {
                       textFileName = JOptionPane.showInputDialog("Please "
                                + "enter the file you wish to decompress: ");
                    }
                }
            }
            else
            {
                textFileName = args[0];
                if(!coder.isFileFound(textFileName))
                {
                   textFileName = JOptionPane.showInputDialog("Please "
                            + "enter the file you wish to decompress: ");
                }
            }
        }
        if(decode)
            coder.decode(textFileName);
        else
            coder.encode(textFileName);
        String text = "";
        try
        {
            BufferedReader readMe = new BufferedReader
                    (new FileReader(coder.codFile));
            while((text = readMe.readLine()) != null)
            {
                coder.sentences.add(text + "\n");
            }  
        }
        catch(FileNotFoundException e)
        {
            System.exit(0);
        }
        System.out.println(coder.sentences);   
        JOptionPane.showMessageDialog(null,
                     coder.hufFile + ":" + coder.compression + "% compression", 
                     "File Information", INFORMATION_MESSAGE, null);
    } 
    /*
     * encode
     * @param fileName the hufFile to encode
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
                textEntered+= sentence;
                for(int i = 0;i < sentence.length();i++)
                {
                    char key = sentence.charAt(i);
                    if(mapCharCount.containsKey(key))
                    {
                        int value = (int) mapCharCount.get(key);
                        value += 1;
                        mapCharCount.put(key,value);
                    }
                    else
                        mapCharCount.put(key,1);                   
                }
            }
            readMe.close();
        }
        catch(FileNotFoundException e)
        {
            System.exit(0);
        }
        catch(IOException e)
        {   
        }
        addCharAndCount();
        theTree = new HuffmanTree(sortedMap);
        stringEncoded();
        buildEncodingBitMap();
        encodeString();
        values = readSetFile(fileName);
        writeEncodedFile(values, fileName);
        compression = decimalFormatter.format(((double)dataEncoded.size()
                /textEntered.length()) * 100);
    } 
 /*
     * decode
     * @param inFileName the hufFile to decode
     */   
    public void decode(String inFileName)
    { 
        FileInputStream fileIn =  null;
        reRead("src/compression/12.txt");
        HashMap<Character, String> charMap = new HashMap<Character, String>();
        charMap.putAll(theTree.huffEncode);
        String tmp = "";
        String output = ""; 
        String code = "";
        try
        {
            String decodedText = new String(Files.readAllBytes(Paths.get(inFileName)));
            int max = decodedText.getBytes().length;
            ByteBuffer bit = ByteBuffer.wrap(decodedText.getBytes());
            int maxChars = bit.getInt(); 
            for (int i = 4; i < max; i++)
            {
                byte b = decodedText.getBytes()[i];
                String s = ("00000000" + 
                        Integer.toBinaryString(0xFF & b)).replaceAll(".*(.{8})$", "$1");
			tmp += s.substring(1);                                    
            }
            for (int i = 0; i < maxChars; i++)
            {
                char c = tmp.charAt(i);
                code += String.valueOf(c);
                if (charMap.containsValue(code))
                {
                    output += getValue(charMap, code);
                    code = "";
                }                
            }           
        }
        catch(FileNotFoundException ex)
        {
            
        } catch (IOException ex)
        {
            Logger.getLogger(Huffman.class.getName()).log(Level.SEVERE, null, ex);
        }
        try
        {
        PrintWriter print = new PrintWriter(new FileOutputStream(
                        txtFile));
         for (int i = 0; i <output.length(); i +=50)
        {
            if ((i +50) < output.length())
                print.println(output.substring(i, i + 50));
            else 
                print.println(output.substring(i));
        }
        print.close();
        }
        catch(FileNotFoundException ex)
        {
            ex.printStackTrace();
        }
    }
    private Character getValue(HashMap<Character, String> map, String code)
    {
        for (Entry<Character, String> entry : map.entrySet()) 
        {
           if (Objects.equals(code, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
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
                        hufFile)); 
            for (int i = 0; i <StringdataEncoded.length(); i +=50)
            {
                if ((i +50) < StringdataEncoded.length())
                    write.println(StringdataEncoded.substring(i, i + 50));
                else 
                    write.println(StringdataEncoded.substring(i));
            }
            write.close();
            
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }  
        try
        {
            FileOutputStream fileOut = new FileOutputStream(codFile);
            int maxVal = StringdataEncoded.length();
            int chars = 0;
            int remain = 0;
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.putInt(maxVal);
            fileOut.write(bb.array());

            while(chars <= maxVal - 7) {
                    String subString = StringdataEncoded.
                            substring(chars, chars + 7);
                    byte b = Byte.parseByte(subString, 2);
                    chars += 7;
                    fileOut.write(b);
            }

            remain = maxVal - chars;
            String subString = StringdataEncoded.
                    substring(chars, chars + remain);
            StringBuffer buffer = new StringBuffer(7);
            buffer.append(subString);
            int rest = 7 - remain;
            for (int i = 0; i < rest; ++i) {
                    buffer.append("0");
            }
            byte b = Byte.parseByte(buffer.toString(), 2);
            fileOut.write(b);
            fileOut.close();
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        } 
        catch (IOException ex) 
        {
            ex.printStackTrace();
        }
    }
    private void reRead(String fileName)
    {
        String decodedText = "";
        try {
            decodedText = new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (IOException ex) {
            Logger.getLogger(Huffman.class.getName()).log(Level.SEVERE, null, ex);
        }
        for(int i = 0;i < decodedText.length();i++)
        {
            char key = decodedText.charAt(i);
            if(mapCharCount.containsKey(key))
            {
                int value = (int) mapCharCount.get(key);
                value += 1;
                mapCharCount.put(key,value);
            }
            else
                mapCharCount.put(key,1);                   
        }
        addCharAndCount();
        theTree = new HuffmanTree(sortedMap);
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
                    if (theTree.huffEncode.containsKey(key))
                        values.add((String) theTree.huffEncode.
                                get(line.charAt(i)));   
                    else
                        return null;
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
    private   void buildEncodingBitMap(){

    for(int i = 0; i <= 255;i++){
      StringBuffer encodeBit = new StringBuffer();
      if((i & 128) > 0){encodeBit.append("1");
        }else{encodeBit.append("0");};
      if((i & 64) > 0){encodeBit.append("1");
        }else {encodeBit.append("0");};
      if((i & 32) > 0){encodeBit.append("1");
        }else {encodeBit.append("0");};
      if((i & 16) > 0){encodeBit.append("1");
        }else {encodeBit.append("0");};
      if((i & 8) > 0){encodeBit.append("1");
        }else {encodeBit.append("0");};
      if((i & 4) > 0){encodeBit.append("1");
        }else {encodeBit.append("0");};
      if((i & 2) > 0){encodeBit.append("1");
        }else {encodeBit.append("0");};
      if((i & 1) > 0){encodeBit.append("1");
        }else {encodeBit.append("0");};
      encodingBitMap.put(encodeBit.toString(), (byte)(i));
    }
  }
    private void stringEncoded()
    {
        StringBuffer tempEncoding = new StringBuffer();
        for(int i = 0;i < textEntered.length();i++)
        {
            tempEncoding.append(theTree.huffEncode.get(textEntered.charAt(i)));
        }
        StringdataEncoded  = tempEncoding.toString();
    }
    private void encodeString()
    {
        int temp = StringdataEncoded.length() % 8;
        for (int i = 0; i < (8-temp); i++)
        {
            StringdataEncoded += "0";
        }
        for (int i = 0; i < StringdataEncoded.length(); i += 8)
        {
            String bit = StringdataEncoded.substring(i, i +8);
            byte btyeChar = encodingBitMap.get(bit);
            dataEncoded.add(btyeChar);
        }
    }
    
    
    /**
     * writeKeyFile
     * @param fileName the name of the hufFile and codFile with the extension 
     * .huf and .cod to write to
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
        // .huf file
        int extensionIndex = editFileName.lastIndexOf(".");
        hufFile = editFileName.substring(0, extensionIndex);
        hufFile += ".huf";
        // .cod file
        codFile = editFileName.substring(0, extensionIndex);
        codFile += ".cod";
        // .txt file
        txtFile = editFileName.substring(0, extensionIndex);
        txtFile += ".txt"; 
    }
    /**
     * count the number characters
     * @param character the character
     */
    private void countChars(byte character)
    {
        if(byteArray.length > 0)
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
    }
    /**
     * recreates a new sorted map.
     */
    private void addCharAndCount()
    {
        for(int i = 0; i < byteArray.length; i++)
        {
            byte element = byteArray[i];
            if(Math.abs(element) > 0)
            {
                char key = (char)i;
                int count = Math.abs(element);
                mapCharCount.put(key,count);
            }
        }
        sortedMap.putAll(sortMap(mapCharCount));
        mapCharCount.clear();
        
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
        for(Map.Entry<Character, Integer> entry : sortedList)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        } 
        return sortedMap;
    }  
    /**
     * finds whether the file exists or can be read
     * @param file the file name
     * @return true if the file is found or false if not found
     */
    private boolean isFileFound(String file)
    {
        boolean found = false;
        File fileIn = new File(file);
        if(fileIn.canRead() && fileIn.exists())
        {
            found = true;
        }
        return found;
    }
}
