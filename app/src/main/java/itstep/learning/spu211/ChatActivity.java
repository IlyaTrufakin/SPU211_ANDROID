package itstep.learning.spu211;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    private final static String chatUrl = "https://chat.momentfor.fun";
    private LinearLayout chatContainer;
    private ScrollView chatScroller;
    private EditText etAuthor;
    private EditText etMessage;
    private final List<ChatMessage> messages = new ArrayList<>();
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
            // Основной контейнер, для которого будут применяться системные вставки

        });

        findViewById(R.id.chat_btn_send).setOnClickListener((this::onSendClick));
        etAuthor = findViewById(R.id.chat_et_nik);
        etMessage = findViewById(R.id.chat_et_message);
        chatContainer = findViewById(R.id.chat_ll_container);
        chatScroller = findViewById(R.id.chat_sv_container);
        handler.post(this::reloadChat);
        // chatContainer.post(() -> new Thread(this::loadChat).start());
    }

    private void reloadChat() {
        new Thread(this::loadChat).start();
        handler.postDelayed(this::reloadChat, 3000);
    }

    class ChatMessage {
        private String id;
        private String author;
        private String text;
        private Date moment;
        private View view;

        public View getView() {
            return view;
        }

        public void setView(View view) {
            this.view = view;
        }

        private final SimpleDateFormat momentFormat =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);


        public ChatMessage(String author, String text) {
            this.setAuthor(author);
            this.setText(text);
        }

        public ChatMessage(JSONObject jsonObject) throws Exception {
            this.setId(jsonObject.getString("id"));
            this.setAuthor(jsonObject.getString("author"));
            this.setText(jsonObject.getString("text"));
            this.setTime(momentFormat.parse(jsonObject.getString("moment")));

        }


        public Date getTime() {
            return moment;
        }

        public void setTime(Date time) {
            this.moment = time;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    private void onSendClick(View view) {
        String author = etAuthor.getText().toString();
        if (author.isBlank()) {
            Toast.makeText(this, "Введите ник", Toast.LENGTH_SHORT).show();
            return;
        }
        String message = etMessage.getText().toString();
        if (message.isBlank()) {
            Toast.makeText(this, "Введите сообщение", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(
                () -> sendMessage(new ChatMessage(author, message))
        ).start();
    }

    private void sendMessage(ChatMessage chatMessage) {
        // для добавления сообщения надо послать запрос методом ПОСТ с данными, имитирующими посылание ФОРМЫ с параметрами author and message
        // author=the%20Author&msg=Hello%20All
        try {
            URL url = new URL(chatUrl);
            // open connect
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //setting connection params
            connection.setDoInput(true); //
            connection.setDoOutput(true); // add request BODY
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); // заголовки
            connection.setRequestProperty("Accept", "application/json"); // заголовки
            connection.setChunkedStreamingMode(0); // не делить на части (чанки)
            //
            OutputStream bodyStream = connection.getOutputStream();
            String body = String.format(
                    "author=%s&msg=%s",
                    URLEncoder.encode(chatMessage.getAuthor(), StandardCharsets.UTF_8.name()),
                    URLEncoder.encode(chatMessage.getText(), StandardCharsets.UTF_8.name())
            );

            bodyStream.write(body.getBytes(StandardCharsets.UTF_8));
            bodyStream.flush(); // инициирование отправки данных из буфера в канал передачи
            bodyStream.close();

            //получаем ответ
            int statusCode = connection.getResponseCode();
            if (statusCode == 201) {
                // обновление чата
                loadChat();
            } else {
                // изучаем тело ответа при ошибке
                InputStream errorStream = connection.getErrorStream();
                String errorMessage = readAsString(connection.getErrorStream());
                runOnUiThread(
                        () -> Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show());
                errorStream.close();
            }


        } catch (Exception ex) {
            Log.e("sendMessage", ex.getMessage());
        }

    }


    private void showChat(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            //chatContainer.removeAllViews();
            boolean wasNewMessage = false;
            for (int i = 0; i < jsonArray.length(); i++) {
                ChatMessage chatMessage = new ChatMessage(jsonArray.getJSONObject(i));
                //проверяем есть ли такое сообщение в ранее
                if (this.messages.stream().noneMatch((m -> m.getId().equals(chatMessage.getId())))) {
                    //если новое сообщение
                    messages.add(chatMessage);
                    wasNewMessage = true;
                }
            }
            if (wasNewMessage) {
                // messages.sort((m1, m2) -> m1.getTime().compareTo(m2.getTime()));
                messages.sort(Comparator.comparing(ChatMessage::getTime));
                //chatContainer.removeAllViews();
                for (ChatMessage chatMessage : messages) {
                    if (chatMessage.getView() == null) {
                        chatMessage.setView(messageView(chatMessage));
                        chatContainer.addView(chatMessage.getView());
                    }

                }
                chatScroller.post(() -> chatScroller.fullScroll(View.FOCUS_DOWN));
            }


        } catch (Exception ex) {
            Log.e("showChat", ex.getMessage());
        }
    }

    private View messageView(ChatMessage chatMessage) {
        LinearLayout box = new LinearLayout(ChatActivity.this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(15, 15, 15, 15);
        box.setBackground(AppCompatResources.getDrawable(
                this, R.drawable.chat_msg
        ));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(5, 7, 7, 20);
        layoutParams.gravity = Gravity.START;
        box.setLayoutParams(layoutParams);


        TextView tv = new TextView(ChatActivity.this);
        tv.setText(chatMessage.getTime() + " " + chatMessage.getAuthor());
        box.addView(tv);
        tv = new TextView(ChatActivity.this);
        tv.setText(chatMessage.getText());
        box.addView(tv);
        //сохраняем связьс объектом -сообщением
        box.setTag(chatMessage);
        box.setOnClickListener(this::messageClick);
        return box;
    }


    private void messageClick(View view) {
        ChatMessage chatMessage = (ChatMessage) view.getTag();
        Toast.makeText(this, chatMessage.getText(), Toast.LENGTH_SHORT).show();
    }

    private void loadChat() {
        try {
            URL url = new URL(chatUrl);
            InputStream urlStream = url.openStream();
            String jsonString = readAsString(urlStream);
            runOnUiThread(() -> showChat(jsonString));
            urlStream.close();

        } catch (
                MalformedURLException ex) {
            Log.d("loadChat", "MalformedURLException" + ex.getMessage());
        } catch (
                IOException ex) {
            Log.d("loadChat", "IOException" + ex.getMessage());
        } catch (
                android.os.NetworkOnMainThreadException ex) {
            Log.d("loadChat", "NetworkOnMainThreadException" + ex.getMessage());
        } catch (
                java.lang.SecurityException ex) {
            Log.d("loadChat", "SecurityException" + ex.getMessage());
        }


    }


    private String readAsString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = inputStream.read(buffer)) > 0) {
            byteBuilder.write(buffer, 0, len);
        }
        return byteBuilder.toString();
    }


}


//{
//        "status":1,
//        "data":[
//        {
//        "id":"3013",
//        "author":"Dell",
//        "text":"Привіт усім!",
//        "moment":"2024-06-19 01:02:43"
//        }
//}