/*
 * HuffmanTree.java
 *
 * Created on May 21, 2007, 2:16 PM
 */

package compression;
import java.util.*;

/**
 * binary tree for Huffman coding
 * @author pbladek
 * @param <T>
 */
public class HuffmanTree<T extends Comparable<? super T>>
        extends BinaryTree<HuffmanData<T>>
{
    private T MARKER = null;
    SortedMap<T, String> codeMap;
    SortedMap<String, T> keyMap;
    TreeSet <HuffTree> huffTree = new TreeSet<>();
    StringBuffer code = new StringBuffer();
    Map <Character,Integer> frequencyData;
    Map <Character,String> huffEncode;
    Map <Character,Integer> sorted;
    int valueTemp = 0;
    private int leafCount = 0;
    
    /**
     * Creates a new instance of HuffmanTree
     */
    public HuffmanTree() 
    {
        super();
    }
   
    /**
     * Creates a new instance of HuffmanTree
     * from an array of Huffman Data
     * @param sortedMap
     */
    public HuffmanTree(Map sortedMap) 
    {
        frequencyData = new HashMap<>();
        huffEncode = new HashMap<>();
        sorted = new LinkedHashMap<>();
        readTree(sortedMap);
        createTree(huffTree.first());
//        codeMap = new TreeMap<>();
//        setMaps(getRootNode(), "");
    }
    private void readTree(Map map)
    {
        frequencyData.putAll(map);
        sorted.putAll(sortMap(frequencyData));
        Iterator <Character> iterate = (Iterator <Character>) 
                sorted.keySet().iterator();
        while(iterate.hasNext())
        {
            Character nextKey = iterate.next();
            huffTree.add(new HuffLeaf(nextKey,sorted.get(nextKey)));
        }
        while (huffTree.size() > 1)
        {
            add(huffTree);          
        }
    }
    /**
     * 
     * @param huffTree 
     */
    private void createTree(HuffTree huffTree)
    {
        if(huffTree instanceof HuffNode)
        {
           HuffNode node = (HuffNode)huffTree;
           HuffTree left = node.getLeft();
           HuffTree right = node.getRight();
           code.append("0");
           createTree(left);
           code.deleteCharAt(code.length() - 1);
           code.append("1");
           createTree(right);
           code.deleteCharAt(code.length() - 1);
        }
        else
        {
            HuffLeaf leaf = (HuffLeaf)huffTree;
            huffEncode.put((char)(leaf.getValue()),code.toString());
        }
        
    }
    /** 
     * creates two new HuffmanTrees and adds them to the root of this tree
     * @param huffTree
     */
    public void add(TreeSet <HuffTree> huffTree)
    {
        HuffTree left = huffTree.first();
        huffTree.remove(left);
        HuffTree right = huffTree.first();
        huffTree.remove(right);
        HuffNode tempNode = new HuffNode(left.getFrequency() 
                + right.getFrequency(),left,right);
        huffTree.add(tempNode);
    }
     /** 
      * set up the 2 maps
      * @param node
      * @param codeString
      */
     private void setMaps(BinaryNodeInterface<HuffmanData<T>> node,
             String codeString)
     { 
        if(node.hasLeftChild())
        {
            setMaps(node.getLeftChild(), codeString + "0");
        }
        if (node.hasRightChild())
        {
            setMaps(node.getRightChild(), codeString + "1");
        }
        if(node.getLeftChild() == null && node.getRightChild() == null)
        {
            codeMap.put(node.getData().getData(), codeString);
            keyMap.put(codeString, node.getData().getData());
        }
        
        
        //not sure if this is right
              
     }
    
    /*
     * accessor for codeMap
     * @ return codeMap
     */
    public SortedMap<T, String> getCodeMap()
    {
        return codeMap;
    }
    
    /*
     * accessor for keyMap
     * @ return keyMap
     */
    public SortedMap<String, T> getKeyMap()
    {
        return keyMap;
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
}
