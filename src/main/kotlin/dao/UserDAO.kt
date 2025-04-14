package com.github.SleekNekro.dao

import com.github.SleekNekro.model.Role
import com.github.SleekNekro.model.UserData
import com.github.SleekNekro.util.ConvertibleToDataClass
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object UserTable : IntIdTable("users") {
    val name = varchar("name", 100)
    val email = varchar("email", 100)
    val password = varchar("password", 100)
    val role = varchar("role", length = 50).default("user")
}

class UserDAO(id: EntityID<Int>) : IntEntity(id), ConvertibleToDataClass<UserData> {
    companion object : IntEntityClass<UserDAO>(UserTable) {
        fun createUser(
            name: String,
            email: String,
            passw: String
        ): UserDAO {
            return transaction { UserDAO.new {
                this.name = name
                this.email= email
                this.password = passw
            } }
        }
        fun registerUser(user: UserData): UserData {
            val generatedId = transaction {
                UserTable.insert {
                    it[name] = user.name
                    it[email] = user.email
                    it[password] = user.password
                } get UserTable.id
            }

            return user.copy(id = generatedId.value)
        }

        fun getAllUsers(): List<UserDAO> {
            return transaction { UserDAO.all().toList() }
        }

        fun getUserById(id: Int): UserDAO? {
            return transaction { UserDAO.findById(id) }
        }

        fun getUserByEmail(email: String): UserDAO? {
            return transaction {
                UserDAO.find {
                    UserTable.email eq email
                }.singleOrNull()
            }
        }
        fun getUserByUsername(username: String): UserDAO? {
            return transaction {
                UserDAO.find{
                    UserTable.name eq username
                }.singleOrNull()
            }
        }

        fun updateUser(
            id: Int,
            newName: String? = null,
            newEmail: String? = null,
            newPassw: String? = null
        ): Boolean {
            return transaction {
                val user = UserDAO.findById(id) ?: return@transaction false
                newName?.let { user.name = newName }
                newEmail?.let { user.email = newEmail }
                newPassw?.let { user.password = newPassw }
                true
            }
        }

        fun deleteUser(id: Int): Boolean {
            return transaction {
                val user = UserDAO.findById(id) ?: return@transaction false
                user.delete()
                true
            }
        }
    }

    var name by UserTable.name
    var email by UserTable.email
    var password by UserTable.password
    var role by UserTable.role

    override fun toDataClass(): UserData {
        return UserData(
            id = this.id.value,
            name = this.name,
            email = this.email,
            password = this.password,
            role = Role.valueOf(this.role.uppercase())
        )
    }
}
