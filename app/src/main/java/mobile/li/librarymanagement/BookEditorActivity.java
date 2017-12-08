package mobile.li.librarymanagement;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static mobile.li.librarymanagement.LibrarianMainActivity.KEY_NAME;

public class BookEditorActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;

    String name = "";
    String author = "";
    String title = "";
    String call_number = "";
    String publisher = "";
    String year = "";
    String location = "";
    String copies = "";
    String status = "";
    String keywords = "";
    String image_path = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_editor);
        setTitle("Librarian Edit Book");

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("books")
                .orderByChild("bookName")
                .equalTo(getIntent().getStringExtra(KEY_NAME))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChildren()){
                            DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                            name = (String)(firstChild.child("bookName").getValue());
                            author = (String)(firstChild.child("bookAuthor").getValue());
                            title = (String)(firstChild.child("bookTitle").getValue());
                            call_number = (String)(firstChild.child("bookCallNumber").getValue());
                            publisher = (String)(firstChild.child("bookPublisher").getValue());
                            year = (String)(firstChild.child("bookYear").getValue());
                            location = (String)(firstChild.child("bookLocation").getValue());
                            copies = Long.toString((Long)(firstChild.child("bookCopies").getValue()));
                            status = ((String)(firstChild.child("bookStatus").getValue())).toUpperCase();
                            keywords = (String)(firstChild.child("bookKeywords").getValue());
                            image_path = (String)(firstChild.child("bookImagePath").getValue());
                            setupUI();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        findViewById(R.id.book_editor_editButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBook();
                if(name == " " || author == " " || title == " " || call_number == " " || publisher == " " || year == " " ||
                        location == " " || copies == " " || status == " " || keywords == " " || image_path == " "){
                    Toast.makeText(BookEditorActivity.this, "Please fill all the blank", Toast.LENGTH_LONG).show();
                }else if(!isNumber(copies)){
                    Toast.makeText(BookEditorActivity.this, "Please enter a valid number on the copies field", Toast.LENGTH_LONG).show();
                }else if(status.equals("OFFLINE")){
                    Toast.makeText(BookEditorActivity.this, "Cannot edit a rented book", Toast.LENGTH_LONG).show();
                }
                else if(name.equals(getIntent().getStringExtra(KEY_NAME))){
                    editBook();
//                    Intent resultIntent = new Intent();
//                    setResult(RESULT_OK, resultIntent);
//                    finish();
                    Intent intent = new Intent(BookEditorActivity.this, LibrarianMainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else {
                    mDatabase.child("books")
                            .orderByChild("bookName")
                            .equalTo(name)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChildren()){
                                        Toast.makeText(BookEditorActivity.this, "Book with same name has been added", Toast.LENGTH_LONG).show();

                                    }else{
                                        editBook();
//                                        Intent resultIntent = new Intent();
//                                        setResult(RESULT_OK, resultIntent);
//                                        finish();
                                        Intent intent = new Intent(BookEditorActivity.this, LibrarianMainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }
        });

        findViewById(R.id.book_editor_delButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(status.equals("OFFLINE")){
                    Toast.makeText(BookEditorActivity.this, "Cannot delete a rented book", Toast.LENGTH_LONG).show();
                }else {

                    mDatabase.child("books")
                            .orderByChild("bookName")
                            .equalTo(getIntent().getStringExtra(KEY_NAME))
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChildren()) {
                                        DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                                        firstChild.getRef().removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
//                Intent resultIntent = new Intent();
//                setResult(RESULT_OK, resultIntent);
//                finish();
                    Intent intent = new Intent(BookEditorActivity.this, LibrarianMainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });






    }

//    private void delete(){
//        mDatabase.child("books")
//                .orderByChild("bookName")
//                .equalTo((String) ((ListView)findViewById(R.id.listView)).getItemAtPosition(position))
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.hasChildren()) {
//                            DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
//                            firstChild.getRef().removeValue();
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//    }

    private void setupUI(){
        ((EditText)findViewById(R.id.book_editor_name)).setText(name);
        ((EditText)findViewById(R.id.book_editor_author)).setText(author);
        ((EditText)findViewById(R.id.book_editor_title)).setText(title);
        ((EditText)findViewById(R.id.book_editor_copies)).setText(copies);
        ((EditText)findViewById(R.id.book_editor_number)).setText(call_number);
        ((EditText)findViewById(R.id.book_editor_publisher)).setText(publisher);
        ((EditText)findViewById(R.id.book_editor_location)).setText(location);
        ((EditText)findViewById(R.id.book_editor_status)).setText(status);
        ((EditText)findViewById(R.id.book_editor_keyword)).setText(keywords);
        ((EditText)findViewById(R.id.book_editor_year)).setText(year);
        ((EditText)findViewById(R.id.book_editor_imagePath)).setText(image_path);
    }

    private void saveBook() {

        name = ((EditText) findViewById(R.id.book_editor_name)).getText().toString().toUpperCase();
        author = ((EditText) findViewById(R.id.book_editor_author)).getText().toString().toUpperCase();
        title = ((EditText) findViewById(R.id.book_editor_title)).getText().toString().toUpperCase();
        call_number = ((EditText) findViewById(R.id.book_editor_number)).getText().toString().toUpperCase();
        publisher = ((EditText) findViewById(R.id.book_editor_publisher)).getText().toString().toUpperCase();
        year = ((EditText) findViewById(R.id.book_editor_year)).getText().toString().toUpperCase();
        location = ((EditText) findViewById(R.id.book_editor_location)).getText().toString().toUpperCase();
        keywords = ((EditText) findViewById(R.id.book_editor_keyword)).getText().toString().toUpperCase();
        image_path = ((EditText) findViewById(R.id.book_editor_imagePath)).getText().toString().toUpperCase();

        if(name == null || name.trim().equals("")){
            name = " ";
        }
        if(author == null || author.trim().equals("")){
            author = " ";
        }
        if(title == null || title.trim().equals("")){
            title = " ";
        }
        if(call_number == null || call_number.trim().equals("")){
            call_number = " ";
        }
        if(publisher == null || publisher.trim().equals("")){
            publisher = " ";
        }
        if(year == null || year.trim().equals("")){
            year = " ";
        }
        if(location == null || location.trim().equals("")){
            location = " ";
        }
//        if(copies == null || copies.trim().equals("")){
//            copies = " ";
//        }
//        if(status == null || status.trim().equals("")){
//            status = " ";
//        }
        if(keywords == null || keywords.trim().equals("")){
            keywords = " ";
        }
        if(image_path == null || image_path.trim().equals("")){
            image_path = " ";
        }
    }

    private void editBook(){
        mDatabase.child("books")
                .orderByChild("bookName")
                .equalTo(getIntent().getStringExtra(KEY_NAME))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChildren()){
                            DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                            firstChild.child("bookName").getRef().setValue(name);
                            firstChild.child("bookAuthor").getRef().setValue(author);
                            firstChild.child("bookTitle").getRef().setValue(title);
                            firstChild.child("bookCallNumber").getRef().setValue(call_number);
                            firstChild.child("bookPublisher").getRef().setValue(publisher);
                            firstChild.child("bookLocation").getRef().setValue(location);
                            firstChild.child("bookCopies").getRef().setValue(Long.parseLong(copies));
                            firstChild.child("bookStatus").getRef().setValue(status);
                            firstChild.child("bookKeywords").getRef().setValue(keywords);
                            firstChild.child("bookImagePath").getRef().setValue(image_path);
                            firstChild.child("bookYear").getRef().setValue(year);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private boolean isNumber(String s){
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");

    }
}

