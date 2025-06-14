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
	import okhttp3.FormBody;
	import java.time.ZoneOffset;
	import java.time.ZonedDateTime;
	import java.time.format.DateTimeFormatter;
	import java.util.List;
	import java.util.ArrayList;
	import java.io.IOException;
	import net.runelite.api.events.ChatMessage;
	import okhttp3.Call;
	import okhttp3.Callback;
	import okhttp3.HttpUrl;
	import okhttp3.OkHttpClient;
	import okhttp3.Request;
	import okhttp3.Response;

	/**
	 This Class handles all IO communication to the backend
	 */

	public class BASHTTPClient implements QueueHelperHTTPClient
	{
		private static BASHTTPClient client;

		private String apikey;

		private static final String HOST_PATH = "vrqgs27251.execute-api.eu-west-2.amazonaws.com";

		private HttpUrl apiBase;

		private OkHttpClient Basclient;

		private List<String[]> csvData;
		private String RetrieveCSVQuery;




		private BASHTTPClient(String apikey, OkHttpClient basclient) throws IOException
		{
			this.Basclient = basclient;
			this.apikey = apikey;
			this.apiBase = new HttpUrl.Builder().scheme("https").host(BASHTTPClient.HOST_PATH).addPathSegment("Bas_Queuehelper").build();
			this.getFilePaths();
			//String[] pathsArray = this.getFilePaths();
			//this.updateFilePaths(pathsArray);
		}

		public static BASHTTPClient getInstance(String apikey,OkHttpClient basclient) throws IOException
		{
			if(BASHTTPClient.client == null){
				BASHTTPClient.client = new BASHTTPClient(apikey, basclient);
			}
			else{
				BASHTTPClient.client.setAPikey(apikey);
			}
			return BASHTTPClient.client;
		}

		public void setAPikey(String apikey)
		{
			this.apikey = apikey;
		}


		public void clearFilePaths(){
			RetrieveCSVQuery = "";

			csvData = null;

		}

		public void updateFilePaths(String[] paths){
			this.RetrieveCSVQuery = paths[0];

		}

		private void getFilePaths() throws IOException {

			OkHttpClient client = Basclient;
			HttpUrl url = apiBase.newBuilder()
					.addPathSegment("grabfilestrings")
					.build();

			Request request = new Request.Builder()
					.header("User-Agent", "RuneLite")
					.url(url)
					.header("Content-Type", "application/json")
					.header("x-api-key", this.apikey)
					.build();

			Response test;
			client.newCall(request).enqueue(new Callback() {
				@Override
				public void onFailure(Call call, IOException e) {

				}

				@Override
				public void onResponse(Call call, Response response) throws IOException {
					updateFilePaths(response.body().string().split("\"")[3].split(","));
				}
			});

		}

		@Override
		public boolean markCustomer(int option, String name,String rankName) throws IOException
		{
			OkHttpClient client = Basclient;
			HttpUrl url = apiBase.newBuilder()
					.addPathSegment("queue")
					.build();

			/*
				3 == online
				0 == cooldown
				2 == done
				1 == in progress
			}*/


			Request request = new Request.Builder()
					.header("User-Agent", "RuneLite")
					.url(url)
					.header("Content-Type", "application/json")
					.header("x-api-key", this.apikey)
					.header("username",name)
					.header("action", String.valueOf(option))
					.header("rankname", rankName)
					.build();

			client.newCall(request).enqueue(new Callback()
			{
				@Override
				public void onFailure(Call call, IOException e)
				{

				}

				@Override
				public void onResponse(Call call, Response response) throws IOException { String eat =(response.body().string()); }
			});
			return true;

		}

		public boolean updateQueuebackend(StringBuilder urlList, String name) throws IOException {
			OkHttpClient client = Basclient;
			HttpUrl url = apiBase.newBuilder()
					.addPathSegment("queuespecific")
					.build();

			Request request = new Request.Builder()
					.header("User-Agent", "RuneLite")
					.url(url)
					.header("Content-Type", "application/json")
					.header("x-api-key", this.apikey)
					.header("returncsv", "0")//treat 0/1 as true/false respectively
					.header("rankname", name)
					.header("csv", urlList.toString().replace("\u00A0", " "))
					.build();

			client.newCall(request).enqueue(new Callback()
			{
				@Override
				public void onFailure(Call call, IOException e)
				{

				}

				@Override
				public void onResponse(Call call, Response response) throws IOException { String eat = (response.body().string()+"\n"); }
			});
			return true;
		}

		@Override
		public List<String[]> readCSV(List<String[]> csv) throws IOException {
			OkHttpClient client = Basclient;
			HttpUrl url = apiBase.newBuilder()
					.addPathSegment("queuespecific")
					.build();

			Request request = new Request.Builder()
					.header("User-Agent", "RuneLite")
					.url(url)
					.header("Content-Type", "application/json")
					.header("x-api-key", this.apikey)
					.header("returncsv", "1")//treat 0/1 as true/false respectively
					.header("rankname", "get")
					.build();

			client.newCall(request).enqueue(new Callback()
			{
				@Override
				public void onFailure(Call call, IOException e)
				{

				}

				@Override
				public void onResponse(Call call, Response response) throws IOException {
					String data = response.body().string().split("body\": \"")[1].replace("}","");
					 csvData = new ArrayList<>();
					String[] lines = data.split("\\\\r\\\\n");

					for (String line : lines) {
						String[] values = line.split(",");
						csvData.add(values);
					}
					response.close();

				}
			});
			return csvData;//by way of how the enqueue/callback works this is always 1 refresh behind.... not the end of the world but annoying
		}


		@Override
		public boolean addCustomer(String itemName, String priority, String custName, String addedBy) throws IOException
		{
			OkHttpClient client = Basclient;

			// Build the URL for your Google Form submission.
			HttpUrl url = new HttpUrl.Builder()
					.scheme("https")
					.host("docs.google.com")
					.addPathSegment("forms")
					.addPathSegment("d")
					.addPathSegment("e")
					// Replace with your actual Google Form ID from the URL:
					// https://docs.google.com/forms/d/e/1FAIpQLSc06_IrTbleP0uZBiOt1yMcI5kvOrvkzgaVLLmEDRLqJSSoVg/viewform
					.addPathSegment("1FAIpQLSc06_IrTbleP0uZBiOt1yMcI5kvOrvkzgaVLLmEDRLqJSSoVg")
					.addPathSegment("formResponse")
					.build();

			// Build the form-encoded request body with the proper entry IDs.
			okhttp3.RequestBody formBody = new FormBody.Builder()
					.add("entry.1481518570", priority)
					.add("entry.1794472797", custName)
					.add("entry.1391010025", itemName)
					.add("entry.1284888696", addedBy)
					.add("entry.1260617128", RetrieveCSVQuery)
					.build();

			// Create a POST request to the Google Form.
			Request request = new Request.Builder()
					.url(url)
					.post(formBody)
					.header("User-Agent", "RuneLite")
					.build();

			client.newCall(request).enqueue(new Callback()
			{
				@Override
				public void onFailure(Call call, IOException e)
				{

				}

				@Override
				public void onResponse(Call call, Response response) throws IOException { String eat =(response.body().string()); }
			});
			return true;
		}

		@Override
		public boolean sendChatMsgDiscord(ChatMessage chatmessage) throws IOException
		{
			String unhashedMsg = chatmessage.getName() + chatmessage.getMessage() + (((int)(chatmessage.getTimestamp()/10)*10));

			int hasedMsg = unhashedMsg.hashCode();
			OkHttpClient client = Basclient;
			HttpUrl url = apiBase.newBuilder()
					.addPathSegment("disc")
					.build();

			Request request = new Request.Builder()
					.header("User-Agent", "RuneLite")
					.url(url)
					.header("Content-Type", "application/json")
					.header("x-api-key", this.apikey)
					.header("username",chatmessage.getName().replace(' ', ' '))
					.header("msg",chatmessage.getMessage())
					.header("hash",String.valueOf(hasedMsg))
					.build();

			client.newCall(request).enqueue(new Callback()
			{
				@Override
				public void onFailure(Call call, IOException e)
				{

				}

				@Override
				public void onResponse(Call call, Response response) throws IOException { response.close(); }
			});
			return true;
		}

		@Override
		public boolean sendRoundTimeServer(String main, String collector, String healer, String leech, String defender, int time, int premiumType, String item
				,int attpts, int defpts, int healpts, int collpts, int eggsCollected, int hpHealed, int wrongAtts, String leechrole) {
			ZonedDateTime currentTimeUTC = ZonedDateTime.now(ZoneOffset.UTC);
			int seconds = currentTimeUTC.getSecond();
			int roundedSeconds = (seconds / 30) * 30; // Round to the nearest 10 seconds for use in the hash/prevent multiple same as discord msgs
			ZonedDateTime roundedTime = currentTimeUTC.withSecond(roundedSeconds).withNano(0);
			String roundedTimestampUTC = roundedTime.format(DateTimeFormatter.ISO_DATE_TIME);

			String unhashedMsg = main + collector + healer + leech + defender + roundedTimestampUTC;

			int hasedMsg = unhashedMsg.hashCode();

			OkHttpClient client = Basclient;
			HttpUrl url = apiBase.newBuilder()
					.addPathSegment("recordRound")
					.build();


			Request request = new Request.Builder()
					.header("User-Agent", "RuneLite")
					.url(url)
					.header("Content-Type", "application/json")
					.header("x-api-key", this.apikey)
					.header("main",main)
					.header("collector",collector)
					.header("healer",healer)
					.header("leech",leech)
					.header("defender",defender)
					.header("time", String.valueOf(time))
					.header("premiumtype",String.valueOf(premiumType))
					.header("item", item)
					.header("hash",String.valueOf(hasedMsg))
					.header("attpts", String.valueOf(attpts))
					.header("defpts", String.valueOf(defpts))
					.header("healpts", String.valueOf(healpts))
					.header("collpts", String.valueOf(collpts))
					.header("eggscollected", String.valueOf(eggsCollected))
					.header("hphealed", String.valueOf(hpHealed))
					.header("wrongatts", String.valueOf(wrongAtts))

					.header("leechrole", String.valueOf(leechrole))
					.build();

			client.newCall(request).enqueue(new Callback()
			{
				@Override
				public void onFailure(Call call, IOException e)
				{

				}

				@Override
				public void onResponse(Call call, Response response) throws IOException { response.close(); }
			});
			return true;
		}





	}
