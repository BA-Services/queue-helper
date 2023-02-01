	/*
	 * Copyright (c) 2019, SkylerPIlot <https://github.com/SkylerPIlot>
	 * All rights reserved.
	 *
	 * Redistribution and use in source and binary forms, with or without
	 * modification, are permitted provided that the following conditions are met:
	 *
	 * 1. Redistributions of source code must retain the above copyright notice, this
	 *    list of conditions and the following disclaimer.
	 * 2. Redistributions in binary form must reproduce the above copyright notice,
	 *    this list of conditions and the following disclaimer in the documentation
	 *    and/or other materials provided with the distribution.
	 *
	 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
	 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
	 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
	 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
	 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
	 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
	 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
	 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
	 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
	 */
package com.queuehelper;


import com.google.inject.Provides;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.Text;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import java.io.IOException;

@PluginDescriptor(name = "BAS Queue Helper", description = "BAS Customer CC Info", tags = {"minigame"})
public class BASPlugin extends Plugin implements ActionListener
{
    private static final Logger log = LoggerFactory.getLogger(BASPlugin.class);

    private static final String ccName = "Ba Services";

    private static final String errorMsg = (new ChatMessageBuilder()).append(ChatColorType.NORMAL).append("BAS QH: ").append(ChatColorType.HIGHLIGHT).append("Please Paste the API key in the plugin settings and restart the plugin").build();

	private BasQueuePanel basQueuePanel;
	private NavigationButton navButton;

	private BASHTTPClient httpclient;

    @Inject
    private Client client;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    private BASConfig config;

	@Inject
	private OkHttpClient BasHttpClient;

	@Inject
	private ClientToolbar clientToolbar;

	public BASPlugin() throws IOException
	{
	}

