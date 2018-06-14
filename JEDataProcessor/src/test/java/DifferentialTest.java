
import org.junit.Test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author broder
 */
public class DifferentialTest {

    public static String DIFFERENTIAL_FOLDER = "testdata/differential";

    @Test
    public void shouldDiffTheValues() {
        String pathToInputFile = DIFFERENTIAL_FOLDER + "/diff_values_in.txt";
        String pathToCleanConfigFile = DIFFERENTIAL_FOLDER + "/diff_values_config.txt";
        String pathToRealOutput = DIFFERENTIAL_FOLDER + "/diff_values_out_real.txt";
        String pathToCorrectOutput = DIFFERENTIAL_FOLDER + "/diff_values_out_exp.txt";
        DataRowChecker checker = new DataRowChecker();
        checker.validate(pathToInputFile, pathToCleanConfigFile, pathToRealOutput, pathToCorrectOutput);
    }
}
