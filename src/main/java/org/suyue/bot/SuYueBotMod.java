package org.suyue.bot;


import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;

public interface SuYueBotMod {
    //void reloadMods();
    default void receiveFriendMessage(FriendMessageEvent event){}
    default void receiveGroupMessage(GroupMessageEvent event){}
    default void receiveMessage(MessageEvent event){}
    default void unloadMod(){}
}
