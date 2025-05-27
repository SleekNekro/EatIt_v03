package com.github.SleekNekro.data.DAO

import com.github.SleekNekro.data.Users
import com.github.SleekNekro.data.clases.UserData
import com.github.SleekNekro.util.ConvertibleToDataClass
import com.github.SleekNekro.util.hashPassword
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

class UserDAO(id: EntityID<Long>) : LongEntity(id), ConvertibleToDataClass<UserData> {
    companion object : LongEntityClass<UserDAO>(Users) {
        fun createUser(username: String, email: String, password: String, profilePic: String?): UserDAO {
            return transaction {
                UserDAO.new {
                    this.username = username
                    this.email = email
                    this.password = password
                    this.profilePic = profilePic
                    this.followers = 0
                    this.createdAt = System.currentTimeMillis()
                }
            }
        }

            fun getAllUsers(): List<UserDAO> {
            return transaction {
                UserDAO.all().toList()
            }
        }
        fun getUserById(id: Long): UserDAO? {
            return transaction {
                UserDAO.findById(id)
            }
        }


        fun updateUser(id: Long, newUsername: String? = null, newEmail: String? = null, newPassword: String? = null, newProfilePic: String? = null): Boolean {
            return transaction {
                val user = UserDAO.findById(id) ?: return@transaction false
                newUsername?.let { user.username = it }
                newEmail?.let { user.email = it }
                newPassword?.let { user.password = it }
                newProfilePic?.let { user.profilePic = it }
                true
            }
        }
        fun getUserByEmail(email: String): UserDAO? {
            return transaction {
                UserDAO.find {
                    Users.email eq email
                }.singleOrNull()
            }
        }


        fun deleteUser(id: Long): Boolean {
            return transaction {
                val user = UserDAO.findById(id) ?: return@transaction false
                user.delete()
                true
            }
        }
    }

    var username by Users.username
    var email by Users.email
    var password by Users.password
    var profilePic by Users.profilePic
    var followers by Users.followers
    var createdAt by Users.createdAt

    override fun toDataClass(): UserData {
        return UserData(
            id = this.id.value,
            username = this.username,
            email = this.email,
            password = this.password,
            profilePic = this.profilePic,
            followers = this.followers,
            createdAt = this.createdAt
        )
    }
}
