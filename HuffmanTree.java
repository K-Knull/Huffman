
/*  Student information for assignment:
 *
 *  On MY honor, Diego Ortiz,
 *  this programming assignment is MY own work
 *  and I have not provided this code to any other student.
 *
 *  Number of slip days used: 2
 *
 *  Student 1: Diego Ortiz
 *  UTEID: dso463
 *  email address: diegosebortiz@gmail.com
 * 
 *  Grader name: Sam
 *  Section number: 54621
 */
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Models a Huffman Tree for compression and decompression.
 * Handles tree construction from counts or streams, code generation,
 * and tree serialization.
 */
public class HuffmanTree {
    private TreeNode root;

    /**
     * Constructs a Huffman Tree from frequency counts.
     * Used for compression or decompression with STORE_COUNTS.
     * 
     * @param counts array of frequencies where index is the character value
     */
    public HuffmanTree(int[] counts) {
        FairPriorityQueue<TreeNode> queue = new FairPriorityQueue<>();
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] > 0) {
                queue.enqueue(new TreeNode(i, counts[i]));
            }
        }

        while (queue.size() > 1) {
            TreeNode left = queue.dequeue();
            TreeNode right = queue.dequeue();
            queue.enqueue(new TreeNode(left, -1, right));
        }
        this.root = queue.dequeue();
    }

    /**
     * Constructs a Huffman Tree by reading from a BitInputStream.
     * Used for decompression with STORE_TREE.
     * 
     * @param bitsIn the stream to read the tree from
     */
    public HuffmanTree(BitInputStream bitsIn) throws IOException {
        this.root = readTree(bitsIn);
    }

    /**
     * Helper to read tree recursively.
     */
    private TreeNode readTree(BitInputStream bitsIn) throws IOException {
        int bit = bitsIn.readBits(1);
        if (bit == 1) {
            int val = bitsIn.readBits(IHuffConstants.BITS_PER_WORD + 1);
            return new TreeNode(val, 1); // Frequency doesn't matter for decompression
        } else {
            TreeNode left = readTree(bitsIn);
            TreeNode right = readTree(bitsIn);
            return new TreeNode(left, -1, right);
        }
    }

    /**
     * Writes the tree to the output stream in standard tree format.
     */
    public void writeTree(BitOutputStream bitsOut) throws IOException {
        writeTree(bitsOut, root);
    }

    private void writeTree(BitOutputStream bitsOut, TreeNode node) throws IOException {
        if (node.isLeaf()) {
            bitsOut.writeBits(1, 1);
            bitsOut.writeBits(IHuffConstants.BITS_PER_WORD + 1, node.getValue());
        } else {
            bitsOut.writeBits(1, 0);
            writeTree(bitsOut, node.getLeft());
            writeTree(bitsOut, node.getRight());
        }
    }

    /**
     * Generates the map of characters to Huffman codes.
     */
    public Map<Integer, String> getCodes() {
        Map<Integer, String> map = new HashMap<>();
        recursiveSearch(root, "", map);
        return map;
    }

    private void recursiveSearch(TreeNode node, String code, Map<Integer, String> map) {
        if (node.isLeaf()) {
            map.put(node.getValue(), code);
        } else {
            recursiveSearch(node.getLeft(), code + "0", map);
            recursiveSearch(node.getRight(), code + "1", map);
        }
    }

    /**
     * Returns the size of the tree in bits (for STORE_TREE header calculation).
     */
    public int size() {
        return getTreeSize(root);
    }

    private int getTreeSize(TreeNode node) {
        if (node.isLeaf()) {
            return 1 + (IHuffConstants.BITS_PER_WORD + 1);
        }
        return 1 + getTreeSize(node.getLeft()) + getTreeSize(node.getRight());
    }

    /**
     * Returns the root of the tree (useful for traversal during decompression).
     */
    public TreeNode getRoot() {
        return root;
    }
}