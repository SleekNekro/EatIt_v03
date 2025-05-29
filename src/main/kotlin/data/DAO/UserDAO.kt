package com.github.SleekNekro.data.DAO


import com.github.SleekNekro.data.Followers
import com.github.SleekNekro.data.Users
import com.github.SleekNekro.data.clases.UserData
import com.github.SleekNekro.util.ConvertibleToDataClass
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select

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

        fun followUser(userId: Long, followerId: Long): Boolean {
            return transaction {
                // Primero verificamos que ambos usuarios existen
                val userExists = Users.selectAll().where { Users.id eq userId }.count() > 0
                val followerExists = Users.selectAll().where { Users.id eq followerId }.count() > 0

                if (!userExists || !followerExists) {
                    return@transaction false
                }

                // Verificamos que no exista ya la relación de seguimiento
                val alreadyFollowing = Followers.selectAll().where {
                    (Followers.userId eq userId) and (Followers.followerId eq followerId)
                }.count() > 0

                if (alreadyFollowing) {
                    return@transaction false
                }

                try {
                    Followers.insert {
                        it[Followers.userId] = userId
                        it[Followers.followerId] = followerId
                    }
                    true
                } catch (e: Exception) {
                    false
                }
            }
        }

        fun unfollowUser(userId: Long, followerId: Long): Boolean {
            return transaction {
                val deletedRows = Followers.deleteWhere {
                    (Followers.userId eq userId) and (Followers.followerId eq followerId)
                }
                deletedRows > 0
            }
        }

        fun getFollowers(userId: Long): List<ResultRow> {
            return transaction {
                (Users innerJoin Followers)
                    .select((Followers.userId eq userId))
                    .toList()
            }
        }

        fun getFollowing(userId: Long): List<ResultRow> {
            return transaction {
                (Users innerJoin Followers)
                    .select((Users.id eq Followers.followerId) and (Followers.userId eq userId))
                    .toList()
            }
        }

        fun isFollowing(userId: Long, followerId: Long): Boolean {
            return transaction {
                Followers.selectAll().where {
                    (Followers.userId eq userId) and (Followers.followerId eq followerId)
                }.count() > 0
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