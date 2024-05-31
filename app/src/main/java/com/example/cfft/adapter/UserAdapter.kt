package com.example.cfft.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cfft.CircleTransform
import com.example.cfft.R
import com.squareup.picasso.Picasso
import java.util.ArrayList


data class UserProfile1(
    val userId: Int,
    val username: String,
    val level: Int,
    val gender: String?,
    val bio: String?,
    val userImage: String
)
class UserAdapter(private val context: Context, private val userList: ArrayList<UserProfile1>) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentUser = userList[position]

        // 设置用户头像
        // 这里使用 Picasso 或 Glide 加载用户头像
         Picasso.get().load(currentUser.userImage).transform(CircleTransform()).into(holder.userAvatarImageView)
        // 或者：Glide.with(context).load(currentUser.userImage).into(holder.userAvatarImageView)

        // 设置用户名
        holder.userNameTextView.text = currentUser.username

        // 设置用户等级
        holder.userLevelTextView.text = "Level: ${currentUser.level}"

        // 设置用户性别
        holder.userGenderTextView.text = "Gender: ${currentUser.gender ?: "Unknown"}"

        // 设置用户简介
        holder.userBioTextView.text = "Bio: ${currentUser.bio ?: "No bio available"}"
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userAvatarImageView: ImageView = itemView.findViewById(R.id.userAvatarImageView)
        val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        val userLevelTextView: TextView = itemView.findViewById(R.id.userLevelTextView)
        val userGenderTextView: TextView = itemView.findViewById(R.id.userGenderTextView)
        val userBioTextView: TextView = itemView.findViewById(R.id.userBioTextView)
    }
}
