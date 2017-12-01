package mobile.li.librarymanagement;

import android.util.Log;

import com.google.firebase.database.Exclude;

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
    private Map<String, String> rentBooks;

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
            for(String millTimeStr : rentBooks.keySet()){
                Long millTime = Long.valueOf(millTimeStr);
                Date date = new Date(millTime * 1000L);
                if(date.getDay() == currentDateTime.getDay()){
                    temp_rentCountOneDay++;
                }
            }
            return temp_rentCountOneDay;
        }
    }

    public Boolean rentNewBook(String newBook){
        rentCountOneDay = updateRentCountOneDay();
        Log.e("Customer-OneDay:" , String.valueOf(rentCountOneDay));
        Log.e("Customer-ALL:" , String.valueOf(rentBooks.size()));
        if(rentBooks.size() <= 9 && rentCountOneDay <= 3){
            rentBooks.put(String.valueOf(System.currentTimeMillis()), newBook);
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

    public void setRentBooks(Map<String, String> input_map){
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

    public Map<String, String> getRentBooks(){
        return rentBooks;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("email", email);
        result.put("rentCountOneDay", rentCountOneDay);
        result.put("rentBooks", rentBooks);

        return result;
    }
}
