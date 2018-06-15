
import org.junit.Test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author broder
 */
public class GapFillingTest {

    public static String GAP_FILLING_FOLDER = "testdata/gap_filling";

    @Test
    public void shouldKeepFirstValueForGap() {
        String currentFolder = GAP_FILLING_FOLDER + "/static";
        String pathToInputFile = currentFolder + "/gap_static_values_in.txt";
        String pathToCleanConfigFile = currentFolder + "/gap_static_values_config.txt";
        String pathToRealOutput = currentFolder + "/gap_static_values_out_real.txt";
        String pathToCorrectOutput = currentFolder + "/gap_static_values_out_exp.txt";
        DataRowChecker checker = new DataRowChecker();
        checker.validate(pathToInputFile, pathToCleanConfigFile, pathToRealOutput, pathToCorrectOutput);
    }

    @Test
    public void shouldSetDefaultValueForGap() {
        String currentFolder = GAP_FILLING_FOLDER + "/default";
        String pathToInputFile = currentFolder + "/gap_default_values_in.txt";
        String pathToCleanConfigFile = currentFolder + "/gap_default_values_config.txt";
        String pathToRealOutput = currentFolder + "/gap_default_values_out_real.txt";
        String pathToCorrectOutput = currentFolder + "/gap_default_values_out_exp.txt";
        DataRowChecker checker = new DataRowChecker();
        checker.validate(pathToInputFile, pathToCleanConfigFile, pathToRealOutput, pathToCorrectOutput);
    }

    @Test
    public void shouldInterpoplateValueForGap() {
        String currentFolder = GAP_FILLING_FOLDER + "/interpolation";
        String pathToInputFile = currentFolder + "/gap_interpolate_values_in.txt";
        String pathToCleanConfigFile = currentFolder + "/gap_interpolate_values_config.txt";
        String pathToRealOutput = currentFolder + "/gap_interpolate_values_out_real.txt";
        String pathToCorrectOutput = currentFolder + "/gap_interpolate_values_out_exp.txt";
        DataRowChecker checker = new DataRowChecker();
        checker.validate(pathToInputFile, pathToCleanConfigFile, pathToRealOutput, pathToCorrectOutput);
    }

}
