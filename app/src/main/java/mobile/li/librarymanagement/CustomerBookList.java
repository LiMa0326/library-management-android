package mobile.li.librarymanagement;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

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
        setTitle("Select the book you want to rent");

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
                                            Toast.makeText(getApplicationContext(), "Rent Book " +rentBookName+ " Successful!", Toast.LENGTH_LONG).show();
                                        }else{
                                            updateBookWaitlist(rentBookName);
                                            Toast.makeText(getApplicationContext(), "Book rented by others! Add to WaitList!", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }else if(userResult == -1){
                                Log.e("CustomerBookList:" , "Rent new Book Failed! Total Rent Limit exceed!");
                                Toast.makeText(getApplicationContext(), "Total Rent Limit exceed!", Toast.LENGTH_LONG).show();
                            }else if(userResult == -2){
                                Log.e("CustomerBookList:" , "Rent new Book Failed! One day Rent Limit exceed!");
                                Toast.makeText(getApplicationContext(), "One day Rent Limit exceed!", Toast.LENGTH_LONG).show();
                            }else if(userResult == -3){
                                Log.e("CustomerBookList:" , "Rent new Book Failed! Cannot Rent duplicate books!");
                                Toast.makeText(getApplicationContext(), "Sorry, you cannot rent duplicate for the same book!", Toast.LENGTH_LONG).show();
                            }else{
                                Log.e("CustomerBookList:" , "Rent new Book Failed!");
                                Toast.makeText(getApplicationContext(), "Rent Book Failed!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                    // finish CustomerBookList activity
                    finish();
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
