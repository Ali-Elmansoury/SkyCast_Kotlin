package com.ities45.skycast.ui.favorites.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ities45.skycast.databinding.ItemFavoriteBinding
import com.ities45.skycast.model.local.entity.FavoriteLocationEntity

class FavoritesListAdapter(
    private val onItemClick: (FavoriteLocationEntity) -> Unit,
    private val onDeleteClick: (FavoriteLocationEntity) -> Unit
) : ListAdapter<FavoriteLocationEntity, FavoritesListAdapter.ViewHolder>(DiffUtilCallback()) {

    class ViewHolder(val binding: ItemFavoriteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val favLoc = getItem(position)
        holder.binding.tvPlaceName.text = favLoc.name
        holder.binding.root.setOnClickListener {
            onItemClick(favLoc)
        }
        holder.binding.ivMore.setOnClickListener {
            onDeleteClick(favLoc)
        }
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<FavoriteLocationEntity>() {
        override fun areItemsTheSame(oldItem: FavoriteLocationEntity, newItem: FavoriteLocationEntity): Boolean {
            return oldItem.locationId == newItem.locationId
        }

        override fun areContentsTheSame(oldItem: FavoriteLocationEntity, newItem: FavoriteLocationEntity): Boolean {
            return oldItem == newItem
        }
    }
}