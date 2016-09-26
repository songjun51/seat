package com.seat.sw_maestro.seat;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/*
이렇게 사용한다.
 DatabaseManager databaseManager = new DatabaseManager(getApplicationContext());
 String date = databaseManager.getCurrentDay();  // 현재의 날짜 예)20160926
 String timeLine = databaseManager.getCurrentHour(); // 현재의 시간 14:25면 14를 리턴
 databaseManager.insertData(timeLine,53,78,date);    // 인자로 현재 시간, 앉은시간(분), 정확도(퍼센트 인트), 현재날짜
 databaseManager.selectData();   // 조회
 */

//DB를 총괄관리
public class DatabaseManager {

    private static final String TAG = "DatabaseManager";

    // DB관련 상수 선언
    private static final String dbName = "datas.db";
    private static final String tableName = "datas";
    public static final int dbVersion = 1;

    // DB관련 객체 선언
    private OpenHelper opener; // DB opener
    private SQLiteDatabase db; // DB controller

    // 부가적인 객체들
    private Context context;

    // 생성자
    public DatabaseManager(Context context) {
        this.context = context;
        this.opener = new OpenHelper(context, dbName, null, dbVersion);
        db = opener.getWritableDatabase();
    }

    // Opener of DB and Table
    private class OpenHelper extends SQLiteOpenHelper {

        public OpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version) {
            super(context, name, null, version);
            // TODO Auto-generated constructor stub
        }

        // 생성된 DB가 없을 경우에 한번만 호출됨
        @Override
        public void onCreate(SQLiteDatabase arg0) {
            Log.d(TAG, "디비생성");
            // DataNumber(자동으로 증가) | TimeLine(String) | SittingTime(int) | Accuracy(int) | Date(String 예: 20160925)
            String createSql = "create table " + tableName + "(" + "DataNumber integer primary key autoincrement,"
                    + "TimeLine text," + "SittingTime integer," + "Accuracy integer," + "Date text);";
            arg0.execSQL(createSql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
        }
    }

    // 데이터 추가   인자 순서 기억!!
    public void insertData(String timeLine, int sittingTime, int accuracy, String date){
        String sql = "insert into " + tableName + " values( NULL," + timeLine + ", "
                + sittingTime + ", " + accuracy + ", "
                + date + ");";
        db.execSQL(sql);
    }

    // 데이터 검색
    public void selectData() {
        // 1) db의 데이터를 읽어와서, 2) 결과 저장, 3)해당 데이터를 꺼내 사용
        Cursor cursor = db.query(tableName, null, null, null, null, null, null);

        // * 위 결과는 select * from datas 가 된다. Cursor는 DB결과를 저장한다. public Cursor
        // * query (String table, String[] columns, String selection, String[]
        // * selectionArgs, String groupBy, String having, String orderBy)

        while (cursor.moveToNext()) {
            // 하나씩 내려가며 로그캣에 출력
            Log.d(TAG, "DataNumber : " + cursor.getInt(cursor.getColumnIndex("DataNumber")));
            Log.d(TAG, "TimeLine : " + cursor.getInt(cursor.getColumnIndex("TimeLine")));
            Log.d(TAG, "SittingTime : " + cursor.getInt(cursor.getColumnIndex("SittingTime")));
            Log.d(TAG, "Accuracy : " + cursor.getInt(cursor.getColumnIndex("Accuracy")));
            Log.d(TAG, "Date : " + cursor.getString(cursor.getColumnIndex("Date")));
        }
    }

