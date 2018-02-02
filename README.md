# LibraryManagement
## 1.1	Authentication and Navigation Part
### 1.1.1	 Login Activity
Login Activity can login with registered email and password. It also supports resend verify email and sign up for new librarian or customer.

### 1.1.2	 Signup Activity
Signup Activity can sign up with SJSU email for librarian or other type of email for customer. All new user should provide 6-digits university ID.

### 1.1.3	 Main Activity
Main Activity act as a navigation activity for the old user re-open the app. If user login as a librarian previous, the Main Activity will automatically navigate to the Library Book List Activity. Similarly, if user login as a customer previous, the Main Activity will automatically navigate to the Customer Rent List Activity.

## 1.2	Librarian Part
### 1.2.1	 Library Book List Activity
Library Book List Activity support list and search of books in the library.

### 1.2.2	 Add New Book Activity
Add New Book Activity could add book with different properties includes Author, Title, Call number, Publisher, Year of publication, Location in the library, Number of copies, Current status, Keywords and Image Path.

### 1.2.3	 Search Book Activity
Search Book Activity could search book name based on the user input in the text field.

### 1.2.4	 Search My Book Activity
Search My Book Activity returns the book created by your email, you can edit them quickly.

### 1.2.5	 Edit Book Activity
You can edit book information or delete the book entirely in the Edit Book Activity.

## 1.3	Customer Part
### 1.3.1	 Customer Rent List Activity
You can login to Customer Rent List Activity with an registered non-SJSU. The Customer Rent List Activity contains the books are being by the customer and three buttons navigation to Rent New Book Activity, Return Book Activity and Waitlist Check Activity.

### 1.3.2	 Rent New Book Activity
The Rent new Book Activity supports rent book directly or by book search. When you click a book name, you will get a pop-up window shows the book rent result. If you rent success, the pop-up window will show the book name, due date and send email confirmation to you email address. If you rent failed, the pop-up windows will show you the failed reason like one rent limit exceed, total limit exceed or book rent by others. If the book you want to rent is rented by other customer, you will add to waitlist automatically.

### 1.3.3	 Return Book Activity
The Return Book Activity can select up to nine books at one time to return, you cannot press continue to confirmation activity if you select exceed 9 books at one time. The title of this activity shows the current selected count.

### 1.3.4	 Return Book Confirmation Activity
In the confirmation activity, you can check the book you want to return with days of rented, due date, penalty for each book and total due now for all rented books. When you click confirm button. The activity will update the database and shows successful returned all the book by a series of animation.

### 1.3.5	 Waitlist Check Activity
In the waitlist check activity, you can see the current waitlist of rent book. It shows the book currently available to rent or not. You can delete from waitlist by click the book name.
