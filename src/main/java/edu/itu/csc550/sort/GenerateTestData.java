
package edu.itu.csc550.sort;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.Random;
import java.util.zip.Checksum;

import org.apache.hadoop.util.PureJavaCrc32;


public class GenerateTestData {

  /**
   * Generate a "binary" record suitable for all sort benchmarks *except* 
   * PennySort.
   */
  static void generateRecord(byte[] recBuf, CustomUnsigned rand,
                                     CustomUnsigned recordNumber) {
    /* generate the 10-byte key using the high 10 bytes of the 128-bit
     * random number
     */
    for(int i=0; i < 10; ++i) {
      recBuf[i] = rand.getByte(i);
    }

    /* add 2 bytes of "break" */
    recBuf[10] = 0x00;
    recBuf[11] = 0x11;

    /* convert the 128-bit record number to 32 bits of ascii hexadecimal
     * as the next 32 bytes of the record.
     */
    for (int i = 0; i < 32; i++) {
      recBuf[12 + i] = (byte) recordNumber.getHexDigit(i);
    }

    /* add 4 bytes of "break" data */
    recBuf[44] = (byte) 0x88;
    recBuf[45] = (byte) 0x99;
    recBuf[46] = (byte) 0xAA;
    recBuf[47] = (byte) 0xBB;

    /* add 48 bytes of filler based on low 48 bits of random number */
    for(int i=0; i < 12; ++i) {
      recBuf[48+i*4] = recBuf[49+i*4] = recBuf[50+i*4] = recBuf[51+i*4] =
        (byte) rand.getHexDigit(20 + i);
    }

    /* add 4 bytes of "break" data */
    recBuf[96] = (byte) 0xCC;
    recBuf[97] = (byte) 0xDD;
    recBuf[98] = (byte) 0xEE;
    recBuf[99] = (byte) 0xFF;
  }


  private static BigInteger makeBigInteger(long x) {
    byte[] data = new byte[8];
    for(int i=0; i < 8; ++i) {
      data[i] = (byte) (x >>> (56 - 8*i));
    }
    return new BigInteger(1, data);
  }

  private static final BigInteger NINETY_FIVE = new BigInteger("95");

  /**
   * Generate an ascii record suitable for all sort benchmarks including 
   * PennySort.
   */
  static String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  static void generateAsciiRecord(byte[] recBuf, CustomUnsigned rand,
                                  CustomUnsigned recordNumber) {
    Random random = new Random();
    for(int i =0; i<10;i++){
      recBuf[i] =(byte)characters.charAt(random.nextInt(62));

    }


    /* add 2 bytes of "break" */
    recBuf[10] = ' ';
    recBuf[11] = ' ';

    /* convert the 128-bit record number to 32 bits of ascii hexadecimal
     * as the next 32 bytes of the record.
     */
    for (int i = 0; i < 32; i++) {
      recBuf[12 + i] = (byte) recordNumber.getHexDigit(i);
    }

    /* add 2 bytes of "break" data */
    recBuf[44] = ' ';
    recBuf[45] = ' ';

    /* add 52 bytes of filler based on low 48 bits of random number */
    for(int i=0; i < 13; ++i) {
      recBuf[46+i*4] = recBuf[47+i*4] = recBuf[48+i*4] = recBuf[49+i*4] =
        (byte) rand.getHexDigit(19 + i);
    }

    /* add 2 bytes of "break" data */
    recBuf[98] = '\r';	/* nice for Windows */
    recBuf[99] = '\n';
}


  private static void usage() {
    PrintStream out = System.out;
    out.println("usage: gensort [-a] [-c] [-bSTARTING_REC_NUM] NUM_RECS FILE_NAME");
    out.println("-a        Generate ascii records required for PennySort or JouleSort.");
    out.println("          These records are also an alternative input for the other");
    out.println("          sort benchmarks.  Without this flag, binary records will be");
    out.println("          generated that contain the highest density of randomness in");
    out.println("          the 10-byte key.");
    out.println( "-c        Calculate the sum of the crc32 checksums of each of the");
    out.println("          generated records and send it to standard error.");
    out.println("-bN       Set the beginning record generated to N. By default the");
    out.println("          first record generated is record 0.");
    out.println("NUM_RECS  The number of sequential records to generate.");
    out.println("FILE_NAME The name of the file to write the records to.\n");
    out.println("Example 1 - to generate 1000000 ascii records starting at record 0 to");
    out.println("the file named \"pennyinput\":");
    out.println("    gensort -a 1000000 pennyinput\n");
    out.println("Example 2 - to generate 1000 binary records beginning with record 2000");
    out.println("to the file named \"partition2\":");
    out.println("    gensort -b2000 1000 partition2");
    System.exit(1);
  }


  public static void outputRecords(OutputStream out,
                                   boolean useAscii,
                                   CustomUnsigned firstRecordNumber,
                                   CustomUnsigned recordsToGenerate,
                                   CustomUnsigned checksum
                                   ) throws IOException {
    byte[] row = new byte[100];
    CustomUnsigned recordNumber = new CustomUnsigned(firstRecordNumber);
    CustomUnsigned lastRecordNumber = new CustomUnsigned(firstRecordNumber);
    Checksum crc = new PureJavaCrc32();
    CustomUnsigned tmp = new CustomUnsigned();
    lastRecordNumber.add(recordsToGenerate);
    CustomUnsigned ONE = new CustomUnsigned(1);
    CustomUnsigned rand = CustomRandom.skipAhead(firstRecordNumber);
    while (!recordNumber.equals(lastRecordNumber)) {
      CustomRandom.nextRand(rand);
      if (useAscii) {
        generateAsciiRecord(row, rand, recordNumber);
      } else {
        generateRecord(row, rand, recordNumber);
      }
      if (checksum != null) {
        crc.reset();
        crc.update(row, 0, row.length);
        tmp.set(crc.getValue());
        checksum.add(tmp);
      }
      recordNumber.add(ONE);
      out.write(row);
    }
  }
                                   
  public static void main(String[] args) throws Exception {
    CustomUnsigned startingRecord = new CustomUnsigned();
    CustomUnsigned numberOfRecords;
    OutputStream out;
    boolean useAscii = false;
    CustomUnsigned checksum = null;

    int i;
    for(i=0; i < args.length; ++i) {
      String arg = args[i];
      int argLength = arg.length();
      if (argLength >= 1 && arg.charAt(0) == '-') {
        if (argLength < 2) {
          usage();
        }
        switch (arg.charAt(1)) {
        case 'a':
          useAscii = true;
          break;
        case 'b':
          startingRecord = CustomUnsigned.fromDecimal(arg.substring(2));
          break;
        case 'c':
          checksum = new CustomUnsigned();
          break;
        default:
          usage();
        }
      } else {
        break;
      }
    }
    if (args.length - i != 2) {
      usage();
    }
    numberOfRecords = CustomUnsigned.fromDecimal(args[i]);
    out = new FileOutputStream(args[i+1]);

    outputRecords(out, useAscii, startingRecord, numberOfRecords, checksum);
    out.close();
    if (checksum != null) {
      System.out.println(checksum);
    }
  }

}
