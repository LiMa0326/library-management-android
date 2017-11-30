package mobile.li.librarymanagement;

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
}
