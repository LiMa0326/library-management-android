package mobile.li.librarymanagement;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

import static mobile.li.librarymanagement.LibrarianMainActivity.KEY_NAME;
import static mobile.li.librarymanagement.LibrarianMainActivity.SEARCH_CODE_FOR_LIBRARIAN;

public class LibrarianSearchList extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private String mLibrarianEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_librarian_search_list);
        setTitle("Select the book you want to edit");

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mLibrarianEmail = mFirebaseUser.getEmail();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        final ListView listView = (ListView) findViewById(R.id.listView_search);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        listView.setAdapter(adapter);

        if((getIntent().getStringExtra(KEY_NAME)).equals(SEARCH_CODE_FOR_LIBRARIAN)){
            mDatabase.child("books")
                    .orderByChild("bookCreateByEmail")
                    .equalTo(mLibrarianEmail)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChildren()) {
                                Iterator<DataSnapshot> iter = dataSnapshot.getChildren().iterator();
                                while(iter.hasNext()) {
                                    String curName = ((String) (iter.next().child("bookName").getValue()));
                                    adapter.add(curName);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

        }else{
            mDatabase.child("books")
                    .orderByChild("bookName")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChildren()) {
                                Iterator<DataSnapshot> iter = dataSnapshot.getChildren().iterator();
                                while(iter.hasNext()) {
                                    String curName = ((String) (iter.next().child("bookName").getValue()));
                                    if (curName.indexOf(getIntent().getStringExtra(KEY_NAME)) > -1) {
                                        adapter.add(curName);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    mDatabase.child("users").child(mUserId).child("items")
//                            .orderByChild("bookName")
//                            .equalTo((String) listView.getItemAtPosition(position))
//                            .addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(DataSnapshot dataSnapshot) {
//                                    if (dataSnapshot.hasChildren()) {
//                                        DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
//                                        firstChild.getRef().removeValue();
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(DatabaseError databaseError) {
//
//                                }
//                            });
                Intent intent = new Intent (LibrarianSearchList.this, BookEditorActivity.class);
                String temp = (String) listView.getItemAtPosition(position);

                intent.putExtra(KEY_NAME, temp);
//                startActivityForResult(intent, REQ_CODE_BOOK_EDITOR);
                startActivity(intent);
            }
        });
    }

}
