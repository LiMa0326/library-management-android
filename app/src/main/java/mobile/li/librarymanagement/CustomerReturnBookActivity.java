package mobile.li.librarymanagement;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
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

import java.util.ArrayList;

public class CustomerReturnBookActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private String mLibrarianId;
    private String mLibrarianEmail;
    ArrayAdapter<String> adapter;
    ListView listViewRented;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_return_book);
        setTitle("Select the book you want to return");

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
            listViewRented = (ListView) findViewById(R.id.listView_ready_to_return);
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, android.R.id.text1);
            listViewRented.setAdapter(adapter);
            listViewRented.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            listViewRented.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    // Capture total checked items
                    int checkedCount = listViewRented.getCheckedItemCount();
                    if(checkedCount > 3){
                        setTitle("Please select less than 3 books");
                    }else{
                        setTitle(checkedCount + " Books Ready to Return");
                    }
                }
            });

            mDatabase.child("customer").orderByChild("email").equalTo(mLibrarianEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Customer temp = null;
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        temp = eventSnapshot.getValue(Customer.class);
                        Log.e("CustomerReturnBook:" , temp.getEmail());
                    }

                    if(temp != null && temp.getEmail().equals(mLibrarianEmail)){
                        if(temp.getRentBooks() != null){
                            adapter.clear();
                            for(String bookName: temp.getRentBooks().values()){
                                adapter.add(bookName);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            // Set up Button
            Button button = (Button) findViewById(R.id.returnBookCheckButton);
            button.setOnClickListener(this);
        }
    }

    public void onClick(View view){
        SparseBooleanArray checked = listViewRented.getCheckedItemPositions();
        ArrayList<String> selectedItems = new ArrayList<String>();
        int checkedCount = 0;
        for (int i = 0; i < checked.size(); i++) {
            // Item position in adapter
            int position = checked.keyAt(i);
            // Add sport if it is checked
            if (checked.valueAt(i)) {
                selectedItems.add(adapter.getItem(position));
                checkedCount++;
            }
        }

        if(checkedCount > 0 && checkedCount <= 3){
            String[] outputStrArr = new String[selectedItems.size()];
            for (int i = 0; i < selectedItems.size(); i++) {
                outputStrArr[i] = selectedItems.get(i);
            }
            // Create a bundle object and start activity
            Intent intent = new Intent(getApplicationContext(), CustomerReturnConfirmActivity.class);
            Bundle b = new Bundle();
            b.putStringArray("selectedItems", outputStrArr);
            intent.putExtras(b);
            startActivity(intent);
            finish();
        }else if(checkedCount == 0){
            Toast.makeText(getApplicationContext(), "Please select books to return or press the back button.", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getApplicationContext(), "Sorry, we only accept 3 or less books return at one time.", Toast.LENGTH_LONG).show();
        }
    }

    private void loadLogInView() {
        Intent intent = new Intent(CustomerReturnBookActivity.this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void loadCustomerMain() {
        Intent intent = new Intent(CustomerReturnBookActivity.this, CustomerMainActivity.class);
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
