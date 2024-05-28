package com.example.barberia

import android.os.StrictMode
import android.util.Log
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException

class ConnectSql {
    private val ip = "192.168.1.36"
    private val port = "1433"
    private val db = "Registro"
    private val username = "usr_juan"
    private val password = "juan-2004"

    fun dbConn(): Connection? {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        var conn: Connection? = null
        val connString: String
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance()
            connString = "jdbc:jtds:sqlserver://$ip:$port;databaseName=$db;user=$username;password=$password"
            conn = DriverManager.getConnection(connString)
            Log.d("DB Connection", "Connection successful")
        } catch (ex: SQLException) {
            Log.e("Error: ", ex.message!!)
        } catch (ex1: ClassNotFoundException) {
            Log.e("Error: ", ex1.message!!)
        } catch (ex2: Exception) {
            Log.e("Error: ", ex2.message!!)
        }
        return conn
    }
}

