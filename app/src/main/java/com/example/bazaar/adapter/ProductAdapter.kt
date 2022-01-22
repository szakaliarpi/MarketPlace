package com.example.bazaar.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.bazaar.MyApplication
import com.example.bazaar.R
import com.example.bazaar.api.model.ProductResponse
import com.example.bazaar.api.model.User
import com.example.bazaar.manager.SharedPreferencesManager
import de.hdodenhof.circleimageview.CircleImageView


class ProductAdapter(
        private val view: View,
        private val mItemClickListener: ItemClickListener,
        private var products: MutableList<ProductResponse>,
) : RecyclerView.Adapter<ProductAdapter.DataViewHolder>(), Filterable {

    var productsFilterList = ArrayList<ProductResponse>()

    init {
        productsFilterList = products as ArrayList<ProductResponse>
    }

    fun getItemData(position: Int): ProductResponse {
        return productsFilterList[position]
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DataViewHolder {
        val itemView =
                LayoutInflater.from(viewGroup.context).inflate(R.layout.product_item, viewGroup, false)
        return DataViewHolder(itemView, mItemClickListener)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {

        val productPricePerQuantityStr = productsFilterList[position].price_per_unit.toString() + " " + productsFilterList[position].price_type.toString() + "/" + productsFilterList[position].amount_type.toString()
        holder.productPricePerQuantityTv.text = productPricePerQuantityStr

        holder.productNameTv.text = productsFilterList[position].title
        holder.profileNameTv.text = productsFilterList[position].username

        if (productsFilterList[position].username == MyApplication.sharedPreferences.getUserValue(SharedPreferencesManager.KEY_USER, User()).username) {
            // for owner
            holder.orderNowBtn?.visibility = View.GONE
            holder.aiLl?.visibility = View.VISIBLE

            if (productsFilterList[position].is_active) {
                holder.checkIv?.setImageResource(R.drawable.ic_checkmark)
                holder.aiTv?.text = "Active"
                holder.aiTv?.setTextColor(Color.parseColor("#00B5C0"))
            } else {
                holder.checkIv?.setImageResource(R.drawable.ic_inactive)
                holder.aiTv?.text = "Inactive"
                holder.aiTv?.setTextColor(Color.parseColor("#9A9A9A"))
            }
        } else {
            // for user
            holder.orderNowBtn?.visibility = View.VISIBLE
            holder.aiLl?.visibility = View.GONE
        }


        holder.orderNowBtn?.setOnClickListener {

            val bundle = Bundle()
            bundle.putParcelable("productResponse", productsFilterList[position])
            view.findNavController().navigate(R.id.productDetailFragment, bundle)
        }

        holder.profileImageCiV.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("username", holder.profileNameTv.text.toString())
            view.findNavController().navigate(R.id.settingsFragment, bundle)
        }

    }

    override fun getItemCount(): Int {
        return productsFilterList.size
    }

    class DataViewHolder(view: View, itemClickListener: ItemClickListener?) :
            RecyclerView.ViewHolder(view), View.OnClickListener {

        var productImageCiV: CircleImageView = view.findViewById(R.id.product_image_civ)
        var profileImageCiV: CircleImageView = view.findViewById(R.id.profile_image_civ)
        var productPricePerQuantityTv: TextView = view.findViewById(R.id.product_price_per_quantity_tv)
        var profileNameTv: TextView = view.findViewById(R.id.profile_name_tv)
        var productNameTv: TextView = view.findViewById(R.id.product_name_tv)
        var orderNowBtn: AppCompatButton? = view.findViewById(R.id.order_now_btn)
        var checkIv: ImageView? = view.findViewById(R.id.check_iv)
        var aiTv: TextView? = view.findViewById(R.id.ai_tv)
        var aiLl: LinearLayout? = view.findViewById(R.id.ai_ll)

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
                productsFilterList = if (charSearch == "") {
                    products as ArrayList<ProductResponse>
                } else {
                    val resultList = ArrayList<ProductResponse>()
                    for (row in products) {
                        if (row.title.toLowerCase().contains(constraint.toString().toLowerCase())) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = productsFilterList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                productsFilterList = results?.values as ArrayList<ProductResponse>
                notifyDataSetChanged()
            }
        }
    }

}