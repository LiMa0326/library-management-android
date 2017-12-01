package mobile.li.librarymanagement;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_book_list);

        // Initialize Firebase Auth and Database Reference
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

            mDatabase.child("books").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    adapter.add((String) dataSnapshot.child("bookName").getValue());
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    adapter.remove((String) dataSnapshot.child("bookName").getValue());
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final int pos = position;
                    mDatabase.child("customer").orderByChild("email").equalTo(mLibrarianEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Customer temp = null;
                            for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                                String key = eventSnapshot.getKey();
                                temp = eventSnapshot.getValue(Customer.class);
                                String rentBookName = (String) listView.getItemAtPosition(pos);
                                Boolean rentSucc = temp.rentNewBook(rentBookName);
                                if(rentSucc){
                                    Map<String, Object> updateValues = temp.toMap();
                                    Map<String, Object> childUpdates = new HashMap<>();
                                    childUpdates.put("/customer/" + key, updateValues);
                                    mDatabase.updateChildren(childUpdates);
                                    Log.e("CustomerBookList:" , "Rent new Book Successful!");
                                    Toast.makeText(getApplicationContext(), "Rent Book " +rentBookName+ " Successful!", Toast.LENGTH_LONG).show();
                                }else{
                                    Log.e("CustomerBookList:" , "Rent new Book Failed!");
                                    Toast.makeText(getApplicationContext(), "Rent Book Limit exceed!", Toast.LENGTH_LONG).show();
                                }
                            }
                            finish();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            });
        }
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
