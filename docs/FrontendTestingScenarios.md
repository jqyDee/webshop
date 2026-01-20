## Overview of the following tests:
1. Login and logout as Customer
2. Non-customers view of the shop
3. Users view of the shop
4. Managers view of the shop
5. Admins view of the shop
6. Test shopping cart options
7. Test order process
8. Test registration

## Login and logout as Customer:
### Description:
Login as Customer check what is possible to do and then logout again. Starting on the "Home" page
###  Requirements:
- Customer does have an Account
- Home page (/)
- Login Page (/login)
### Procedure:
1. Press "Login" in the top right corner
2. Enter login data (user2, passwd)
3. Press "Login" button
4. Press "Logout" button
### Results:
- After step 1:
  - User sees login page
  - field to enter username
  - field to enter password
  - "Register" button
  - "Login" button
- After step 2:
  - User end up on the shop homepage
  - Possible Actions in the menu bar:
    * Products (view the product list)
    * Shopping cart (view the shopping cart of user2)
    * Press on users name and view more options:
      * Orders (view the orders of user2)
      * Logout
- after step 3:
  - Back at the login page

## Non-customers view of the shop:
### Description:
Check what is possible to see and use as non customer. Therefore, move around the website and check all posibilities of actions.
###  Requirements:
Finished webshop design of everything a non customer should be allowed to do.
Starting on the "Home" page (/)
### Procedure:
1. Enter the Url (http://localhost:3000)
2. Click on "Home"
3. Click on "Products"
   4. Search for "Iphone 14"
   5. Click on "Iphone 14"
   6. Click on "Add to Cart"
7. Click on the Cart symbol in the top right corner
### Results:
- After step 1:
  - Customer sees the "Home" page of the Shop
- After step 2:
  - Customer stays on the "Home" page
- After step 3: (/products)
  - Customer sees the List of all products offered
  - Customer sees for each product: name, picture, short description, rating, stock, Price, Cart symbol and if available discount
- After step 4:
  - Customer only sees the "Iphone 14"
- After step 5: (/products/id)
  - Customer sees all details of the product:
    * name, rating, stock, Price, big product picture
    * If available: Discount, discounted Price and crossed out original price
    * "Add to Cart" Button
    * Long description
    * Private Details
    * Customer Reviews (sortable)
- After step 6:
  - User stay on the product details page and red number on the cart symbol increases by one
- After step 7: (/shopping-cart)
  - User sees all products currently in cart
  - For each cart item: name, price, quantity, stock, subtotal, "bin" button, "Fix" button
  - Search bar
  - Clear all Button
  - Total Price of all items

## Users view of the shop:
### Description:
A User can do everything a non-user can do which was tested above. But there are things only a user can do after login.
Test begins after login on "Home" page.
###  Requirements:
Login as User (user2, passwd)
Finished web shop for users
### Procedure:
1. Click on customers name
2. Click on "Order"
3. Click on "view" on with status Delivered/Shipped/Cancelled/Processing
4. Click arrow in tp left corner of the order details window
5. Click on "view" on with status Pending/Pending payment/Paid
6. Click on Cart

### Result
- After step 2: (/orders)
  - Customer sees all orders placed
  - List and grid option of view
  - Customer sees for each order: Id, staus, total, date of creation, "View button"
- After step 3: (orders/id)
  - Customer sees all products in the order
  - For each order item customer sees: name, quantity, price, sum
  - Customer sees Shipping Address and Billing Address
  - And everything the customer saw at the orders overview
- After step 4: (/orders)
  - Customer is back on the orders overview
- After step 5: (/orders/id)
  - Here the customer sees a "Cancel" button at left button corner allowing to cancel orders which are not processed yet
- After step 6: (/shopping-cart)
  - Compared to the non-user view of the shopping cart there now is an "Order now" button

## Managers view of the shop:
### Description:
This test is concered with a Manager's view of the shop.
###  Requirements:
Login as Manager (user1, passwd)
Start test on the "Home" Page
### Procedure:
1. Click on Products
2. Click on "Edit pen" at the right of a product
3. Go back to the list
4. Click on "Create Product" button
5. Go back
6. Click on the managers name
### Result
- After step 1: (/products)
  - Manager sees all the products in the shop like a customer
  - At the top right there is a "Create Product" button
  - On top Manager sees a pen symbol to the right of each product
  - The cart symbol at the right is not clickable
- After step 2:
  - Manager gets a window to edit the product.
  - At the bottom there is a "save" button to save the changes
  - And there is a "delete" button to delete the product
- After step 4:
  - Manager gets the same window as after step 3 only with "Create" instead of "Save" Button at the bottom
- After step 6:
  - Manger doesnt have any options here except logout
  

## Admins view of the shop:
### Description:
Previous tests checked what user and non-user see and can do on the page. 
Now this test is concerned about everything the admin sees and can do
###  Requirements:
Logged in as admin. (admin, passwd)
Starting one the "Home" page
### Procedure:
1. Click on Products
2. Click on the name of the Admin
3. Click on "Admin Panel"
4. Click on "Details" of some user
5. Go back 
6. Click on "Add User"
7. Go back 
8. Click on Admins name and then on "Orders"
### Result
- After step 1: (/products)
  - Admin sees all the products and has all the options a manager has
- After step 2:
  - Admin sees: Orders, Admin Panel and Logout 
- After step 3: (/manage-users)
  - Admin now sees a list of user
  - For each user Admin sees: username, first name, last name, role, enabled and Details button
- After step 4:
  - Admin gets window to see more user information (email, phone number, password)
  - Admin can edit everything
  - "Cancel" and "Save" button at the bottom
- After step 6:
  - Admin gets window to create a user
  - "Cancel" and "Create" button at the bottom
- After step 8: (/orders)
  - Admin sees all orders from all customers
  - Admin also sees which customer placed the order
  - Everything else is identical to a customers view

## Test shopping cart options:
### Description:
Testing what can be done in the shopping cart interface
###  Requirements:
Login as User (user2, passwd)
Add some products to the cart as described in the User view test
Test starts at the shopping cart interface 
### Procedure:
1. After login go to http://localhost:3000/shopping-cart
2. Click on the up and down arrow next to the quantity of a product
3. Set the quantity to > Stock
4. Click on "Fix" button
5. Click on the bin symbol at the right of one product
6. Click on the "Clear all" Button
### Result
- After step 1: (/shopping-cart)
  - User sees the shopping cart as describe in non-user/user view
- After step 2:
  - When clicking the "up" arrow the Quatity increases by 1 and the down arrow decreases it by 1
- After step 4:
  - The Quantity decreases to the maximum stock
- After step 5:
  - The item is removed from the shopping cart
- After step 6:
  - The shopping cart is empty now

## Test order process:
### Description:
Testing the order process as a customer. A customer should be able to place an order with all the cart items
###  Requirements:
Login as User (user2, passwd)
Add some products to the cart as described in the User view test
Test starts at the shopping cart interface
### Procedure:
1. Set the quantity of one product > Stock
2. Click on the "Order now" button
3. Change the quantity to something < stock
4. Again click on "Order now" button
5. Click on "Next"
6. Enter Address
7. Again click on next
8. Click on "Order now"
9. Click on "Continue Shopping"
### Result
- After step 2: (/shopping-cart)
  - There appears a warning telling the user it is impossible to order the current shopping cart
- After step 4:
  - Customer sees all steps of the order process on the left side (Address, Overview, Payment, Confirmation)
  - Customer can now enter a shipping address and a payment address
  - In the bottom right corner customer sees a "next" button
- After step 5:
  - All address fields get red and the user is told to enter something
- After step 7:
  - The Customer now sees they arrived at the Overview step
  - An overview of the order and the entered addresses is displayed 
  - At the bottom there is a "Back" and an "Order now" button
- After step 8:
  - Payment is skipped as there is now real payment implementation in our shop
  - Customer arrives at the confirmation step of the order process
  - Customer again sees the products ordered
  - At the bottom is a "Continue Shopping" button
- After step 9:
  - the shopping cart is empty now

## Test registration:
### Description:
Testing how a user can register themselves
###  Requirements:
Test starts on the login page 
### Procedure:
1. Click on the "Register" button
2. Click on Create
### Result:
- After step 1:
  - User sees a window to enter all their credentials (username, first name, last name, e-mail, password, phone number)
  - At the bottom there is a "Cancel" and a "Create" button
- After step 2:
  - The user now has an account and is already logged in with their just created account