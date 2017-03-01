import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class EncryptionDetector {
  /*
   * In order to detect encryption, we will determine if the values are randomized.
   * As encryption of a file would cause the data to be randomized. There are 2
   * main methods to detect encryption.
   * 
   * 
   * Chi Square Distribution 
   * -------------------------------------------------------------------
   * To determine the deviation of observed result from expected results. 
   * In order to detect randomness, we use Chi Square Distribution
   * to determine if there is a lack of randomness
   * 
   * Monte Carlo Pi 
   * -------------------------------------------------------------------
   * To approximate the value of pi from given set of random (x,y) coord.
   * The more uniquely well distributed data points, the closer the approximation
   * to the actual value of pi. To say that a set of data is random, is where
   * the pi approximation is very close.
   * 
   * 
   * 
   */
  private long byteCount[] = new long[256]; 
  private long totalc = 0;
  private double chisq = 0;
  private int MONTEN = 6; //Bytes used for Monte Carlo coords
  private long monte[] = new long[MONTEN];
  private int monteCounter = 0;
  private double mcount, montex, montey, incircle, inmonte, montepi;
  private File filename;



  public static void main(String [] args) throws IOException {
    if (args.length>=1){
      for (int i = 0; i < args.length; i++){
        EncryptionDetector ed = new EncryptionDetector(new File(args[i]));
        ed.printEncDetectStats();   
      }
    }else{
      System.out.println("java EncryptionDetector FILE [FILE...]");
      System.exit(0);
    }





  }

  private void printEncDetectStats() {
    double chisq = getChiSquare();
    double monteCarlo = getMonteCarlo();
    double monteCarloErrRate = getMonteCarloErrRate();

    System.out.println("File: " + filename);
    System.out.println("Chi-Square Distribution: " + chisq);
    System.out.println("Monte Carlo Pi Value: " + monteCarlo);
    System.out.println("Monte Carlo Pi Error Rate: " + monteCarloErrRate);
    System.out.println("The file has a "+ getEncryptDetect() + "% confidence of encryption.");
  }

  /*
   * Returns the confidence value of encryption of the file
   * 
   * Chi Square value to reference = 300, as  256 values and that translates 
   * into 255 degrees of freedom. Next, if we select p=0.05, we want to determine that
   * random has a 95% of certainty, then referencing from chisquare tables, critical value 
   * would be 293.24, rounded to 300. 
   * 
   * Values used in this method are based of articles online:
   * http://www.devttys0.com/2013/06/differentiate-encryption-from-compression-using-math/
   * http://cs.smith.edu/dftwiki/images/b/b9/MonteCarloBookChapter.pdf
   * 
   */
  private double getEncryptDetect() {
    double percent = 0;
    double monteErrRate = getMonteCarloErrRate();
    //Very accurate pi calculations (< .01% error) are sure signs of encryption.
    if ( monteErrRate  < 0.01 ){
      percent += 95;
    }
    //Higher chi values (> 300) with lower pi errors (< .03%) are indicative of encryption.
    if ( chisq > 300 && monteErrRate < 0.03){
      percent += 75;
    }
    //Lower chi values (< 300) with higher pi error (> .03%) are indicative of compression.
    if ( chisq < 300 && monteErrRate > 0.03){
      percent += 60;
    }
    //Large deviations in the chi square distribution, or large percentages of error in the Monte Carlo approximation are sure signs of compression.
    if ( chisq > 2000 && monteErrRate > 20){
      percent = 0;
    }

    if(percent>=100){
      percent = 99;
    }
    return percent;

  }

  /*
   * Returns the Monte Carlo Error Rate of the file
   */
  private double getMonteCarloErrRate() {
    return 100.0 * (Math.abs(Math.PI - montepi) / Math.PI);
  }


  /*
   * Returns the Monte Carlo value of the file
   */
  private double getMonteCarlo() {
    return montepi = 4.0 * (((double) inmonte) / mcount);
  }

  /*
   * Print the size of the counts of all possible bytes, from 0 - 255 inclusive
   */
  private void printBytesBin() {
    for (int i = 0; i < 256; i++) {
      System.out.println(byteCount[i]);
    }
  }

  /*
   * Returns the Chi Square value of the file
   */
  public double getChiSquare()
  {
    double expecti = (double)totalc/byteCount.length;
    double t = 0;
    for (int i = 0; i < byteCount.length; i++)
    {
      double diff = byteCount[i] - expecti;
      t += (diff * diff)/expecti;
    }
    return chisq = t;
  }

  /*
   * Sets up the variables to allow calculation of chisquare and montecarlo pi values.
   */
  public EncryptionDetector(File file) throws IOException {
    inmonte = mcount = 0;
    incircle = Math.pow(Math.pow(256.0, (MONTEN / 2d)) -1, 2.0);

    for (int i = 0; i < 256; i++) {
      byteCount[i] = 0;
    }

    filename = file;
    //Read and Process file.
    DataInputStream fin = new DataInputStream( new FileInputStream(file));
    try {
      while ( true ) {
        int oc = fin.readUnsignedByte();

        //Setup counts for Chi-Square distribution
        totalc += 1;
        byteCount[oc]++;


        //Setup inside/oustide circle count for Monte Carlo PI
        monte[monteCounter++] = oc;       /* Save character for Monte Carlo */
        if (monteCounter >= MONTEN) {     /* Calculate every MONTEN character */
          int montej;

          monteCounter = 0;
          mcount++;
          montex = montey = 0;
          for (montej = 0; montej < MONTEN / 2; montej++) {
            montex = (montex * 256.0) + monte[montej];
            montey = (montey * 256.0) + monte[(MONTEN / 2) + montej];
          }
          if ((montex * montex + montey * montey) <= incircle) {
            inmonte++;
          }
        }


      }
    } catch( EOFException e ) {
      //End of File.
    } finally {
      fin.close();
    }
  }


}
