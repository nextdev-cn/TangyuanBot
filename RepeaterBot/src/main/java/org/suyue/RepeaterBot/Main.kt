package org.suyue.RepeaterBot

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChain
import org.suyue.bot.ModConfig
import org.suyue.bot.Perms
import org.suyue.bot.SuYueBotMod

class Main : SuYueBotMod {
    private val config : ModConfig = ModConfig("RepeaterBot")
    private val rules : LinkedHashMap<Long,Int> = linkedMapOf()
    private val counter : LinkedHashMap<Long,Int> = linkedMapOf()
    private val messageTemp : LinkedHashMap<Long,MessageChain> = linkedMapOf()
    constructor(){
        readConfig()
    }
    private fun readConfig(){
        rules.clear()
        var a : List<String> = listOf()
        val jsonObject : JSONObject = JSON.parseObject(config.readConfig())
        val jsonArray: JSONArray = jsonObject.getJSONArray("groups")
        for (i in jsonArray.indices) {
            val groupJson = jsonArray.getJSONObject(i)
            val groupId = groupJson.getLong("groupId")
            val triggerTimes = groupJson.getInteger("triggerTimes")
            rules[groupId] = triggerTimes
        }
    }
    private fun saveConfig(){
        val jsonObject = JSONObject()
        val jsonArray = JSONArray()
        for(i in rules.entries){
            val groupJson = JSONObject().apply {
                put("groupId",i.key)
                put("triggerTimes",i.value)
            }
            jsonArray.add(groupJson)
        }
        jsonObject["groups"] = jsonArray
        config.saveConfig(jsonObject.toJSONString())
    }
    override fun receiveGroupMessage(event: GroupMessageEvent?) {
        if(event == null)
            return
//        val messageChain = event.message
//        for(i in messageChain.indices){
//            print("消息段落$i，段落类型${messageChain[i].javaClass}，段落转字符串${messageChain[i].contentToString()}")
//        }
        if(Perms.isAdministrator(event.sender.id)){
            var message = event.message.contentToString()
            if(message.startsWith("启动复读")){
                runBlocking {
                    if(rules.containsKey(event.group.id))
                        event.group.sendMessage(At(event.sender).plus("本群已启动复读姬，无需重复操作"))
                    else {
                        val list = message.split(' ')
                        val times = list[1].toIntOrNull() ?: 3
                        rules[event.group.id] = times
                        saveConfig()
                        event.group.sendMessage(At(event.sender).plus("已启动复读姬，复读触发次数为:$times"))
                    }
                }
            }else if(message.startsWith("关闭复读")){
                runBlocking {
                    if(rules.containsKey(event.group.id)){
                        rules.remove(event.group.id)
                        saveConfig()
                        event.group.sendMessage(At(event.sender).plus("已关闭复读姬"))
                    }else event.group.sendMessage(At(event.sender).plus("本群未启动复读姬，无法关闭"))
                }
            }
        }
        if(!rules.contains(event.group.id))
            return
        if(!messageTemp.contains(event.group.id)||!compareMessage(messageTemp[event.group.id], event.message)){
            messageTemp[event.group.id] = event.message
            counter[event.group.id] = 1
        }
        else if(counter.getOrDefault(event.group.id,0).compareTo(rules.getOrDefault(event.group.id,0)-1) == 0)
            runBlocking { event.group.sendMessage(event.message) }
        else counter[event.group.id] = counter.getOrDefault(event.group.id,0)+1
    }
    private fun compareMessage(message1 : MessageChain?, message2 : MessageChain?) : Boolean {
        if(message1 == null || message2 == null)
            return false
        if(message1.indices != message2.indices)
            return false
        for(i in message1.indices.drop(1)){
            if(message1[i] != message2[i])
                return false
        }
        return true
    }
}