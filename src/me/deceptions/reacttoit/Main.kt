package me.deceptions.reacttoit

import net.milkbowl.vault.economy.Economy
import org.apache.commons.lang.RandomStringUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.TimeUnit

data class React(val text: String, val startTime: Long)

class Main : JavaPlugin(), Listener {

    /*
    Plugin made by FXA
    Plugin started: 20/05/2017
    Plugin finished: 21/05/2017
    Last update: 25/05/2017

    Contributors:
        - okkero
     */

    private lateinit var econ: Economy
    private val money: Int = config.getInt("Money-To-Win")

    private var react: React? = null

    override fun onEnable() {
        super.onEnable()
        Bukkit.getPluginManager().registerEvents(this, this)
        getCommand("react").executor = this
        saveDefaultConfig()
        config.options().copyDefaults(true)

        // If vault is not installed, disable the plugin.
        econ = setupEconomy() ?: run {
            println("Economy plugin not found, disabling..")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
    }

    @EventHandler
    fun onChat(e: AsyncPlayerChatEvent) {
        react?.let {
            if (e.message != it.text) {
                return
            }

            // Declaring variables
            val p = e.player
            val secondsTaken = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) // The time taken in seconds.
            val secondsStartTime = TimeUnit.MILLISECONDS.toSeconds(it.startTime) // The start time in seconds.
            val finalTime: Long = secondsTaken - secondsStartTime // The final time (reaction time)

            // Actually send messages and run things
            react = null // Set the react event to null.
            if (config.getBoolean("Message-Player")) {
                p.sendMessage(col(config.getString("Word-Correct"))) // Tell the player they got the word right.
            }
            econ.depositPlayer(p, config.getInt("Money-To-Win").toDouble()) // Deposit the money to the players bank.
            p.sendMessage(col(config.getString("Tell-New-Balance").replace("{balance}", econ.getBalance(p).toInt().toString()))) // Tell them their new balance.
            e.isCancelled = true // Cancel the message
            Bukkit.broadcastMessage(col(config.getString("Broadcast-Winner").replace("{player}", p.name).replace("{time}", finalTime.toString()))) // Broadcast the reaction time to players.
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("react.start")) {
            sender.sendMessage(col("&cNo permission."))
        }
        if (sender.hasPermission("react.start")) {
            val str = makeString() // Make a random string to broadcast
            val startTime = System.currentTimeMillis() // The time the command was sent

            react = React(str, startTime)
            Bukkit.broadcastMessage(col(config.getString("Broadcast").replace("{string}", react.toString()).replace("{money}", money.toString()))) // Announce the string and price you get for winning
            return true
        }
        return false
    }

    fun makeString(): String {
        return RandomStringUtils.randomAlphanumeric(config.getInt("String-Length")) // Make a string with the length defined in the config
    }

    fun col(text: String): String {
        return ChatColor.translateAlternateColorCodes('&', text)
    }

    private fun setupEconomy(): Economy? {
        if (server.pluginManager.getPlugin("Vault") == null) {
            return null
        }

        return server.servicesManager.getRegistration(Economy::class.java)?.provider
    }

}