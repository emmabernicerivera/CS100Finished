package com.CS100MessagingApp;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;


public class Chat extends AppCompatActivity {
    LinearLayout layout;
    RelativeLayout layout_2;
    ImageView sendButton, sendPicture,sendVideo;
    EditText messageArea;
    ScrollView scrollView;
    DatabaseReference reference1, reference2;
    private StorageReference imageRef;
    private static int GALLERY_INTENT = 2;
    private static int GALLERY_INTENT_VIDEO = 3;  // for video file


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        layout = (LinearLayout) findViewById(R.id.layout1);
        layout_2 = (RelativeLayout)findViewById(R.id.layout2);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        messageArea = (EditText)findViewById(R.id.messageArea);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        sendPicture = (ImageView)findViewById(R.id.sendPicture);
        sendVideo = (ImageView)findViewById(R.id.sendVideo);
        //Ref 1 = The User, Ref 2 = User that is being sent a message. imageRef is for sent images
        reference1 = FirebaseDatabase.getInstance().getReferenceFromUrl("https://messaging-app-cs100.firebaseio.com/messages/" + UserDetails.username + "_" + UserDetails.chatWith);
        reference2 = FirebaseDatabase.getInstance().getReferenceFromUrl("https://messaging-app-cs100.firebaseio.com/messages/" + UserDetails.chatWith + "_" + UserDetails.username);
        imageRef = FirebaseStorage.getInstance().getReference();


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();

