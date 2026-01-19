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

## The shop as non Customer:
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

## Users view of the page:
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

## Admins view of the page:
### Description:
Previous tests checked what user and non-user see and can do on the page. 
Now this test is concerned about everything the admin sees and can do
###  Requirements:
Logged in as admin. (admin, passwd)
Staring one the "Home" page
### Procedure:
1. 
### Result
- After step 1:
- After step 2:
- After step 3:
- After step 4:
- After step 5:
- After step 6:
- After step 7:
- After step 8:


## Users view of the page:
### Description:

###  Requirements:

### Procedure:

### Result
- After step 1:
- After step 2:
- After step 3:
- After step 4:
- After step 5:
- After step 6:
- After step 7:
- After step 8: