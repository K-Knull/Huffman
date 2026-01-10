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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class SimpleHuffProcessor implements IHuffProcessor {

    private IHuffViewer myViewer;
    private int[] frequencies;
    private HuffmanTree huffTree;
    private Map<Integer, String> table;
    private int format;
    private int totalBits;
    private int compressedBits;

    public SimpleHuffProcessor(IHuffViewer viewer) {
        this.myViewer = viewer;
        this.frequencies = new int[ALPH_SIZE + 1];
        this.huffTree = null;
        this.table = new HashMap<>();
        this.format = 0;
        this.totalBits = 0;
        this.compressedBits = 0;
    }

    public SimpleHuffProcessor() {
        this(null);
    }

    @Override
    /**
     * Preprocess data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is <em>not</em> a BitInputStream, so wrap it int one as needed.
     * 
     * @param in           is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind
     *                     of header to use, standard count format, standard tree
     *                     format, or possibly some
     *                     format added in the future.
     * @return number of bits saved by compression or some other measure Note, to
     *         determine
     *         the number of bits saved, the number of bits written includes ALL
     *         bits that will be
     *         written including the magic number, the header format number, the
     *         header to reproduce the
     *         tree, AND the actual data.
     * @throws IOException if an error occurs while reading from the input file.
     */
    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
        this.format = headerFormat;
        getFrequencies(in); // Count character frequencies

        // Build the tree and generate Huffman codes
        this.huffTree = new HuffmanTree(this.frequencies);
        this.table = this.huffTree.getCodes();

        // Calculate potential savings
        int originalBits = getOriginalBits();
        this.compressedBits = calculateCompressedBits(headerFormat);
        this.totalBits = originalBits - this.compressedBits;

        // Display compression details to the viewer
        showInfo(originalBits);

        if (this.myViewer != null) {
            this.myViewer.showMessage("Total bits saved: " + totalBits);
        }
        
        return this.totalBits;
    }

    /**
     * Coordinates the display of frequencies, codes, and stats.
     */
    private void showInfo(int originalBits) {
        if (this.myViewer != null) {
            showFreq(originalBits);
            this.myViewer.update("");

            showCodeTable();
            this.myViewer.update("");

            showRunningTotals();
            myViewer.update("");
        }
    }

    /**
     * Displays character frequency counts.
     */
    private void showFreq(int originalBits) {
        this.myViewer.update("Frequencies of values in file:");
        for (int i = 0; i < this.frequencies.length; i++) {
            if (this.frequencies[i] > 0) {
                String charStr = (i == 32) ? " " : (char) i + "";
                if (i == PSEUDO_EOF) {
                    charStr = "?";
                }
                String binary = Integer.toBinaryString(i);
                if (i == 32) {
                    binary = "100000 ";
                }
                this.myViewer.update(i + " " + binary + " " + charStr + " " + this.frequencies[i]);
            }
        }
        this.myViewer.update("bits in original file: " + originalBits);
    }

    /**
     * Displays the generated Huffman codes.
     */
    private void showCodeTable() {
        this.myViewer.update("Codes for values in file: ");
        for (int i = 0; i < this.frequencies.length; i++) {
            if (this.frequencies[i] > 0) {
                String charStr = (i == 32) ? " " : (char) i + "";
                if (i == IHuffConstants.PSEUDO_EOF) {
                    charStr = "?";
                }
                this.myViewer.update(i + " " + charStr + " " + this.table.get(i));
            }
        }
    }

    /**
     * Displays running totals of bit usage.
     */
    private void showRunningTotals() {
        this.myViewer.update("Showing running total of bits for value: ");
        int runningTotal = 0;
        for (int i = 0; i < this.frequencies.length; i++) {
            if (this.frequencies[i] > 0) {
                String charStr = (i == 32) ? " " : (char) i + "";
                if (i == IHuffConstants.PSEUDO_EOF) {
                    charStr = "?";
                }
                int bits = this.frequencies[i] * this.table.get(i).length();
                runningTotal += bits;
                this.myViewer.update(charStr + " " + this.frequencies[i] + " " +
                        this.table.get(i).length() + " " + runningTotal);
            }
        }
        this.myViewer.update("bits in compressed file: " + this.compressedBits);
        double percent = (1.0 - ((double) this.compressedBits / getOriginalBits())) * 100.0;
        this.myViewer.update("Percent compression: " + percent);
    }

    /**
     * Helper method to calculate the number of bits in the original file
     * 
     * @return the number of bits in the original file
     */
    private int getOriginalBits() {
        int bits = 0;
        for (int i = 0; i < IHuffConstants.ALPH_SIZE; i++) {
            bits += frequencies[i] * IHuffConstants.BITS_PER_WORD;
        }
        return bits;
    }

    /**
     * Helper method to estimates the size of the compressed file including headers.
     * 
     * @param headerFormat the header format
     * @return the number of compressed bits
     */
    private int calculateCompressedBits(int headerFormat) {
        int compressedBits = 0;
        // Add bits for data
        for (int key : this.table.keySet()) {
            compressedBits += this.frequencies[key] * this.table.get(key).length();
        }

        compressedBits += 2 * BITS_PER_INT; // For MAGIC_NUMBER and header format

        // Add bits for header
        if (headerFormat == STORE_COUNTS) {
            compressedBits += ALPH_SIZE * BITS_PER_INT;
        } else if (headerFormat == STORE_TREE) {
            compressedBits += this.huffTree.size() + BITS_PER_INT;
        } else {
            if (this.myViewer != null) {
                this.myViewer.showError("Unknown header format: " + headerFormat);
                throw new IllegalArgumentException("Header format not supported: " + format);
            }
        }
        return compressedBits;
    }

    @Override
    /**
     * Compresses input to output, where the same InputStream has
     * previously been pre-processed via <code>preprocessCompress</code>
     * storing state used by this call.
     * <br>
     * pre: <code>preprocessCompress</code> must be called before this method
     * 
     * @param in    is the stream being compressed (NOT a BitInputStream)
     * @param out   is bound to a file/stream to which bits are written
     *              for the compressed file (not a BitOutputStream)
     * @param force if this is true create the output file even if it is larger than
     *              the input file.
     *              If this is false do not create the output file if it is larger
     *              than the input file.
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or
     *                     writing to the output file.
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
        // Check if compression is actually beneficial
        if (this.totalBits <= 0 && !force) {
            if (this.myViewer != null) {
                this.myViewer.showError("Compressed size larger. Use Force Compression.");
            }
            return 0;
        }

        // Write standard header
        BitOutputStream bitsOut = new BitOutputStream(out);
        bitsOut.writeBits(BITS_PER_INT, MAGIC_NUMBER);
        bitsOut.writeBits(BITS_PER_INT, this.format);

        // Write format-specific header (tree or counts)
        writeHeader(bitsOut);

        // Write compressed data body
        BitInputStream bitsIn = new BitInputStream(in);
        readAndWrite(bitsIn, bitsOut);

        // Close streams to flush data
        bitsIn.close();
        bitsOut.close();

        if (this.myViewer != null) {
            this.myViewer.showMessage("Compressed " + this.compressedBits + " bits.");
        }
        return this.compressedBits;
    }

    /**
     * Helper to write the specific header format (counts or tree).
     */
    private void writeHeader(BitOutputStream bitsOut) throws IOException {
        if (this.format == STORE_COUNTS) {
            for (int i = 0; i < ALPH_SIZE; i++) {
                bitsOut.writeBits(BITS_PER_INT, this.frequencies[i]);
            }
        } else if (this.format == STORE_TREE) {
            bitsOut.writeBits(BITS_PER_INT, this.huffTree.size());
            this.huffTree.writeTree(bitsOut);
        } else {
            if (this.myViewer != null) {
                this.myViewer.showError("Unknown header format: " + format);
                throw new IllegalArgumentException("Header format not supported: " + format);
            }

        }
    }

    @Override
    /**
     * Uncompress a previously compressed stream in, writing the
     * uncompressed bits/data to out.
     * 
     * @param in  is the previously compressed data (not a BitInputStream)
     * @param out is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file/stream
     * @throws IOException if an error occurs while reading from the input file or
     *                     writing to the output file.
     */
    public int uncompress(InputStream in, OutputStream out) throws IOException {
        BitInputStream bitsIn = new BitInputStream(in);

        // Verify magic number
        int magic = bitsIn.readBits(BITS_PER_INT);
        if (magic != MAGIC_NUMBER) {
            if (this.myViewer != null) {
                this.myViewer.showError("Invalid magic number: " + magic);
            }
            bitsIn.close();
            return -1;
        }

        // Read format and rebuild tree
        this.format = bitsIn.readBits(BITS_PER_INT);
        readHeader(bitsIn);

        // Decode the body using the tree
        int count = rewrite(bitsIn, out);
        bitsIn.close();
        return count;
    }

    /**
     * Helper to rebuild the Huffman tree from the header.
     */
    private void readHeader(BitInputStream bitsIn) throws IOException {
        if (this.format == STORE_COUNTS) {
            this.frequencies = new int[ALPH_SIZE + 1];
            for (int i = 0; i < ALPH_SIZE; i++) {
                this.frequencies[i] = bitsIn.readBits(BITS_PER_INT);
            }
            this.frequencies[PSEUDO_EOF] = 1;
            this.huffTree = new HuffmanTree(this.frequencies); // Build from counts
        } else if (this.format == STORE_TREE) {
            bitsIn.readBits(BITS_PER_INT); // Skip size bits
            this.huffTree = new HuffmanTree(bitsIn); // Build from tree stream
        } else {
            this.myViewer.showError("Unknown header format: " + this.format);
            throw new IllegalArgumentException("Unknown header format: " + format);
        }
    }

    /**
     * Helper to read input stream and count character frequencies.
     * 
     * @param in the input stream
     * @throws IOException if an error occurs while reading from the input file.
     */
    private void getFrequencies(InputStream in) throws IOException {
        BitInputStream bits = new BitInputStream(in);
        this.frequencies = new int[ALPH_SIZE + 1];
        int b;
        while ((b = bits.readBits(BITS_PER_WORD)) != -1) {
            this.frequencies[b]++;
        }
        this.frequencies[PSEUDO_EOF] = 1;
        bits.close();
    }

    /**
     * Reads the input file again and writes the compressed bits.
     */
    private void readAndWrite(BitInputStream bitsIn, BitOutputStream bitsOut) throws IOException {
        int b = bitsIn.readBits(BITS_PER_WORD);
        while (b != -1) {
            stringBits(this.table.get(b), bitsOut);
            b = bitsIn.readBits(BITS_PER_WORD);
        }
        stringBits(this.table.get(PSEUDO_EOF), bitsOut);
    }

    /**
     * Converts a string of '0's and '1's into actual bits.
     */
    private void stringBits(String code, BitOutputStream bitsOut) {
        for (char c : code.toCharArray()) {
            bitsOut.writeBits(1, c == '1' ? 1 : 0);
        }
    }

    /**
     * Helper method that reads compressed bits and writes the original characters.
     * 
     * @param bitsIn
     * @param out
     * @return
     * @throws IOException
     */
    private int rewrite(BitInputStream bitsIn, OutputStream out) throws IOException {
        BitOutputStream bitsOut = new BitOutputStream(out);
        TreeNode current = this.huffTree.getRoot();
        TreeNode rootNode = this.huffTree.getRoot();
        int count = 0;
        int bit = 0;

        while ((bit = bitsIn.readBits(1)) != -1) {
            current = (bit == 0) ? current.getLeft() : current.getRight();

            // Found a leaf node (character)
            if (current.isLeaf()) {
                if (current.getValue() == PSEUDO_EOF) {
                    bitsOut.close(); // Done processing
                    return count;
                }
                bitsOut.writeBits(BITS_PER_WORD, current.getValue());
                count += BITS_PER_WORD;
                current = rootNode; // Reset to top of tree
            }
        }
        bitsOut.close();
        return count;
    }

    @Override
    public void setViewer(IHuffViewer viewer) {
        this.myViewer = viewer;
    }
}