	@Provides
    BASConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(BASConfig.class);
    }

	private Queue queue;

    protected void startUp() throws Exception
    {
    	if(!isConfigApiEmpty())
		{
			this.basQueuePanel = new BasQueuePanel(this, this.config);
			this.queue = Queue.getInstance(config.apikey(), basQueuePanel, this);
			navButton = queue.getNav();
			navButton.setPanel(basQueuePanel);
			clientToolbar.addNavigation(navButton);
			SwingUtilities.invokeLater(() -> basQueuePanel.populate(queue.getQueue()));
		}
    }

    protected void shutDown() throws Exception
    {
		clientToolbar.removeNavigation(navButton);
		this.queue = null;
		httpclient = null;

    }


	//checks on startup of plugin and throws an error
    private boolean isConfigApiEmpty(){

        if(config.apikey().equals("Paste your key here") || config.apikey().equals("")){

            BASPlugin.this.chatMessageManager.queue(QueuedMessage.builder()
                    .type(ChatMessageType.CONSOLE)
                    .runeLiteFormattedMessage(errorMsg)
                    .build());
            return true;
        }
        return false;

    }


    @Subscribe
    public void onFriendsChatMemberJoined(FriendsChatMemberJoined event) throws IOException
	{
		this.queue.ShouldUpdate(true);
    }


    @Subscribe
    public void onFriendsChatMemberLeft(FriendsChatMemberLeft event) throws IOException
	{
		this.queue.ShouldUpdate(true);
    }


	//used in sending discord webhook messages
    private boolean isRank()
    {
        FriendsChatManager clanMemberManager = this.client.getFriendsChatManager();
        return this.client.getLocalPlayer().getName() != null && clanMemberManager != null && clanMemberManager.getCount() >= 1 && clanMemberManager.getOwner().equals(ccName);
    }

	//builds a stringbuilder that is then passed to the Implementation of BASHTTPClient to call the backend
    public void updateQueue() throws IOException
	{
        FriendsChatManager clanMemberManager = this.client.getFriendsChatManager();
        if (!this.config.autoUpdateQueue() || clanMemberManager == null)
            return;
        StringBuilder csv = new StringBuilder();
        for (FriendsChatMember member : (FriendsChatMember[]) clanMemberManager.getMembers())
        {
            String memberName = member.getName();
            if (csv.toString().equals(""))
            {
                csv = new StringBuilder(memberName + "#" + member.getRank().getValue());
            } else
            {
                csv.append(",").append(memberName).append("#").append(member.getRank().getValue());
            }
        }
        if (csv.toString().equals(""))
            return;
        if (isConfigApiEmpty()){
            return;
        }
        String name = config.queueName();
        if(client.getLocalPlayer().getName() != null){
			name = Text.sanitize(client.getLocalPlayer().getName());
		}

        queue.updateQueuebackend(csv, name);
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage)
    {

    }


	@Override
	public void actionPerformed(ActionEvent e)
	{
		//required, didn't feel like using instead used specific functions
	}

	//used in BasQueueRow to run the "Next" button
	public void getNext(){
    	Customer next = queue.getNext();
		String chatMessage = (new ChatMessageBuilder()).append(ChatColorType.NORMAL).append("Next customer in line: ").append(ChatColorType.HIGHLIGHT).append("Priority: " + next.getPriority() + " " + next.getName() + " " + next.getItem() + " " + next.getNotes()).build();
		BASPlugin.this.chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(chatMessage)
			.build());
	}

	//TODO implement this to send all msgs instead of sometimes building my own and sometimes not
	public void sendChat(String msg){
		String chatMessage = (new ChatMessageBuilder()).append(ChatColorType.NORMAL).append(msg).build();
    	BASPlugin.this.chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(chatMessage)
			.build());
	}

	//Adds a customer to the queue and is called by the addcustomer button in the basQueuePanel
	public void addToQueue(String name, String item, String priority){
    	if(name.equals("Customer")){
    		sendChat("Please enter a name");
    		return;
		}
    	if(queue.addToQueue(item, priority, name, config.queueName())){
			String chatMessage = (new ChatMessageBuilder()).append(ChatColorType.NORMAL).append("Added: ").append(ChatColorType.HIGHLIGHT).append(name + " for " + priority + " " + item).build();
			BASPlugin.this.chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.CONSOLE)
				.runeLiteFormattedMessage(chatMessage)
				.build());
		}
    	else{
			String chatMessage = (new ChatMessageBuilder()).append(ChatColorType.NORMAL).append("Failed to add: ").append(ChatColorType.HIGHLIGHT).append(name + " for " + priority + " " + item).build();
			BASPlugin.this.chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.CONSOLE)
				.runeLiteFormattedMessage(chatMessage)
				.build());
		}
		refreshQueue();

	}
	//used in BasQueueRow to run the "Refresh" button
	public void refreshQueue()
	{
		//creates a list of online nonranks to update a autocomplete function
		FriendsChatManager clanMemberManager = this.client.getFriendsChatManager();
		FriendsChatMember[] memberlist = clanMemberManager.getMembers();
		ArrayList<String> keywords = new ArrayList<>();
		for(FriendsChatMember member: memberlist){
			if(member.getRank() == FriendsChatRank.UNRANKED && !queue.doesCustExist(member.getName())){
				keywords.add(member.getName());
			}
		}
		basQueuePanel.setAutoCompleteKeyWords(keywords);

		try
		{
			queue.refresh();
		}
		catch (IOException ioException)
		{
			ioException.printStackTrace();
		}
		SwingUtilities.invokeLater(() -> basQueuePanel.populate(queue.getQueue()));
	}
	//used in BasQueueRow to run the right click options
	public void markCustomer(int option, Customer cust)
	{
		int UNSUPORRTED = 0;
		if(option != UNSUPORRTED)
		{
			try
			{
				queue.mark(option, cust);
			}
			catch (IOException ioException)
			{
				ioException.printStackTrace();
			}
		}
		this.refreshQueue();
		SwingUtilities.invokeLater(() -> basQueuePanel.populate(queue.getQueue()));
	}

}
