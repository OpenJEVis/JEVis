/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.junit.Test;

/**
 * @author broder
 */
public class AlignmentTest {

    public static String ALIGN_FOLDER = "testdata/alignment";

    @Test
    public void shouldHaveSetTheCorrectTime() {
        String currentFolder = ALIGN_FOLDER + "/correct_time";
        String pathToInputFile = currentFolder + "/align_correct_time_in.txt";
        String pathToCleanConfigFile = currentFolder + "/align_correct_time_config.txt";
        String pathToRealOutput = currentFolder + "/align_correct_time_out_real.txt";
        String pathToCorrectOutput = currentFolder + "/align_correct_time_out_exp.txt";
        DataRowChecker checker = new DataRowChecker();
        checker.validate(pathToInputFile, pathToCleanConfigFile, pathToRealOutput, pathToCorrectOutput);
    }

    @Test
    public void shouldOnlyKeepTheLastValue() {
        String currentFolder = ALIGN_FOLDER + "/delete_values";
        String pathToInputFile = currentFolder + "/align_delete_values_in.txt";
        String pathToCleanConfigFile = currentFolder + "/align_delete_values_config.txt";
        String pathToRealOutput = currentFolder + "/align_delete_values_out_real.txt";
        String pathToCorrectOutput = currentFolder + "/align_delete_values_out_exp.txt";
        DataRowChecker checker = new DataRowChecker();
        checker.validate(pathToInputFile, pathToCleanConfigFile, pathToRealOutput, pathToCorrectOutput);
    }

    @Test
    public void shouldCalculateTheAverage() {
        String currentFolder = ALIGN_FOLDER + "/avg_values";
        String pathToInputFile = currentFolder + "/align_avg_values_in.txt";
        String pathToCleanConfigFile = currentFolder + "/align_avg_values_config.txt";
        String pathToRealOutput = currentFolder + "/align_avg_values_out_real.txt";
        String pathToCorrectOutput = currentFolder + "/align_avg_values_out_exp.txt";
        DataRowChecker checker = new DataRowChecker();
        checker.validate(pathToInputFile, pathToCleanConfigFile, pathToRealOutput, pathToCorrectOutput);
    }

    @Test
    public void shouldCalculateTheSum() {
        String currentFolder = ALIGN_FOLDER + "/sum_values";
        String pathToInputFile = currentFolder + "/align_sum_values_in.txt";
        String pathToCleanConfigFile = currentFolder + "/align_sum_values_config.txt";
        String pathToRealOutput = currentFolder + "/align_sum_values_out_real.txt";
        String pathToCorrectOutput = currentFolder + "/align_sum_values_out_exp.txt";
        DataRowChecker checker = new DataRowChecker();
        checker.validate(pathToInputFile, pathToCleanConfigFile, pathToRealOutput, pathToCorrectOutput);
    }

    @Test
    public void shouldNotAlignAndMultipliedWith2AndCalcDifferential() {
        String currentFolder = ALIGN_FOLDER + "/no_alignment";
        String pathToInputFile = currentFolder + "/no_align_in.txt";
        String pathToCleanConfigFile = currentFolder + "/no_align_config.txt";
        String pathToRealOutput = currentFolder + "/no_align_out_real.txt";
        String pathToCorrectOutput = currentFolder + "/no_align_out_exp.txt";
        DataRowChecker checker = new DataRowChecker();
        checker.validate(pathToInputFile, pathToCleanConfigFile, pathToRealOutput, pathToCorrectOutput);
    }

    @Test
    public void shouldChangeTimeBasedOnPositiveOffset() {
        String currentFolder = ALIGN_FOLDER + "/offset";
        String pathToInputFile = currentFolder + "/align_offset_in.txt";
        String pathToCleanConfigFile = currentFolder + "/align_offset_config.txt";
        String pathToRealOutput = currentFolder + "/align_offset_out_real.txt";
        String pathToCorrectOutput = currentFolder + "/align_offset_out_exp.txt";
        DataRowChecker checker = new DataRowChecker();
        checker.validate(pathToInputFile, pathToCleanConfigFile, pathToRealOutput, pathToCorrectOutput);
    }
}
