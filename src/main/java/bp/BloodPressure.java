/*
 */
package bp;

import javax.ejb.Singleton;
/**
 * Singleton session bean used to store the blood pressure readings and category for "/bp" resource
 * 
 * @author kanolan
 */
@Singleton
public class BloodPressure {
    protected final int SYSTOLIC_MIN = 70;
    protected final int SYSTOLIC_MAX = 190;
    protected final int DIASTOLIC_MIN = 40;
    protected final int DIASTOLIC_MAX = 100;  
    
    private int systolic;
    private int diastolic;
    private BPCategory category;

    public BloodPressure() {}
    
    public BloodPressure(int systolic, int diastolic) throws Exception {
        try {
            this.setSystolic(systolic);
            this.setDiastolic(diastolic);
            this.setBPCategory();
        }
        catch(Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public int getSystolic() { // mmHG
        return this.systolic;
    }   
    
    protected void setSystolic(int systolic) throws Exception { // mmHG
        if( systolic >= SYSTOLIC_MIN && systolic <= SYSTOLIC_MAX)
            this.systolic = systolic;
        else if( systolic < SYSTOLIC_MIN)
          throw new Exception("Invalid Systolic Value - too low");  
        else if( systolic > SYSTOLIC_MAX)
          throw new Exception("Invalid Systolic Value - too high"); 
        else
          throw new Exception("Invalid Systolic Value");
    }
    
    public int getDiastolic() { // mmHG
        return this.diastolic;
    }   
    
    protected void setDiastolic(int diastolic) throws Exception { // mmHG
        if( diastolic >= DIASTOLIC_MIN && diastolic <= DIASTOLIC_MAX)
            this.diastolic = diastolic;
        else if( diastolic < DIASTOLIC_MIN)
          throw new Exception("Invalid Diastolic Value - too low");  
        else if( diastolic > DIASTOLIC_MAX)
          throw new Exception("Invalid Diastolic Value - too high"); 
        else
          throw new Exception("Invalid Diastolic Value");
    }
    
    public BPCategory getBPCategory() { 
        return this.category;
    }   
    
    protected void setBPCategory() { 
        //Low
        if( this.getDiastolic() < 60 && this.getSystolic() < 90 )
            this.category = BPCategory.Low;
        //Ideal
        else if( (this.getDiastolic() >=60 && this.getDiastolic() < 80) && this.getSystolic() < 90 )
            this.category = BPCategory.Normal;
        //Ideal
        else if( this.getDiastolic() < 80 && (this.getSystolic() >= 90 && this.getSystolic() < 120) )
            this.category = BPCategory.Normal;
        //Pre-High
        else if( (this.getDiastolic() >=80 && this.getDiastolic() < 90) && this.getSystolic() < 120 )
            this.category = BPCategory.PreHigh;
        //Pre-High
        else if( this.getDiastolic() < 90 && (this.getSystolic() >= 120 && this.getSystolic() < 140) )
            this.category = BPCategory.PreHigh;              
        //High
        else if( (this.getDiastolic() >=90 && this.getDiastolic() < 100) && this.getSystolic() < 140 )
            this.category = BPCategory.High;
        //High
        else if( this.getDiastolic() < 100 && (this.getSystolic() >= 140 && this.getSystolic() < 190) )
            this.category = BPCategory.High;  
    }
}
