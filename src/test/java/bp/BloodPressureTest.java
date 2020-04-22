/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bp;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author kaza_
 */
@RunWith(Parameterized.class)
public class BloodPressureTest {

    private BloodPressure bloodPressure;
    private BPCategory expectedResult; 
    private int systolic; 
    private int diastolic;
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        bloodPressure = new BloodPressure();
    }
    
    @After
    public void tearDown() {
        bloodPressure = null;
    } 
    
    // Each parameter should be placed as an argument here 
    // Every time runner triggers, it will pass the arguments 
    // from parameters 
    public BloodPressureTest(BPCategory expectedResult, int systolicNumber, int diastolicNumber) {
        this.expectedResult = expectedResult; 
        this.systolic = systolicNumber; 
        this.diastolic = diastolicNumber;
    }
    
    @Parameterized.Parameters 
    public static Collection addNumbers() { 
        return Arrays.asList(new Object[][] { 
            { BPCategory.Low, 85, 55 }, 
            { BPCategory.Normal, 85, 65 }, 
            { BPCategory.Normal, 95, 75 }, 
            { BPCategory.PreHigh, 115, 85 }, 
            { BPCategory.PreHigh, 125, 85 }, 
            { BPCategory.High, 135, 95 }, 
            { BPCategory.High, 145, 95 },           
        }); 
    }
    
    // This test will run for each row of test data we have in the Parameterised.Parameters
    @Test 
    public void testSetBPCategory() { 
        try {
            BloodPressure bp1 = new BloodPressure(this.systolic, this.diastolic);
            bp1.setBPCategory();
            assertEquals(this.expectedResult, bp1.getBPCategory());
        }
        catch (Exception e) {            
            fail("An error was thrown: " + e.getMessage() );
        }
    }
    
    @Test
    public void testSetSystolic_systolicTooLow_returnsInvalidValue() throws Exception {
        Logger.getLogger(getClass().getName()).log(Level.INFO, "testSetSystolic_systolicTooLow_returnsInvalidValue");
        
        //Arrange - where we initialise our object
        String expResult = "Invalid Systolic Value - too low";
        int systolic = 10;
        
        try {
            //Act - where we act on this object
            bloodPressure.setSystolic(systolic);
        }
        catch (Exception e) {
            //Assert -  verify that the result is what you expected
            assertEquals(expResult, e.getMessage());
        }
    }
    
    @Test
    public void testSetSystolic_systolicTooHigh_returnsInvalidValue() throws Exception {
        Logger.getLogger(getClass().getName()).log(Level.INFO, "testSetSystolic_systolicTooHigh_returnsInvalidValue");
        
        //Arrange - where we initialise our object
        String expResult = "Invalid Systolic Value - too high";
        int systolic = 200;
        
        try {
            //Act - where we act on this object
            bloodPressure.setSystolic(systolic);
        }
        catch (Exception e) {
            //Assert -  verify that the result is what you expected
            assertEquals(expResult, e.getMessage());
        }
    }
    
    @Test
    public void testSetSystolic_systolicValid_setsValue() throws Exception {
        Logger.getLogger(getClass().getName()).log(Level.INFO, "testSetSystolic_systolicValid_setsValue");
        
        //Arrange - where we initialise our object
        int systolic = 180;
        
        try {
            //Act - where we act on this object
            bloodPressure.setSystolic(systolic);
        }
        catch (Exception e) {            
            fail("An error was thrown: " + e.getMessage() );
        }
        
        //Assert -  verify that the result is what you expected
        assertEquals(systolic, bloodPressure.getSystolic());
    }
    
    @Test
    public void testSetDiastolic_diastolicTooLow_returnsInvalidValue() throws Exception {
        Logger.getLogger(getClass().getName()).log(Level.INFO, "testSetDiastolic_diastolicTooLow_returnsInvalidValue");
        
        //Arrange - where we initialise our object
        String expResult = "Invalid Diastolic Value - too low";
        int diastolic = 10;
        
        try {
            //Act - where we act on this object
            bloodPressure.setDiastolic(diastolic);
        }
        catch (Exception e) {
            //Assert -  verify that the result is what you expected
            assertEquals(expResult, e.getMessage());
        }
    }
    
    @Test
    public void testSetDiastolic_diastolicTooHigh_returnsInvalidValue() throws Exception {
        Logger.getLogger(getClass().getName()).log(Level.INFO, "testSetDiastolic_diastolicTooHigh_returnsInvalidValue");
        
        //Arrange - where we initialise our object
        String expResult = "Invalid Diastolic Value - too high";
        int diastolic = 200;
        
        try {
            //Act - where we act on this object
            bloodPressure.setDiastolic(diastolic);
        }
        catch (Exception e) {
            //Assert -  verify that the result is what you expected
            assertEquals(expResult, e.getMessage());
        }
    }
    
    @Test
    public void testSetDiastolic_diastolicValid_setsValue() throws Exception {
        Logger.getLogger(getClass().getName()).log(Level.INFO, "testSetDiastolic_diastolicValid_setsValue");
        
        //Arrange - where we initialise our object
        int diastolic = 80;
        
        try {
            //Act - where we act on this object
            bloodPressure.setDiastolic(diastolic);
        }
        catch (Exception e) {            
            fail("An error was thrown: " + e.getMessage() );
        }
        
        //Assert -  verify that the result is what you expected
        assertEquals(diastolic, bloodPressure.getDiastolic());
    }
}