                if(!messageText.equals("")){
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("message", messageText);
                    map.put("user", UserDetails.username);
                    map.put("type", "text");
                    reference1.push().setValue(map);
                    reference2.push().setValue(map);
                    messageArea.setText("");
                }
            }
        });

        sendPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,2);

            }

        });

        sendVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_PICK);
                intent.setType("video/*");
                startActivityForResult(intent,3);  //3 for video file in onActivityResult function

            }

        });

        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map<String,Object>map = (Map<String,Object>)dataSnapshot.getValue();
                String message = map.get("message").toString();
                String userName = map.get("user").toString();
                String type;
                if(map.containsKey("type"))
                {
                    type = map.get("type").toString();
                } else{
                    type = "text";
                }

                if(userName.equals(UserDetails.username)){
                    if(type.equals("image"))
                    {
                        addMessageBox(message,1,type);
                    }else if(type.equals("text")){
                        addMessageBox("You:\n" + message, 1, type);
                    }else if (type.equals("video")){
                        addMessageBox(message, 1, type);
                    }
                }
                else{
                    if(type.equals("image")) {
                        addMessageBox(message, 2, type);
                    }else if(type.equals("text")){
                        addMessageBox(UserDetails.chatWith + ":\n" + message, 2, type);
                    }else if (type.equals("video")){
                        addMessageBox(message , 2, type);
                    }

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }

    /**
     * @param type : type==1 means that the message is sent by you, and type==2 means that it is sent by your group chat members;
     * @param what : the type of the message (image, text or video);
     */
    public void addMessageBox(String message, int type,String what){

        TextView textView = new TextView(Chat.this);
        ImageView myImage = new ImageView(Chat.this);


        // What checks to see if the message is a text or image.
        if(what.equals("text")) {
            textView.setText(message);
            myImage.setVisibility(View.GONE);
        }else if(what.equals("image"))
        {
            textView.setVisibility(View.GONE);
            myImage.setVisibility(View.VISIBLE);
        }else if (what.equals("video")){
            textView.setVisibility(View.GONE);
            myImage.setVisibility(View.VISIBLE);
        }

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 1.0f;

        // If the message is sent by you
        if(type == 1) {
            if(what.equals("text")) {
                //The message type is text
                lp2.gravity = Gravity.LEFT;
                textView.setBackgroundResource(R.drawable.bubble_out);
                textView.setLayoutParams(lp2);
                layout.addView(textView);
                scrollView.fullScroll(View.FOCUS_DOWN);
                myImage.setVisibility(View.GONE);

            } else if (what.equals("image")){
                // The message type is Image
                lp2.gravity = Gravity.LEFT;
                myImage.setBackgroundResource(R.drawable.bubble_out);
                Glide.with(this)
                        .load(message)
                        .apply(new RequestOptions()
                                .override(190,300).centerCrop())
                        .into(myImage);
                myImage.setLayoutParams(lp2);
                layout.addView(myImage);
                scrollView.fullScroll(View.FOCUS_DOWN);

            }else if (what.equals("video")){
                final String tempURI = message;
                lp2.gravity = Gravity.LEFT;
                myImage.setBackgroundResource(R.drawable.bubble_out);
                Glide.with(this)
                        .load(R.drawable.unpressd)
                        .apply(new RequestOptions()
                                .override(190,190).centerCrop())
                        .into(myImage);
                myImage.setLayoutParams(lp2);
                myImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Chat.this, VideoPlay.class);
                        intent.putExtra("videoURI",tempURI);
                        startActivity(intent);
                    }
                });
                layout.addView(myImage);
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        }
        else{
            //If the message is sent by other people
            if(what.equals("text")) {
                //The message type is text
                lp2.gravity = Gravity.RIGHT;
                textView.setBackgroundResource(R.drawable.bubble_in);
                textView.setLayoutParams(lp2);
                layout.addView(textView);
                scrollView.fullScroll(View.FOCUS_DOWN);
                myImage.setVisibility(View.GONE);

            } else if (what.equals("image")){
                // The message type is Image
                lp2.gravity = Gravity.RIGHT;
                myImage.setBackgroundResource(R.drawable.bubble_in);
                Glide.with(this)
                        .load(message)
                        .apply(new RequestOptions()
                                .override(190,300).centerCrop())
                        .into(myImage);
                myImage.setLayoutParams(lp2);
                layout.addView(myImage);
                scrollView.fullScroll(View.FOCUS_DOWN);

            }else if (what.equals("video")){
                // The message type is Video
                final String tempURI = message;
                lp2.gravity = Gravity.RIGHT;
                myImage.setBackgroundResource(R.drawable.bubble_in);
                Glide.with(this)
                        .load(R.drawable.unpressd)
                        .apply(new RequestOptions()
                                .override(190,190).centerCrop())
                        .into(myImage);
                myImage.setLayoutParams(lp2);
                myImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Chat.this, VideoPlay.class);
                        intent.putExtra("videoURI",tempURI);
                        startActivity(intent);
                    }
                });
                layout.addView(myImage);
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode == RESULT_OK){
            // Get URI for image picked from gallery.
            android.net.Uri sentFileURI = data.getData();
            // Gets ID for what the image will be called.
            final Map messageMap = new HashMap();
            final String push_id = String.valueOf(System.identityHashCode(messageMap));
            //if the image add button is called, send file to image storage
            if(requestCode == GALLERY_INTENT) {

                {
                    //Gets the Filepath for the Firebase Storage.
                    StorageReference filepath = imageRef.child("message_images").child(reference1.getKey()).child(push_id+".jpg");
                    //Puts the the image into the Firebase Storage.
                    filepath.putFile(sentFileURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                //Gets a URL for the Image.
                                String download_url = task.getResult().getDownloadUrl().toString();
                                // Sends the Image as message into the Firebase RealTime Database.

                                messageMap.put("message", download_url);
                                messageMap.put("user", UserDetails.username);
                                messageMap.put("type", "image");
                                reference1.push().setValue(messageMap);
                                reference2.push().setValue(messageMap);
                            }
                        }


                    });
                }
            }
            //if the video add button is called, send file to video storage
            else if(requestCode == GALLERY_INTENT_VIDEO)
            {
                //Gets the Filepath for the Firebase Storage.
                StorageReference filepath = imageRef.child("message_video").child(reference1.getKey()).child(push_id+".avi");
                //Puts the the video into the Firebase Storage.
                filepath.putFile(sentFileURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            //Gets a URL for the video.
                            String download_url = task.getResult().getDownloadUrl().toString();
                            // Sends the video as message into the Firebase RealTime Database.
                            messageMap.put("message", download_url);
                            messageMap.put("user", UserDetails.username);
                            messageMap.put("type", "video");
                            reference1.push().setValue(messageMap);
                            reference2.push().setValue(messageMap);
                        }
                    }


                });
            }

        }


    }

}
