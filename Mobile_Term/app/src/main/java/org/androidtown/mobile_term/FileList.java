package org.androidtown.mobile_term;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
/**
 *
 * @brief 폴더 내에 있는 파일을 출력해주는 클래스이다.
 * @details 파일을 삭제, 이름변경, 폴더 위치 변경 등을 관리해주고 파일과 프로그램을 연결해준다.
 * @author 가천대 소프트웨어학과 10조
 * @date 2019-05-04
 * @version 0.0.1
 *
 */
public class FileList extends AppCompatActivity{

    static String servicepath = null;
    static String servicename = null;
    private Spinner spinner;
    String reqLocation;
    String FolderName;
    String spinnerset;

    int folderandfile = 0;
    int foldernum = 0;
    int filenum = 0;
    int pdf = 0;
    int mp3 = 0;

    ArrayList<BookPojo> folderAndFileList;
    ArrayList<BookPojo> foldersList;
    ArrayList<BookPojo> filesList;
    ArrayList<BookPojo> pdfList;
    public static ArrayList<BookPojo> mp3List;
    BookAdapter FolderAdapter;
    ListView listView;
    String location = Environment.getExternalStorageDirectory().getAbsolutePath();
    boolean pickFiles;
    Intent receivedIntent;
    String bookfolderName;

    private Messenger mServiceMessenger = null;
/********************************************************************************************************************
    //녹음
    String path = "";
    MediaRecorder recorder = new MediaRecorder();
    private boolean isRecording = false;
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    Button appbarbtn;
    Button pausebtn;


    //일시정지 포함 녹음
    String finalpath = null;
    private List outputFileList = new ArrayList();
    //private ArrayList<String> outputFileList;
    int count = 0;
    int noti_count = 0;
    public final static int STATE_PREV = 0;     //녹음 시작 전
    public final static int STATE_RECORDING = 1;    //녹음 중
    public final static int STATE_PAUSE = 2;        // 일시 정지 중
    private int state = STATE_PREV;
****************************************************************************************************************************/

    final int REQUEST_PERMISSION_CODE = 1000;