    public String getCurrentTime(){
        String time = new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date(System.currentTimeMillis()));
        Log.d(TAG, "현재시간 : " +  time);
        return time;
    }

    public String getCurrentHour(){
        String hour = new SimpleDateFormat("HH").format(new Date(System.currentTimeMillis()));
        //Log.d(TAG, "현재시간 : " +  hour);
        return hour;
    }

    public String getCurrentDay(){
        String day = new SimpleDateFormat("yyyyMMdd").format(new Date(System.currentTimeMillis()));
        //Log.d(TAG, "현재날짜 : " +  day);
        return day;
    }

    public int getSittingTime(String timeLine, String date){    // timeLine과 date에 해당하는 SittingTime을 리턴함.
        String sql = "select SittingTime from " + tableName + " where TimeLine = " + timeLine + " AND Date = " + date + ";";
        Cursor result = db.rawQuery(sql, null);
        int sittingTime;

        // result(Cursor 객체)가 비어 있으면 false 리턴
        if(result.moveToFirst()){
            //while(result.moveToNext()) {  // 이론적으로 내려가면서 할 필요는 없겠지. 우리의 계획상으로는 디비에 타임라인, 시간이 같은 것이 중복되서 들어올 수 없어.
            sittingTime = result.getInt(0);
            //Log.d(TAG, "date : " + date + " timeLine : " + timeLine + " 앉은시간 : " + sittingTime);
            //}
        }
        else{
            Log.d(TAG, "데이터가 존재하지 않음. 0을 리턴하겠습니다.");
            sittingTime = 0;
        }
        result.close();

        return sittingTime;
    }

    public int getAccuracy(String timeLine, String date){    // timeLine과 date에 해당하는 Accuracy을 리턴함.
        String sql = "select Accuracy from " + tableName + " where TimeLine = " + timeLine + " AND Date = " + date + ";";
        Cursor result = db.rawQuery(sql, null);
        int accuracy;

        // result(Cursor 객체)가 비어 있으면 false 리턴
        if(result.moveToFirst()){
            accuracy = result.getInt(0);
        }
        else{
            Log.d(TAG, "데이터가 존재하지 않음. 0을 리턴하겠습니다.");
            accuracy = 0;
        }
        result.close();

        return accuracy;
    }

    public int getSittingTime_OneDay(String date){  // 이것은 date를 넣으면 그 하루동안 앉았던 sittingTime을 더해서 리턴해준다.
        int sittingTime_OneDay = 0;
        for(int i = 0; i<24; i++){  // 타임라인은 0부터 23까지 존재하니까 0~23까지 루프를 돌면 되겠음.
            String numberToString;  // int -> string 변환을 해줘야 getSitiingTime 함수를 부를 수 있음.
            if(i<10){
                numberToString = String.format ("%01d", i);    // 10 이하의 경우
            }
            else{
                numberToString = String.format ("%02d", i);    // 이상의 경우
            }
            sittingTime_OneDay += getSittingTime(numberToString,date); // date 날짜 동안의 타임라인에 해당하는 sittingTime 값을 다 더하면 하루의 값이 됨
        }
        return sittingTime_OneDay;
    }

    public int getAccuracy_OneDay(String date){  // 이것은 date를 넣으면 그 하루동안의 Accuracy를 더해서 리턴해준다.
        int accuracy_OneDay = 0;
        for(int i = 0; i<24; i++){  // 타임라인은 0부터 23까지 존재하니까 0~23까지 루프를 돌면 되겠음.
            String numberToString;
            if(i<10){
                numberToString = String.format ("%01d", i);    // 10 이하의 경우
            }
            else{
                numberToString = String.format ("%02d", i);    // 이상의 경우
            }
            accuracy_OneDay += getAccuracy(numberToString,date); // date 날짜 동안의 타임라인에 해당하는 accuracy 값을 다 더하면 하루의 값이 됨
        }
        return accuracy_OneDay;
    }

    public float[] makeTimeDatas_OneDay(String date){ // 그래프에 보여줄 하루 동안의 데이터들 (통계기간 일 선택했을 때)
        float[] timeDatas = new float[24];
        for(int i = 0; i<24; i++){
            String numberToString;
            if(i<10){
                numberToString = String.format ("%01d", i);    // 10 이하의 경우
            }
            else{
                numberToString = String.format ("%02d", i);    // 이상의 경우
            }

            timeDatas[i] = getSittingTime(numberToString,date); // 함수 리턴은 int형이지만 크게 상관 없을듯..
        }
        return timeDatas;
    }

    public float[] makeAccuracyDatas_OneDay(String date){ // 그래프에 보여줄 하루 동안의 데이터들 (통계기간 일 선택했을 때)
        float[] accuracyDatas = new float[24];
        for(int i = 0; i<24; i++){
            String numberToString;
            if(i<10){
                numberToString = String.format ("%01d", i);    // 10 이하의 경우
            }
            else{
                numberToString = String.format ("%02d", i);    // 이상의 경우
            }

            accuracyDatas[i] = getAccuracy(numberToString,date); // 함수 리턴은 int형이지만 크게 상관 없을듯..
        }
        return accuracyDatas;
    }
}
