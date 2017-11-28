package mobile.li.librarymanagement;

import android.util.Log;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Li on 2017/11/22.
 */

public class Customer {
    private String id;
    private String email;
    private int rentCountOneDay;
    private Map<String, Long> rentBooks;

    Customer(){}

    Customer(String input_id, String input_email){
        id = input_id;
        email = input_email;
        rentBooks = new HashMap<>();
        rentCountOneDay = updateRentCountOneDay();
    }

    public int updateRentCountOneDay(){
        if(rentBooks.size() == 0){
            return 0;
        }else{
            Date currentDateTime = new Date(System.currentTimeMillis());
            int temp_rentCountOneDay = 0;
            for(long millTime : rentBooks.values()){
                Date date = new Date(millTime);
                if(date.getDay() == currentDateTime.getDay()){
                    temp_rentCountOneDay++;
                }
            }
            return temp_rentCountOneDay;
        }
    }

    public Boolean rentNewBook(String newBook){
        if(rentBooks.size() <= 9 && rentCountOneDay <= 3){
            rentBooks.put(newBook, System.currentTimeMillis());
            rentCountOneDay = updateRentCountOneDay();
            return true;
        }else{
            return false;
        }
    }

    public Boolean returnBook(String book){
        if(rentBooks.containsKey(book)){
            rentBooks.remove(book);
            return true;
        }else{
            return false;
        }
    }


    public void setId(String input_id){
        id = input_id;
    }

    public void setEmail(String input_email){
        email = input_email;
    }

    public void setRentCountOneDay(int input_rentCountDay){
        rentCountOneDay = input_rentCountDay;
    }

    public void setRentBooks(Map<String, Long> input_map){
        if(input_map == null){
            Log.e("Customer class:" , "setRentBooks function directly return");
            return;
        }
        if(rentBooks == null){
            rentBooks = new HashMap<>();
        }
        rentBooks.putAll(input_map);
    }

    public String getId(){
        return id;
    }

    public String getEmail(){
        return email;
    }

    public int getRentCountOneDay(){
        return rentCountOneDay;
    }

    public Map<String, Long> getRentBooks(){
        return rentBooks;
    }
}
