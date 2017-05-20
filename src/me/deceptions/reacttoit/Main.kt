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

class Main : JavaPlugin(), Listener {

    /*
    TODO:
    Nothing.
     */

    var econ: Economy? = null
    var react: String? = null
    var money: Int? = config.getInt("Money-to-win")

    override fun onEnable() {
        super.onEnable()
        Bukkit.getPluginManager().registerEvents(this, this)
        getCommand("react").executor = this
        saveConfig()
        config.options().copyDefaults(true)
        if (!setupEconomy()) {
            println("Vault not found.")
            return
        }
    }

    @EventHandler
    fun onChat(e: AsyncPlayerChatEvent) {
        val p = e.player
        val msg = e.message

        // if player got it right
        if (msg == react) {
            p.sendMessage(col("&7Congratulations, you got the word right!"))
            react = null
            econ!!.depositPlayer(p, config.getInt("Money-to-win").toDouble())
            p.sendMessage(col("&7You now have: &a$${econ!!.getBalance(p).toInt()}"))
            e.isCancelled = true
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        makeString()
        Bukkit.broadcastMessage(col("&7First person to type &c$react &7wins &a$$money"))
        return false
    }

    fun makeString() {
        react = RandomStringUtils.randomAlphanumeric(8)
    }

    fun col(text: String): String {
        return ChatColor.translateAlternateColorCodes('&', text)
    }

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