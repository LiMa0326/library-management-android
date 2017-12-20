package mobile.li.librarymanagement;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class CustomerBookList extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private String mLibrarianId;
    private String mLibrarianEmail;

    public interface OnGetUserDataListener {
        void onSuccess(int userResult);
    }

    public interface OnGetDataListener {
        void onSuccess(boolean result);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_book_list);
        setTitle("Check Out New Book");

        // Initialize FireBase Auth and Database Reference
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (mFirebaseUser == null){
            // Not logged in, launch the Log In activity
            loadLogInView();
        } else {
            mLibrarianId = mFirebaseUser.getUid();
            mLibrarianEmail = mFirebaseUser.getEmail();

            // Set up ListView
            final ListView listView = (ListView) findViewById(R.id.listView);
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
            listView.setAdapter(adapter);

            // Set up Button
            final Button button = (Button) findViewById(R.id.backToCustomerMainButton);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //Go to new customer book list activity
                    loadCustomerMain();
                }
            });

            // Set up other UI component
            final ProgressDialog progress = new ProgressDialog(this);
            progress.setTitle("Loading");
            progress.setMessage("Renting Books...");
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog

            // Set up search field
            final EditText editSearch = (EditText) findViewById(R.id.searchText);
            editSearch.setSingleLine(true);
            editSearch.clearFocus();

            mDatabase.child("books").orderByChild("bookName").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded (DataSnapshot dataSnapshot, String s){
                    String getbookName = (String) dataSnapshot.child("bookName").getValue();
                    adapter.add(getbookName);
                }

                @Override
                public void onChildChanged (DataSnapshot dataSnapshot, String s){

                }

                @Override
                public void onChildRemoved (DataSnapshot dataSnapshot){
                    adapter.remove((String) dataSnapshot.child("bookName").getValue());
                }

                @Override
                public void onChildMoved (DataSnapshot dataSnapshot, String s){

                }

                @Override
                public void onCancelled (DatabaseError databaseError){

                }
            });

            // Capture Text in EditText
            editSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                    // When user changed the Text
                    adapter.getFilter().filter(cs);
                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

                }

                @Override
                public void afterTextChanged(Editable arg0) {

                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final String rentBookName = (String) listView.getItemAtPosition(position);
                    //Set get user can rent listener
                    getUserCanRent(rentBookName, new OnGetUserDataListener() {
                        @Override
                        public void onSuccess(int userResult) {
                            if(userResult == 1){
                                // Set get book can rent listener
                                getBookCanRent(rentBookName, new OnGetDataListener() {
                                    @Override
                                    public void onSuccess(boolean result) {
                                        if(result){
                                            updateBothInSuccess(rentBookName);
                                            //Toast.makeText(getApplicationContext(), "Rent Book " +rentBookName+ " Successful!", Toast.LENGTH_LONG).show();
                                            Date currentDateTime = new Date(System.currentTimeMillis());
                                            showPopupWindow(true, "[" +rentBookName+ "]\n" + "Due Date: " + getDueDate(currentDateTime) + "\nEmail confirmation send.");
                                            if(isOnline()){
                                                sendEmailToCustomer("Rent Book " + "[" + rentBookName +"] Successfully",
                                                        "Thank you. You successfully rented book. \n"
                                                        + "\tBook Name: " + "[" +rentBookName+ "]\n"
                                                        + "\tDue Date: " + getDueDate(currentDateTime));
                                            }
                                        }else{
                                            updateBookWaitlist(rentBookName);
                                            //Toast.makeText(getApplicationContext(), "Book rented by others! Add to WaitList!", Toast.LENGTH_LONG).show();
                                            showPopupWindow(false, "Book rented by others! \n" + "Add to WaitList!");
                                        }
                                    }
                                });
                            }else if(userResult == -1){
                                Log.e("CustomerBookList:" , "Rent new Book Failed! Total Rent Limit exceed!");
                                //Toast.makeText(getApplicationContext(), "Total Rent Limit exceed!", Toast.LENGTH_LONG).show();
                                showPopupWindow(false, "Your Total Rent Limit exceed!");
                            }else if(userResult == -2){
                                Log.e("CustomerBookList:" , "Rent new Book Failed! One day Rent Limit exceed!");
                                //Toast.makeText(getApplicationContext(), "One day Rent Limit exceed!", Toast.LENGTH_LONG).show();
                                showPopupWindow(false, "Your One day Rent Limit exceed!");
                            }else if(userResult == -3){
                                Log.e("CustomerBookList:" , "Rent new Book Failed! Cannot Rent duplicate books!");
                                //Toast.makeText(getApplicationContext(), "Sorry, you cannot rent duplicate for the same book!", Toast.LENGTH_LONG).show();
                                showPopupWindow(false, "Sorry, you cannot rent duplicate for the same book!");
                            }else{
                                Log.e("CustomerBookList:" , "Rent new Book Failed!");
                                //Toast.makeText(getApplicationContext(), "Rent Book Failed!", Toast.LENGTH_LONG).show();
                                showPopupWindow(false, "Rent Book Failed!");
                            }
                        }
                    });
                }
            });
        }
    }

    private void getUserCanRent(final String rentBookName, final OnGetUserDataListener listener){
        mDatabase.child("customer").orderByChild("email").equalTo(mLibrarianEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot eventSnapshot = dataSnapshot.getChildren().iterator().next();
                Customer temp = eventSnapshot.getValue(Customer.class);
                int userResult = temp.rentNewBook(rentBookName);
                listener.onSuccess(userResult);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getBookCanRent(final String rentBookName, final OnGetDataListener listener){
        mDatabase.child("books").orderByChild("bookName").equalTo(rentBookName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean result = false;
                DataSnapshot eventSnapshot = dataSnapshot.getChildren().iterator().next();
                Book temp = eventSnapshot.getValue(Book.class);
                if(temp.getBookCopies() == 0 || temp.getBookStatus().equals("offline")){
                    result = false;
                    Log.e("CBookL-getBookCanRent:" , "CAN NOT RENT : " + rentBookName);
                }else{
                    result = true;
                    Log.e("CBookL-getBookCanRent:" , "CAN RENT : " + rentBookName);
                }
                listener.onSuccess(result);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateBookWaitlist(String bookName){
        mDatabase.child("books").orderByChild("bookName").equalTo(bookName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot eventSnapshot = dataSnapshot.getChildren().iterator().next();
                String key = eventSnapshot.getKey();
                Book temp = eventSnapshot.getValue(Book.class);

                //Add customer email to WaitList
                temp.addToWaitList(mLibrarianEmail);

                //Update database with Map and updateChildren function
                Map<String, Object> updateValues = temp.toMap();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/books/" + key, updateValues);
                mDatabase.updateChildren(childUpdates);

                Log.e("CBookL-updateWaitList:" , "Add to WaitList" + mLibrarianEmail);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateBothInSuccess(final String bookName){
        mDatabase.child("customer").orderByChild("email").equalTo(mLibrarianEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Customer temp = null;
                DataSnapshot eventSnapshot = dataSnapshot.getChildren().iterator().next();
                String key = eventSnapshot.getKey();
                temp = eventSnapshot.getValue(Customer.class);
                int rentResult = temp.rentNewBook(bookName);
                if(rentResult == 1){
                    Map<String, Object> updateValues = temp.toMap();
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/customer/" + key, updateValues);
                    mDatabase.updateChildren(childUpdates);

                    Log.e("CBookL-updateBoth-C:" , "Success update customer table add book :" + bookName);
                }else{
                    Log.e("CBookL-updateBoth-C:" , "Failed update customer table!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child("books").orderByChild("bookName").equalTo(bookName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot eventSnapshot = dataSnapshot.getChildren().iterator().next();
                String key = eventSnapshot.getKey();
                Book temp = eventSnapshot.getValue(Book.class);
                int bookCopies = temp.getBookCopies();
                if(bookCopies == 0 || temp.getBookStatus().equals("offline")){
                    Log.e("CBookL-updateBoth-B:" , "Failed update books table!");
                }else if(bookCopies == 1){
                    temp.setBookCopies(0);
                    temp.setBookStatus("offline");
                    temp.removeFromWaitList(mLibrarianEmail);

                    //Update database with Map and updateChildren function
                    Map<String, Object> updateValues = temp.toMap();
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/books/" + key, updateValues);
                    mDatabase.updateChildren(childUpdates);

                    Log.e("CBookL-updateBoth-B:" , "Success update books table with book [offline] for:" + bookName);
                }else{
                    temp.setBookCopies(temp.getBookCopies() - 1);
                    temp.setBookStatus("online");
                    temp.removeFromWaitList(mLibrarianEmail);

                    //Update database with Map and updateChildren function
                    Map<String, Object> updateValues = temp.toMap();
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/books/" + key, updateValues);
                    mDatabase.updateChildren(childUpdates);

                    Log.e("CBookL-updateBoth-B:" , "Success update books table with copies:" + String.valueOf(bookCopies)+ " [online] for:" + bookName);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showPopupWindow(boolean result, String notification){
        // get a reference to the already created main layout
        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.customer_bookList_linerLayout);

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.customer_rent_popup_window, null);

        ImageView popupImage = popupView.findViewById(R.id.ib_sign);
        TextView popupTitle = popupView.findViewById(R.id.popup_title);
        TextView popupText = popupView.findViewById(R.id.popup_text);

        if(result){
            popupTitle.setText(R.string.customer_rent_success);
            popupImage.setImageResource(R.drawable.ic_check_circle_white_24px);
        }else{
            popupTitle.setText(R.string.customer_rent_failed);
            popupImage.setImageResource(R.drawable.ic_info_white_24px);
        }

        popupText.setText(notification);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        popupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();

                // finish CustomerBookList activity
                // finish();

                return true;
            }
        });
    }

    private void sendEmailToCustomer(String title, String body){
        class SendConfirmationEmail extends AsyncTask<String, Void, Void> {
            private Exception exception;
            protected Void doInBackground(String... param) {
                final String username = "li.for.app@gmail.com";
                final String password = "67226554";

                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");

                Session session = Session.getInstance(props,
                        new javax.mail.Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(username, password);
                            }
                        });
                try {
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress("scontdiplex@gmail.com"));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mLibrarianEmail));
                    message.setSubject(param[0]);
                    message.setText(param[1]);

                    Transport.send(message);

                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }

                return null;
            }

            protected void onPostExecute() {

            }
        }

        new SendConfirmationEmail().execute(title, body);
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    private String getDueDate(Date rentDateTime){
        Calendar c = Calendar.getInstance();
        c.setTime(rentDateTime);
        c.add(Calendar.DATE, 30);
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return dateFormat.format(c.getTime());
    }

    private void loadLogInView() {
        Intent intent = new Intent(this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void loadCustomerMain() {
        Intent intent = new Intent(CustomerBookList.this, CustomerMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_customer_booklist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_back_customer_main){
            loadCustomerMain();
            //Toast.makeText(this, "Rent New Book!", Toast.LENGTH_LONG).show();
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            //mFirebaseAuth.signOut();
            loadLogInView();
        }

        return super.onOptionsItemSelected(item);
    }
}