    //날짜 셋팅
    long mNow;
    Date mDate;
    SimpleDateFormat mFormat = new SimpleDateFormat("yyMMdd-HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);

        //Request Runtime permission************************************************************************************************
        //if (!checkPermissionFromDevice())
        //    requsetPermission();

        /*LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.app_bar_list,,true);*/
        try {
            setToolbar();
        } catch (IOException e) {
            ;
        }

        if (!isExternalStorageReadable()) {
            Toast.makeText(this, "Storage access permission not given", Toast.LENGTH_LONG).show();
            finish();
        }

        /*mBtnPlayPause.setOnClickListener(new View.OnClickListener() {
            AudioApplication.getInstance().getServiceInterface().togglePlay();
            /*@Override
            public void onClick(View v) {
                case R.id.btn_rewind:
                // 이전곡으로 이동
                AudioApplication.getInstance().getServiceInterface().rewind();
                break;
                case R.id.btn_play_pause:
                // 재생 또는 일시정지
                break;
                case R.id.btn_forward:
                // 다음곡으로 이동
                AudioApplication.getInstance().getServiceInterface().forward();
                break;
            }
        });*/

        FloatingActionButton include = (FloatingActionButton) findViewById(R.id.include);
        include.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickFile();
            }
        });

        try {
            receivedIntent = getIntent();
            if (receivedIntent.hasExtra("location")) {
                reqLocation = receivedIntent.getExtras().getString("location");//전체 값
                bookfolderName = reqLocation;
                FolderName = receivedIntent.getExtras().getString("name");
                String Location = Environment.getExternalStorageDirectory().getAbsolutePath() + reqLocation; //ㄹㅇ주소
                if (Location != null) {
                    File requestedFolder = new File(Location);
                    if (requestedFolder.exists())
                        location = Location;
                    else {
                        createNewFolder(Location);
                        location = Location;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        pickFiles = true;
        loadLists(location);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: { //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    void pickFile() {
        Intent intent = new Intent(this, FolderPicker.class);
        intent.putExtra("location", Environment.getExternalStoragePublicDirectory("").getAbsolutePath());
        intent.putExtra("folderName", bookfolderName);
        startActivityForResult(intent, 3);
    }

    /* Checks if external storage is available to at least read */
    boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /*파일 load*/
    void loadLists(String location) {
        try {
            File folder = new File(location);
            if (!folder.isDirectory())
                exit();
            File[] files = folder.listFiles();

            foldersList = new ArrayList<>();
            filesList = new ArrayList<>();
            pdfList = new ArrayList<>();
            mp3List = new ArrayList<>();
            for (File currentFile : files) {
                long lastModifie = currentFile.lastModified();
                String patter = "yyyy년 MM월 dd일 aa hh:mm";
                SimpleDateFormat simpleDateForma = new SimpleDateFormat(patter);
                Date lastModifiedDat = new Date(lastModifie);
                Long L = currentFile.length();
                if (currentFile.isDirectory()) {
                    BookPojo bookPojo = new BookPojo(currentFile.getName(), true);
                    bookPojo.setDay(simpleDateForma.format(lastModifiedDat));
                    bookPojo.setSize(getFileCount(currentFile, 0) + "개");
                    bookPojo.setPosi(foldernum);
                    foldersList.add(bookPojo);
                    foldernum++;
                } else {
                    BookPojo bookPojo = new BookPojo(currentFile.getName(), false);
                    bookPojo.setDay(simpleDateForma.format(lastModifiedDat));
                    bookPojo.setSize(formatFileSize(L));
                    bookPojo.setLocation(location);
                    if (currentFile.getName().contains("pdf")) {
                        bookPojo.setPosi(pdf);
                        pdfList.add(bookPojo);
                        pdf++;
                    } else if (currentFile.getName().contains("mp3")||currentFile.getName().contains("m4a")||currentFile.getName().contains("mp4")) {
                        bookPojo.setPosi(mp3);
                        mp3List.add(bookPojo);
                        mp3++;
                    }
                    bookPojo.setPosi(filenum);
                    filesList.add(bookPojo);
                    filenum++;

                }
            }
            // sort & add to final List - as we show folders first add folders first to the final list
            Collections.sort(foldersList, comparatorAscending);
            folderAndFileList = new ArrayList<>();
            folderAndFileList.addAll(foldersList);
            //if we have to show files, then add files also to the final list
            if (pickFiles) {
                Collections.sort(filesList, comparatorAscending);
                folderAndFileList.addAll(filesList);
            }
            showList();

        } catch (Exception e) {
            e.printStackTrace();
        }

    } // load List

    Comparator<BookPojo> comparatorAscending = new Comparator<BookPojo>() {
        @Override
        public int compare(BookPojo f1, BookPojo f2) {
            return f1.getName().compareTo(f2.getName());
        }
    };

    private String formatFileSize(long bytes) {
        return android.text.format.Formatter.formatFileSize(getApplicationContext(), bytes);
    }

    public int getFileCount(File f, int totalCount) {
        if (f.isDirectory()) {
            String[] list = f.list();
            for (int i = 0; i < list.length; i++) {
                totalCount++;
            }
        } else {
            totalCount++;
        }
        return totalCount;
    }

    /**
     *
     *     @brief Show file list in FileList Activity
     *     @details Can sort it by expend of files
     *
     *      pdf, mp3 따로 정렬할때 건드릴 코드 folderAdaper 주석부분 확인
     */
    void showList() {
        try {
            if (spinnerset.equals("전체"))
                FolderAdapter = new BookAdapter(this, folderAndFileList);
            else if (spinnerset.equals("PDF"))
                FolderAdapter = new BookAdapter(this, pdfList);
            else if (spinnerset.equals("MP3"))
                FolderAdapter = new BookAdapter(this, mp3List);
            listView = (ListView) findViewById(R.id.book_listView);
            listView.setAdapter(FolderAdapter);
            //이부분은 나중에 pdf여는 코드
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    //    FolderAdapter.setIsVisible(isVisible);
                    //   FolderAdapter.notifyDataSetChanged();
                    listClick(position);
                }
            });
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
                    PopupMenu popup = new PopupMenu(getApplicationContext(), view);
                    getMenuInflater().inflate(R.menu.listview_popup, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {//눌러진 MenuItem의 Item Id를 얻어와 식별
                                case R.id.pop_name_change:
                                    namechange(position);
                                    break;
                                case R.id.pop_delete:
                                    filedelete(position);
                                    break;
                                case R.id.pop_share:
                                    fileshare(position);
                                    break;
                                case R.id.pop_info:
                                    fileinfo(position);
                                    break;
                            }
                            return false;
                        }
                    });
                    popup.show();
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void listClick(int position) {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + bookfolderName + folderAndFileList.get(position).getName());

        int K = 0;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", file);
        String fileExtend = getExtension(folderAndFileList.get(position).getName());
        if (fileExtend.equalsIgnoreCase("mp3") ||fileExtend.equalsIgnoreCase("m4a") ||fileExtend.equalsIgnoreCase("mp4")) {
            K = 1;
            Intent intent1 = new Intent(this,MP3Service.class);
            stopService(intent1);
            intent1.setAction(CommandActions.TOGGLE_PLAY);
            servicename = folderAndFileList.get(position).getName();;
            servicepath = Environment.getExternalStorageDirectory().getAbsolutePath() + bookfolderName + folderAndFileList.get(position).getName();
            startService(intent1);
        } /*else if (fileExtend.equalsIgnoreCase("mp4")) {
            intent.setDataAndType(uri, "video/*");
        }*/ else if (fileExtend.equalsIgnoreCase("jpg")
                || fileExtend.equalsIgnoreCase("jpeg")
                || fileExtend.equalsIgnoreCase("gif")
                || fileExtend.equalsIgnoreCase("png")
                || fileExtend.equalsIgnoreCase("bmp")) {
            intent.setDataAndType(uri, "image/*");
        } else if (fileExtend.equalsIgnoreCase("txt")) {
            intent.setDataAndType(uri, "text/*");
        } else if (fileExtend.equalsIgnoreCase("doc")
                || fileExtend.equalsIgnoreCase("docx")) {
            intent.setDataAndType(uri, "application/msword");
        } else if (fileExtend.equalsIgnoreCase("xls")
                || fileExtend.equalsIgnoreCase("xlsx")) {
            intent.setDataAndType(uri,
                    "application/vnd.ms-excel");
        } else if (fileExtend.equalsIgnoreCase("ppt")
                || fileExtend.equalsIgnoreCase("pptx")) {
            intent.setDataAndType(uri,
                    "application/vnd.ms-powerpoint");
        } else if (fileExtend.equalsIgnoreCase("pdf")) {
            intent.setDataAndType(uri, "application/pdf");
            //나중에 우리 readerIntent.
        } else if (fileExtend.equalsIgnoreCase("hwp")) {
            intent.setDataAndType(uri,
                    "application/haansofthwp");
        }
        //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            if (K == 0)

                startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, "기기에 파일을 열수 있는 어플이 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    /*파일 확장자 찾기*/
    public static String getExtension(String fileStr) {
        return fileStr.substring(fileStr.lastIndexOf(".") + 1, fileStr.length());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLists(location);
    }

    @Override
    public void onBackPressed() {
        goBack(null);
    }

    public void goBack(View v) {
        exit();
    }

    void exit() {
        setResult(RESULT_CANCELED, receivedIntent);
        finish();
    }

    void createNewFolder(String filename) {
        try {
            File file = new File(filename);
            file.mkdirs();
            loadLists(filename);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error:" + e.toString(), Toast.LENGTH_LONG)
                    .show();
        }
    }

    public void select(View v) {
        if (pickFiles) {
            Toast.makeText(this, "You have to select a file", Toast.LENGTH_LONG).show();
        } else if (receivedIntent != null) {
            receivedIntent.putExtra("data", location);
            setResult(RESULT_OK, receivedIntent);
            finish();
        }
    }

    public void cancel(View v) {
        exit();
    }

    /*순서대로 파일이름변경, 삭제, 공유, 상세정보*/
    public void namechange(final int position) {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle("파일 이름 변경");
        // ad.setMessage("Message");   // 내용 설정
        final EditText et = new EditText(this);
        et.setText(folderAndFileList.get(position).getName());
        et.setSelection(et.length());
        ad.setView(et);
        ad.setPositiveButton("이름 변경", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = et.getText().toString();
                File filePre = new File(location, folderAndFileList.get(position).getName());
                File fileNow = new File(location, value);
                filePre.renameTo(fileNow);
                dialog.dismiss();
                loadLists(location);
            }
        });
        ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ad.show();
    }

    public void filedelete(final int position) {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setMessage("파일을 삭제할까요?");   // 내용 설정
        ad.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Text 값 받아서 로그 남기기
                File file = new File(location, folderAndFileList.get(position).getName());
                file.delete();
                dialog.dismiss();
                loadLists(location);
            }
        });
        ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ad.show();
    }

    public void fileinfo(final int position) {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle("상세정보");   // 내용 설정
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.file_info_dialog, null);
        ad.setView(dialogView);
        TextView name = (TextView) dialogView.findViewById(R.id.name);
        TextView size = (TextView) dialogView.findViewById(R.id.size);
        TextView day = (TextView) dialogView.findViewById(R.id.day);
        TextView locat = (TextView) dialogView.findViewById(R.id.location);
        name.setText(folderAndFileList.get(position).getName());
        size.setText(folderAndFileList.get(position).getSize());
        day.setText(folderAndFileList.get(position).getDay());
        locat.setText(location + folderAndFileList.get(position).getName());
        ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ad.show();
    }

    private void fileshare(int position) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        //   intent.setAction(Intent.ACTION_SEND);
        intent.setType("application/*");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID,
                new File(Environment.getExternalStorageDirectory().getAbsolutePath() + bookfolderName + folderAndFileList.get(position).getName())));
        Intent chooser = Intent.createChooser(intent, "공유하기");
        this.startActivity(chooser);
    }

    public void setToolbar() throws IOException{
        Toolbar toolbar2 = (Toolbar) findViewById(R.id.toolbar2);
        spinner = (Spinner) findViewById(R.id.spinner_nav);

        setSupportActionBar(toolbar2);

        getSupportActionBar().setDisplayShowTitleEnabled(false); //기존 타이틀은 안보여주게
        final TextView f_text = (TextView) findViewById(R.id.folder_text);
        f_text.setText(FolderName);

        /*appbarbtn = (Button)findViewById(R.id.record_btn);
        appbarbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (state == STATE_PREV) {
                        showCustomNotification();
                        play();
                        appbarbtn.setText("녹음 중지");
                    }
                    else {
                        try {
                            stop();
                            appbarbtn.setText("녹음 시작");
                        } catch (IOException e) {
                            ;
                        }
                        loadLists(location);
                    }
               }
        });

        pausebtn = (Button)findViewById(R.id.pause);
        pausebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state == STATE_RECORDING) {
                    pause();
                    pausebtn.setText("일시정지 중");
                } else {
                    play();
                    pausebtn.setText("일시정지");
                }
            }
        });*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //뒤로가기 버튼 설정
        addItemToSpinner();
    }

    private void showCustomNotification(){
        NotificationCompat.Builder mBuilder = createNotification();

        RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.record_notification);

        mBuilder.setContent(remoteViews)
                .setDefaults(Notification.DEFAULT_ALL);

        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1,mBuilder.build());
    }

    private NotificationCompat.Builder createNotification() {
        String channelId = "channel";
        String channelName = "Channel Name";
        NotificationManager notifManager
                = (NotificationManager) getSystemService (Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notifManager.createNotificationChannel(mChannel);
        }

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(icon)
                .setContentTitle("StatusBar Title")
                .setContentText("StatusBar subTitle")
                .setSmallIcon(R.mipmap.ic_launcher)/*스와이프 전 아이콘*/
                //.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL);

        return builder;
    }

    public void addItemToSpinner() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("전체");
        list.add("PDF");
        list.add("MP3");

        CustomSpinnerAdapter spinnerAdapter = new CustomSpinnerAdapter(getApplicationContext(), list);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                spinnerset = parent.getItemAtPosition(position).toString();
                loadLists(location);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ;
            }
        });
    }
