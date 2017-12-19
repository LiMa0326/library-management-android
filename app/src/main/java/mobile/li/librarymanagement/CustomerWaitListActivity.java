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
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CustomerWaitListActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private String mLibrarianId;
    private String mLibrarianEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_wait_list);
        setTitle("Current Waitlist of Rent Book");

        // Initialize FireBase Auth and Database Reference
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (mFirebaseUser == null) {
            // Not logged in, launch the Log In activity
            loadLogInView();
        } else {
            mLibrarianId = mFirebaseUser.getUid();
            mLibrarianEmail = mFirebaseUser.getEmail();

            // Set up ListView
            final ListView listView = (ListView) findViewById(R.id.waitlist_listView);
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
            listView.setAdapter(adapter);

            // Set up Button
            final Button button = (Button) findViewById(R.id.waitlist_backToCustomerMainButton);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //Go to new customer book list activity
                    loadCustomerMain();
                }
            });

            mDatabase.child("books").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Book temp;
                    Set<String> bookNames = new HashSet<>();
                    adapter.clear();

                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        temp = eventSnapshot.getValue(Book.class);
                        if(temp != null && temp.getWaitList() != null) {
                            if (temp.getWaitList().values().contains(mLibrarianEmail)) {
                                if (!bookNames.contains(temp.getBookName())) {
                                    StringBuilder print = new StringBuilder();
                                    print.append(temp.getBookName());
                                    print.append("\n");
                                    print.append("[Available to rent now: ");
                                    if(temp.getBookStatus().equals("online")){
                                        print.append("yes]");
                                    }else{
                                        print.append("no]");
                                    }
                                    adapter.add(print.toString());
                                    bookNames.add(temp.getBookName());
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String getItem = (String) adapterView.getItemAtPosition(i);
                    final String bookName = getItem.split("\n")[0];
                    mDatabase.child("books").orderByChild("bookName").equalTo(bookName).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            DataSnapshot eventSnapshot = dataSnapshot.getChildren().iterator().next();
                            String key = eventSnapshot.getKey();
                            Book temp = eventSnapshot.getValue(Book.class);
                            HashMap<String, String> newWaitlist = new HashMap<>();
                            for(Map.Entry<String, String> entry : temp.getWaitList().entrySet()){
                                if(!entry.getValue().equals(mLibrarianEmail)){
                                    newWaitlist.put(entry.getKey(), entry.getValue());
                                }
                            }
                            temp.setWaitList(newWaitlist);

                            //Update database with Map and updateChildren function
                            Map<String, Object> updateValues = temp.toMap();
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/books/" + key, updateValues);
                            mDatabase.updateChildren(childUpdates);

                            Log.e("CustomerWaitlist:" , "Delete waitlist: [" + bookName + "] for " + mLibrarianEmail);
                            Toast.makeText(getApplicationContext(), "Delete From Waitlist: " + bookName, Toast.LENGTH_LONG).show();
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
        Intent intent = new Intent(CustomerWaitListActivity.this, CustomerMainActivity.class);
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