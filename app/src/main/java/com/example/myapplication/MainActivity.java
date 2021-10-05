package com.example.myapplication;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.NumberUtils;
import com.googlecode.tesseract.android.TessBaseAPI;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends AppCompatActivity {

    Bitmap image; //사용되는 이미지
    private TessBaseAPI mTess; //Tess API reference
    String datapath = "" ; //언어데이터가 있는 경로

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent tt = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:0100000000"));

        //이미지 디코딩을 위한 초기화
        image = BitmapFactory.decodeResource(getResources(), R.drawable.sampleimg); //샘플이미지파일
        //언어파일 경로
        datapath = getFilesDir()+ "/tesseract/";

        //트레이닝데이터가 카피되어 있는지 체크
        checkFile(new File(datapath + "tessdata/"));

        //Tesseract API
        String lang = "eng";

        mTess = new TessBaseAPI();
        mTess.init(datapath, lang);
        //startActivity(tt);
    }

    //Process an Image
    public void processImage(View view) {
        String OCRresult = null;
        String restext=null;
        char[] pretext=null;
        String phone="";
        mTess.setImage(image);
        OCRresult = mTess.getUTF8Text();
        TextView OCRTextView = (TextView) findViewById(R.id.OCRTextView);

        //읽어들인 값 전처리(공백, - 치환)
        OCRresult=OCRresult.replace(" ","");
        OCRresult=OCRresult.replace("—","-");
        OCRTextView.setText(OCRresult);

        System.out.println(OCRresult);

        //숫자와 -만 빼오기
        pretext=OCRresult.toCharArray();
        int l=0;
        int j=0;
        for(l=0;l<pretext.length;l++){
            System.out.println("숫자인가?"+pretext[l]);
            if(Character.isDigit(pretext[l])){
                phone=phone+pretext[l];
                j++;
            }
        }
//        for(l=0;l<pretext.length;l++){
//            if(Character.isDigit(pretext[l])||(pretext[l]=='-')){
//                phone=phone+pretext[l];
//                j++;
//            }
//        }

        System.out.println("전화번호"+phone);
//        System.out.println(phone.indexOf("-"));
//        phone=phone.replace("-","");
//        phone=phone.substring(0,10);
        onClick_setting_costume_save(phone);

    }

    public void onClick_setting_costume_save( String phone){
        new AlertDialog.Builder(this)
                .setTitle("전화번호 확인")
                .setMessage(phone+"맞습니까?")
                //.setIcon(android.R.drawable.ic_menu_save)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // 확인시 처리 로직
                        Toast.makeText( MainActivity.this, "전화를 연결합니다.", Toast.LENGTH_SHORT).show();
                        Intent tt = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+phone));
                        startActivity(tt);
                        finish();
                    }})
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // 취소시 처리 로직
                        Toast.makeText( MainActivity.this, "취소하였습니다.", Toast.LENGTH_SHORT).show();
                    }})
                .show();

    }

    //copy file to device
    private void copyFiles() {
        try{
            String filepath = datapath + "/tessdata/eng.traineddata";
            AssetManager assetManager = getAssets();
            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //check file on the device
    private void checkFile(File dir) {
        //디렉토리가 없으면 디렉토리를 만들고 그후에 파일을 카피
        if(!dir.exists()&& dir.mkdirs()) {
            copyFiles();
        }
        //디렉토리가 있지만 파일이 없으면 파일카피 진행
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);
            if(!datafile.exists()) {
                copyFiles();
            }
        }
    }

}