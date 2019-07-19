

package edu.itu.csc550;


import edu.itu.csc550.sort.GenerateMain;
import edu.itu.csc550.sort.MySort;
import edu.itu.csc550.sort.SortValidate;
import org.apache.hadoop.util.ProgramDriver;


public class SortLoad {
  
  public static void main(String argv[]){
    System.out.println("CSC550 SA project--------------------- start");
    int exitCode = -1;
    ProgramDriver pgd = new ProgramDriver();
    try {

      pgd.addClass("gen", GenerateMain.class, "Generate test data for the sort");
      pgd.addClass("sort", MySort.class, "Run the sort");
      pgd.addClass("validate", SortValidate.class, "Checking results of sort");
      exitCode = pgd.run(argv);

    }
    catch(Throwable e){
      e.printStackTrace();
    }
    System.exit(exitCode);
  }
}
	
