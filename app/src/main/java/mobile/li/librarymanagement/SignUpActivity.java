package mobile.li.librarymanagement;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    protected EditText passwordEditText;
    protected EditText emailEditText;
    protected EditText uIDEditText;
    protected Button signUpButton;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance();

        passwordEditText = (EditText)findViewById(R.id.passwordField);
        emailEditText = (EditText)findViewById(R.id.emailField);
        signUpButton = (Button)findViewById(R.id.signupButton);
        uIDEditText = (EditText) findViewById(R.id.uIDField);

        // Set up other UI component
        final LinearLayout background = findViewById(R.id.return_confirm_background);
        ColorDrawable[] color = {new ColorDrawable(Color.WHITE), new ColorDrawable(Color.DKGRAY)};
        final TransitionDrawable trans = new TransitionDrawable(color);
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Registering user and sending verify email...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.show();

                String password = passwordEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String id = uIDEditText.getText().toString();

                password = password.trim();
                email = email.trim();
                id = id.trim();

                if (password.isEmpty() || email.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    builder.setMessage(R.string.signup_error_message)
                            .setTitle(R.string.signup_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else if(id.length() != 6 || (!isNumber(id))){
                    Toast.makeText(SignUpActivity.this, "Please enter a valid University ID", Toast.LENGTH_LONG).show();
                }
                else {
                    mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseAuth.getInstance().signOut();
                                        String email = emailEditText.getText().toString();
                                        String password = passwordEditText.getText().toString();

                                        email = email.trim();
                                        password = password.trim();

                                        if (email.isEmpty() || password.isEmpty()) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                            builder.setMessage(R.string.login_error_message)
                                                    .setTitle(R.string.login_error_title)
                                                    .setPositiveButton(android.R.string.ok, null);
                                            AlertDialog dialog = builder.create();
                                            dialog.show();
                                        } else {
                                            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                                                    .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                                            if (task.isSuccessful()) {
                                                                sendEmailVerified();
                                                                progress.dismiss();
                                                            } else {
                                                                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                                                builder.setMessage(task.getException().getMessage())
                                                                        .setTitle(R.string.login_error_title)
                                                                        .setPositiveButton(android.R.string.ok, null);
                                                                AlertDialog dialog = builder.create();
                                                                dialog.show();
                                                            }
                                                        }
                                                    });
                                        }

                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                        builder.setMessage(task.getException().getMessage())
                                                .setTitle(R.string.login_error_title)
                                                .setPositiveButton(android.R.string.ok, null);
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                }
                            });
                }
            }
        });
    }

    private boolean isNumber(String s){
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");

    }

    private void sendEmailVerified() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user.isEmailVerified()) {
            // user is verified, so you can finish this activity or send user to activity which you want.
            Toast.makeText(SignUpActivity.this,
                    "this user has been verified",
                    Toast.LENGTH_SHORT).show();

        } else {
            // email is not verified, so just prompt the message to the user and restart this activity.
            // NOTE: don't forget to log out the user.
            user.sendEmailVerification()
                    .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this,
                                        "Verification email sent ",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SignUpActivity.this,
                                        "Failed to send verification email.",
                                        Toast.LENGTH_SHORT).show();
                            }

                            // [END_EXCLUDE]
                            Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    });


            //restart this activity

        }
    }
}
