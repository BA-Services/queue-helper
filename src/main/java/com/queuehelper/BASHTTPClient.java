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
	/* !!! All of the new import options are added below with a tab to identify them*/
	import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
	import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
	import software.amazon.awssdk.services.s3.S3Client;
	import software.amazon.awssdk.services.s3.model.PutObjectRequest;
	import software.amazon.awssdk.core.sync.RequestBody;
	import software.amazon.awssdk.regions.Region;
	import java.nio.charset.StandardCharsets;
	import java.time.LocalDateTime;
	import java.time.format.DateTimeFormatter;
	import java.io.IOException;
	import java.awt.image.BufferedImage;
	import java.io.IOException;
	import java.util.List;
	import net.runelite.api.FriendsChatRank;
	import net.runelite.api.events.ChatMessage;
	import net.runelite.client.ui.NavigationButton;
	import net.runelite.client.util.ImageUtil;
	import net.runelite.client.util.Text;
	import okhttp3.Call;
	import okhttp3.Callback;
	import okhttp3.HttpUrl;
	import okhttp3.OkHttpClient;
	import okhttp3.Request;
	import okhttp3.Response;
	import java.net.URLEncoder;

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

		private String CustIDQuery;
		private String RetrieveCSVQuery;
		private String UPDATE_OPTION_GNC;
		private String UPDATE_OPTION_ATQ;
		private String UPDATE_OPTION_PRI;
		private String UPDATE_OPTION_NAM;
		private String UPDATE_OPTION_FORMI;
		private String UPDATE_OPTION_QN;
		private String csvList;
		private String UPDATE_OPTION_QHN;
		private String UPDATE_OPTION_C;
		private String UPDATE_OPTION_M;
		private String UPDATE_OPTION_R;
		private String OptionQuery;
		private String CustomerNameQuery;
		private String basephp;



		private BASHTTPClient(String apikey, OkHttpClient basclient) throws IOException
		{
			this.Basclient = basclient;
			this.apikey = apikey;
			this.apiBase = new HttpUrl.Builder().scheme("https").host(BASHTTPClient.HOST_PATH).addPathSegment("Bas_Queuehelper").build();
			String[] pathsArray = this.getFilePaths();
			this.updateFilePaths(pathsArray);
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

			UPDATE_OPTION_GNC = "";

			UPDATE_OPTION_ATQ = "";

			UPDATE_OPTION_PRI = "";

			UPDATE_OPTION_NAM = "";

			UPDATE_OPTION_FORMI = "";

			UPDATE_OPTION_QN = "";

			csvList = "";

			UPDATE_OPTION_QHN = "";

			UPDATE_OPTION_C = "";

			UPDATE_OPTION_M = "";

			UPDATE_OPTION_R = "";

			OptionQuery = "";

			CustomerNameQuery = "";

			basephp = "";

			CustIDQuery = "";
		}

		private void updateFilePaths(String[] paths){
			this.RetrieveCSVQuery = paths[0];
			this.UPDATE_OPTION_GNC = paths[1];
			this.UPDATE_OPTION_ATQ = paths[2];
			this.UPDATE_OPTION_PRI = paths[3];
			this.UPDATE_OPTION_NAM = paths[4];
			this.UPDATE_OPTION_FORMI = paths[5];
			this.UPDATE_OPTION_QN = paths[6];
			this.csvList = paths[7];
			this.UPDATE_OPTION_QHN = paths[8];
			this.UPDATE_OPTION_C = paths[9];
			this.UPDATE_OPTION_M = paths[10];
			this.UPDATE_OPTION_R = paths[11];
			this.OptionQuery = paths[12];
			this.CustomerNameQuery = paths[13];
			this.basephp = paths[14];
			this.CustIDQuery = paths[15];
		}

		private String[] getFilePaths() throws IOException {

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

			try (Response response = client.newCall(request).execute())
			{
				return response.body().string().split("\"")[3].split(",");
			}
		}
		@Override
		public String getCustomerID(String name) throws IOException
		{
			OkHttpClient client = Basclient;
			HttpUrl url = apiBase.newBuilder()
					.addPathSegment("bas")
					.addPathSegment(basephp)
					.addQueryParameter(CustIDQuery, URLEncoder.encode(name.replace(' ', ' '),"UTF-8"))
					.build();

			Request request = new Request.Builder()
					.header("User-Agent", "RuneLite")
					.url(url)
					.header("Content-Type", "application/json")
					.header("x-api-key", this.apikey)
					.build();

			try (Response response = client.newCall(request).execute())
			{
				return response.body().string();
			}

		}
		@Override
		public boolean markCustomer(int option, String name) throws IOException
		{
			OkHttpClient client = Basclient;
			HttpUrl url = apiBase.newBuilder()
					.addPathSegment("bas")
					.addPathSegment(basephp)
					.addQueryParameter(OptionQuery, option + "")
					.addQueryParameter(CustomerNameQuery, URLEncoder.encode(Text.removeTags(Text.sanitize(name)).replace(' ', ' '),"UTF-8"))
					.build();

			Request request = new Request.Builder()
					.header("User-Agent", "RuneLite")
					.url(url)
					.header("Content-Type", "application/json")
					.header("x-api-key", this.apikey)
					.build();

			try (Response response = client.newCall(request).execute())
			{
				return response.isSuccessful();
			}

		}
		@Override
		public List<String[]> readCSV(List<String[]> csv) throws IOException{

			int PRIORITY = 2;
			int RNAMES = 4;
			int PNAME = 3;
			int STATUS = 0;
			int ID = 1;
			int ITEM = 5;
			int NOTES = 7;
			OkHttpClient client = Basclient;
			HttpUrl url = apiBase.newBuilder()
					.addPathSegment("bas")
					.addPathSegment(basephp)
					.addQueryParameter(RetrieveCSVQuery, "1")
					.build();

			Request request = new Request.Builder()
					.header("User-Agent", "RuneLite")
					.url(url)
					.header("Content-Type", "application/json")
					.header("x-api-key", this.apikey)
					.build();

			try (Response response = client.newCall(request).execute())
			{
				csv.clear();

				String[] CSVLines = response.body().string().split("\n");
				for(String line : CSVLines ){
					String[] LineItems = line.split(",");
					if(LineItems[0].equals("Current time:") || LineItems[0].equals("Last edited:") || LineItems[0].equals("Status")){
						continue;
					}
					else{
						try {
							csv.add(new String[]{LineItems[PRIORITY], LineItems[PRIORITY].equals("R") ? LineItems[RNAMES] : LineItems[PNAME], LineItems[STATUS], LineItems[ID], LineItems[ITEM], LineItems[NOTES]});
						} catch (Exception e) {
							//throw new RuntimeException(e);
						}
					}
				}
				response.close();
				return csv;
			}

		}

		@Override
		public NavigationButton getNavButton()
		{
			final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/panellogo.png");

			NavigationButton navButton = NavigationButton.builder()
					.tooltip("BAS queue + options")
					.icon(icon)
					.priority(2)
					//.panel(BasQueuePanel)
					.build();
			return navButton;
		}


		public boolean updateQueuebackend(StringBuilder urlList, String name) throws IOException {
			String bucketName = "rl2qdatatestcsv";
			String key = "rl2q received data/" + generateFileName(name); // Save in the correct folder

			// Convert the CSV content to a String
			String csvContent = urlList.toString();

			// Replace non-breaking spaces with regular spaces
			csvContent = csvContent.replace("\u00A0", " ");

			// Remove the #[number] parts if desired (e.g., "Gizzy#1" becomes "Gizzy")
			csvContent = csvContent.replaceAll("#-?\\d+", "");

			// Set your test credentials (only for testing; do not hardcode in production)

			//TODO Credentials go here
			try (S3Client s3 = S3Client.builder()
					.region(Region.EU_WEST_2)
					.credentialsProvider(StaticCredentialsProvider.create(testCredentials))
					.build()) {

				s3.putObject(
						PutObjectRequest.builder()
								.bucket(bucketName)
								.key(key)
								.contentType("text/csv; charset=utf-8")  // Set proper content type and charset
								.build(),
						RequestBody.fromString(csvContent, StandardCharsets.UTF_8)
				);

				System.out.println("File uploaded to S3: " + key);
				return true;
			} catch (Exception e) {
				System.err.println("S3 upload failed: " + e.getMessage());
				return false;
			}
		}

		private String generateFileName(String name) {
			return "queue.csv";
		}

		@Override
		public boolean addCustomer(String itemName, String priority, String custName, String addedBy) throws IOException
		{
			OkHttpClient client = Basclient;
			HttpUrl url = apiBase.newBuilder()
					.addPathSegment("bas")
					.addPathSegment(basephp)
					.addQueryParameter(UPDATE_OPTION_ATQ, "1")
					.addQueryParameter(UPDATE_OPTION_PRI, priority)
					.addQueryParameter(UPDATE_OPTION_NAM, URLEncoder.encode(custName.replace(' ', ' '), "UTF-8"))
					.addQueryParameter(UPDATE_OPTION_FORMI, itemName)
					.addQueryParameter(UPDATE_OPTION_QN, addedBy)
					.build();

			Request request = new Request.Builder()
					.header("User-Agent", "RuneLite")
					.url(url)
					.header("Content-Type", "application/json")
					.header("x-api-key", this.apikey)
					.build();

			try (Response response = client.newCall(request).execute())
			{
				return response.isSuccessful();
			}
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

	}
