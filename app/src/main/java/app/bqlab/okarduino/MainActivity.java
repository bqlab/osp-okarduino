package app.bqlab.okarduino;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    Boolean pw = false; //전원
    Integer br = 50; //밝기
    Integer tm = 0; //온도

    Button mainPower;
    Button mainBright;
    Button mainTemp;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        mainPower = findViewById(R.id.main_power);
        mainBright = findViewById(R.id.main_bright);
        mainTemp = findViewById(R.id.main_temp);
        mainPower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("디바이스의 전원을 켭니다.")
                        .setPositiveButton("켜기", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                databaseReference.child("pw").setValue(true);

                            }
                        })
                        .setNegativeButton("끄기", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                databaseReference.child("pw").setValue(false);
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
                        databaseReference.child("br").setValue(progress);
                        if (!pw)
                            Toast.makeText(MainActivity.this, "전원이 켜졌을 때 적용됩니다.", Toast.LENGTH_LONG).show();
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
        mainTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String s = "디바이스가 감지한 온도는 " + tm.toString() + "도입니다.";
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage(s)
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                } catch (NullPointerException e) {
                    showNoDataDialog();
                }
            }
        });
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) { //서버의 데이터가 변경되었을 때 호출되는 함수
                try {
                    MainActivity.this.pw = dataSnapshot.child("pw").getValue(Boolean.class);
                    MainActivity.this.br = dataSnapshot.child("br").getValue(Integer.class);
                    MainActivity.this.tm = dataSnapshot.child("tm").getValue(Integer.class);
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
