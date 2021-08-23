package com.lazyshoppers.firestore

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.lazyshoppers.models.*
import com.lazyshoppers.ui.activities.*
import com.lazyshoppers.ui.fragments.DashboardFragment
import com.lazyshoppers.ui.fragments.OrdersFragment
import com.lazyshoppers.ui.fragments.ProductsFragment
import com.lazyshoppers.ui.fragments.SoldProductFragment
import com.lazyshoppers.utils.Constants

class FirestoreClass {
    private val mFirestore = FirebaseFirestore.getInstance()

    fun registerUser(activity: RegisterActivity, userInfo: User) {
        mFirestore.collection(Constants.USERS)
            .document(userInfo.id)
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisterationSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while registering. ", e
                )
            }
    }

    fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId = ""
        if (currentUser != null) {
            currentUserId = currentUser.uid
        }
        return currentUserId
    }

    fun getUserDetails(activity: Activity) {
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.data.toString())
                val user = document.toObject(User::class.java)!!

                val sharePreferences = activity.getSharedPreferences(
                    Constants.LS_PREFERENCE,
                    Context.MODE_PRIVATE
                )

                val editor: SharedPreferences.Editor = sharePreferences.edit()
                editor.putString(
                    Constants.LOGGED_IN_USERNAME,
                    "${user.firstName} ${user.lastName}"
                )
                editor.apply()

                when (activity) {
                    is LoginActivity -> {
                        activity.userLoggedSuccess(user)
                    }
                    is SettingsActivity -> {
                        activity.userDetailsSuccess(user)
                    }
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is LoginActivity -> {
                        activity.hideProgressDialog()
                    }
                    is SettingsActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(activity.javaClass.simpleName, "Error Occurred while getting data", e)
            }
    }

    fun updateUserProfileDetails(activity: Activity, userHashMap: HashMap<String, Any>) {
        mFirestore.collection(Constants.USERS).document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                when (activity) {
                    is UserProfileActivity -> {
                        activity.userProfileUpdateSuccess()
                    }
                }
            }.addOnFailureListener { e ->
                when (activity) {
                    is UserProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while updating the user details ",
                    e
                )
            }
    }

    fun uploadImageToCloudStorage(activity: Activity, imageFileUri: Uri?, imageType: String) {
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
            imageType + System.currentTimeMillis() + "."
                    + Constants.getFileExtension(
                activity,
                imageFileUri
            )
        )

        sRef.putFile(imageFileUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.e(
                    "FireBase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        Log.e("Download Image Url", uri.toString())
                        when (activity) {
                            is UserProfileActivity -> {
                                activity.imageUploadSuccess(uri.toString())
                            }
                            is AddProductActivity -> {
                                Log.e("AddProductAct", "uploadImageToCloudStorage called")

                                activity.imageUploadSuccess(uri.toString())
                            }
                        }
                    }
            }
            .addOnFailureListener { exception ->
                when (activity) {
                    is UserProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                    is AddProductActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    exception.message,
                    exception
                )

            }
    }

    fun uploadProductDetails(activity: AddProductActivity, productInfo: Product) {
        Log.e("AddProductAct FireStore", "uploadProductImage called")

        mFirestore.collection(Constants.PRODUCTS)
            .document()
            .set(productInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.productUploadSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error While uploading product", e
                )
            }
    }

    fun getProductDetails(fragment: Fragment) {
        mFirestore.collection(Constants.PRODUCTS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                Log.e("Product Document", document.documents.toString())
                val productList: ArrayList<Product> = ArrayList()
                for (i in document.documents) {
                    val product = i.toObject(Product::class.java)
                    product!!.product_id = i.id
                    productList.add(product)
                }
                when (fragment) {
                    is ProductsFragment -> {
                        fragment.successProductsListFromFireStore(productList)
                    }
                }
            }
            .addOnFailureListener { }

    }

    fun getDashboardItemsList(fragment: DashboardFragment) {
        mFirestore.collection(Constants.PRODUCTS)
            .get()
            .addOnSuccessListener { document ->
                Log.e("Products List", document.documents.toString())
                val productList: ArrayList<Product> = ArrayList()
                for (i in document.documents) {
                    val product = i.toObject(Product::class.java)
                    product!!.product_id = i.id
                    productList.add(product)
                }
                fragment.successDashBoardItemsList(productList)
            }
            .addOnFailureListener {
                fragment.hideProgressDialog()
                Log.e("Products List", "Error Loading")
            }
    }

    fun deleteProduct(fragment: ProductsFragment, productId: String) {
        mFirestore.collection(Constants.PRODUCTS)
            .document(productId)
            .delete()
            .addOnSuccessListener {
                fragment.productDeleteSuccess()
            }
            .addOnFailureListener { e ->
                fragment.hideProgressDialog()
                Log.e("Delete Product", e.toString())
            }
    }

    fun getProductDetails(activity: ProductDetailsActivity, productId: String) {
        mFirestore.collection(Constants.PRODUCTS)
            .document(productId)
            .get()
            .addOnSuccessListener { document ->
                val product = document.toObject(Product::class.java)!!
                activity.productDetailsSuccess(product)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while getting product Details",e)
            }
    }

    fun addCartItems(activity: ProductDetailsActivity, addToCart: CartItem) {
        mFirestore.collection(Constants.CART_ITEMS)
            .document()
            .set(addToCart, SetOptions.merge())
            .addOnSuccessListener {
                activity.addToCartSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating the document to cart item ", e
                )

            }
    }

    fun checkIfProductExistInCart(activity: ProductDetailsActivity, productId: String) {
        mFirestore.collection(Constants.CART_ITEMS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserId())
            .whereEqualTo(Constants.PRODUCT_ID, productId)
            .get()
            .addOnSuccessListener { document ->
                if (document.documents.size > 0) {
                    activity.productExistInCart()
                } else {
                    activity.hideProgressDialog()
                }

            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while Loading cart item ", e
                )
            }
    }

    fun getCartList(activity: Activity) {
        mFirestore.collection(Constants.CART_ITEMS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())
                val list: ArrayList<CartItem> = ArrayList<CartItem>()

                for (i in document.documents) {
                    val cartItem = i.toObject(CartItem::class.java)!!
                    cartItem.id = i.id
                    list.add(cartItem)
                }
                when (activity) {
                    is CartListActivity -> {
                        activity.successCartItemsList(list)
                    }
                    is CheckoutActivity -> {
                        activity.successCartItemList(list)
                    }
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is CartListActivity -> {
                        activity.hideProgressDialog()
                    }
                    is CheckoutActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName, "Error while getting the cart list items ", e)
            }
    }


    fun getAllProductsList(activity: Activity) {
        mFirestore.collection(Constants.PRODUCTS)
            .get()
            .addOnSuccessListener { document ->
                Log.e("Products List", document.documents.toString())
                val productsList = ArrayList<Product>()
                for (i in document.documents) {
                    val product = i.toObject(Product::class.java)!!
                    product.product_id = i.id
                    productsList.add(product)
                }
                when (activity) {
                    is CartListActivity -> {
                        activity.successProductsListFromFireStore(productsList)
                    }
                    is CheckoutActivity -> {
                        activity.successProductListFromFirestore(productsList)
                    }

                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is CartListActivity -> {
                        activity.hideProgressDialog()
                    }
                    is CheckoutActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e("Get Products List", "Error ", e)
            }
    }

    fun removeItemFromCart(context: Context, cartId: String) {
        mFirestore.collection(Constants.CART_ITEMS)
            .document(cartId)
            .delete()
            .addOnSuccessListener {
                when (context) {
                    is CartListActivity -> {
                        context.itemRemovedSuccess()
                    }
                }
            }
            .addOnFailureListener { e ->
                when (context) {
                    is CartListActivity -> {
                        context.hideProgressDialog()
                    }
                }
                Log.e("Remove Cart Item", "Error ", e)
            }
    }

    fun updateMyCart(context: Context, cart_id: String, itemHashMap: HashMap<String, Any>) {
        mFirestore.collection(Constants.CART_ITEMS)
            .document(cart_id)
            .update(itemHashMap)
            .addOnSuccessListener {
                when (context) {
                    is CartListActivity -> {
                        context.itemUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener { e ->
                when (context) {
                    is CartListActivity -> {
                        context.hideProgressDialog()
                    }
                }
                Log.e(context.javaClass.simpleName, e.toString())
            }
    }

    fun addAddress(activity: AddEditAddressActivity, addressInfo: Address) {
        mFirestore.collection(Constants.ADDRESSES)
            .document()
            .set(addressInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.addUpdateAddressSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error Adding Address ", e
                )
            }
    }

    fun getAddressList(activity: AddressListActivity) {
        mFirestore.collection(Constants.ADDRESSES)
            .whereEqualTo(Constants.USER_ID, getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())
                val addressList: ArrayList<Address> = ArrayList()

                for (i in document.documents) {
                    val address = i.toObject(Address::class.java)!!
                    address.id = i.id
                    addressList.add(address)
                }
                activity.successAddressListFromFireStore(addressList)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error Adding Address List ", e
                )
            }
    }

    fun updateAddress(activity: AddEditAddressActivity, addressInfo: Address, addressId: String) {
        mFirestore.collection(Constants.ADDRESSES)
            .document(addressId)
            .set(addressInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.addUpdateAddressSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error Updating Address ", e
                )
            }
    }


    fun deleteAddress(activity: AddressListActivity, addressId: String) {
        mFirestore.collection(Constants.ADDRESSES)
            .document(addressId)
            .delete()
            .addOnSuccessListener {
                activity.deleteAddressSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error Deleting Address ", e
                )
            }
    }

    fun placeOrder(activity: CheckoutActivity, order: Order) {
        mFirestore.collection(Constants.ORDERS)
            .document()
            .set(order, SetOptions.merge())
            .addOnSuccessListener {
                activity.orderPlacedSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error Placing Order ", e
                )
            }
    }

    fun updateAllDetails(activity: CheckoutActivity, cartList: ArrayList<CartItem>, order: Order) {
        val writeBatch = mFirestore.batch()

        for (cartItem in cartList) {
            val productHashMap = HashMap<String, Any>()
            productHashMap[Constants.STOCK_QUANTITY] = (cartItem.stock_quantity.toInt() - cartItem.cart_quantity.toInt()).toString()
            val documentReference = mFirestore.collection(Constants.PRODUCTS)
                .document(cartItem.product_id)

            writeBatch.update(documentReference, productHashMap)
        }

        for (cartItem in cartList) {

            val soldProduct = SoldProduct(
                cartItem.product_owner_id,
                cartItem.title,
                cartItem.price,
                cartItem.cart_quantity,
                cartItem.image,
                order.title,
                order.order_datetime,
                order.sub_total_amount,
                order.shipping_charges,
                order.total_amount,
                order.address
            )

            val documentReference = mFirestore.collection(Constants.SOLD_PRODUCTS)
                .document(cartItem.product_id)

            writeBatch.set(documentReference, soldProduct)
        }

        for (cartItem in cartList) {
            val documentReference = mFirestore.collection(Constants.CART_ITEMS)
                .document(cartItem.id)
            writeBatch.delete(documentReference)
        }

        writeBatch.commit()
            .addOnSuccessListener {
                activity.allDetailsSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error Clearing cart ", e
                )
            }
    }

    fun getMyOrderList(fragment: OrdersFragment) {
        mFirestore.collection(Constants.ORDERS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                val list: ArrayList<Order> = ArrayList()
                for (i in document.documents) {
                    val orderItem = i.toObject(Order::class.java)!!
                    orderItem.id = i.id
                    list.add(orderItem)
                }
                fragment.populateOrdersListinUI(list)
            }
            .addOnFailureListener { e ->
                fragment.hideProgressDialog()
                Log.e(
                    fragment.javaClass.simpleName,
                    "Error Loading Orders ", e
                )
            }
    }

    fun getSoldProductsList(fragment: SoldProductFragment){
        mFirestore.collection(Constants.SOLD_PRODUCTS)
            .whereEqualTo(Constants.USER_ID,getCurrentUserId())
            .get()
            .addOnSuccessListener { document->
                val list: ArrayList<SoldProduct> = ArrayList()
                for (i in document.documents) {
                    val soldProduct = i.toObject(SoldProduct::class.java)!!
                    soldProduct.id = i.id
                    list.add(soldProduct)
                }
                fragment.successSoldProductList(list)
            }
            .addOnFailureListener { e->
                fragment.hideProgressDialog()
                Log.e(
                    fragment.javaClass.simpleName,
                    "Error Loading Sold Orders ", e
                )
            }
    }

}