/******************************************************************************************************************
    public void play() {
        count += 1;
        path = Environment.getExternalStorageDirectory().getAbsolutePath() + reqLocation;
        String cnt = Integer.toString(count);
        String nowFile = path + cnt + ".mp4";
        outputFileList.add(nowFile);

        if (checkPermissionFromDevice()){

            path = Environment.getExternalStorageDirectory().getAbsolutePath() + reqLocation
                    + "[" + getTime() + "] " + FolderName + ".m4a";

            setupMediaRecorder();
            mediaRecorder.setOutputFile(nowFile);

            try {
                mediaRecorder.prepare();
                //Prepares the recorder to begin capturing and encoding data.
                // This method must be called after setting up the desired audio and video sources, encoders, file format, etc., but before start().
                mediaRecorder.start();
            }catch (IOException e){
                e.printStackTrace();
            }catch (IllegalStateException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "Recording...", Toast.LENGTH_SHORT).show();
        }else{
            requsetPermission();
        }

        state = STATE_RECORDING;
    }

    public void stop() throws IOException{
        if (state == STATE_PAUSE) {
            //일시정지 상태에서 정지버튼
        } else {
            //재상상태에서 정지버튼
            try {
                mediaRecorder.stop();
            } catch (RuntimeException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"녹음실패",Toast.LENGTH_SHORT).show();
            }
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }

        count = 0;

        try {
            append(outputFileList);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Append Error!!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        state = STATE_PREV;
    }

    public void pause() {
        mediaRecorder.stop();     //현재 녹음 중인 파일 종료 (임시파일)
        mediaRecorder.reset();
        mediaRecorder.release();
        mediaRecorder = null;

        state = STATE_PAUSE;
    }

    public void append(List<String> list) throws IOException{
        Movie[] inMovies;
        inMovies = new Movie[list.size()];
        try {
            for (int i = 0; i<list.size();i++) {
                inMovies[i] = MovieCreator.build(list.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Track> audioTracks = new LinkedList<Track>();

        for (Movie m : inMovies) {
            for (Track t : m.getTracks()) {
                if (t.getHandler().equals("soun")) {
                    audioTracks.add(t);
                }
            }
        }

        Movie result = new Movie();

        if (audioTracks.size() > 0) {
            result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
        }

        Container out = new DefaultMp4Builder().build(result);
        FileChannel fc = null;

        try {
            fc = new FileOutputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + reqLocation
                    + "[" + getTime() + "] " + FolderName + ".mp4")).getChannel();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            out.writeContainer(fc);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            fc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i<list.size();i++) {
            File fl = new File(list.get(i));
            fl.delete();
            //inMovies[i] = MovieCreator.build(list.get(i));
        }
    }

    private void setupMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        //mediaRecorder.setOutputFile(path);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_PERMISSION_CODE:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED )
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            break;
        }
    }

    private boolean checkPermissionFromDevice() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return  write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                record_audio_result == PackageManager.PERMISSION_GRANTED;
    }

    private void requsetPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        },REQUEST_PERMISSION_CODE);
    }
**************************************************************************************************************************************/
    private String getTime() {
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }

}