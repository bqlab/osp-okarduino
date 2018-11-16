package app.bqlab.okarduino;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements Runnable {

    boolean pw; //전원
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

    @Override
    public void run() {
        while(pw) {
            sync();
            monitorData();
        }
    }

    private void init() {
        br = getSharedPreferences("settings", MODE_PRIVATE).getInt("br", 100);
        mainConnect = findViewById(R.id.main_power);
        mainBright = findViewById(R.id.main_bright);
        mainConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("디바이스의 전원을 켭니다.")
                        .setPositiveButton("켜기", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MainActivity.this.pw = true;

                            }
                        })
                        .setNegativeButton("끄기", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MainActivity.this.pw = false;
                                getSharedPreferences("settings", MODE_PRIVATE).edit().putBoolean("pw", MainActivity.this.pw).apply();
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
                try {
                    MainActivity.this.pw = (boolean) dataSnapshot.child("pw").getValue();
                    MainActivity.this.br = (int) dataSnapshot.child("br").getValue();
                } catch (NullPointerException e) {
                    showNoDataDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showNoDataDialog();
            }
        });
    }


    private void sync() {
        databaseReference.child("pw").setValue(MainActivity.this.pw);
        databaseReference.child("br").setValue(MainActivity.this.br);
        getSharedPreferences("settings", MODE_PRIVATE).edit().putBoolean("pw", MainActivity.this.pw).apply();
        getSharedPreferences("settings", MODE_PRIVATE).edit().putInt("br", MainActivity.this.br).apply();
    }

    private void monitorData() {
        try {
            Log.d("전원", databaseReference.child("pw").getKey());
            Log.d("밝기", databaseReference.child("br").getKey());
        } catch (NullPointerException e) {
            showNoDataDialog();
        }
    }

    private void showNoDataDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("데이터를 불러올 수 없습니다.")
                .setMessage(" - 네트워크 상태를 확인하세요.\n - 서버 접근 권한을 요청하세요.\n - 기본 데이터를 삭제하지 마세요.")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finishAffinity();
                    }
                })
                .show();
    }
}
