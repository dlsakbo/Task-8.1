package com.example.chatbot;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class ChatActivity extends AppCompatActivity {

    // UI elements for input and sending messages
    EditText messageInputEditText;
    ImageView sendButton;

    // Variables to hold the user's name and a list of messages
    String username;
    List<String[]> chatMessages;

    // RecyclerView for displaying the chat messages
    RecyclerView chatRecyclerView;

    // JSON array to store the chat history
    JSONArray chatHistoryArray;

    // Adapter for managing the chat messages in the RecyclerView
    ChatMessageAdapter chatMessageAdapter;

    // Base URL of the server
    private static final String BASE_URL = "http://10.0.2.2:5000/";

    // Retrofit interface for sending chat messages
    interface ChatApiService {
        @POST("chat")
        Call<JsonObject> postMessage(@Body JsonObject payload);
    }

    // Retrofit service instance
    private ChatApiService chatApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_window);

        // Initializing UI elements
        messageInputEditText = findViewById(R.id.et_message_input);
        messageInputEditText.setInputType(InputType.TYPE_NULL);
        sendButton = findViewById(R.id.button_send);
        chatRecyclerView = findViewById(R.id.rv_chat);

        // Get the user's name from the intent
        username = getIntent().getStringExtra("name");

        // Initialize chat history and message list
        chatHistoryArray = new JSONArray();
        chatMessages = new ArrayList<>();

        // Set up the RecyclerView with a linear layout manager and the message adapter
        chatMessageAdapter = new ChatMessageAdapter(chatMessages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatMessageAdapter);

        // Set up the HTTP logging interceptor for debugging
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Build the OkHttpClient with the logging interceptor
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .readTimeout(10, TimeUnit.MINUTES)
                .build();

        // Build the Retrofit instance with the Gson converter
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        // Create the chat service using Retrofit
        chatApiService = retrofit.create(ChatApiService.class);

        // Set the send button click listener
        sendButton.setOnClickListener(v -> {
            String userMessage = messageInputEditText.getText().toString();
            String[] newMessage = new String[]{userMessage, "user"};
            chatMessageAdapter.addMessage(newMessage); // Add the user's message to the message list
            messageInputEditText.setText(""); // Clear the input field
            JSONObject payload = createMessagePayload(userMessage); // Create the JSON payload
            postChatMessage(ChatActivity.this, payload, userMessage); // Send the request to the server
        });
    }

    // Method to create the JSON payload for the request
    private JSONObject createMessagePayload(String userMessage) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("userMessage", userMessage);
            payload.put("chatHistory", chatHistoryArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return payload;
    }

    // Method to send the request using Retrofit
    private void postChatMessage(Context context, JSONObject payload, String userMessage) {
        // Convert the JSONObject to a JsonObject
        JsonObject gsonPayload = new Gson().fromJson(payload.toString(), JsonObject.class);
        Call<JsonObject> call = chatApiService.postMessage(gsonPayload);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject responseBody = response.body();
                        String botResponse = responseBody.get("message").getAsString();
                        Log.d("ChatActivity", "botResponse: " + botResponse);
                        JSONObject chatHistoryItem = new JSONObject();
                        chatHistoryItem.put("User", userMessage);
                        chatHistoryItem.put("Bot", botResponse);
                        chatHistoryArray.put(chatHistoryItem);
                        String[] newMessage = new String[]{botResponse, "bot"};
                        chatMessageAdapter.addMessage(newMessage); // Add the bot's response to the message list
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("ChatActivity", "Response unsuccessful: " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("ChatActivity", "Request failed: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    // Adapter class for managing the chat messages in the RecyclerView
    private class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {

        List<String[]> messages;

        public ChatMessageAdapter(List<String[]> messages) {
            this.messages = messages;
        }

        @NonNull
        @Override
        public ChatMessageAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
            return new ChatMessageAdapter.MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatMessageAdapter.MessageViewHolder holder, int position) {
            String[] message = messages.get(position);
            holder.bind(message);
        }

        public void addMessage(String[] newMessage) {
            messages.add(newMessage);
            notifyItemInserted(messages.size() - 1);
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        public class MessageViewHolder extends RecyclerView.ViewHolder {
            TextView usernameTextView, messageTextView;
            ImageView botImageView;
            CardView messageCardView;

            public MessageViewHolder(@NonNull View itemView) {
                super(itemView);
                botImageView = itemView.findViewById(R.id.img_ai);
                usernameTextView = itemView.findViewById(R.id.tv_user_name);
                messageTextView = itemView.findViewById(R.id.tv_message);
                messageCardView = itemView.findViewById(R.id.cv_text);
            }

            // Method to bind the message data to the UI elements
            public void bind(String[] message) {
                usernameTextView.setText((username.charAt(0) + "").toUpperCase());
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) messageCardView.getLayoutParams();
                if (message[1].equals("bot")) {
                    botImageView.setVisibility(View.VISIBLE);
                    usernameTextView.setVisibility(View.INVISIBLE);
                } else {
                    botImageView.setVisibility(View.INVISIBLE);
                    usernameTextView.setVisibility(View.VISIBLE);
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                    messageCardView.setLayoutParams(params);
                }
                messageTextView.setText(message[0]);
            }
        }
    }
}
