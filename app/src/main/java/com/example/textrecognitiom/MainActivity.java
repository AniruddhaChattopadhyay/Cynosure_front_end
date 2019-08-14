package com.example.textrecognitiom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.cloud.translate.v3beta1.LocationName;
import com.google.cloud.translate.v3beta1.TranslateTextRequest;
import com.google.cloud.translate.v3beta1.TranslateTextResponse;
import com.google.cloud.translate.v3beta1.TranslationServiceClient;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private TextView textView;
    private ImageView imageView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    final int[] colorList = {Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.CYAN, Color.BLACK, Color.DKGRAY, Color.YELLOW, Color.GRAY};
    private List<String> langs = Arrays.asList("en", "hi", "bn","mr","ta","te");
    Translate translate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button)findViewById(R.id.button);
        imageView = (ImageView)findViewById(R.id.imageView);
        textView = (TextView)findViewById(R.id.textView);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                else
                    CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(MainActivity.this);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(MainActivity.this);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            CropImage.ActivityResult cropimage = CropImage.getActivityResult(data);
            Uri resultUri = cropimage.getUri();
            try {
                final Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                textView.setText("");
                imageView.setImageDrawable(null);
                Bitmap convertedBitmap = convert(bitmap);
                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(convertedBitmap);
                FirebaseVisionCloudTextRecognizerOptions options =
                        new FirebaseVisionCloudTextRecognizerOptions.Builder()
                                .setLanguageHints(langs)
                                .build();
                FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                        .getCloudTextRecognizer(options);
                Task<FirebaseVisionText> result = detector.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                int c = 0;
                                Bitmap newbitmap = bitmap.copy(bitmap.getConfig(), true);
                                Log.d("TEXT RECO", firebaseVisionText.getText());
                                for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
                                    String temp = block.getText().replace("\n", " ");
                                    List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
                                    for (RecognizedLanguage language : blockLanguages) {
                                        Log.d("LANGUAGE", language.getLanguageCode() + "   ");
                                    }
                                    getTranslateService();
                                    translate(temp, c);
                                    Rect blockFrame = block.getBoundingBox();
                                    Canvas canvas = new Canvas(newbitmap);
                                    Paint myPaint = new Paint();
                                    myPaint.setColor(colorList[c++]);
                                    myPaint.setStrokeWidth(10);
                                    myPaint.setStyle(Paint.Style.STROKE);
                                    assert blockFrame != null;
                                    canvas.drawRect(blockFrame, myPaint);
                                    if (c > colorList.length) break;
                                }
                                imageView.setImageBitmap(newbitmap);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("TEXT RECO2", Objects.requireNonNull(e.getLocalizedMessage()));
                                imageView.setImageBitmap(bitmap);
                            }
                        });
            } catch (Exception e){
                Log.d("CROPPER", e.getLocalizedMessage());
            }
        }
    }

    private Bitmap convert(Bitmap bitmap) {
        Bitmap convertedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(convertedBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return convertedBitmap;
    }

//    private void translateText(final String toTranslate, final int c){
//        Thread thread = new Thread(){
//            @Override
//            public void run() {
//                try {
//                    TranslationServiceClient translationServiceClient = TranslationServiceClient.create();
//                    LocationName locationName =
//                            LocationName.newBuilder().setProject("orendaa-2-0").setLocation("global").build();
//                    TranslateTextRequest translateTextRequest =
//                            TranslateTextRequest.newBuilder()
//                                    .setParent(locationName.toString())
//                                    .setMimeType("text/plain")
//                                    .setSourceLanguageCode("hi")
//                                    .setTargetLanguageCode("en")
//                                    .addContents(toTranslate)
//                                    .build();
//                    TranslateTextResponse response = translationServiceClient.translateText(translateTextRequest);
//                    String temp = response.getTranslationsList().get(0).getTranslatedText();
//                    Spannable word = new SpannableString(temp);
//                    word.setSpan(new ForegroundColorSpan(colorList[c]), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                    textView.append(word);
//                }catch (Exception e){
//                    Log.d("TRANSLATE", e.toString());
//                }
//            }
//        };
//        thread.start();
//            Handler handler = new Handler();
//            final Runnable r = new Runnable() {
//                @Override
//                public void run() {
//                    try{
//
//                    }catch (Exception e){
//                        Log.d("TRANSLATE", e.toString());
//                    }
//                }
//            };
//            handler.post(r);
//    }

    public void getTranslateService() {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try (InputStream is = getResources().openRawResource(R.raw.gcpcredential)) {

            //Get credentials:
            final GoogleCredentials myCredentials = GoogleCredentials.fromStream(is);

            //Set credentials and get translate service:
            TranslateOptions translateOptions = TranslateOptions.newBuilder().setCredentials(myCredentials).build();
            translate = translateOptions.getService();

        } catch (IOException ioe) {
            ioe.printStackTrace();

        }
    }

    public void translate(String originalText, int c) {

        //Get input text to be translated:
        Translation translation = translate.translate(originalText, Translate.TranslateOption.targetLanguage("en"), Translate.TranslateOption.model("base"));
        String translatedText = translation.getTranslatedText();

        //Translated text and original text are set to TextViews:
        Log.d("TRANSLATED", originalText + " --> " + translatedText+"\n");
        Spannable word = new SpannableString(originalText + " --> " + translatedText+"\n");
        word.setSpan(new ForegroundColorSpan(colorList[c]), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.append(word);
    }
}
