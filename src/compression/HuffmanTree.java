/*
 * HuffmanTree.java
 *
 * Created on May 21, 2007, 2:16 PM
 */

package compression;
import java.util.*;
import javafx.scene.Node;
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
     * @param dataArray n array of Huffman Data
     * ////////////////          in progress.
     */
   public HuffmanTree(HuffmanData<T>[] dataArray)
    {
        
//        keyMap = new TreeMap<String, T>();
//        codeMap = new TreeMap<T, String>();
//        setMaps(getRootNode(), "");
        List<HuffmanData<T>> dataList = new ArrayList<HuffmanData<T>>();
        for(HuffmanData<T> data : dataArray)
            dataList.add(data);
        Collections.sort(dataList);
        Stack<HuffmanData<T>> pq = new Stack<HuffmanData<T>>();
        
        while(!dataList.isEmpty())
        {
            HuffmanData tempData1 = dataList.get(0);
            dataList.remove(0);
            pq.add(tempData1);
            
            if(!dataList.isEmpty())
            {
                HuffmanData tempData2= dataList.get(0);
                dataList.remove(0);
                pq.add(tempData2);
                HuffmanData nodeData = new HuffmanData(null,tempData1.getOccurances() + tempData2.getOccurances());
                dataList.add(nodeData);
                Collections.sort(dataList);
                // System.out.println("tree: " + dataList);
            }
            //System.out.println("pq: " + pq);
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
}
