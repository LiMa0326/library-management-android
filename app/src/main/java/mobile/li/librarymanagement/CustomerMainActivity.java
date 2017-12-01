package mobile.li.librarymanagement;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

public class CustomerMainActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private String mLibrarianId;
    private String mLibrarianEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main);

        // Initialize Firebase Auth and Database Reference
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Log.i("CustomerMainActivity:" , "Set Firebase Successful");

        if (mFirebaseUser == null){
            // Not logged in, launch the Log In activity
            loadLogInView();
        } else {
            mLibrarianId = mFirebaseUser.getUid();
            mLibrarianEmail = mFirebaseUser.getEmail();

            // Set up ListView
            final ListView listViewRented = (ListView) findViewById(R.id.listView_rented);
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
            listViewRented.setAdapter(adapter);

            // Set up Button
            final Button button = (Button) findViewById(R.id.rentNewButton);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //Go to new customer book list activity
                    loadCustomerBookList();
                }
            });

            mDatabase.child("customer").orderByChild("email").equalTo(mLibrarianEmail).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Customer temp = null;
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        temp = eventSnapshot.getValue(Customer.class);
                        Log.e("CustomerMainActivity:" , temp.getEmail());
                    }

                    if(temp != null && temp.getEmail().equals(mLibrarianEmail)){
                        if(temp.getRentBooks() != null){
                            for(String bookName: temp.getRentBooks().values()){
                                adapter.add(bookName);
                            }
                        }
                    }else{
                        Customer newCustomer = new Customer(mLibrarianId, mLibrarianEmail);
                        Map<String, String> newMap = new HashMap<>();
                        newMap.put(String.valueOf(System.currentTimeMillis()),"恨别鸟惊心第一卷");
                        newCustomer.setRentBooks(newMap);
                        mDatabase.child("customer").push().setValue(newCustomer);
                        Log.e("CustomerMainActivity:" , "Add new customer: " + mLibrarianEmail);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

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

    private void loadCustomerBookList(){
        Intent intent = new Intent(CustomerMainActivity.this, CustomerBookList.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_customer_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_rentNewbook){
            loadCustomerBookList();
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
