
import org.junit.Test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author broder
 */
public class ScalingTest {

    public static String SCALING_FOLGER = "testdata/scaling";

    @Test
    public void shouldScaleTheValues() {
        String pathToInputFile = SCALING_FOLGER + "/scale_values_in.txt";
        String pathToCleanConfigFile = SCALING_FOLGER + "/scale_values_config.txt";
        String pathToRealOutput = SCALING_FOLGER + "/scale_values_out_real.txt";
        String pathToCorrectOutput = SCALING_FOLGER + "/scale_values_out_exp.txt";
        DataRowChecker checker = new DataRowChecker();
        checker.validate(pathToInputFile, pathToCleanConfigFile, pathToRealOutput, pathToCorrectOutput);
    }
}
