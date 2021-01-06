package com.example.wingman


import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import java.util.*
import com.example.wingman.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {
    private lateinit var mainActivity: ActivityMainBinding
    private lateinit var messageList: ArrayList<MessageClass>
    private lateinit var editText: EditText
    private lateinit var sendBtn: FloatingActionButton
    private val USER = 0
    private val BOT = 1
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivity = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainActivity.root)
        editText = this.findViewById(R.id.message_box)
        messageList = ArrayList<MessageClass>()
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        mainActivity.messageList.layoutManager = linearLayoutManager
        val adapter = MessageAdapter(this, messageList)
        mainActivity.messageList.adapter = adapter
       mainActivity.sendBtn.setOnClickListener {
          //  val msg = mainActivity.messageBox.text.toString()
          sendMessage()
          //  adapter.notifyItemInserted(messageList.size - 1)
         //   Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show()
         //   mainActivity.messageBox.setText("")

        }


    }

    fun sendMessage() {
        val msg = mainActivity.messageBox.text.toString()
      //  val msg: String = editText.text.toString().trim()
       // val date = Date(System.currentTimeMillis())

        val okHttpClient = OkHttpClient()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.105:5005/webhooks/rest/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        val userMessage = UserMessage()
        if (msg.trim().isEmpty())
            Toast.makeText(this, "Please enter your query", Toast.LENGTH_SHORT).show()
        else {
            Log.e("MSg", "msssage: $msg")
            editText.setText("")
            userMessage.UserMessage("User", msg)
            showTextView(msg, USER)

        }
        val messageSender = retrofit.create(MessageSender::class.java)
        val response = messageSender.sendMessage(userMessage)
        response.enqueue(object : Callback<List<BotResponse>>{
            override fun onResponse(
                call: Call<List<BotResponse>>,
                response: Response<List<BotResponse>>
            ) {
                if (response.body() == null || response.body()!!.size == 0) {
                    val botMessage = "Sorry didn't understand"
                    showTextView(botMessage, BOT)
                } else {
                    var i = 0
                    Log.e("size res", "${response.body()!!.size}")
                    while (i < response.body()!!.size) {
                        val botResponse = response.body()!![i]
                        Log.e("size", "$i")
                        if (botResponse.text != null) {
                            showTextView(botResponse.text, BOT)
                        }
                        if (botResponse.image != null) {
                            Log.e("Image", "${response.body()!![i].image}")
                            showImageView(response.body()!![i].image)
                        }
                        if (botResponse.buttons != null) {
                            Log.e("Button c", "${botResponse.buttons.size}")
                        }
                        i += 1
                    }
                }
            }
            override fun onFailure(call: Call<List<BotResponse>>, t: Throwable) {
                val botMessage = "Check your network connection"
                showTextView(botMessage, BOT)
                t.printStackTrace()
                Toast.makeText(this@MainActivity, "" + t.message, Toast.LENGTH_SHORT).show()
            }

        })


    }


            fun showImageView(imageUrl: String) {
                var frameLayout: FrameLayout? = getBotLayout()

                val linearLayout = findViewById<LinearLayout>(R.id.linear_layout)
                linearLayout.addView(frameLayout)
                imageView = findViewById(R.id.myImg) as ImageView
                imageView.visibility = View.VISIBLE
                Glide.with(this)
                    .load(imageUrl)
                    .centerCrop()
                    .thumbnail(0.25f)
                    .into(imageView!!)


            };


            fun showTextView(message: String, type: Int) {
                var frameLayout: FrameLayout? = null
                val linearLayout = findViewById<LinearLayout>(R.id.linear_layout)
                when (type) {
                    USER -> {
                        frameLayout = getUserLayout()
                    }
                    BOT -> {
                        frameLayout = getBotLayout()
                    }
                    else -> {
                        frameLayout = getBotLayout()
                    }
                }
                frameLayout?.isFocusableInTouchMode = true
                linearLayout?.addView(frameLayout)

                val messageTextView = frameLayout?.findViewById<TextView>(R.id.message_tv_user)

                messageTextView?.setText(message)
                frameLayout?.requestFocus()
                editText.requestFocus()

            }



        fun getUserLayout(): FrameLayout? {
            val inflater: LayoutInflater = LayoutInflater.from(this)
            return inflater.inflate(R.layout.user_message_box, null) as FrameLayout?
        }

        fun getBotLayout(): FrameLayout? {
            val inflater: LayoutInflater = LayoutInflater.from(this)
            return inflater.inflate(R.layout.bot_message_box, null) as FrameLayout?
        }

    }

