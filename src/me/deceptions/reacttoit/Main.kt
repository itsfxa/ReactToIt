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
    TODO:
    Config shit ugh end me
     */

    var econ: Economy? = null
    var react: String? = null
    var money: Int? = config.getInt("Money-to-win")
    var startTime: Long? = null

    override fun onEnable() {
        super.onEnable()
        Bukkit.getPluginManager().registerEvents(this, this)
        getCommand("react").executor = this
        saveConfig()
        config.options().copyDefaults(true)
        // If vault is not installed, disable the plugin.
        if (!setupEconomy()) {
            println("Vault not found. Plugin disabling...")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
    }

    @EventHandler
    fun onChat(e: AsyncPlayerChatEvent) {
        val p = e.player
        val msg = e.message

        /* if player got it right */
        if (msg === react) {
            /* Declaring variables */
            val secondsTaken = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) // The time taken in seconds.
            val secondsStartTime = TimeUnit.MILLISECONDS.toSeconds(startTime!!.toLong()) // The start time in seconds.
            val finalTime: Long? = secondsTaken - secondsStartTime // The final time (reaction time)

            /* Actually send messages and run things */
            react = null // Set the react string to null.
            p.sendMessage(col("&7Congratulations, you got the word right!")) // Tell the player they got the word right.
            econ!!.depositPlayer(p, config.getInt("Money-to-win").toDouble()) // Deposit the money to the players bank.
            p.sendMessage(col("&7You now have: &a$${econ!!.getBalance(p).toInt()}")) // Tell them their new balance.
            e.isCancelled = true // Cancel the message
            Bukkit.broadcastMessage(col("&c${p.name} &7reacted in &c$finalTime &7seconds!")) // Broadcast the reaction time to players.
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        makeString() // Make a random string to broadcast
        Bukkit.broadcastMessage(col("&7First person to type &c$react &7wins &a$$money")) // Announce the string and price you get for winning
        startTime = System.currentTimeMillis() // Log the time the command was sent
        return false
    }

    fun makeString() {
        react = RandomStringUtils.randomAlphanumeric(8) // Make a random 8 char string.
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