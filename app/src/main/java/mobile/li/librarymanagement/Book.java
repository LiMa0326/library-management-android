package mobile.li.librarymanagement;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Created by Li on 2017/11/19.
 */

public class Book {
    private String bookName;
    private String bookAuthor;
    private String bookTitle;
    private String bookCallNumber;
    private String bookPublisher;
    private String bookYear;
    private String bookLocation;
    private int bookCopies = 1;
    private String bookStatus;
    private String bookKeywords;
    private String bookImagePath;

    private String bookCreatedBy;
    private String bookCreateByEmail;
    private Map<String, String> waitList;

    Book(){}

    Book(String input_bookName){
        bookName = input_bookName;
    }

    public void setBookName(String Name){ bookName = Name;}

    public void setBookAuthor(String Author){
        bookAuthor = Author;
    }

    public void setBookTitle(String Title) { bookTitle = Title;}

    public void setBookCallNumber(String callNumber){
        bookCallNumber = callNumber;
    }

    public void setBookPublisher(String Publisher){
        bookPublisher = Publisher;
    }

    public void setBookYear(String Year){
        bookYear = Year;
    }

    public void setBookLocation(String Location){
        bookLocation = Location;
    }

    public void setBookCopies(int copies){
        bookCopies = copies;
    }

    public void setBookStatus(String Status){
        bookStatus = Status;
    }

    public void setBookKeywords(String Keywords){
        bookKeywords = Keywords;
    }

    public void setBookImagePath(String ImagePath){
        bookImagePath = ImagePath;
    }

    public void setBookCreatedBy(String CreatedBy){
        bookCreatedBy = CreatedBy;
    }

    public void setBookCreateByEmail(String Email){
        bookCreateByEmail = Email;
    }

    public void setWaitList(Map<String, String> input_waitlist){
        if(waitList == null){
            waitList = new HashMap<>();
        }else if(input_waitlist == null){
            return;
        }
        waitList.clear();
        waitList.putAll(input_waitlist);
    }

    public void addToWaitList(String email){
        if(waitList == null){
            waitList = new HashMap<>();
        }
        waitList.put(String.valueOf(System.currentTimeMillis()), email);
    }

    public String getBookName(){
        return bookName;
    }

    public String getBookAuthor(){
        return bookAuthor;
    }

    public String getBookCallNumber(){
        return bookCallNumber;
    }

    public String getBookPublisher(){
        return bookPublisher;
    }

    public String getBookYear(){
        return bookYear;
    }

    public String getBookTitle() {return bookTitle;}

    public String getBookLocation(){
        return bookLocation;
    }

    public int getBookCopies(){
        return bookCopies;
    }

    public boolean removeFromWaitList(String inputEmail){
        if(waitList == null || waitList.isEmpty() || inputEmail == null){
            return false;
        }else{
            Map<String, String> newWaitList = new HashMap<>();
            for(Map.Entry<String, String> entry : waitList.entrySet()){
                if(!entry.getValue().equals(inputEmail)){
                    newWaitList.put(entry.getKey(), entry.getValue());
                }
            }
            setWaitList(newWaitList);
            return true;
        }
    }

    public String getBookStatus(){
        return bookStatus;
    }

    public String getBookKeywords(){
        return bookKeywords;
    }

    public String getBookImagePath(){
        return bookImagePath;
    }

    public String getBookCreatedBy(){
        return bookCreatedBy;
    }

    public String getBookCreateByEmail(){
        return bookCreateByEmail;
    }

    public Map<String, String> getWaitList(){
        return waitList;
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("bookName", bookName);
        result.put("bookAuthor",bookAuthor);
        result.put("bookTitle",bookTitle);
        result.put("bookCallNumber",bookCallNumber);
        result.put("bookPublisher",bookPublisher);
        result.put("bookYear",bookYear);
        result.put("bookLocation",bookLocation);
        result.put("bookCopies",bookCopies);
        result.put("bookStatus",bookStatus);
        result.put("bookKeywords",bookKeywords);
        result.put("bookImagePath",bookImagePath);
        result.put("bookCreatedBy",bookCreatedBy);
        result.put("bookCreateByEmail",bookCreateByEmail);
        result.put("waitList",waitList);
        return result;
    }

}
