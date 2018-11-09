package app.bqlab.okarduino;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    boolean co; //연결
    int br; //밝기

    Button mainConnect;
    Button mainBright;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    public void init() {
        br = getSharedPreferences("settings", MODE_PRIVATE).getInt("br", 100);
        mainConnect = findViewById(R.id.main_connect);
        mainBright = findViewById(R.id.main_bright);
        mainConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("서버와 연결을 시작합니다.")
                        .setPositiveButton("연결", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                databaseReference.child("test").setValue(true);
                                Log.d("연결", databaseReference.child("test").getKey());
                                co = true;
                            }
                        })
                        .setNegativeButton("연결 끊음", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("연결", String.valueOf(co));
                                co = false;
                            }
                        })
                        .show();
            }
        });
        mainBright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeekBar s = new SeekBar(MainActivity.this);
                s.setProgress(MainActivity.this.br);
                s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        getSharedPreferences("settings", MODE_PRIVATE).edit().putInt("br", MainActivity.this.br).apply();
                        Log.d("밝기", String.valueOf(MainActivity.this.br));
                        MainActivity.this.br = progress;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("밝기를 설정합니다.")
                        .setView(s)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) { //서버의 데이터가 변경되었을 때 호출되는 함수
                dataSnapshot.child("test").getValue(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("tag", "onCancelled", databaseError.toException());
            }
        });
    }
}
