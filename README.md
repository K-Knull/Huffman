# Huffman
A Java-based file compression and decompression tool implementing the Huffman coding algorithm. Features a text-based user interface, multiple header formats, and bit-level I/O handling.

About The Project -

This project is a lossless data compression tool written in Java that implements the Huffman coding algorithm. It allows users to compress files to save space and decompress them back to their original state. The system uses a priority queue to construct optimal prefix codes based on character frequencies, ensuring efficient storage for data with redundant patterns.​

The application includes a text-based user interface (TUI) for easy interaction and supports different header storage formats for the compressed files.​

- Features -

  - Lossless Compression: 
  
      1. Reduces file size without losing any data by assigning shorter binary codes to more frequent characters.
  ​
  - Dual Header Support:

      1. STORE_COUNTS: Stores character frequency counts in the header.
    ​
      2. STORE_TREE: Serializes the Huffman tree structure directly into the header.​
    
  - Interactive Interface: 
  
      1. A console-based menu system (TextHuffViewer) allows users to select files, toggle "force compression," and switch header formats.​
  
  - Safety Checks:
  
      1. Includes a "Magic Number" validation to ensure files being decompressed are in the correct format.
  ​
  - Efficiency Analysis:
  
      1. The "Preprocess" feature calculates potential space savings before committing to compression.​

- How It Works - 

  - Compression
  
    - Frequency Analysis:
    
        1. The program reads the input file and counts the occurrences of every character.​
    
    - Tree Construction: 
    
        1. A FairPriorityQueue is used to build a Huffman Tree, where more frequent characters are placed near the root (shorter paths).
    ​
    - Code Generation:
    
        1. The tree is traversed to generate unique binary codes for each character.
    ​
    - Writing Output: 
    
        1. The program writes a header (counts or tree) followed by the encoded data bits using BitOutputStream.
​
  - Decompression

    - Header Parsing: 
    
      1. The program reads the header to reconstruct the exact Huffman Tree used during compression.
    ​
    - Decoding:
    
        1. It reads the compressed data bit-by-bit using BitInputStream, traversing the reconstructed tree until it reaches a leaf node (a character).​
    
    - Output: 
    
        1. The found character is written to the output file, and the process repeats until the file ends.
​
- File Structure -

  - Core Logic
  
    - SimpleHuffProcessor.java:
    
        1. The main engine that handles the compression and decompression workflows.​
    
    - HuffmanTree.java: 
    
        1. Manages tree nodes, code generation, and tree serialization/deserialization.
    ​
    - FairPriorityQueue.java: 
      
        1. A custom priority queue is used to build the Huffman tree efficiently.
​
  - I/O Utilities
  
    - BitInputStream.java / BitOutputStream.java: 
    
        1. Handles reading and writing data at the bit level.​

  - User Interface
    
    - TextHuffViewer.java: 
    
        1. Provides the command-line interface for the user.
    
    - IHuffViewer.java: 
    
        1. Interface defining the required methods for any viewer attached to the processor.​

  - Interfaces & Constants
    
    - IHuffProcessor.java: 
    
        1. Defines the standard operations for the compression model.​
    
    - IHuffConstants.java: 
    
        1. Stores global constants like MAGIC_NUMBER and bit depths.
​
Usage
 - Compile all Java files.
 - Run the main application (likely via a driver class that initializes TextHuffViewer).
 - Use the menu options to:
   
    1: Select a file to process (Compress/Uncompress).
   
    2: Toggle "Force Compression" (create output file even if it's larger).
   
    3: Switch between STORE_COUNTS and STORE_TREE header formats.

