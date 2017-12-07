package mobile.li.librarymanagement;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerReturnConfirmActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private String mLibrarianId;
    private String mLibrarianEmail;

    public interface OnGetDataListener {
        void onSuccess(int result);
    }

    public interface OnUpdateDataListener {
        void onSuccess(boolean result);
    }

    public interface OnUpdateDataByNameAndCountListener {
        void onSuccess(int result, String Name);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_return_confirm);
        setTitle("Books Return Confirmation");

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

            //Handle bundle
            Bundle b = getIntent().getExtras();
            String[] resultArr = b.getStringArray("selectedItems");
            final List<String> confirmBooks = Arrays.asList(resultArr);

            // Set up ListView
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
            ListView listView = (ListView) findViewById(R.id.listView_confirm);
            listView.setAdapter(adapter);

            final ArrayAdapter<String> adapterLog = new ArrayAdapter<>(this, R.layout.customer_return_confirm_log_list, android.R.id.text1);
            ListView listViewLog = (ListView) findViewById(R.id.listView_log);
            listViewLog.setAdapter(adapterLog);

            // Set up Button
            final Button buttonConfirm = findViewById(R.id.returnBookConfirmButton);
            final Button buttonBack = findViewById(R.id.backToCustomerMainInConfirmButton);
            buttonBack.setEnabled(false);

            // Set up other UI component
            final LinearLayout background = findViewById(R.id.return_confirm_background);
            ColorDrawable[] color = {new ColorDrawable(Color.WHITE), new ColorDrawable(Color.DKGRAY)};
            final TransitionDrawable trans = new TransitionDrawable(color);
            final ProgressDialog progress = new ProgressDialog(this);
            progress.setTitle("Loading");
            progress.setMessage("Wait while returning...");
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog

            getPenaltyAndPrintout(confirmBooks, adapter, new OnGetDataListener() {
                @Override
                public void onSuccess(int penaltyResult) {
                    adapter.add("TOTAL DUE: $ " + penaltyResult + ".00 ");
                    buttonConfirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            progress.show();
                            buttonConfirm.setEnabled(false);
                            background.setBackground(trans);
                            trans.startTransition(500);
                            TextView textview = findViewById(R.id.return_log_text);
                            textview.setText(R.string.return_book_log);
                            textview.setTextColor(Color.LTGRAY);
                            updateCustomerTable(confirmBooks, new OnUpdateDataListener(){
                                @Override
                                public void onSuccess(boolean result) {
                                    //Success update Customer table
                                    adapterLog.add("Successful update customer table");
                                    final int bookCount = confirmBooks.size();
                                    int bookNumber = 0;
                                    for(String book : confirmBooks){
                                        bookNumber++;
                                        updateBookTable(book, bookNumber, bookCount, new OnUpdateDataByNameAndCountListener() {
                                            @Override
                                            public void onSuccess(int bookNumber, String bookName) {
                                                if(bookNumber != bookCount){
                                                    adapterLog.add("Successful returned book: [" + bookName + "]");
                                                }else{
                                                    adapterLog.add("Successful returned book: [" + bookName + "]");
                                                    //Toast.makeText(getApplicationContext(), "Thank you. You successfully returned all books. Press button to return.", Toast.LENGTH_LONG).show();
                                                    buttonBack.setEnabled(true);
                                                    progress.dismiss();
                                                    adapterLog.add("Thank you! You successfully returned all selected books.");
                                                    buttonBack.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            finish();
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    private void getPenaltyAndPrintout(final List<String> confirmBooks, final ArrayAdapter<String> adapter,
                                       final OnGetDataListener listener){
        mDatabase.child("customer").orderByChild("email").equalTo(mLibrarianEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot eventSnapshot = dataSnapshot.getChildren().iterator().next();
                Customer temp = eventSnapshot.getValue(Customer.class);
                Map<String, String> rentBooks = temp.getRentBooks();
                Date currentDateTime = new Date(System.currentTimeMillis());
                int penaltyResult = 0;
                for(Map.Entry<String, String> entry : rentBooks.entrySet()){
                    if(confirmBooks.contains(entry.getValue())){
                        Date rentDateTime = new Date(Long.valueOf(entry.getKey()));
                        int daysOfRented = getDaysOfRented(currentDateTime, rentDateTime);
                        String dueDate = getDueDate(rentDateTime);
                        int penaltyOfRented = getPenaltyOfRented(daysOfRented);
                        if(penaltyOfRented != -1){
                            adapter.add(convertToBookPrint(entry.getValue(), daysOfRented, dueDate, penaltyOfRented));
                            penaltyResult += penaltyOfRented;
                        }
                        Log.e("CustomerReturnConfirm:" , entry.getValue() + " Days of Rented: " + String.valueOf(daysOfRented) + " Penalty of Rented:" + String.valueOf(penaltyOfRented));
                    }
                }
                listener.onSuccess(penaltyResult);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateCustomerTable(final List<String> confirmBooks, final OnUpdateDataListener listener){
        mDatabase.child("customer").orderByChild("email").equalTo(mLibrarianEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot eventSnapshot = dataSnapshot.getChildren().iterator().next();
                String key = eventSnapshot.getKey();
                Customer temp = eventSnapshot.getValue(Customer.class);
                Map<String, String> rentedBooks = temp.getRentBooks();
                Map<String, String> updateRentedBooks = new HashMap<>();
                for(Map.Entry<String, String> entry : rentedBooks.entrySet()){
                    if(!confirmBooks.contains(entry.getValue())){
                        updateRentedBooks.put(entry.getKey(), entry.getValue());
                        //Log.e("ReturnBookConfirm:" , "put: " + entry.getKey() + " " + entry.getValue());
                    }
                }
                temp.setRentBooks(updateRentedBooks);

                //Update database
                Map<String, Object> updateValues = temp.toMap();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/customer/" + key, updateValues);
                mDatabase.updateChildren(childUpdates);

                Log.e("ReturnBookConfirm:" , "Updated customer table complete");
                listener.onSuccess(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void updateBookTable(final String book, final int bookNumber, final int bookCount, final OnUpdateDataByNameAndCountListener listener){
        final String bookName = book;
        mDatabase.child("books").orderByChild("bookName").equalTo(bookName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot eventSnapshot = dataSnapshot.getChildren().iterator().next();
                String key = eventSnapshot.getKey();
                Book temp = eventSnapshot.getValue(Book.class);
                int bookCopies = temp.getBookCopies();
                if(bookCopies == 0 || temp.getBookStatus().equals("offline")){
                    temp.setBookCopies(1);
                    temp.setBookStatus("online");
                }else{
                    temp.setBookCopies(bookCopies + 1);
                    temp.setBookStatus("online");
                }

                //Update database with Map and updateChildren function
                Map<String, Object> updateValues = temp.toMap();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/books/" + key, updateValues);
                mDatabase.updateChildren(childUpdates);

                Log.e("ReturnBookConfirm:" , "Returned book: " + bookName + " with copies: " + String.valueOf(temp.getBookCopies()));
                listener.onSuccess(bookNumber, bookName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String getDueDate(Date rentDateTime){
        Calendar c = Calendar.getInstance();
        c.setTime(rentDateTime);
        c.add(Calendar.DATE, 30);
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return dateFormat.format(c.getTime());
    }

    private int getDaysOfRented(Date currentDateTime, Date rentDateTime){
        return (int)((currentDateTime.getTime() - rentDateTime.getTime()) / (1000 * 60 * 60 * 24)) + 1;
    }

    private int getPenaltyOfRented(int daysOfRented){
        if(daysOfRented >= 0 && daysOfRented <= 30){
            return 0;
        }else if(daysOfRented > 30){
            return (daysOfRented - 30);
        }else{
            return -1;
        }
    }

    private String convertToBookPrint(String bookName, int daysOfRented, String dueDate, int penaltyOfRented){
        return "[" + bookName + "]" + "\n"
                + "DAYS OF RENTED: " + String.valueOf(daysOfRented) + ", "
                + "DUE DATE: " + dueDate + ", "
                + "PENALTY: $ " + String.valueOf(penaltyOfRented) + ".00 ";
    }

    private void loadLogInView() {
        Intent intent = new Intent(CustomerReturnConfirmActivity.this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void loadCustomerMain() {
        Intent intent = new Intent(CustomerReturnConfirmActivity.this, CustomerMainActivity.class);
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