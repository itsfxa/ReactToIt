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

class Main : JavaPlugin(), Listener {

    /*
    Plugin made by FXA
    Plugin started: 20/05/2017
    Plugin finished: 21/05/2017
    Last update: 21/05/2017
     */

    var econ: Economy? = null
    var react: String? = null
    var money: Int? = config.getInt("Money-To-Win")
    var startTime: Long? = null

    override fun onEnable() {
        super.onEnable()
        Bukkit.getPluginManager().registerEvents(this, this)
        getCommand("react").executor = this
        saveDefaultConfig()
        config.options().copyDefaults(true)
        // If vault is not installed, disable the plugin.
        if (!setupEconomy()) {
            println("Economy plugin not found. Plugin disabling...")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
    }

    @EventHandler
    fun onChat(e: AsyncPlayerChatEvent) {
        val p = e.player
        val msg = e.message

        if (msg == react) {
            // Declaring variables
            val secondsTaken = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) // The time taken in seconds.
            val secondsStartTime = TimeUnit.MILLISECONDS.toSeconds(startTime!!.toLong()) // The start time in seconds.
            val finalTime: Long = secondsTaken - secondsStartTime // The final time (reaction time)

            // Actually send messages and run things
            react = null // Set the react string to null.
            if (config.getBoolean("Message-Player")) {
                p.sendMessage(col(config.getString("Word-Correct"))) // Tell the player they got the word right.
            }
            econ!!.depositPlayer(p, config.getInt("Money-To-Win").toDouble()) // Deposit the money to the players bank.
            p.sendMessage(col(config.getString("Tell-New-Balance").replace("{balance}", econ!!.getBalance(p).toInt().toString()))) // Tell them their new balance.
            e.isCancelled = true // Cancel the message
            Bukkit.broadcastMessage(col(config.getString("Broadcast-Winner").replace("{player}", p.name).replace("{time}", finalTime.toString()))) // Broadcast the reaction time to players.
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("react.start")) {
            sender.sendMessage(col("&cNo permission."))
        }
        if (sender.hasPermission("react.start")) {
            makeString() // Make a random string to broadcast
            Bukkit.broadcastMessage(col(config.getString("Broadcast").replace("{string}", react.toString()).replace("{money}", money.toString()))) // Announce the string and price you get for winning
            startTime = System.currentTimeMillis() // Log the time the command was sent
            return true
        }
        return false
    }

    fun makeString() {
        react = RandomStringUtils.randomAlphanumeric(config.getInt("String-Length")) // Make a string with the length defined in the config
    }

    fun col(text: String): String {
        return ChatColor.translateAlternateColorCodes('&', text)
    }

    // Setup Vault economy
    private fun setupEconomy(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) {
            return false
        }
        val rsp = server.servicesManager.getRegistration(Economy::class.java)
        if (rsp === null) {
            return false
        }
        econ = rsp.provider
        return econ != null
    }

}