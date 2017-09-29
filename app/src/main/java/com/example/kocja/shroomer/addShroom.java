package com.example.kocja.shroomer;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kocja on 20/08/2017.
 */

public class addShroom extends AppCompatActivity {
    //TODO question of is it on the same location...Radius...somehow

    int[] shroomTypes= new int[]{R.drawable.ic_icons8_mushroom_96,R.drawable.ic_arrow_downward_black_24dp,R.drawable.ic_arrow_upward_black_24dp,R.drawable.ic_arrow_forward_black_24dp};
    int positionOfPicture = 0;

    int SELECT_PHOTO = 1234;
    int REQUEST_TAKE_PHOTO = 0;
    String mCurrentPhotoPath;
    Uri photoURI;
    Animation inAnimForward, outAnimForward, inAnimBack, outAnimBack;
    ImageView photoOfShroom;
    String photoURIString;
    int id;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addshroom);
        final ImageSwitcher switcher = (ImageSwitcher) findViewById(R.id.imageSwitcher);

        inAnimForward = AnimationUtils.loadAnimation(addShroom.this,R.anim.slide_in_right);
        outAnimForward = AnimationUtils.loadAnimation(addShroom.this,R.anim.slide_out_left);
        inAnimBack = AnimationUtils.loadAnimation(addShroom.this,android.R.anim.slide_in_left);
        outAnimBack = AnimationUtils.loadAnimation(addShroom.this,android.R.anim.slide_out_right);
        photoOfShroom = (ImageView) findViewById(R.id.photoOfShroom);

        final ImageButton backButton = (ImageButton) findViewById(R.id.backButton);
        final ImageButton forwardButton = (ImageButton) findViewById(R.id.forwardbutton);
        final ImageButton takeAPhotoShroom = (ImageButton) findViewById(R.id.takePhotoShroom);

        switcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                //ImageView view = new ImageView(getApplicationContext());


                return new ImageView(getApplicationContext());
            }
        });

        Intent intent= getIntent();
        id = intent.getIntExtra("Id",-1);
        int indexOfImage = intent.getIntExtra("IndexOfImage",-1);
        //positionOfPicture is a counter of which array index the user is on. When pulling from a database, set the int to the counter
        if(id == 69) {
            positionOfPicture = indexOfImage;
            switcher.setImageResource(shroomTypes[positionOfPicture]);
        }
        else{
            switcher.setImageResource(R.drawable.ic_icons8_mushroom_96);
        }


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switcher.setOutAnimation(outAnimBack);
                switcher.setInAnimation(inAnimBack);
                if(positionOfPicture != 0) {
                    positionOfPicture -= 1;
                    switcher.setImageResource(shroomTypes[positionOfPicture]);

                }
            }
        });

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switcher.setOutAnimation(outAnimForward);
                switcher.setInAnimation(inAnimForward);
                if( positionOfPicture != shroomTypes.length-1){
                    positionOfPicture += 1;
                    switcher.setImageResource(shroomTypes[positionOfPicture]);

                }

            }
        });

        takeAPhotoShroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence choose[] = new CharSequence[]{"Select from gallery","Take a photo"};
                AlertDialog.Builder builder = new AlertDialog.Builder(addShroom.this)
                        .setTitle("Choose a photo")
                        .setCancelable(true)
                        .setItems(choose, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0){
                                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                                    photoPickerIntent.setType("image/*");
                                    startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                                }
                                else{
                                    dispatchTakePictureIntent();
                                }
                            }
                        });
                builder.show();
            }
        });

        ImageButton checkDone = (ImageButton) findViewById(R.id.check_Done);
        checkDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = getIntent();
                result.putExtra("dateOfFound",new SimpleDateFormat("dd/MM/yy").format(new Date()));
                result.putExtra("URI",photoURIString);
                result.putExtra("indexOfImage",positionOfPicture);
                result.putExtra("ID",id);
                setResult(RESULT_OK,result);
                finish();
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode,int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data != null) {

            // Let's read picked image data - its URI
            Uri pickedImage = data.getData();
            //load a picture into the view
            Glide.with(addShroom.this).load(pickedImage).into(photoOfShroom);

            photoURIString = pickedImage.toString();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("error occured");
                dialog.show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
            //display a picture in intent. Save the uri as a string so i can save it later.
            Glide.with(this).load(photoURI).centerCrop().into(photoOfShroom);
            photoURIString = photoURI.toString();

        }
    }
}
