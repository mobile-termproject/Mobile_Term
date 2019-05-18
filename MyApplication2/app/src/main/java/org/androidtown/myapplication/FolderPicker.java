package org.androidtown.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class FolderPicker extends Activity {
    ArrayList<FilePojo> folderAndFileList;
    ArrayList<FilePojo> foldersList;
    ArrayList<FilePojo> filesList;
    boolean tempCnt = true;
    FolderAdapter FolderAdapter;
    ListView listView;
    boolean isVisible = false;
    TextView tv_location;
    Button move, selectAll;
    String location = Environment.getExternalStorageDirectory().getAbsolutePath();
    boolean pickFiles;
    Intent receivedIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fp_main_layout);

        if (!isExternalStorageReadable()) {
            Toast.makeText(this, "Storage access permission not given", Toast.LENGTH_LONG).show();
            finish();
        }
        tv_location = (TextView) findViewById(R.id.fp_tv_location);
        move = (Button) findViewById(R.id.move);
        selectAll = (Button) findViewById(R.id.selectAll);
        try {
            receivedIntent = getIntent();
            if (receivedIntent.hasExtra("location")) {
                String reqLocation = receivedIntent.getExtras().getString("location");
                if (reqLocation != null) {
                    File requestedFolder = new File(reqLocation);
                    if (requestedFolder.exists())
                        location = reqLocation;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        pickFiles = true;
        loadLists(location);
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

    void loadLists(String location) {
        try {

            File folder = new File(location);
            if (!folder.isDirectory())
                exit();

            tv_location.setText("Location : " + folder.getAbsolutePath());
            File[] files = folder.listFiles();

            foldersList = new ArrayList<>();
            filesList = new ArrayList<>();

            for (File currentFile : files) {
                long lastModifie = currentFile.lastModified();
                String patter = "yyyy년 MM월 dd일 aa hh:mm";
                SimpleDateFormat simpleDateForma = new SimpleDateFormat(patter);
                Date lastModifiedDat = new Date(lastModifie);
                Long L = currentFile.length();
                if (currentFile.isDirectory()) {
                    FilePojo filePojo = new FilePojo(currentFile.getName(), true);
                    filePojo.setDay(simpleDateForma.format(lastModifiedDat));
                    filePojo.setSize(getFileCount(currentFile, 0) + "개");
                    foldersList.add(filePojo);
                } else {
                    FilePojo filePojo = new FilePojo(currentFile.getName(), false);
                    filePojo.setDay(simpleDateForma.format(lastModifiedDat));
                    filePojo.setSize(formatFileSize(L));
                    filePojo.setLocation(location);
                    filesList.add(filePojo);
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


    Comparator<FilePojo> comparatorAscending = new Comparator<FilePojo>() {
        @Override
        public int compare(FilePojo f1, FilePojo f2) {
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

    void showList() {
        try {
            FolderAdapter = new FolderAdapter(this, folderAndFileList);
            listView = (ListView) findViewById(R.id.fp_listView);
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            listView.setAdapter(FolderAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    isVisible = true;
                    FolderAdapter.setIsVisible(isVisible);
                    FolderAdapter.notifyDataSetChanged();
                    listClick(position);
                }
            });
            selectAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int cnt = 0;
                    cnt = listView.getCount();
                    if (tempCnt) {
                        tempCnt = false;
                        isVisible = true;
                        FolderAdapter.setIsVisible(isVisible);
                        FolderAdapter.notifyDataSetChanged();
                        selectAll.setText("취소");
                    } else {
                        tempCnt = true;
                        FolderAdapter.setIsVisible(false);
                        FolderAdapter.notifyDataSetChanged();
                        selectAll.setText("전체선택");
                    }
                    for (int i = 0; i < cnt; i++) {
                        if (!(pickFiles && !folderAndFileList.get(i).isFolder())) {
                            continue;
                        } else if (!tempCnt) {
                            listView.setItemChecked(i, true);
                        } else {
                            listView.setItemChecked(i, false);
                        }
                        listClick(i);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    ArrayList<String> result = new ArrayList<String>();
    ArrayList<String> result2 = new ArrayList<String>();

    void listClick(int position) {
        if (pickFiles && !folderAndFileList.get(position).isFolder()) {
            String data = location + File.separator + folderAndFileList.get(position).getName();
            String temp = folderAndFileList.get(position).getName();
            result2.add(location + File.separator);
            result.add(folderAndFileList.get(position).getName());
            int j = result.size();
            for (int i = 0; i < j - 1; i++) {
                if (result.get(i).equals(temp)) {
                    if (i == 0 && result.size() == 2) {
                        result.clear();
                        result2.clear();
                    } else {
                        result.remove(i);//0
                        result.remove(result.size() - 1);//1
                        result2.remove(i);
                        result2.remove(result.size() - 1);
                        j--;
                    }
                    break;
                }
            }
            receivedIntent.putExtra("data", data);
            setResult(RESULT_OK, receivedIntent);
            /*파일이동*/
            move.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int i = 0; i < result.size(); i++) {
                        moveFile(result2.get(i), result.get(i), Environment.getExternalStorageDirectory().getAbsolutePath() + "/혜연/");
                    }
                    result.clear();
                    result2.clear();
                    BookFileList a = new BookFileList();
                    a.loadLists(Environment.getExternalStorageDirectory().getAbsolutePath() + "/혜연/");
                    finish();
                }
            });
        } else {
            location = location + File.separator + folderAndFileList.get(position).getName();
            loadLists(location);
        }

    }

    @Override
    public void onBackPressed() {
        goBack(null);
    }

    public void goBack(View v) {
        if (location.equals("/storage/emulated/0")) {
            exit();
        }
        if (location != null && !location.equals("") && !location.equals("/")) {
            int start = location.lastIndexOf('/');
            String newLocation = location.substring(0, start);
            location = newLocation;
            loadLists(location);
        } else {
            exit();
        }
    }

    void exit() {
        setResult(RESULT_CANCELED, receivedIntent);
        BookFileList br = new BookFileList();
        finish();
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

    private void moveFile(String inputPath, String inputFile, String outputPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
            new File(inputPath + inputFile).delete();

        } catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }
}
