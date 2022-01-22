package com.example.bazaar.adapter

import android.app.Activity
import android.app.Dialog
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.bazaar.MyApplication
import com.example.bazaar.R
import com.example.bazaar.api.model.Orders
import com.example.bazaar.api.model.User
import com.example.bazaar.manager.SharedPreferencesManager
import com.example.bazaar.viewmodels.RemoveOrderViewModel
import com.example.bazaar.viewmodels.UpdateOrderViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch

class OrderAdapter(
        private val view: View,
        private val mItemClickListener: ItemClickListener,
        private var orders: MutableList<Orders>,
        private val updateOrderViewModel: UpdateOrderViewModel,
        private val removeOrderViewModel: RemoveOrderViewModel,
        private val lifecycleOwner: LifecycleOwner,
        private val activity : Activity
) : RecyclerView.Adapter<OrderAdapter.DataViewHolder>(), Filterable {


    var ordersFilterList = ArrayList<Orders>()
    private var checkSpinner = 0
    private var isInOnGoingOrder = false

    init {
        ordersFilterList = orders as ArrayList<Orders>
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DataViewHolder {
        val itemView =
                LayoutInflater.from(viewGroup.context).inflate(R.layout.order_item, viewGroup, false)
        return DataViewHolder(itemView, mItemClickListener)
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {

        // Just in case status is set to ""
        /*
        if(ordersFilterList[position].status == ""){
            updateOrderViewModel.updateOrderRequest.value.let {
                if (it != null) {
                    it.title = ordersFilterList[position].title
                    it.price_per_unit = ordersFilterList[position].price_per_unit.toInt()
                    it.status = "OPEN"
                }
                lifecycleOwner.lifecycleScope.launch {
                    updateOrderViewModel.updateOrder(ordersFilterList[position].order_id)
                }
            }
        }
         */

        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter.createFromResource(
                view.context,
                R.array.order_options_array,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            holder.statusSp.adapter = adapter
        }

        if (ordersFilterList[position].owner_username ==
                MyApplication.sharedPreferences.getUserValue(SharedPreferencesManager.KEY_USER, User()).username) {

            //for on going sales view

            holder.profileNameTv.text = ordersFilterList[position].username

            isInOnGoingOrder = false

            when (ordersFilterList[position].status.toLowerCase().capitalize()) {
                "Incoming", "Open" -> {
                    holder.acceptFab.visibility = View.VISIBLE
                    holder.acceptedFab.visibility = View.GONE
                    holder.ignoreFab.visibility = View.VISIBLE
                    holder.ignoredFab.visibility = View.GONE
                    holder.waitingFab.visibility = View.GONE
                    holder.statusSp.visibility = View.INVISIBLE
                    holder.statusSp.isClickable = false
                    true
                }
                "Accepted", "Delivering" -> {

                    holder.acceptFab.visibility = View.GONE
                    holder.acceptedFab.visibility = View.VISIBLE
                    holder.ignoreFab.visibility = View.GONE
                    holder.ignoredFab.visibility = View.GONE
                    holder.waitingFab.visibility = View.GONE
                    holder.statusSp.visibility = View.VISIBLE
                    holder.statusSp.isClickable = true
                    holder.statusEt.visibility = View.GONE
                    true
                }
                "Delivered" -> {
                    holder.acceptFab.visibility = View.GONE
                    holder.acceptedFab.visibility = View.VISIBLE
                    holder.ignoreFab.visibility = View.GONE
                    holder.ignoredFab.visibility = View.GONE
                    holder.waitingFab.visibility = View.GONE
                    holder.statusSp.visibility = View.INVISIBLE
                    holder.statusSp.isClickable = false

                    true
                }
                "Declined" -> {
                    holder.acceptFab.visibility = View.GONE
                    holder.acceptedFab.visibility = View.GONE
                    holder.ignoreFab.visibility = View.GONE
                    holder.ignoredFab.visibility = View.VISIBLE
                    holder.waitingFab.visibility = View.GONE
                    holder.statusSp.visibility = View.INVISIBLE
                    holder.statusSp.isClickable = false
                    true
                }
                else -> false
            }
        } else {

            //for on going order view

            isInOnGoingOrder = true

            holder.profileNameTv.text = ordersFilterList[position].owner_username

            when (ordersFilterList[position].status.toLowerCase().capitalize()) {
                "Incoming", "Open" -> {
                    holder.acceptFab.visibility = View.GONE
                    holder.acceptedFab.visibility = View.GONE
                    holder.ignoreFab.visibility = View.GONE
                    holder.ignoredFab.visibility = View.GONE
                    holder.waitingFab.visibility = View.VISIBLE
                    holder.statusSp.visibility = View.INVISIBLE
                    holder.statusSp.isClickable = false
                    true
                }
                "Accepted", "Delivering", "Delivered" -> {

                    holder.acceptFab.visibility = View.GONE
                    holder.acceptedFab.visibility = View.VISIBLE
                    holder.ignoreFab.visibility = View.GONE
                    holder.ignoredFab.visibility = View.GONE
                    holder.waitingFab.visibility = View.GONE
                    holder.statusSp.visibility = View.INVISIBLE
                    holder.statusSp.isClickable = false
                    true
                }
                "Declined" -> {
                    holder.acceptFab.visibility = View.GONE
                    holder.acceptedFab.visibility = View.GONE
                    holder.ignoreFab.visibility = View.GONE
                    holder.ignoredFab.visibility = View.VISIBLE
                    holder.waitingFab.visibility = View.GONE
                    holder.statusSp.visibility = View.INVISIBLE
                    holder.statusSp.isClickable = false
                    true
                }
                else -> false
            }
        }


        var counter = 0
        holder.statusSp.setSelection(adapter.getPosition(ordersFilterList[position].status.toLowerCase().capitalize()), false)
        holder.statusSp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, positionSpinner: Int, id: Long) {
                var status = ""
                if (counter >= 1) {
                    //change status based on spinner item
                    when (positionSpinner) {
                        0 -> {
                            status = "INCOMING"
                            updateOrderViewModel.updateOrderRequest.value.let {
                                if (it != null) {
                                    it.title = ordersFilterList[position].title
                                    it.price_per_unit = ordersFilterList[position].price_per_unit.toInt()
                                    it.status = status
                                }
                            }
                            lifecycleOwner.lifecycleScope.launch {
                                updateOrderViewModel.updateOrder(ordersFilterList[position].order_id)
                            }

                            true
                        }
                        1 -> {
                            status = "ACCEPTED"
                            updateOrderViewModel.updateOrderRequest.value.let {
                                if (it != null) {
                                    it.title = ordersFilterList[position].title
                                    it.price_per_unit = ordersFilterList[position].price_per_unit.toInt()
                                    it.status = status
                                }
                            }
                            lifecycleOwner.lifecycleScope.launch {
                                updateOrderViewModel.updateOrder(ordersFilterList[position].order_id)
                            }
                            true
                        }
                        2 -> {
                            status = "DECLINED"
                            updateOrderViewModel.updateOrderRequest.value.let {
                                if (it != null) {
                                    it.title = ordersFilterList[position].title
                                    it.price_per_unit = ordersFilterList[position].price_per_unit.toInt()
                                    it.status = status
                                }
                            }
                            lifecycleOwner.lifecycleScope.launch {
                                updateOrderViewModel.updateOrder(ordersFilterList[position].order_id)
                            }
                            true
                        }
                        3 -> {
                            status = "DELIVERING"
                            updateOrderViewModel.updateOrderRequest.value.let {
                                if (it != null) {
                                    it.title = ordersFilterList[position].title
                                    it.price_per_unit = ordersFilterList[position].price_per_unit.toInt()
                                    it.status = status
                                }
                            }
                            lifecycleOwner.lifecycleScope.launch {
                                updateOrderViewModel.updateOrder(ordersFilterList[position].order_id)
                            }
                            true
                        }
                        4 -> {
                            status = "DELIVERED"
                            updateOrderViewModel.updateOrderRequest.value.let {
                                if (it != null) {
                                    it.title = ordersFilterList[position].title
                                    it.price_per_unit = ordersFilterList[position].price_per_unit.toInt()
                                    it.status = status
                                }
                            }
                            lifecycleOwner.lifecycleScope.launch {
                                updateOrderViewModel.updateOrder(ordersFilterList[position].order_id)
                            }
                            true
                        }
                        else -> false
                    }

                }
                else{
                    counter++
                }
            }
        }

        holder.statusEt.text = ordersFilterList[position].status.toLowerCase().capitalize()

        //change status based on button
        holder.acceptFab.setOnClickListener {
            var status = "ACCEPTED"

            updateOrderViewModel.updateOrderRequest.value.let {
                if (it != null) {
                    it.title = ordersFilterList[position].title
                    it.price_per_unit = ordersFilterList[position].price_per_unit.toInt()
                    it.status = status
                }
            }
            lifecycleOwner.lifecycleScope.launch {
                updateOrderViewModel.updateOrder(ordersFilterList[position].order_id)
            }
        }

        //change status based on button
        holder.ignoreFab.setOnClickListener {
            var status = "DECLINED"
            updateOrderViewModel.updateOrderRequest.value.let {
                if (it != null) {
                    it.title = ordersFilterList[position].title
                    it.price_per_unit = ordersFilterList[position].price_per_unit.toInt()
                    it.status = status
                }
            }

            lifecycleOwner.lifecycleScope.launch {
                updateOrderViewModel.updateOrder(ordersFilterList[position].order_id)
            }
        }

        holder.productNameTv.text = ordersFilterList[position].title
        holder.priceEditTv.text = ordersFilterList[position].price_per_unit
        holder.amountEditTv.text = ordersFilterList[position].units
        holder.descriptionTv.text = ordersFilterList[position].description

        holder.arrowIv.setOnClickListener {
            if (holder.descriptionTv.visibility == View.GONE) {
                holder.descriptionTv.visibility = View.VISIBLE
                holder.arrowIv.setImageResource(R.drawable.ic_arrow_up_order_item)
            } else {
                holder.descriptionTv.visibility = View.GONE
                holder.arrowIv.setImageResource(R.drawable.ic_arrow_down_order_item)
            }
        }


        holder.deleteIv.setOnClickListener{
            val dialog = Dialog(activity)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.dialog_delete_order)

            val deleteBtn: Button = dialog.findViewById(R.id.delete_order_btn) as Button
            val closeBtn: Button = dialog.findViewById(R.id.cancel_btn) as Button

            deleteBtn.setOnClickListener {

                lifecycleOwner.lifecycleScope.launch {
                    removeOrderViewModel.removeOrder(ordersFilterList[position].order_id)
                }

                dialog.dismiss()
            }

            closeBtn.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }


    }

    override fun getItemCount(): Int {
        return ordersFilterList.size
    }

    class DataViewHolder(view: View, itemClickListener: ItemClickListener?) :
            RecyclerView.ViewHolder(view), View.OnClickListener {

        var productImageCiV: CircleImageView = view.findViewById(R.id.product_image_civ)
        var profileNameTv: TextView = view.findViewById(R.id.profile_name_tv)
        var profileImageCiV: CircleImageView = view.findViewById(R.id.profile_image_civ)
        var productNameTv: TextView = view.findViewById(R.id.product_name_tv)
        var amountTv: TextView = view.findViewById(R.id.amount_tv)
        var amountEditTv: TextView = view.findViewById(R.id.amount_edit_tv)
        var priceTv: TextView = view.findViewById(R.id.price_tv)
        var priceEditTv: TextView = view.findViewById(R.id.price_edit_tv)
        var arrowIv: ImageView = view.findViewById(R.id.arrow_iv)
        var ignoreFab: FloatingActionButton = view.findViewById(R.id.ignore_fab)
        var acceptFab: FloatingActionButton = view.findViewById(R.id.accept_fab)
        var ignoredFab: FloatingActionButton = view.findViewById(R.id.ignored_fab)
        var acceptedFab: FloatingActionButton = view.findViewById(R.id.accepted_fab)
        var waitingFab: FloatingActionButton = view.findViewById(R.id.waiting_fab)
        var descriptionTv: TextView = view.findViewById(R.id.description_tv)
        var statusSp: Spinner = view.findViewById(R.id.status_sp)
        var statusEt: TextView = view.findViewById(R.id.status_et)
        var deleteIv: ImageView = view.findViewById(R.id.delete_iv)


        var mItemClickListener: ItemClickListener? = itemClickListener


        override fun onClick(view: View) {
            if (mItemClickListener != null) {
                mItemClickListener!!.onItemClick(adapterPosition)
            }
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    interface ItemClickListener {
        fun onItemClick(position: Int)
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                ordersFilterList = if (charSearch == "") {
                    orders as ArrayList<Orders>
                } else {
                    val resultList = ArrayList<Orders>()
                    for (row in orders) {
                        if (row.title.toLowerCase().contains(constraint.toString().toLowerCase())) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = ordersFilterList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                ordersFilterList = results?.values as ArrayList<Orders>
                notifyDataSetChanged()
            }
        }
    }

}