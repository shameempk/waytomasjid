package in.qbics.way2masjid;

import java.util.Calendar;

/**
 * Created by basheer on 6/13/2014.
 */
class Calculator {

    //constructor method
    public Calculator(){
        this.setMonthYear();
        this.setTimeStamp();
    }

    //VARIABLE DECLARATION
        private int nMonth;
        private int nYear;
        private String timeStamp;

    //DATE CALCULATION:
    void setMonthYear() {

        Calendar calendar = Calendar.getInstance();
        this.nMonth = calendar.get(Calendar.MONTH) + 1;
        this.nYear = calendar.get(Calendar.YEAR);

    }

    public int getnMonth(){
        return this.nMonth;
    }
    public int getnYear(){
        return  this.nYear;
    }

    //TIMESTAMP CALCULATION
    void setTimeStamp(){
        Long tsLong = System.currentTimeMillis()/1000;
        this.timeStamp = tsLong.toString();

    }

    public  String getTimeStamp(){
        return this.timeStamp;
    }



}
